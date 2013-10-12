package net.abesto.treasurer;

import java.util.HashSet;
import java.util.Set;

import net.abesto.treasurer.SmsReceiver.Handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
	interface Handler {
		void handle(String sms);
	}
	
	private Set<String> wantedSenders;
	private Handler handler;
	
	public SmsReceiver(Set<String> wantedSenders2, Handler handler2) {
		wantedSenders = wantedSenders2;
		handler = handler2;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        if (wantedSenders.contains(msg_from)) {
                        	handler.handle(msgBody);
                        }
                    }
                }catch(Exception e){
                    Log.d("SmsReceiver",e.getMessage());
                }
            }
        }
	}
	
}
