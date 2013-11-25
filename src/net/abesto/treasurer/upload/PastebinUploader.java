package net.abesto.treasurer.upload;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class PastebinUploader extends Uploader<PastebinUploaderDataProvider> {
	public PastebinUploader(PastebinUploaderDataProvider dataProvider) {
		super(dataProvider);
	}

	private DefaultHttpClient http = new DefaultHttpClient();
	private final String apiKey = "44ad5ad2f24bca7151918c453be5b8c1";
	
	private String post(String text) throws UploadFailed {
		HttpPost post = new HttpPost("http://pastebin.com/api/api_post.php");
		List<NameValuePair> data = new ArrayList<NameValuePair>(4);
		data.add(new BasicNameValuePair("api_dev_key", apiKey));
		data.add(new BasicNameValuePair("api_option", "paste"));
		data.add(new BasicNameValuePair("api_paste_code", text));
		data.add(new BasicNameValuePair("private", "1"));
		try {
			post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			return EntityUtils.toString(http.execute(post).getEntity());
		} catch (Exception e) {
			throw new UploadFailed(e);
		}

	}
	
	@Override
	public String upload() throws UploadFailed {
		String rawDataUrl = post(dataProvider.getRawData());
		String reportUrl = post(dataProvider.getReport(rawDataUrl));
		return reportUrl;
	}
}
