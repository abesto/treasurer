package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.TransactionStore;
import net.abesto.treasurer.upload.MailerDataProvider;

public class YNABMailerDataProvider implements MailerDataProvider {
	private Context context;
	private YNABUploadData data;
	
	public YNABMailerDataProvider(Context context, TransactionStore.Data data) {
		this.context = context;
		this.data = YNABUploadData.fromTransactionStoreData(data);
	}
	
	@Override
	public String getTitle() {
		return data.title;
	}

	@Override
	public String getBody() {
		return YNABStringBuilder.buildReport(data);
	}

	@Override
	public String getAttachmentFilename() {
		return data.title
				.toLowerCase(context.getResources().getConfiguration().locale)
				.replace(' ', '_').replace('/', '_') + ".csv";
	}

	@Override
	public String getAttachmentText() {
		return YNABStringBuilder.toCsv(data);
	}
}
