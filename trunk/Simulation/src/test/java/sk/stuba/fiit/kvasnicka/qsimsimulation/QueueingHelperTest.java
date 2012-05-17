package sk.stuba.fiit.kvasnicka.qsimsimulation;

import org.junit.Test;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.QueueingHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Igor Kvasnicka
 */
public class QueueingHelperTest {
    public QueueingHelperTest() {
    }


    /**
     * tests method that calculates, how many fragments is needed for a packet
     */
    @Test
    public void testCalculateFragmentSize() {

        int frSize = QueueingHelper.calculateFragmentSize(1, 2, 10, 5);
        assertEquals(5, frSize);

        int frSize2 = QueueingHelper.calculateFragmentSize(1, 2, 10, 11);
        assertEquals(10, frSize2);

        int frSize3 = QueueingHelper.calculateFragmentSize(2, 2, 10, 11);
        assertEquals(1, frSize3);

        int frSize4 = QueueingHelper.calculateFragmentSize(2, 2, 5, 9);
        assertEquals(4, frSize4);

        int frSize5 = QueueingHelper.calculateFragmentSize(1, 3, 5, 10);
        assertEquals(5, frSize5);


        try {
            QueueingHelper.calculateFragmentSize(2, 2, 10, 30);
            fail("this should throw exception, because I need more fragments for this packet - this is a problem in QueueingHelper.calculateNumberOfFragments()");
        } catch (IllegalStateException e) {
            //OK
        }

        try {
            QueueingHelper.calculateFragmentSize(3, 2, 10, 11);
            fail("this should throw exception, because fragment index is bigger than max fragment count");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testcalculateNumberOfFragments() {
        int frCount1 = QueueingHelper.calculateNumberOfFragments(10, 6);
        assertEquals(2, frCount1);

        int frCount2 = QueueingHelper.calculateNumberOfFragments(10, 5);
        assertEquals(2, frCount2);

        int frCount3 = QueueingHelper.calculateNumberOfFragments(10, 11);
        assertEquals(1, frCount3);

        try {
            QueueingHelper.calculateNumberOfFragments(10, 0);
            fail("this should throw exception, because MTU must not be 0");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            QueueingHelper.calculateNumberOfFragments(10, - 1);
            fail("this should throw exception, because MTU must not be negative");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }
}
