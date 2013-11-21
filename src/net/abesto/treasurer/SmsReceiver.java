package net.abesto.treasurer;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import net.abesto.treasurer.parsers.ParserFactory;
import net.abesto.treasurer.parsers.SmsParser;
import net.abesto.treasurer.parsers.SmsParserDatabaseAdapter;

public class SmsReceiver extends BroadcastReceiver {
    private static final String otp = "+36309400700";
    private static final String TAG = "SmsReceiver";

	private Set<String> wantedSenders;

	public SmsReceiver() {
        super();
        wantedSenders = new HashSet<String>();
        wantedSenders.add(otp);
	}

    private SmsParser getParser() {
        return new SmsParserDatabaseAdapter(
                ParserFactory.getInstance().buildFromConfig()
        );
    }

	@Override
	public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String msgFrom;
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
