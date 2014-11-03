package ch.ethz.globis.distindex.client.pht;

import ch.ethz.globis.pht.PVIterator;
import ch.ethz.globis.pht.PhTree;
import ch.ethz.globis.pht.PhTreeQStats;
import ch.ethz.globis.pht.PhTreeV;

import java.util.List;

/**
 * Represents a distributed key-value PH Tree. It also conforms to the PH-Tree interfaces.
 * @param <V>                                   The type of the value.
 */
public class DistributedPhTreeV<V> implements PhTreeV<V> {

    /** Proxy to the remote PH tree */
    private DistributedPHTreeProxy<V> proxy;

    /**
     * Main constructor, avoids responsibility of building the proxy.
     * @param proxy
     */
    public DistributedPhTreeV(DistributedPHTreeProxy<V> proxy) {
        this.proxy = proxy;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getNodeCount() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PhTreeQStats getQuality() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PhTree.Stats getStats() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public PhTree.Stats getStatsIdealNoNode() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public V put(long[] key, V value) {
        return proxy.put(key, value);
    }

    @Override
    public boolean contains(long... key) {
        return proxy.contains(key);
    }

    @Override
    public V get(long... key) {
        return proxy.get(key);
    }

    @Override
    public V remove(long... key) {
        return proxy.remove(key);
    }

    @Override
    public String toStringPlain() {
        return "This PhTree instance is distributed. This method is not currently implemented for a distributed PhTree.";
    }

    @Override
    public String toStringTree() {
        return "This PhTree instance is distributed. This method is not currently implemented for a distributed PhTree.";
    }

    @Override
    public PVIterator<V> queryExtent() {
        return new DistributedPhTreeIterator<>(proxy.iterator());
    }

    @Override
    public PVIterator<V> query(long[] longs, long[] longs2) {
        return new DistributedPhTreeIterator<>(proxy.query(longs, longs2));
    }

    @Override
    public boolean isRangeEmpty(long[] longs, long[] longs2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getDIM() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getDEPTH() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<long[]> nearestNeighbour(int k, long... key) {
        return proxy.getNearestNeighbors(key, k);
    }
}