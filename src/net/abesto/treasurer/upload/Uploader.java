package net.abesto.treasurer.upload;

abstract class Uploader<DP extends DataProvider> {
	public static class UploadFailed extends Exception {
		private static final long serialVersionUID = -4944296088855945962L;
		public UploadFailed(Exception e) 		{
			super(e);
		}
	}
	
	protected DP dataProvider;
	
	public Uploader(DP dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public abstract String upload() throws UploadFailed;
}
