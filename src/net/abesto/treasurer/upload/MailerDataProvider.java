package net.abesto.treasurer.upload;

public interface MailerDataProvider {
	String getTitle();
	String getBody();
	String getAttachmentFilename();
	String getAttachmentText();
}
