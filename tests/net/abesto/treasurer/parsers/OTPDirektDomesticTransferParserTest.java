package net.abesto.treasurer.parsers;

import net.abesto.treasurer.model.Transaction;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import java.util.GregorianCalendar;


public class OTPDirektDomesticTransferParserTest {
    @Test
    public void testParse() throws Exception {
        SmsParser p = new OTPDirektDomesticTransferParser();
        assertFalse(p.parse("foobar", null).isSuccess());
        GregorianCalendar sent = Mockito.mock(GregorianCalendar.class);

        ParseResult r = p.parse(
                "OTPdirekt - Belföldi forint átutalás ...0123 számlán 123.456 HUF összeggel 11223344-5566778 számlára. Azonosító: 12345678 Jóváhagyás 16:11-ig.",
                sent);
        assertTrue(r.isSuccess());
        Transaction t = r.getTransaction();
        assertSame(sent, t.getDate());
        assertEquals(0, t.getInflow().intValue());
        assertEquals(123456, t.getOutflow().intValue());
        assertEquals("11223344-5566778", t.getPayee());
        assertEquals("12345678", t.getMemo());
    }
}
