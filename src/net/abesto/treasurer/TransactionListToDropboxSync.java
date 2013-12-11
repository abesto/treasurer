package net.abesto.treasurer;

import android.content.Context;
import android.database.ContentObserver;
import android.util.Log;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.database.Provider;
import net.abesto.treasurer.upload.ynab.YNABStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class TransactionListToDropboxSync {
    private static final String TAG = "TransactionListToDropboxSync";
    private static DbxAccountManager dbxAccountManager;
    private static Context context;
    private static Map<String, ContentObserver> observers;

    public static void initializeComponent(Context _context) {
        context = _context;
        observers = new HashMap<String, ContentObserver>();
        dbxAccountManager = DbxAccountManagerFactory.build(context.getApplicationContext());
    }

    public static void register(final String filename) {
        if (observers.containsKey(filename)) {
            throw new RuntimeException("already registered");
        }

        ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                if (!dbxAccountManager.hasLinkedAccount()) {
                    Log.w(TAG, "no_linked_account");
                    return;
                }
                String text = YNABStringBuilder.listToCsv(
                        Queries.getAppInstance().list(Transaction.class)
                );
                try {
                    DbxFileSystem fs = DbxFileSystem.forAccount(dbxAccountManager.getLinkedAccount());
                    DbxPath path = new DbxPath("/" + filename);
                    DbxFile file;
                    if (fs.exists(path)) {
                        file = fs.open(path);
                    } else {
                        file = fs.create(path);
                    }
                    file.writeString(text);
                    file.close();
                } catch (Exception e) {
                    Log.e(TAG, "failed_to_write", e);
                }
            }
        };

        context.getContentResolver().registerContentObserver(
                Provider.TRANSACTIONS_URI, false, observer);
        observers.put(filename, observer);
        observer.onChange(true);
    }
}
