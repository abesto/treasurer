package net.abesto.treasurer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class Store<Data extends Serializable> {
    private static Map<String, Store> instanceMap = new HashMap<String, Store>();

	private String fileName;
	private static Context context;
	
	private Store(String id) {
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

    public static void initializeComponent(Context _context) {
        context = _context;
    }

    public static <D extends Serializable> Store<D> getInstance(Class<D> cls) {
        return getInstance(cls.getSimpleName());
    }

    public static <D extends Serializable> Store<D> getInstance(String id) {
        if (!instanceMap.containsKey(id)) {
            instanceMap.put(id, new Store<D>(id));
        }
        //noinspection unchecked
        return (Store<D>) instanceMap.get(id);
    }

	private void save(List<Data> d) throws IOException {
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
	
	public void add(Data item) throws IOException, ClassNotFoundException {
		List<Data> data = get();
		data.add(item);
		save(data);
	}
	
	public void remove(Data t) throws IOException, ClassNotFoundException {
		List<Data> d = get();
		d.remove(t);
		save(d);
	}
	
	public void flush() throws IOException {
		save(new LinkedList<Data>());
	}

    public String getFileName() {
        return fileName;
    }
}
