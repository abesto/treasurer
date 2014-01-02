package net.abesto.treasurer.upload.ynab;

import android.content.Context;
import net.abesto.treasurer.upload.MailerDataProvider;
import net.abesto.treasurer.upload.UploadData;

public class YNABMailerDataProvider extends MailerDataProvider {
	public YNABMailerDataProvider(Context context, UploadData uploadData) throws InvalidConfigurationException {
		super(context, uploadData);
	}

	@Override
	public String getTitle() {
		return uploadData.getTitle();
	}

	@Override
	public String getBody() {
		return YNABStringBuilder.buildReport(uploadData);
	}

	@Override
	public String getAttachmentFilename() {
		return uploadData.getTitle()
				.toLowerCase(context.getResources().getConfiguration().locale)
				.replace(' ', '_').replace('/', '_') + ".csv";
	}

	@Override
	public String getAttachmentText() {
		return YNABStringBuilder.toCsv(uploadData);
	}
}
