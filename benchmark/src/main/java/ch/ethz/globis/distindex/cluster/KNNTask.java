package ch.ethz.globis.distindex.cluster;

import ch.ethz.globis.distindex.client.pht.PHFactory;
import ch.ethz.globis.pht.nv.PhTreeNV;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class KNNTask implements Runnable {
    private PhTreeNV tree;
    private int nrEntries;

    public KNNTask(PHFactory factory, int nrEntries, int dim, int depth) {
        this.tree = factory.createPHTreeSet(dim, depth);
        this.nrEntries = nrEntries;
    }

    @Override
    public void run() {
        work(tree, nrEntries);
    }

    private void work(PhTreeNV tree, int nrEntries) {
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
        return (long) ((Short.MAX_VALUE - 1) * r);
    }

    private void doWork(PhTreeNV tree, List<long[]> points) {
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long start, end;
        int k = 100;
        List<long[]> neighbours;
        for (long[] point : points) {
            tree.insert(point);
        }
        long[] point;
        for (int i = 0; i < nrEntries / 10; i++) {
            point = points.get(i);
            start = System.nanoTime();
            neighbours = tree.nearestNeighbour(k, point);
            end = System.nanoTime();
            System.out.println(date.format(new Date()) + ",end,knn,"+ ((end - start) / 1000000.0) + "," + neighbours.size());
        }
    }
}
