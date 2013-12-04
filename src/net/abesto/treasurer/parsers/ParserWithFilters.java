package net.abesto.treasurer.parsers;

import net.abesto.treasurer.filters.TransactionFilter;

import java.util.GregorianCalendar;

public class ParserWithFilters implements SmsParser {
    private SmsParser parser;
    private TransactionFilter[] filters;

    public ParserWithFilters(SmsParser parser, TransactionFilter... filters) {
        this.parser = parser;
        this.filters = filters;
    }

    @Override
    public ParseResult parse(String sms, GregorianCalendar sent) {
        ParseResult r = parser.parse(sms, sent);
        if (r.isSuccess()) {
            for (TransactionFilter f : filters) {
                f.filter(r.getTransaction());
            }
        }
        return r;
    }
}
