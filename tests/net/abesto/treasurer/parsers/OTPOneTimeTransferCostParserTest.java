package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class OTPOneTimeTransferCostParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPOneTimeTransferCostParser();
        assertFalse(p.parse("foobar", null).isSuccess());

        ParseResult r = p.parse("...0418 Szàmla (131216) ESETI MEGBIZàSOK KÖLTSÉGE:-278,-HUF; Egy:+123.456,-HUF; OTPdirekt", null);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        GregorianCalendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(16, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, d.get(Calendar.MINUTE));
        assertEquals(0, t.getInflow().intValue());
        assertEquals(278, t.getOutflow().intValue());
        assertEquals("OTP", t.getPayee());
        assertEquals("Eseti megbízások költsége", t.getMemo());
    }
}
