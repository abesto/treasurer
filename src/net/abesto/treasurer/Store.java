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

public class Store<Data extends Serializable> {
	private String fileName;
	
	private Context context;
	
	public Store(Context context, Class<?> cls) {
		this(context, cls.getSimpleName());
	}
	
	public Store(Context context, String id) {
		this.context = context;
		this.fileName = id + ".dat";
		try {
			context.openFileInput(fileName);
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
	
	private void save(List<Data> d) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
		out.writeObject(d);
		out.close();
	}
	
	public List<Data> get() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(context.openFileInput(fileName));
		@SuppressWarnings("unchecked")
		List<Data> ret = (List<Data>) in.readObject();
		in.close();
		return ret;
	}
	
	public void add(Data item) throws StreamCorruptedException, FileNotFoundException, IOException, ClassNotFoundException {
		List<Data> data = get();
		data.add(item);
		save(data);
	}
	
	public void remove(Data t) throws IOException, ClassNotFoundException {
		List<Data> d = get();
		d.remove(t);
		save(d);
	}
	
	public void flush() throws FileNotFoundException, IOException {
		save(new LinkedList<Data>());
	}

}
