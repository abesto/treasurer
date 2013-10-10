package net.abesto.treasurer.upload;

public interface PastebinUploaderDataProvider {

	String getRawData();
	String getReport(String rawDataUrl);

}
