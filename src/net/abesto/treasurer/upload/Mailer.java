package net.abesto.treasurer.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import net.abesto.treasurer.upload.Uploader;
import android.content.Intent;
import android.net.Uri;

public class Mailer extends Uploader<MailerDataProvider> {
	public Mailer(MailerDataProvider dataProvider) {
		super(dataProvider);
	}

	@Override
	public String upload() throws UploadFailed {
		Intent i = new Intent(Intent.ACTION_SEND);
		Uri attachmentUri;
		
		try {
			attachmentUri = writeAttachmentFile();
		} catch (IOException e) {
			throw new UploadFailed(e);
		}
		
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"abesto0@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, dataProvider.getTitle());
		i.putExtra(Intent.EXTRA_TEXT, dataProvider.getBody());
		i.putExtra(Intent.EXTRA_STREAM, attachmentUri);
		dataProvider.getContext().startActivity(Intent.createChooser(i, "Send transaction report"));
		return null;
	}

	private Uri writeAttachmentFile() throws IOException {
		File file = new File(dataProvider.getContext().getExternalCacheDir(),
							 dataProvider.getAttachmentFilename());
		FileOutputStream stream = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		writer.write(dataProvider.getAttachmentText());		
		writer.close();
		stream.flush();
		stream.close();
		return Uri.fromFile(file);
	}

}
