package ch.ethz.globis.distindex.client;

import ch.ethz.globis.distindex.api.IndexEntry;
import ch.ethz.globis.distindex.api.IndexEntryList;
import ch.ethz.globis.distindex.api.IndexIterator;
import ch.ethz.globis.distindex.mapping.KeyMapping;
import ch.ethz.globis.distindex.operation.Response;

import java.util.Iterator;

public class DistributedIndexIterator<K, V> implements IndexIterator<K, V> {

    /** The index over which the iterator is running. */
    DistributedIndexProxy<K, V> indexProxy;

    /** The entries that are currently buffered*/
    IndexEntryList<K, V> entryBuffer;

    /** The number of entries to bring in a batch request */
    private int batchSize = 2;

    private String currentHostId;
    private KeyMapping<K> keyMapping;
    private String iteratorId = "";

    // the position within the entry buffer
    private int position = -1;

    public DistributedIndexIterator(DistributedIndexProxy<K, V> indexProxy, KeyMapping<K> keyMapping) {
        this.indexProxy = indexProxy;
        this.keyMapping = keyMapping;
        this.currentHostId = keyMapping.getFirst();

        getRemoteEntries();
    }

    @Override
    public boolean hasNext() {
        if (position < entryBuffer.size()) {
            return true;
        }
        entryBuffer.clear();
        getRemoteEntries();
        return entryBuffer.size() > 0;
    }

    @Override
    public IndexEntry<K, V> next() {
        return entryBuffer.get(position++);
    }

    private void getRemoteEntries() {
        if (currentHostId == null) {
            return;
        }
        Response<K, V> response = indexProxy.getNextBatch(currentHostId, iteratorId, batchSize);
        if (response != null) {
            if (response.getIteratorId().equals("")) {
                currentHostId = keyMapping.getNext(currentHostId);
            }
            iteratorId = response.getIteratorId();
            entryBuffer = response.getEntries();
        } else {
            entryBuffer = new IndexEntryList<>();
        }
        position = 0;
    }
}