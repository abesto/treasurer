package net.abesto.treasurer.upload.ynab;

import net.abesto.treasurer.upload.Uploader;

public interface YNABUploader extends Uploader {
	public void upload(YNABUploadData data) throws Uploader.UploadFailed;
}
