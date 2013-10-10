package net.abesto.treasurer.upload;

import net.abesto.treasurer.TransactionStore;

public interface Uploader {
	public class UploadFailed extends Exception {
		private static final long serialVersionUID = -4944296088855945962L;
		public UploadFailed(Exception e) 		{
			super(e);
		}
	};	
	
	public String upload() throws UploadFailed;
}
