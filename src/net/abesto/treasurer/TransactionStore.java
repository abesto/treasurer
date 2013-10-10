package net.abesto.treasurer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class TransactionStore {
	public static class Data implements Serializable {
		private static final long serialVersionUID = 412046542504975150L;
		public List<Transaction> transactions;
		public List<String> failedToParse;
		
		public Data() {
			transactions = new LinkedList<Transaction>();
			failedToParse = new LinkedList<String>();
		}
	}
	
	private static String FILE_NAME = "storage.dat";
	
	private Context context;
	
	public TransactionStore(Context context) {
		this.context = context;
		try {
			context.openFileInput(FILE_NAME);
		} catch (FileNotFoundException e) {
			try {
				flush();
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}
	
	private void save(Data d) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
		out.writeObject(d);
		out.close();
	}
	
	private Data load() throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(context.openFileInput(FILE_NAME));
		Data d = (Data) in.readObject();
		in.close();
		return d;
	}
	
	public void add(Transaction t) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		Data d = load();
		d.transactions.add(t);
		Log.i("add", Integer.valueOf(d.transactions.size()).toString());
		save(d);
	}
	
	public void failed(String s) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		Data d = load();
		d.failedToParse.add(s);
		Log.i("failed", Integer.valueOf(d.failedToParse.size()).toString());
		save(d);
	}
	
	public Data get() throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		Data d = load();
		Log.i("get", Integer.valueOf(d.transactions.size()).toString());
		return d;
	}
	
	public void flush() throws FileNotFoundException, IOException {
		save(new Data());
	}
}
