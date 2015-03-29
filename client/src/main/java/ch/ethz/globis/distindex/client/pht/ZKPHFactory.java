package ch.ethz.globis.distindex.client.pht;

import ch.ethz.globis.pht.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZKPHFactory implements PHFactory {

    private String zkHost;
    private int zkPort;

    private List<PHTreeIndexProxy> proxies = new ArrayList<>();

    public ZKPHFactory(String zkHost, int zkPort) {
        this.zkHost = zkHost;
        this.zkPort = zkPort;
    }

    @Override
    public <V> PHTreeIndexProxy<V> createProxy(int dim, int depth) {
        PHTreeIndexProxy<V> tree =  new PHTreeIndexProxy<>(zkHost, zkPort);
        proxies.add(tree);
        tree.create(dim, depth);
        return tree;
    }

    @Override
    public <V> PhTreeV<V> createPHTreeMap(int dim, int depth) {
        PHTreeIndexProxy<V> proxy = new PHTreeIndexProxy<>(zkHost, zkPort);
        proxies.add(proxy);
        proxy.create(dim, depth);
        return new DistributedPhTreeV<>(proxy);
    }

    @Override
    public <V> PhTreeVD<V> createPHTreeVD(int dim) {
        PhTreeV<V> proxy = createPHTreeMap(dim, Double.SIZE);

        return new PhTreeVD<>(proxy);
    }

    @Override
    public PhTree createPHTreeSet(int dim, int depth) {
        PhTreeV<Object> proxy = createPHTreeMap(dim, depth);

        return new PhTreeVProxy(proxy);
    }

    @Override
    public PhTreeRangeD createPHTreeRangeSet(int dim, int depth) {
        PhTree backingTree = createPHTreeSet(2 * dim, depth);

        return new PhTreeRangeD(backingTree);
    }

    @Override
    public void close() {
        for (PHTreeIndexProxy proxy : proxies) {
            try {
                proxy.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}