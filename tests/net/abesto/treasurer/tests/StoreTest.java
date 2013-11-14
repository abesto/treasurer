package net.abesto.treasurer.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import net.abesto.treasurer.Store;
import net.abesto.treasurer.model.Transaction;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {
    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        Store.initializeComponent(mockContext);
    }

    @Test
    public void testGetInstanceByClass() throws Exception {
        Store<String> s1 = Store.getInstance(String.class);
        Store<String> s2 = Store.getInstance(String.class);
        assertSame(s1, s2);
        assertEquals("String.dat", s1.getFileName());
        verify(mockContext, times(1)).openFileInput("String.dat");
        Store<Transaction> t = Store.getInstance(Transaction.class);
        assertEquals("Transaction.dat", t.getFileName());
    }

    @Test
    public void testGetInstanceByName() throws Exception {
        Store<String> s1 = Store.getInstance("S1");
        Store<String> s2 = Store.getInstance("S2");
        assertNotSame(s1, s2);
        assertEquals("S1.dat", s1.getFileName());
        assertEquals("S2.dat", s2.getFileName());
        verify(mockContext, times(1)).openFileInput("S1.dat");
        verify(mockContext, times(1)).openFileInput("S2.dat");
    }
}
