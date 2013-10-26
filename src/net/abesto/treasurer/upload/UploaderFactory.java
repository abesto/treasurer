package net.abesto.treasurer.upload;

import android.content.Context;
import net.abesto.treasurer.upload.ynab.YNABMailerDataProvider;
import net.abesto.treasurer.upload.ynab.YNABPastebinUploaderDataProvider;

public class UploaderFactory {
    private static UploaderFactory instance;
    private static Context context;

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

    public Uploader buildFromConfig() {
        // TODO
        return null;
    }

    private UploaderFactory() {}
    public static UploaderFactory getInstance() {
        if (instance == null) {
            instance = new UploaderFactory();
        }
        return instance;
    }
}

