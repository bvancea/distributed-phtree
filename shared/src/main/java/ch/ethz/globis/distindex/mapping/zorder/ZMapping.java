package ch.ethz.globis.distindex.mapping.zorder;

import ch.ethz.globis.distindex.mapping.bst.BST;
import ch.ethz.globis.pht.PhTreeRangeVD;

import java.util.*;

/**
 * Mapping for the Z-Order curve.
 */
public class ZMapping {

    int dim;
    private ZOrderService service;
    private boolean consistent = true;
    private PhTreeRangeVD<String> tree;
    private Map<String, Integer> sizes;
    private Map<String, Integer> order;

    public ZMapping(int dim) {
        this.dim = dim;
        this.service = new ZOrderService();
        this.tree = new PhTreeRangeVD<>(dim);
        this.sizes = new TreeMap<>();
        this.order = new TreeMap<>();
    }

    public Map<String, String> add(String hostId) {
        checkConsistency();

        tree = new PhTreeRangeVD<>(dim);

        Set<String> hosts = sizes.keySet();
        BST bst = new BST();
        for (String host : hosts) {
            bst.add(host);
        }
        bst.add(hostId);
        Map<String, String> mapping = bst.asMap();
        String prefix, host;
        int orderCount = 0;
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            prefix = entry.getKey();
            host = entry.getValue();

            //for each host
            long[] start = service.generateRangeStart(prefix, dim);
            long[] end = service.generateRangeEnd(prefix, dim);
            tree.put(convert(start), convert(end), host);
            order.put(host, orderCount++);
            sizes.put(host, 1);
        }
        return mapping;
    }

    public void remove(String hostId) {
        this.consistent = false;
    }

    public String get(long[] k) {
        checkConsistency();

        double[] key = convert(k);

        PhTreeRangeVD<String>.PHREntryIterator it = tree.queryIntersect(key, key);
        double[] start;
        double[] end;
        PhTreeRangeVD.PHREntry entry;

        String host = null;
        if (it.hasNext()) {
            entry = it.next();
            start = entry.lower();
            end = entry.upper();
            if (it.hasNext()) {
                throw new IllegalStateException("Areas not overlapping, more intersections returned.");
            }
            host = getValueFromTree(start, end);
        }
        return host;
    }

    public List<String> get(long[] l, long[] u) {
        checkConsistency();

        double[] lower = convert(l);
        double[] upper = convert(u);

        PhTreeRangeVD<String>.PHREntryIterator it = tree.queryIntersect(lower, upper);
        List<String> unsortedHosts = new ArrayList<>();
        String host;
        PhTreeRangeVD.PHREntry e;
        while (it.hasNext()) {
            e = it.next();
            host = getValueFromTree(e.lower(), e.upper());
            unsortedHosts.add(host);
        }

        //ToDO sort the hosts according to the z-order
        return unsortedHosts;
    }

    private String getValueFromTree(double[] lower, double[] upper) {
        String host = tree.put(lower, upper, null);
        if (host != null) {
            tree.put(lower, upper, host);
            return host;
        }
        return null;
    }

    private void checkConsistency() {
        if (!consistent) {
            throw new IllegalStateException("Mapping is inconsistent!");
        }
    }

    private double[] convert(long[] key) {
        double[] d = new double[key.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = key[i];
        }
        return d;
    }

    private long[] convert(double[] key) {
        long[] l = new long[key.length];
        for (int i = 0; i < key.length; i++) {
            l[i] = (long) key[i];
        }
        return l;
    }
}