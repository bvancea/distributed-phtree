package ch.ethz.globis.distindex.benchmark;

import ch.ethz.globis.pht.PhTreeV;

import java.util.List;
import java.util.concurrent.Callable;

public class ThreadedReaderWithInserts implements Callable<Result> {

    private final int startIndex;
    private final int endIndex;
    private final PhTreeV<Object> tree;
    private final List<long[]> entries;

    private int magic = 0;

    ThreadedReaderWithInserts(int startIndex, int endIndex, PhTreeV<Object> tree, List<long[]> entries) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.tree = tree;
        this.entries = entries;
    }

    @Override
    public Result call() throws Exception {
        double averageResponseTime = 0;
        long start = System.nanoTime();

        long s, e;

        for (int i = startIndex; i < endIndex; i++) {

            tree.put(entries.get(i), null);
            s = System.nanoTime();
            Object o = tree.get(entries.get(i));
            e = System.nanoTime();
            averageResponseTime += (e - s) / 1000000.0;

            //attempt to prevent compiler from optimizing this
            magic += (o == null) ? magic + 1: magic;
        }

        averageResponseTime /= (endIndex - startIndex);
        long end = System.nanoTime();
        int nrEntries = endIndex - startIndex;
        start /= 1000000.0;
        end /= 1000000.0;
        return new Result(start, end, nrEntries, averageResponseTime);
    }
}
