package net.abesto.treasurer;

import android.app.AlertDialog;
import android.content.Context;

public class SimpleAlertDialog extends AlertDialog.Builder {

	public SimpleAlertDialog(Context context, String title, String message) {
		super(context);
		setTitle(title);
		setMessage(message);
		setPositiveButton("Ok", null);
	}
	
	public static SimpleAlertDialog show(Context context, String title, String message) {
		SimpleAlertDialog dlg = new SimpleAlertDialog(context, title, message);
		dlg.show();
		return dlg;
	}
}
