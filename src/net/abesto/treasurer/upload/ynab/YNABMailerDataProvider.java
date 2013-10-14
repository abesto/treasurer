package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.upload.MailerDataProvider;
import net.abesto.treasurer.upload.UploadData;

public class YNABMailerDataProvider extends MailerDataProvider {
	private YNABUploadData data;
	
	public YNABMailerDataProvider(Context context, UploadData uploadData) {
		super(context, uploadData);
		data = YNABUploadData.fromUploadData(uploadData);
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
