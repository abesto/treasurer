package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class OTPTransferParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPTransferParser();
        assertFalse(p.parse("foobar", null).isSuccess());
        GregorianCalendar sent = Mockito.mock(GregorianCalendar.class);

        ParseResult r = p.parse(
                "...0418 Szàmla (131216) NAPKÖZBENI àTUTALàS:-14.000,-HUF; Közl:Közleményke; Partner:Egyszeri Pàl; Egy:+123.456,-HUF; OTPdirekt",
                sent);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        Calendar d = t.getDate();
        assertEquals(2013, d.get(Calendar.YEAR));
        assertEquals(11, d.get(Calendar.MONTH));
        assertEquals(16, d.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, d.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, d.get(Calendar.MINUTE));
        assertEquals(0, t.getInflow().intValue());
        assertEquals(14000, t.getOutflow().intValue());
        assertEquals("Egyszeri Pàl", t.getPayee());
        assertEquals("Közleményke", t.getMemo());
    }
}
