package net.abesto.treasurer.tests.filters;

import net.abesto.treasurer.Store;
import net.abesto.treasurer.Transaction;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class PayeeToCategoryFilterTest {
    private PayeeToCategoryFilter filter;
    private List<PayeeToCategoryFilter.Rule> rules;

    private void assertPayeeToCategory(String payee, String category) {
        Transaction t = new Transaction(new Date(), payee, null, null, 0, 0);
        filter.filter(t);
        assertEquals(category, t.getCategory());
    }

    private void assertPayeeNotMatched(String payee) {
        assertPayeeToCategory(payee, null);
    }

    private void addRule(String category, String... payeeSubstrings) {
        rules.add(new PayeeToCategoryFilter.Rule(category, payeeSubstrings));
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    @Before
    public void setUp() throws IOException, ClassNotFoundException {
        //noinspection unchecked
        Store<PayeeToCategoryFilter.Rule> store = (Store<PayeeToCategoryFilter.Rule>) mock(Store.class);
        rules = new LinkedList<PayeeToCategoryFilter.Rule>();
        when(store.get()).thenReturn(rules);
        filter = new PayeeToCategoryFilter(store);
    }

    @Test
    public void testFullStringMatch() {
        String category = randomString();
        String payee1 = randomString();
        String payee2 = randomString();
        addRule(category, payee1, payee2);
        assertPayeeToCategory(payee1, category);
        assertPayeeToCategory(payee2, category);
    }

    @Test
    public void testSecondRuleSecondPayee() {
        String category = randomString();
        String payee = randomString();
        addRule(randomString(), randomString(), randomString());
        addRule(category, randomString(), payee);
        assertPayeeToCategory(payee, category);
    }

    @Test
    public void testNoMatch() {
        addRule(randomString(), randomString());
        addRule(randomString(), randomString(), randomString());
        assertPayeeNotMatched(randomString());
    }

    @Test
    public void testSubstring() {
        String category = randomString();
        String payee = randomString();
        String payeeSubstring = payee.substring(5, 20);
        addRule(category, payeeSubstring);
        assertPayeeToCategory(payee, category);
    }

    @Test
    public void testNonMatchingSubstring() {
        String category = randomString();
        String payee = randomString();
        String payeeSubstring = payee.substring(5, 20);
        addRule(category, payeeSubstring);

        String wrongPayee = randomString();
        while (wrongPayee.contains(payeeSubstring)) {
            wrongPayee = randomString();
        }

        assertPayeeNotMatched(wrongPayee);
    }

    @Test
    public void testAccents1() {
        String accented = "árvíztűrő tükörfúrógép";
        String unaccented = "arvizturo tukorfurogep";
        String category = randomString();
        addRule(category, accented);
        assertPayeeToCategory(unaccented, category);
    }

    @Test
    public void testAccents2() {
        String accented = "àrvíztűrő tükörfúrógép";
        String unaccented = "arvizturo tukorfurogep";
        String category = randomString();
        addRule(category, unaccented);
        assertPayeeToCategory(accented, category);
    }

    @Test
    public void testAccentsNegative() {
        addRule(randomString(), "a", "e");
        assertPayeeNotMatched("ö");
        assertPayeeNotMatched("ő");
    }
}
