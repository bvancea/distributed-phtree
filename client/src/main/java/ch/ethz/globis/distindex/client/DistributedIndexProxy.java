package ch.ethz.globis.distindex.client;

import ch.ethz.globis.distindex.api.Index;
import ch.ethz.globis.distindex.api.IndexEntry;
import ch.ethz.globis.distindex.api.IndexEntryList;
import ch.ethz.globis.distindex.client.io.RequestDispatcher;
import ch.ethz.globis.distindex.mapping.KeyMapping;
import ch.ethz.globis.distindex.operation.*;
import ch.ethz.globis.distindex.orchestration.ClusterService;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Proxy class for working with a distributed, remote index.
 *
 * Translates each method call into a request to one or more remote remote nods which store the index data. The
 * received response are processed to decode the results. *
 *
 * @param <K>
 * @param <V>
 */
public class DistributedIndexProxy<K, V> implements Index<K, V>, Closeable, AutoCloseable{

    protected RequestDispatcher<K, V> requestDispatcher;
    protected ClusterService<K> clusterService;

    public boolean create(int dim, int depth) {
        KeyMapping<K> keyMapping = clusterService.getMapping();
        List<String> hostIds = keyMapping.getHostIds();

        CreateRequest request = Requests.newCreate(dim, depth);
        List<Response<K, V>> responses = requestDispatcher.send(hostIds, request);
        for (Response<K, V> response : responses) {
            if (response.getStatus() != OpStatus.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void put(K key, V value) {
        KeyMapping<K> keyMapping = clusterService.getMapping();
        String hostId = keyMapping.getHostId(key);

        PutRequest<K, V> request = Requests.newPut(key, value);
        requestDispatcher.send(hostId, request);
    }

    @Override
    public V get(K key) {
        KeyMapping<K> keyMapping = clusterService.getMapping();
        String hostId = keyMapping.getHostId(key);

        GetRequest<K> request = Requests.newGet(key);
        Response<K, V> response = requestDispatcher.send(hostId, request);

        return (response.getNrEntries() == 0) ? null :  response.singleEntry().getValue();
    }

    @Override
    public IndexEntryList<K, V> getRange(K start, K end) {
        KeyMapping<K> keyMapping = clusterService.getMapping();
        List<String> hostIds = keyMapping.getHostIds(start, end);

        GetRangeRequest<K> request = Requests.newGetRange(start, end);
        List<Response<K, V>> responses = requestDispatcher.send(hostIds, request);
        return combine(responses);
    }

    @Override
    public IndexEntryList<K, V> getNearestNeighbors(K key, int k) {
        KeyMapping<K> keyMapping = clusterService.getMapping();
        List<String> hostIds = keyMapping.getHostIds();

        GetKNNRequest<K> request = Requests.newGetKNN(key, k);
        List<Response<K, V>> responses = requestDispatcher.send(hostIds, request);
        return combine(responses);
    }

    public Response<K, V> getNextBatch(String hostId, String iteratorId, int size, K start, K end) {
        GetIteratorBatch<K> request = Requests.newGetBatch(iteratorId, size, start, end);
        Response<K, V> response = requestDispatcher.send(hostId, request);

        checkStatus(response);
        return response;
    }

    public Response<K, V> getNextBatch(String hostId, String iteratorId, int size) {
        GetIteratorBatch<K> request = Requests.newGetBatch(iteratorId, size);
        Response<K, V> response = requestDispatcher.send(hostId, request);

        checkStatus(response);
        return response;
    }

    @Override
    public Iterator<IndexEntry<K, V>> iterator() {
        KeyMapping<K> keyMapping = clusterService.getMapping();

        return new DistributedIndexIterator<>(this, keyMapping);
    }

    public Iterator<IndexEntry<K, V>> query(K start, K end) {
        KeyMapping<K> keyMapping = clusterService.getMapping();

        return new DistributedIndexRangedIterator<>(this, keyMapping, start, end);
    }

    private IndexEntryList<K, V> combine(List<Response<K, V>> responses) {
        IndexEntryList<K, V> results = new IndexEntryList<>();
        for (Response<K,V> response : responses) {
            results.addAll(response.getEntries());
        }
        return results;
    }

    private void checkStatus(Response response) {
        if (response.getStatus() != OpStatus.SUCCESS) {
            throw new RuntimeException("Error on server side");
        }
    }

    @Override
    public void close() throws IOException {
        if (requestDispatcher != null) {
            requestDispatcher.close();
        }
        if (clusterService != null) {
            clusterService.disconnect();
        }
    }
}