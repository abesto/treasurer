package net.abesto.treasurer.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import net.abesto.treasurer.R;
import net.abesto.treasurer.upload.ynab.YNABMailerDataProvider;
import net.abesto.treasurer.upload.ynab.YNABPastebinUploaderDataProvider;

public class UploaderFactory {
    private static UploaderFactory instance;
    private static Context context;
    private static final String TAG = "UploaderFactory";

    public static void initializeComponent(Context _context) {
        context = _context;
    }

    public enum UploaderFormat {YNAB}
    public enum UploaderType {PASTEBIN, MAIL}

    public Uploader build(UploaderFormat format, UploaderType uploader, UploadData data) {
        switch(format) {
            case YNAB: switch(uploader) {
                case MAIL:
                    return new Mailer(new YNABMailerDataProvider(context, data));
                case PASTEBIN:
                    return new PastebinUploader(new YNABPastebinUploaderDataProvider(context, data));
            }
        }
        return null;
    }

    public Uploader buildFromConfig(UploadData data) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        UploaderFormat format = UploaderFormat.YNAB;

        UploaderType type;
        String typeStr = prefs.getString(
                res.getString(R.string.pref_senderType_key),
                res.getString(R.string.pref_senderType_default)
        );


        if (typeStr.equals(res.getString(R.string.pref_senderType_emailValue))) {
            type = UploaderType.MAIL;
        } else if (typeStr.equals(res.getString(R.string.pref_senderType_pastebinValue))) {
            type = UploaderType.PASTEBIN;
        } else {
            throw new RuntimeException("Unknown uploader type found in preferences: " + typeStr);
        }

        Log.i(TAG, String.format("built_uploader %s %s", format, type));

        return build(format, type, data);
    }

    private UploaderFactory() {}
    public static UploaderFactory getInstance() {
        if (instance == null) {
            instance = new UploaderFactory();
        }
        return instance;
    }
}

