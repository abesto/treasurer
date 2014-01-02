package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class OTPCashWithdrawalParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPCashWithdrawalParser();
        assertFalse(p.parse("foobar", null).isSuccess());

        ParseResult r = p.parse("131221 08:47 ATM készpénz felvét/zàrolàs: -29.000 HUF; OTP, VALAHOL BUDAPESTEN; Kàrtyaszàm: ...5918; Egyenleg: 123.456 HUF - OTPdirekt", null);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        GregorianCalendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(21, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(8, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(47, d.get(Calendar.MINUTE));
        assertEquals(0, t.getInflow().intValue());
        assertEquals(29000, t.getOutflow().intValue());
        assertEquals("Transfer: Cash", t.getPayee());
        assertEquals("OTP, VALAHOL BUDAPESTEN", t.getMemo());
    }
}
