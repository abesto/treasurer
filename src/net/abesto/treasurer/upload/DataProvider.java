package net.abesto.treasurer.upload;

import android.content.Context;

public abstract class DataProvider {
    public static class InvalidConfigurationException extends Exception {
        public InvalidConfigurationException(String detailMessage) {
            super(detailMessage);
        }
    }

	protected Context context;
	protected UploadData uploadData;
	
	public DataProvider(Context context, UploadData uploadData) throws InvalidConfigurationException {
		this.context = context;
		this.uploadData = uploadData;
	}
	
	public Context getContext() {
		return context;
	}
}
