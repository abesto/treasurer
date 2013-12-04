package net.abesto.treasurer.parsers;

import java.util.GregorianCalendar;

public interface SmsParser {
	ParseResult parse(String sms, GregorianCalendar sent);
}
