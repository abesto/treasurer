package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;

import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPDirektDomesticTransferParser implements SmsParser {
    private static String patternString = "(\\d{2})(\\d{2})(\\d{2}) (\\d{2}):(\\d{2}) k.rty.s v.s.rl.s[^:]*: -([0-9\\.]*) HUF; ([^;]*)";
    private static Pattern pattern = Pattern.compile(patternString, Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);

    @Override
    public ParseResult parse(String sms, GregorianCalendar sent) {
        Matcher m = pattern.matcher(sms);
        if (!m.find()) return ParseResult.fail();
        return ParseResult.success(new Transaction(
                sent,
                m.group(7), // payee
                null,       // no category
                "",         // memo
                Integer.parseInt(m.group(6).replaceAll("\\.", ""), 10),  // outflow
                0           // inflow
        ));
    }
}
