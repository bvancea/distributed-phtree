package ch.ethz.globis.distindex.phtree;

import ch.ethz.globis.distindex.BaseParameterizedTest;
import ch.ethz.globis.distindex.client.pht.PHFactory;
import ch.ethz.globis.pht.Bits;
import ch.ethz.globis.pht.PhTree;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

@Ignore
public class TestDeletion extends BaseParameterizedTest {

    private PHFactory factory;

    public TestDeletion(int nrServers) throws IOException {
        super(nrServers);
        factory = new PHFactory(HOST, ZK_PORT);
    }

    @Test
    public void testDeleteSingle() {
        PhTree ind = factory.createPHTreeSet(3, 32);
        Random R = new Random(0);
        for (int i = 0; i < 10000; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt(), R.nextInt()};
            ind.insert(v);
            assertTrue(ind.contains(v));
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testDeleteMulti2D() {
        PhTree ind = factory.createPHTreeSet(2, 16);
        Random R = new Random(1);
        int N = 20000;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt()};
            vals[i] = v;
            if (ind.insert(v)) {
                //catch duplicates, maybe in future we should just skip them
                i--;
                continue;
            }
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testDeleteMulti3D() {
        PhTree ind = factory.createPHTreeSet(3, 32);
        Random R = new Random(0);
        int N = 20000;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt(), R.nextInt()};
            vals[i] = v;
            if (ind.insert(v)) {
                //catch duplicates, maybe in future we should just skip them
                i--;
                continue;
            }
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testDeleteMultiMulti() {
        for (int s = 0; s < 5000; s++) {
            checkSeed(s);
        }
    }

    private void checkSeed(int s) {
        PhTree ind = factory.createPHTreeSet(3, 32);
        Random R = new Random(s);
        int N = 100;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt(), R.nextInt()};
            vals[i] = v;
            if (ind.insert(v)) {
                fail();
            }
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue("s=" + s + " i="+ i, ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug1() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        ArrayList<long[]> vA = new ArrayList<long[]>();
        vA.add(new long[]{1157023572, 396984392});//, 349120689});
        vA.add(new long[]{1291704192, 862408176});//, 837789372});

        for (long[] v: vA) {
            assertFalse(ind.insert(v));
        }

        for (long[] v: vA) {
            assertTrue(ind.delete(v));
        }
    }

    @Test
    public void testBug2() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        Random R = new Random(1);
        int N = 20;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt()};
            vals[i] = v;
            ind.insert(v);
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug3() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        Random R = new Random(3);
        int N = 25;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt()};
            vals[i] = v;
            ind.insert(v);
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug3b() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        ArrayList<long[]> vA = new ArrayList<long[]>();

        vA.add(new long[]{1904347123, 1743248268});
        vA.add(new long[]{1773228306, 318397575});
        vA.add(new long[]{2093614540, 470886284});

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            ind.insert(v);
        }

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            assertTrue(ind.contains(v));
        }

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug4() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        Random R = new Random(0);
        int N = 10;
        long[][] vals = new long[N][];
        for (int i = 0; i < N; i++) {
            long[] v = new long[]{R.nextInt(), R.nextInt()};
            vals[i] = v;
            ind.insert(v);
        }

        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug4b() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        ArrayList<long[]> vA = new ArrayList<long[]>();

        vA.add(new long[]{-1557280266, 1327362106});
        vA.add(new long[]{-518907128, 99135751});
        vA.add(new long[]{-252332814, 755814641});

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            ind.insert(v);
        }

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug5() {
        PhTree ind = factory.createPHTreeSet(2, 32);
        ArrayList<long[]> vA = new ArrayList<long[]>();

        vA.add(new long[]{-845879838, -187653156});
        vA.add(new long[]{-82500903, -2124478282});
        vA.add(new long[]{-423784092, -1668441430});
        vA.add(new long[]{-763332258, -1982438190});

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            ind.insert(v);
        }

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            //ind.printTree();
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testBug6() {
        //seed=2264
        PhTree ind = factory.createPHTreeSet(2, 32);
        ArrayList<long[]> vA = new ArrayList<long[]>();

        vA.add(new long[]{-571503246, -911425707});
        vA.add(new long[]{-291777302, -243251700});
        vA.add(new long[]{-2102706601, -693417435});
        vA.add(new long[]{-828373431, -133249064});
        vA.add(new long[]{-502327513, -1036737024});

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            assertFalse(ind.insert(v));
        }

        for (int i = 0; i < vA.size(); i++) {
            long[] v = vA.get(i);
            assertTrue(ind.delete(v));
            assertFalse(ind.contains(v));
            //try again.
            assertFalse(ind.delete(v));
            assertFalse(ind.contains(v));
        }
    }

    @Test
    public void testHighD64Neg() {
        final int MAX_DIM = 60;
        final int N = 1000;
        final int DEPTH = 64;
        Random R = new Random(0);

        for (int DIM = 3; DIM <= MAX_DIM; DIM++) {
            long[][] vals = new long[N][];
            PhTree ind = factory.createPHTreeSet(DIM, DEPTH);
            for (int i = 0; i < N; i++) {
                long[] v = new long[DIM];
                for (int j = 0; j < DIM; j++) {
                    v[j] = R.nextLong();
                }
                vals[i] = v;
                assertFalse(Bits.toBinary(v, DEPTH), ind.insert(v));
            }

            //delete all
            for (long[] v: vals) {
                assertTrue("DIM=" + DIM + " v=" + Bits.toBinary(v, DEPTH), ind.contains(v));
                assertTrue(ind.delete(v));
            }

            //check empty result
            long[] min = new long[DIM];
            long[] max = new long[DIM];
            for (int i = 0; i < DIM; i++) {
                min[i] = Long.MIN_VALUE;
                max[i] = Long.MAX_VALUE;
            }
            Iterator<long[]> it = ind.query(min, max);
            assertFalse(it.hasNext());

            assertEquals(0, ind.size());
        }
    }

    /**
     * Fails only with NI-threshold = 0 for subs and posts.
     */
    @Test
    public void testHighD64NegBug2() {
        final int DIM = 32;
        final int DEPTH = 64;

        long[][] vals = {
                { -1891420301368910726L, -7510416602987178625L, 7854356540669723662L, -4313796973086734422L, -3735585381220621847L, -2358801111684039663L, 8500592403625077914L, -3165069678806047833L, 408239638479573154L, 1394612278238908584L, 2346699220259279979L, 3580868071482899881L, 7961017168244967288L, -2014050115153595926L, 8051105003483558108L, -3700506842608314642L, -4048666454762884880L, 9008299648439358285L, 6204108650229647936L, 778480900451172040L, -8434661093710232123L, -8212527426587524194L, -917510832255457703L, 3958127369241215261L, 2259550045798965095L, 7032686370062058363L, 4591905256552858337L, 902882491532829926L, 303331575839660663L, 7449544573896043481L, 3092090196943101957L, 4515887688766405296L,  },
                { -4062685296717429368L, -1458447326603816251L, 72520369377032730L, 2504023206924321121L, 1315032411642037417L, -671118087238692233L, -5937697448152876824L, 2914069232554644162L, -122761879731138883L, -2704799728189953919L, -3595630054475699660L, -6904106471410655605L, 5398810977192619702L, -6244681884589765467L, 3001783947703718265L, 9096083008774451479L, 7106685045394120506L, 1612660455506562941L, -1602009131231155926L, 4674088701860260058L, 3523904087147023653L, -7471386555745361678L, 6434863598619692329L, 3519486867992011992L, 2580325349084506629L, 1716045732687783621L, -7492979958698560176L, -4514641440177765589L, 3721608777574387356L, -1662765351114890487L, 3457037762958540780L, -1786853829876224128L,  }};
        final int N = vals.length;
        PhTree ind = factory.createPHTreeSet(DIM, DEPTH);
        for (int i = 0; i < N; i++) {
            long[] v = vals[i];
            assertFalse(Bits.toBinary(v, DEPTH), ind.insert(v));
        }

        Iterator<long[]> it3 = ind.queryExtent();
        assertArrayEquals(vals[1], it3.next());
        assertArrayEquals(vals[0], it3.next());

        //delete all
        for (long[] v: vals) {
            assertTrue("DIM=" + DIM + " v=" + Bits.toBinary(v, DEPTH), ind.contains(v));
            assertTrue(ind.delete(v));
        }

        //check empty result
        long[] min = new long[DIM];
        long[] max = new long[DIM];
        for (int i = 0; i < DIM; i++) {
            min[i] = Long.MIN_VALUE;
            max[i] = Long.MAX_VALUE;
        }
        Iterator<long[]> it = ind.query(min, max);
        assertFalse(it.hasNext());

        assertEquals(0, ind.size());
    }
}
