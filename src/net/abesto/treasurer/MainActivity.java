package net.abesto.treasurer;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import net.abesto.treasurer.R;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import net.abesto.treasurer.filters.TransactionFilter;
import net.abesto.treasurer.parsers.OTPCreditCardUsageParser;
import net.abesto.treasurer.parsers.ParseResult;
import net.abesto.treasurer.parsers.SmsParser;
import net.abesto.treasurer.upload.ynab.YNABCsvBuilder;
import net.abesto.treasurer.upload.ynab.YNABPastebinUploader;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ListView list = (ListView) findViewById(R.id.listView1);

		List<String> messages = getLastMonthsMessages();		
				
		SmsParser p = new OTPCreditCardUsageParser();
	    TransactionFilter f = new PayeeToCategoryFilter();
	    TransactionStore s = new TransactionStore(this);
	    try {
			s.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
 
	    
	    for (String sms : messages) {
	    	Log.i("Parsing", sms);
	    	ParseResult r = p.parse(sms);
	    	if (r.isSuccess()) {
	    		Transaction t = r.getTransaction();
	    		f.filter(t);
	    		try {
					s.add(t);
					Log.i("dump", t.getPayee());
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
	    	} else {
	    		try {
					s.failed(sms);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
	    	}
	    }
	    
	    Log.i("parsing", "done");
	    
//	    YNABPastebinUploader u = new YNABPastebinUploader();
//		try {
//			hello.setText(u.upload(s.get()));
//		} catch (Exception e) {
//			hello.setText(e.toString());
//			e.printStackTrace();
//		}
	    try {
	    	TransactionAdapter a = new TransactionAdapter(this, R.id.listView1, 
	    			new ArrayList<Transaction>(s.get().transactions));
	    	list.setAdapter(a);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private List<String> getHardcodedMessages() {
		return Arrays.asList(
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; jegy-és bérletpánzt, budapest nyugati pu.metro; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"131006 21:19 kártyás vásárlás/zárolás: -3.850 huf; hülye payee; kártyaszám: ...5918; egyenleg: 111.111 huf - otpdirekt",
			"ez meg eleve rossz"
		);
	}
	
	private List<String> getLastMonthsMessages() {
        ArrayList<String> messages = new ArrayList<String>();

    	Calendar monthAgo = Calendar.getInstance();
    	monthAgo.add(Calendar.MONTH, -1);
        
        final String[] projection =
                new String[] { "body" };
        String selection = "address = ? AND date > ?";
        String[] selectionArgs = new String[]{"+36309400700", Long.valueOf(monthAgo.getTimeInMillis()).toString()};
        final String sortOrder = "date ASC";

        // Create cursor
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursor != null) {
            try {
                int count = cursor.getCount();
                if (count > 0) {
                    while (cursor.moveToNext()) {
                        messages.add(cursor.getString(0));
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return messages;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
//    private Boolean sendMail(TransactionStore store) {
//        Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.port", "587");
//		
//        props.setProperty("mail.smtp.host", "smtp.gmail.com");
//        
//		Session session = Session.getInstance(props,
//				  new javax.mail.Authenticator() {
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication("abesto0@gmail.com", "password");
//					}
//				  });
//
//        try {
//            Message msg = new MimeMessage(session);
//            msg.setFrom(new InternetAddress("abesto0@gmail.com", "Treasurer"));
//            msg.addRecipient(Message.RecipientType.TO,
//                             new InternetAddress("abesto0@gmail.com", "Zolt??n Nagy"));
//            msg.setSubject("Transaction report");
//            msg.setSentDate(new Date());
//
//            BodyPart body = new MimeBodyPart();
//            body.setText(text);
//            
//            BodyPart attachment = new MimeBodyPart();
//            attachment.setFileName("transaction-20130101.csv");
//            attachment.setText(text);
//            
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(body);
//            multipart.addBodyPart(attachment);
//            msg.setContent(multipart);
//            
//            Transport.send(msg);
//        } catch (Exception e) {
//        	// TODO report error
//            e.printStackTrace();
//        	return false;
//		}
//        return true;
//    }
}
