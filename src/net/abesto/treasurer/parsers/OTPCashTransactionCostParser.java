package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;

import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPCashTransactionCostParser implements SmsParser {
    private static String patternString = "\\.\\.\\.[0-9]{4} Sz.mla \\((\\d{2})(\\d{2})(\\d{2})\\) KP\\.FELV.T/-BEFIZ. DIJA:-([0-9\\.]*),-HUF;.*";
    private static Pattern pattern = Pattern.compile(patternString, Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);

    @Override
    public ParseResult parse(String sms, GregorianCalendar sent) {
        Matcher m = pattern.matcher(sms);
        if (!m.find()) return ParseResult.fail();
        return ParseResult.success(new Transaction(
                new GregorianCalendar(
                        Integer.parseInt("20" + m.group(1)),
                        Integer.parseInt(m.group(2)) - 1,
                        Integer.parseInt(m.group(3)),
                        0, 0
                ),
                "OTP",      // payee
                "Kézpénz felvétel / befizetés díja",         // memo
                Integer.parseInt(m.group(4).replaceAll("\\.", ""), 10),  // outflow
                0           // inflow
        ));
    }
}
