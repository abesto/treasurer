package net.abesto.treasurer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class TextInputDialogBuilder extends AlertDialog.Builder {
    private EditText textField;

    public interface OnPositiveClickListener {
        void onClick(String enteredText);
    }

    public interface OnNegativeClickListener {
        void onClick();
    }

    public TextInputDialogBuilder(Context context) {
        super(context);
        textField = new EditText(context);
        setView(textField);
    }

    public TextInputDialogBuilder(Context context, String title) {
        this(context);
        setTitle(title);
    }

    public void setDefaultValue(String text) {
        textField.setText(text);
    }

    public TextInputDialogBuilder setPositiveButton(String text, final OnPositiveClickListener listener) {
        setPositiveButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(textField.getText().toString());
            }
        });
        return this;
    }

    public TextInputDialogBuilder setNegativeButton(String text, final OnNegativeClickListener listener) {
        setNegativeButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick();
            }
        });
        return this;
    }
}
