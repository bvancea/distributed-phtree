package ch.ethz.globis.distindex.mapping.bst;

import ch.ethz.globis.distindex.mapping.KeyMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BSTMapping<K> implements KeyMapping<K> {

    private BST<K> bst;
    private KeyConverter<K> converter;
    private List<String> intervals;

    public BSTMapping(KeyConverter<K> converter) {
        this.converter = converter;
        this.bst = new BST<K>();
        this.intervals = bst.leafs();
    }

    public BSTMapping(KeyConverter<K> converter, String[] hosts) {
        this.converter = converter;
        this.bst = BST.fromArray(hosts);
        this.intervals = bst.leafs();
    }

    @Override
    public String getHostId(K key) {
        BSTNode node = find(key);
        if (node == null) {
            return null;
        }
        return node.getContent();
    }

    @Override
    public List<String> getHostIds(K start, K end) {
        if (bst == null) {
            return new ArrayList<>();
        }
        return bst.leafs();
    }

    @Override
    public List<String> getHostIds() {
        if (bst == null) {
            return new ArrayList<>();
        }
        return bst.leafs();
    }

    @Override
    public String getFirst() {
        return intervals.get(0);
    }

    @Override
    public String getNext(String hostId) {
        int index = Collections.binarySearch(intervals, hostId);
        return intervals.get(index + 1);
    }

    @Override
    public void add(String host) {
        List<String> keys = bst.leafs();
        keys.add(host);
        bst = BST.fromArray(keys.toArray(new String[keys.size()]));
    }

    private BSTNode find(K key) {
        BSTNode current = bst.getRoot();
        if (current == null) {
            return null;
        }
        int position = 0;
        BSTNode previous = null;
        while (current != null) {
            previous = current;
            current = converter.isBitSet(key, position) ? current.getRight() : current.getLeft();
            position++;
        }
        return previous;
    }

}
