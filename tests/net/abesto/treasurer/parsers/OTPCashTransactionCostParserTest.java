package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class OTPCashTransactionCostParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPCashTransactionCostParser();
        assertFalse(p.parse("foobar", null).isSuccess());
        GregorianCalendar sent = Mockito.mock(GregorianCalendar.class);

        ParseResult r = p.parse(
                "...0418 Szàmla (131210) KP.FELVÉT/-BEFIZ. DIJA:-561,-HUF; Egy:+123.456,-HUF; OTPdirekt",
                sent);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        Calendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(10, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, d.get(Calendar.MINUTE));
        assertEquals(561, t.getOutflow().intValue());
        assertEquals("OTP", t.getPayee());
        assertEquals("Kézpénz felvétel / befizetés díja", t.getMemo());
    }
}
