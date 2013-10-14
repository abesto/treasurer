package net.abesto.treasurer.upload;

import android.content.Context;

public abstract class DataProvider {
	protected Context context;
	protected UploadData uploadData;
	
	public DataProvider(Context context, UploadData uploadData) {
		this.context = context;
		this.uploadData = uploadData;
	}
	
	public Context getContext() {
		return context;
	}
}
