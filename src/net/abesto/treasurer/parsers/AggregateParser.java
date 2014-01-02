package net.abesto.treasurer.parsers;

import java.util.GregorianCalendar;

public class AggregateParser implements SmsParser {
    private SmsParser[] parsers;

    public AggregateParser(SmsParser... parsers) {
        this.parsers = parsers;
    }

    @Override
    public ParseResult parse(String sms, GregorianCalendar sent) {
        for (SmsParser parser : parsers) {
            ParseResult result = parser.parse(sms, sent);
            if (result.isSuccess()) return result;
        }
        return ParseResult.fail();
    }
}
