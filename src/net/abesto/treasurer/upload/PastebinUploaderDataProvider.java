package net.abesto.treasurer.upload;

import android.content.Context;

public abstract class PastebinUploaderDataProvider extends DataProvider {
	public PastebinUploaderDataProvider(Context context, UploadData data) {
		super(context, data);
	}
	public abstract String getRawData();
	public abstract String getReport(String rawDataUrl);
}
