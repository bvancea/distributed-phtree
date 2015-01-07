package ch.ethz.globis.distindex.mapping.zorder;

import ch.ethz.globis.distindex.mapping.KeyMapping;
import ch.ethz.globis.distindex.mapping.bst.BST;
import ch.ethz.globis.distindex.util.CollectionUtil;
import ch.ethz.globis.distindex.util.SerializerUtil;
import ch.ethz.globis.pht.PhTreeRangeV;

import java.util.*;

/**
 * Mapping for the Z-Order curve.
 */
public class ZMapping implements KeyMapping<long[]>{

    /** The dimension of the mapping. */
    int dim;

    /** The bit width of the mapping */
    int depth;

    /** A ZOrderService instance used by this mapping */
    private ZOrderService service;

    /** Consistency flag - is true only when the mapping is consistent */
    private boolean consistent = true;

    /** A ranged PhTree containing geometrical zones from the z-mapping mapped to each
     * host*/

    private transient PhTreeRangeV<String> tree;

    /** Mapping from the host ids to the sizes of the hosts. */
    private Map<String, Integer> sizes;

    private Map<String, long[]> startKeys;

    private Map<String, long[]> endKeys;

    /** A list of the hosts ids.*/
    private List<String> hosts = new ArrayList<>();

    private int version = 0;

    /**
     * No-arg constructor, needed for Kryo deserialization.
     * Should not be called.
     */
    public ZMapping() {
    }

    public ZMapping(int dim, int depth) {
        this(dim, depth, new ArrayList<String>());
    }

    public ZMapping(int dim, int depth, List<String> hosts) {
        this.dim = dim;
        this.depth = depth;
        this.service = new ZOrderService(depth);
        this.tree = new PhTreeRangeV<>(dim, depth);
        this.startKeys = new TreeMap<>();
        this.endKeys = new TreeMap<>();
        this.sizes = new TreeMap<>();
        this.hosts = hosts;
    }

    /**
     * Add multiple host id's at once.
     *
     *  @param hostIds
     */
    public void add(List<String> hostIds) {
        checkConsistency();

        this.hosts.addAll(hostIds);
        String[] hosts = hostIds.toArray(new String[hostIds.size()]);
        BST bst = BST.fromArray(hosts);

        updateRegions(bst.asMap());
        updateTree();
    }

    /**
     * Add the current host id to the mapping. Following the addition, the new host should also
     * be mapped to an interval on the z-order curve.
     *
     *
     * @param hostId                                    The id of the new host.
     * @return                                          The mapping as a Map object. Each entry contains the prefix
     *                                                  as the key and the hostId as the value.
     */
    public void add(String hostId) {
        checkConsistency();

        this.hosts.add(hostId);
        //add the nest hostId to the mapping
        Map<String, String> mapping = constructNewMapping(hostId);

        //reconstruct the set of regions mapped to each host
        updateRegions(mapping);
        updateTree();
    }

    public void updateTree() {
        this.tree = new PhTreeRangeV<>(dim);
        long[] start, end;
        for (String hostId : hosts) {
            start = startKeys.get(hostId);
            end = endKeys.get(hostId);
            Set<HBox> regions = service.regionEnvelopeInclusive(start, end);
            for (HBox region : regions) {
                tree.put(service.generateRangeStart(region.getCode(), dim),
                        service.generateRangeEnd(region.getCode(), dim),
                        hostId);
            }
        }
    }

    /**
     * Add the new host to the mapping.
     * @param newHostId
     * @return
     */
    private Map<String, String> constructNewMapping(String newHostId) {
        Set<String> hosts = sizes.keySet();
        BST bst = new BST();
        for (String host : hosts) {
            bst.add(host);
        }
        bst.add(newHostId);
        Map<String, String> mapping = bst.asMap();
        return mapping;
    }

    /**
     * Reconstruct the set of hquad regions associated with each host
     * based on the new mapping.
     *
     * @param mapping
     */
    private void updateRegions(Map<String, String> mapping) {
        tree = new PhTreeRangeV<>(dim);
        String prefix, host;
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            prefix = entry.getKey();
            host = entry.getValue();

            //for each host
            long[] start = service.generateRangeStart(prefix, dim);
            startKeys.put(host, start);
            long[] end = service.generateRangeEnd(prefix, dim);
            endKeys.put(host, end);
            sizes.put(host, 0);
        }
    }

    /**
     * Remove a host from the mapping.
     *
     * The mapping SHOULD be marked inconsistent and should not be able to be used following this operation.
     *
     * @param hostId
     */
    public void remove(String hostId) {
        this.consistent = false;
        this.sizes.remove(hostId);
        this.hosts.remove(hostId);
    }

    /**
     * Return the host id mapped to the zone of which the key k belongs to.
     *
     * @param k                                 The input key.
     * @return
     */
    public String get(long[] k) {
        checkConsistency();

        PhTreeRangeV<String>.PHREntryIterator it = tree.queryIntersect(k, k);
        PhTreeRangeV.PHREntry<String> entry;

        String host = null;
        if (it.hasNext()) {
            entry = it.next();
            if (it.hasNext()) {
                throw new IllegalStateException("Areas not overlapping, more intersections returned for " + Arrays.toString(k));
            }
            host = entry.value();
        }
        return host;
    }

    /**
     * Return the list of host id's whose zones intersect with the hyper-cubic range determined by the
     * parameters l and u.
     *
     * @param l
     * @param u
     * @return
     */
    public List<String> get(long[] l, long[] u) {
        checkConsistency();

        PhTreeRangeV<String>.PHREntryIterator it = tree.queryIntersect(l, u);

        TreeSet<String> unsortedHosts = new TreeSet<>();
        String host;
        PhTreeRangeV.PHREntry e;
        while (it.hasNext()) {
            e = it.next();
            host = getValueFromTree(e.lower(), e.upper());
            unsortedHosts.add(host);
        }

        //ToDO sort the hosts according to the z-order
        return new ArrayList<>(unsortedHosts);
    }

    private String getValueFromTree(long[] lower, long[] upper) {
        String host = tree.put(lower, upper, null);
        if (host != null) {
            tree.put(lower, upper, host);
            return host;
        }
        return null;
    }

    /**
     * @return                                      A List of the host id's of the hosts in the mapping.
     */
    @Override
    public List<String> get() {
        checkConsistency();

        return hosts;
    }

    /**
     * @return                                      The host id of the first host in the mapping.
     */
    @Override
    public String getFirst() {
        checkConsistency();

        return (hosts != null && hosts.size() > 0 ) ? hosts.get(0) : null;
    }

    /**
     * Return the id of the host whose mapped region follows the region of the host whose id was received
     * as an argument.
     * @param hostId                                The preceding host's id.
     * @return                                      The next host's id.
     */
    @Override
    public String getNext(String hostId) {
        checkConsistency();

        int index = CollectionUtil.search(hosts, hostId);

        //index of the following
        index += 1;
        return (hosts.size() > index) ? hosts.get(index) : null;
    }

    @Override
    public String getPrevious(String hostId) {
        checkConsistency();

        int index = CollectionUtil.search(hosts, hostId);

        //index of the following
        index -= 1;
        return (index >= 0) ? hosts.get(index) : null;
    }

    /**
     * @return                                      The number of hosts in the mapping.
     */
    @Override
    public int size() {
        checkConsistency();

        return hosts.size();
    }

    /**
     * Clear the mapping.
     */
    @Override
    public void clear() {
        this.hosts.clear();
        this.sizes.clear();
        this.startKeys.clear();
        this.endKeys.clear();
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    public void changeIntervalStart(String host, long[] start) {
        this.startKeys.put(host, start);
    }

    public void changeIntervalEnd(String host, long[] end) {
        this.endKeys.put(host, end);
    }

    /**
     * Serialize the current mapping object into an array of bytes.
     *
     * @return                                  The array of bytes.
     */
    public byte[] serialize() {
       return SerializerUtil.getInstance().serialize(this);
    }

    /**
     * Create a mapping object from a serialized representation of a mapping.
     *
     * @param data                              The byte array obtained by serializing a previous mapping.
     * @return                                  A mapping object.
     */
    public static ZMapping deserialize(byte[] data) {
        if (data.length == 0) {
            return null;
        }
        ZMapping mapping = SerializerUtil.getInstance().deserialize(data);
        mapping.updateTree();
        return mapping;
    }

    private void checkConsistency() {
        if (!consistent) {
            throw new IllegalStateException("Mapping is inconsistent!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZMapping)) return false;

        ZMapping mapping = (ZMapping) o;

        if (consistent != mapping.consistent) return false;
        if (dim != mapping.dim) return false;
        if (service != null ? !service.equals(mapping.service) : mapping.service != null) return false;
        if (sizes != null ? !sizes.equals(mapping.sizes) : mapping.sizes != null) return false;

        //FIXME need to also compare the tree
        // if (tree != null ? !tree.toString().equals(mapping.tree.toString()) : mapping.tree != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dim;
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (consistent ? 1 : 0);
        result = 31 * result + (tree != null ? tree.hashCode() : 0);
        result = 31 * result + (sizes != null ? sizes.hashCode() : 0);
        return result;
    }
}