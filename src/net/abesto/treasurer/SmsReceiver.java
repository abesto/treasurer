package net.abesto.treasurer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParser;
import net.abesto.treasurer.parsers.SmsParserDatabaseAdapter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    private SmsParser getParser() {
        return new SmsParserDatabaseAdapter(
                ParserFactory.getInstance().buildFromConfig()
        );
    }

    private Set<String> getWantedSenders(Context context) {
        return new HashSet<String>(Arrays.asList(
            StringUtils.split(PreferenceManager.getDefaultSharedPreferences(context).getString(
                    context.getResources().getString(R.string.pref_wantedSender_key),
                    context.getResources().getString(R.string.pref_wantedSender_default))
            , ',')
        ));
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String msgFrom;
            Set<String> wantedSenders = getWantedSenders(context);
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msgFrom = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        if (wantedSenders.contains(msgFrom)) {
                            Log.i(TAG, "received_sms " + msgFrom + " " + msgBody);
                            getParser().parse(msgBody);
                        }
                    }
                } catch(Exception e){
                    Log.e("SmsReceiver", "failure", e);
                }
            }
        } else {
            Log.w(TAG, "unexpected_intent_action " + intent.getAction());
        }
	}
	
}
