package net.abesto.treasurer.upload.ynab;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;

public class YNABDateFormatter {
	@SuppressLint("SimpleDateFormat")
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
	public static String formatDate(Calendar date) {
		return dateFormat.format(date);
	}
}
