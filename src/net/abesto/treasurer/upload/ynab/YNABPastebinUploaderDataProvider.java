package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.upload.PastebinUploaderDataProvider;
import net.abesto.treasurer.upload.UploadData;

public class YNABPastebinUploaderDataProvider extends PastebinUploaderDataProvider {
    public YNABPastebinUploaderDataProvider(Context context, UploadData data) throws InvalidConfigurationException {
        super(context, data);
    }

    @Override
	public String getRawData() {
		return YNABStringBuilder.toCsv(uploadData);
	}

	@Override
	public String getReport(String rawDataUrl) {
		return YNABStringBuilder.buildReport(uploadData) + "\nCSV: " + rawDataUrl;
	}
}
