package net.abesto.treasurer.upload.ynab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.TransactionStore.Data;
import net.abesto.treasurer.upload.Uploader;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class YNABPastebinUploader implements Uploader {
	private DefaultHttpClient http = new DefaultHttpClient();
	private String apiKey = "yourapikey";
	
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
	
	private String buildCSV(YNABUploadData data) {
		List<Transaction> transactions = data.goodTransactions.subList(0, data.goodTransactions.size());
		transactions.addAll(data.noCategoryTransactions);
		return YNABCsvBuilder.listToCsv(transactions);
	}
	
	private String buildReport(YNABUploadData data, String csvUrl) {
		StringBuilder sb = new StringBuilder(data.title).append("\n");
		sb.append("CSV url: ").append(csvUrl).append("\n");
		sb.append(data.goodTransactions.size()).append(" transactions successfully parsed\n\n");
		sb.append(data.noCategoryTransactions.size()).append(" transactions had unknown payees:\n");
		
		Set<String> unknownPayees = new HashSet<String>();
		for (Transaction t : data.noCategoryTransactions) {
			if (unknownPayees.contains(t.getPayee())) continue;
			unknownPayees.add(t.getPayee());
			sb.append(t.getPayee()).append("\n");
		}
		sb.append("\n");
		
		sb.append("Failed to parse ").append(data.failedToParse.size()).append(" SMS messages\n");
		for (String line : data.failedToParse) {
			sb.append(line).append("\n");
		}
		
		return sb.toString();
	}
	
	@Override
	public String upload(Data storeData) throws UploadFailed {
		YNABUploadData data = YNABUploadData.fromTransactionStoreData(storeData);
		String csvUrl = post(buildCSV(data));
		String reportUrl = post(buildReport(data, csvUrl));
		return reportUrl;
	}
}
