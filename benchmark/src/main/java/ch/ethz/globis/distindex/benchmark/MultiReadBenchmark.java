package ch.ethz.globis.distindex.benchmark;

import ch.ethz.globis.pht.PhTreeV;

import java.util.List;
import java.util.concurrent.Callable;

public class MultiReadBenchmark extends MultiBenchmark {

    public MultiReadBenchmark(int nrThreads, PhTreeV<Object> tree, List<long[]> entries) {
        super(nrThreads, tree, entries);

        for (long[] entry : entries) {
            tree.put(entry, null);
        }
    }

    @Override
    protected Callable<Result> createTask(int i, int nrEntriesPerThread, PhTreeV<Object> tree, List<long[]> entries) {
        return new ThreadedReader(i * nrEntriesPerThread, (i + 1) * nrEntriesPerThread, tree, entries);
    }
}