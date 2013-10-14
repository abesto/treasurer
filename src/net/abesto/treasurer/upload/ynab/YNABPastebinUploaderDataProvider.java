package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.upload.PastebinUploaderDataProvider;
import net.abesto.treasurer.upload.UploadData;

public class YNABPastebinUploaderDataProvider extends PastebinUploaderDataProvider {
	private YNABUploadData data;
	
	public YNABPastebinUploaderDataProvider(Context context, UploadData uploadData) {
		super(context, uploadData);
		data = YNABUploadData.fromUploadData(uploadData);
	}
	
	@Override
	public String getRawData() {
		return YNABStringBuilder.toCsv(data);
	}

	@Override
	public String getReport(String rawDataUrl) {
		return YNABStringBuilder.buildReport(data) + "\nCSV: " + rawDataUrl;
	}
}
