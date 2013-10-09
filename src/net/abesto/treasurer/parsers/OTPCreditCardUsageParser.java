package net.abesto.treasurer.parsers;

import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.abesto.treasurer.Transaction;

public class OTPCreditCardUsageParser implements SmsParser {
	private static String patternString = "^(\\d{2})(\\d{2})(\\d{2}) \\d{2}:\\d{2} kártyás vásárlás/zárolás: -([0-9\\.]*) HUF; ([^;]*); Kártyaszám: [0-9\\.]*;.*$";
	private static Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
	
	@Override
	public ParseResult parse(String sms) {
		Matcher m = pattern.matcher(sms);
		if (!m.matches()) return ParseResult.fail();
		return ParseResult.success(new Transaction(
				new GregorianCalendar(Integer.parseInt("20" + m.group(1)),
									  Integer.parseInt(m.group(2)),
									  Integer.parseInt(m.group(3))
				).getTime(),
				m.group(5), // payee				
				"",         // category
				"",         // memo
				Integer.parseInt(m.group(4).replaceAll("\\.", ""), 10),  // outflow
				0           // inflow
		));
	}
}
