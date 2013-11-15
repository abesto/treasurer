package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class OTPCreditCardUsageParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPCreditCardUsageParser();
        assertFalse(p.parse("foobar").isSuccess());

        ParseResult r = p.parse("131102 17:24 Kartyas vasarlas/zarolas: -4.180 HUF; WWW.NETPINCER.HU etterem; Kartyaszam: ...5918; Egyenleg: 1 HUF - OTPdirekt");
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        GregorianCalendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(2, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(17, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(24, d.get(Calendar.MINUTE));
        assertEquals(0, t.getInflow().intValue());
        assertEquals(4180, t.getOutflow().intValue());
        assertEquals("WWW.NETPINCER.HU etterem", t.getPayee());
    }


}
