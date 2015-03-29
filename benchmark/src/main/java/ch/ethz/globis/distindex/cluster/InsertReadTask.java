package ch.ethz.globis.distindex.cluster;

import ch.ethz.globis.distindex.client.pht.PHFactory;
import ch.ethz.globis.pht.PhTree;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class InsertReadTask implements Runnable {

    private PhTree tree;
    private int nrEntries;

    public InsertReadTask(PHFactory factory, int nrEntries, int dim, int depth) {
        this.tree = factory.createPHTreeSet(dim, depth);
        this.nrEntries = nrEntries;
    }

    @Override
    public void run() {
        work(tree, nrEntries);
    }

    private void work(PhTree tree, int nrEntries) {
        List<long[]> entries = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < nrEntries; i++) {
            entries.add(new long[]{
                    gaussianRandomValue(random), gaussianRandomValue(random)
            });
        }

        doWork(tree, entries);
    }

    private long gaussianRandomValue(Random random) {
        double r = random.nextGaussian();
        return (long) ((Long.MAX_VALUE - 1) * r);
    }

    private boolean doWork(PhTree tree, List<long[]> points) {
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long start, end;
        boolean res = true;
        for (long[] point : points) {
            start = System.currentTimeMillis();
            tree.insert(point);
            end = System.currentTimeMillis();
            System.out.println(date.format(new Date()) + ",end,insert,"+ (end - start));
            start = System.currentTimeMillis();
            res &= tree.contains(point);
            end = System.currentTimeMillis();
            System.out.println(date.format(new Date()) + ",end,get,"+ (end - start));
        }
        return res;
    }
}