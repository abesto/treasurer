package net.abesto.treasurer.upload;

import android.content.Context;

public abstract class MailerDataProvider extends DataProvider {
	public MailerDataProvider(Context context, UploadData data) {
		super(context, data);
	}
	public abstract String getTitle();
	public abstract String getBody();
	public abstract String getAttachmentFilename();
	public abstract String getAttachmentText();
}
