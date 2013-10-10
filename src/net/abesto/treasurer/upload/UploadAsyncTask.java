package net.abesto.treasurer.upload;

import net.abesto.treasurer.SimpleAlertDialog;
import net.abesto.treasurer.TransactionStore;
import net.abesto.treasurer.TransactionStore.Data;
import net.abesto.treasurer.upload.Uploader.UploadFailed;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

public class UploadAsyncTask extends
		AsyncTask<Void, Void, Pair<String, String>> {
	
	private Context context;
	private Uploader uploader;
	private ProgressDialog progressDialog;
	
	public UploadAsyncTask(Context context, Uploader uploader) {
		this.context = context;
		this.uploader = uploader;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context, "Uploading / Sending", "Please wait...");
	}
	
	@Override
	protected Pair<String, String> doInBackground(Void... args) {
		try {
			return new Pair<String, String>("Upload successful", uploader.upload());
		} catch (UploadFailed e) {
			e.printStackTrace();
			return new Pair<String, String>("Upload failed", e.toString());
		}		
	}

	@Override
	protected void onPostExecute(Pair<String, String> result) {
		progressDialog.dismiss();
		if (result.second != null) {
			SimpleAlertDialog.show(context, result.first, result.second);
		}
	}
}
