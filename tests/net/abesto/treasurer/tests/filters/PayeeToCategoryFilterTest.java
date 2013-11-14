package net.abesto.treasurer.tests.filters;

import net.abesto.treasurer.database.ObjectNotFoundException;
import net.abesto.treasurer.database.Queries;
import net.abesto.treasurer.model.Category;
import net.abesto.treasurer.model.PayeeSubstringToCategory;
import net.abesto.treasurer.model.Transaction;
import net.abesto.treasurer.filters.PayeeToCategoryFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PayeeToCategoryFilterTest {
    private PayeeToCategoryFilter filter;
    private LinkedList<PayeeSubstringToCategory> rules;
    private Long nextRandomLong = 0L;

    private void assertPayeeToCategory(String payee, Long categoryId) {
        Transaction t = new Transaction(new Date(), payee, null, null, 0, 0);
        filter.filter(t);
        assertEquals(categoryId, t.getCategoryId());
    }

    private void assertPayeeNotMatched(String payee) {
        assertPayeeToCategory(payee, null);
    }

    private void addRule(Long categoryId, String... payeeSubstrings) {
        for (String payee : payeeSubstrings) {
            rules.add(new PayeeSubstringToCategory(payee, categoryId));
        }
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    @Before
    public void setUp() throws IOException, ClassNotFoundException, ObjectNotFoundException {
        //noinspection unchecked
        Queries queries = Mockito.mock(Queries.class);
        rules = new LinkedList<PayeeSubstringToCategory>();
        Mockito.when(queries.list(PayeeSubstringToCategory.class)).thenReturn(rules);
        Mockito.when(queries.get(Mockito.eq(Category.class), Mockito.anyLong())).thenReturn(new Category("Mock category"));
        filter = new PayeeToCategoryFilter(queries);
    }

    @Test
    public void testFullStringMatch() {
        Long category = randomLong();
        String payee1 = randomString();
        String payee2 = randomString();
        addRule(category, payee1, payee2);
        assertPayeeToCategory(payee1, category);
        assertPayeeToCategory(payee2, category);
    }

    private Long randomLong() {
        return nextRandomLong++;
    }

    @Test
    public void testSecondRuleSecondPayee() {
        Long category = randomLong();
        String payee = randomString();
        addRule(randomLong(), randomString(), randomString());
        addRule(category, randomString(), payee);
        assertPayeeToCategory(payee, category);
    }

    @Test
    public void testNoMatch() {
        addRule(randomLong(), randomString());
        addRule(randomLong(), randomString(), randomString());
        assertPayeeNotMatched(randomString());
    }

    @Test
    public void testSubstring() {
        Long category = randomLong();
        String payee = randomString();
        String payeeSubstring = payee.substring(5, 20);
        addRule(category, payeeSubstring);
        assertPayeeToCategory(payee, category);
    }

    @Test
    public void testNonMatchingSubstring() {
        Long category = randomLong();
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
        Long category = randomLong();
        addRule(category, accented);
        assertPayeeToCategory(unaccented, category);
    }

    @Test
    public void testAccents2() {
        String accented = "àrvíztűrő tükörfúrógép";
        String unaccented = "arvizturo tukorfurogep";
        Long category = randomLong();
        addRule(category, unaccented);
        assertPayeeToCategory(accented, category);
    }

    @Test
    public void testAccentsNegative() {
        addRule(randomLong(), "a", "e");
        assertPayeeNotMatched("ö");
        assertPayeeNotMatched("ő");
    }

    @Test
    public void testCaseInsensitive1() {
        Long category = randomLong();
        String payee = randomString();
        String uppercase = payee.toUpperCase();
        addRule(category, payee);
        assertPayeeToCategory(uppercase, category);
    }

    @Test
    public void testCaseInsensitive2() {
        Long category = randomLong();
        String payee = randomString();
        String uppercase = payee.toUpperCase();
        addRule(category, uppercase);
        assertPayeeToCategory(payee, category);
    }
}
