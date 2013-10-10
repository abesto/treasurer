package net.abesto.treasurer.upload.ynab;

import net.abesto.treasurer.TransactionStore;
import net.abesto.treasurer.upload.PastebinUploaderDataProvider;

public class YNABPastebinUploaderDataProvider implements
		PastebinUploaderDataProvider {
	
	private YNABUploadData data;
	
	public YNABPastebinUploaderDataProvider(TransactionStore.Data data) {
		this.data = YNABUploadData.fromTransactionStoreData(data);
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
