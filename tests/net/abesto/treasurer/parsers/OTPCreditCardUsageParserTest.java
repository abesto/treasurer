package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class OTPCreditCardUsageParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPCreditCardPaymentParser();
        assertFalse(p.parse("foobar", null).isSuccess());

        ParseResult r = p.parse("131206 09:54 Kàrtyàs vàsàrlàs/zàrolàs: -29.867 HUF; ONLINE ?GYF?LSZOLG.,BI; Kàrtyaszàm: ...5918; Egyenleg: 123.456 HUF - OTPdirekt", null);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        GregorianCalendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(6, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(54, d.get(Calendar.MINUTE));
        assertEquals(0, t.getInflow().intValue());
        assertEquals(29867, t.getOutflow().intValue());
        assertEquals("ONLINE ?GYF?LSZOLG.,BI", t.getPayee());
    }
}
