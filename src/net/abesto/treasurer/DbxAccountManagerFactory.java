package net.abesto.treasurer;

import android.content.Context;
import com.dropbox.sync.android.DbxAccountManager;

public class DbxAccountManagerFactory {
    public static DbxAccountManager build(Context context) {
        return DbxAccountManager.getInstance(context,
                context.getString(R.string.dropbox_key),
                context.getString(R.string.dropbox_secret));
    }
}
