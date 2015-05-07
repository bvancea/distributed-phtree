package ch.ethz.globis.disindex.codec;

import ch.ethz.globis.disindex.codec.api.FieldDecoder;
import ch.ethz.globis.disindex.codec.api.RequestDecoder;
import ch.ethz.globis.distindex.operation.request.*;
import ch.ethz.globis.distindex.util.SerializerUtil;
import ch.ethz.globis.pht.PhPredicate;
import ch.ethz.globis.pht.util.PhMapper;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Decodes request messages from the client.
 *
 * The value is not decodeResult on the server side and is inserted in the tree as is.
 *
 * @param <K>                   The type of the key.
 */
public class ByteRequestDecoder<K> implements RequestDecoder<K, byte[]> {

    private static final Splitter.MapSplitter splitter = Splitter.on(",").withKeyValueSeparator("=>");

    /** The decoder used for the key.*/
    private FieldDecoder<K> keyDecoder;

    public ByteRequestDecoder(FieldDecoder<K> keyDecoder) {
        this.keyDecoder = keyDecoder;
    }

    /**
     * Decode a get request from the client.
     *
     * @param buffer                    The ByteBuffer containing bytes sent by the client.
     *                                  The request type has already been read.
     * @return                          The key for which the get operation has to be executed.
     */
    @Override
    public GetRequest<K> decodeGet(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        K key = decodeKey(buffer);
        assert !buffer.hasRemaining();

        return new GetRequest<>(requestId, opCode, indexName, mappingVersion, key);
    }

    /**
     * Decode a contains request from the client.
     *
     * @param buffer                    The ByteBuffer containing bytes sent by the client.
     *                                  The request type has already been read.
     * @return                          The key for which the contains operation has to be executed.
     */
    @Override
    public ContainsRequest<K> decodeContains(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        K key = decodeKey(buffer);
        assert !buffer.hasRemaining();
        return new ContainsRequest<>(requestId, opCode, indexName, mappingVersion, key);
    }

    /**
     * Decode a put request from the client.
     * @param buffer                    The ByteBuffer containing bytes sent by the client.
     *                                  The request type has already been read.
     * @return                          The key and value that have to be added to the index.
     */
    @Override
    public PutRequest<K, byte[]> decodePut(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K key = decodeKey(buffer);
        byte[] value = readValue(buffer);
        return new PutRequest<>(requestId, opCode, indexName, mappingVersion, key, value);
    }

    /**
     * Decode a get values in range request from the client.
     * @param buffer                    The ByteBuffer containing bytes sent by the client.
     *                                  The request type has already been read.
     * @return                          The keys containing the range for the request.
     */
    @Override
    public GetRangeRequest<K> decodeGetRange(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K start = decodeKey(buffer);
        K end = decodeKey(buffer);
        double distance = buffer.getDouble();
        return new GetRangeRequest<>(requestId, opCode, indexName, mappingVersion, start, end, distance);
    }

    @Override
    public GetRangeFilterMapperRequest<K> decodeGetRangeFilterMapper(ByteBuffer buffer) {
                try {
            byte opCode = buffer.get();
            int requestId = buffer.getInt();
            String indexName = new String(readValue(buffer));
            int mappingVersion = buffer.getInt();

            K start = decodeKey(buffer);
            K end = decodeKey(buffer);

            int maxResults = buffer.getInt();
            int mapperLength = buffer.getInt();
            byte[] mapperBytes = new byte[mapperLength];
            buffer.get(mapperBytes);
            PhMapper<?, ?> mapper;
            mapper = SerializerUtil.getInstance().deserializeDefault(mapperBytes);

            int filterLength = buffer.getInt();
            byte[] filterBytes = new byte[filterLength];
            buffer.get(filterBytes);
            PhPredicate filter = SerializerUtil.getInstance().deserializeDefault(filterBytes);
            return new GetRangeFilterMapperRequest<>(requestId, opCode, indexName, mappingVersion, start, end, maxResults, filter, mapper);
        } catch (IOException | ClassNotFoundException e) {
            throw new UnsupportedOperationException("Failed to perform decoding.", e);
        }
    }

    @Override
    public UpdateKeyRequest<K> decodeUpdateKeyRequest(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K start = decodeKey(buffer);
        K end = decodeKey(buffer);
        return new UpdateKeyRequest<K>(requestId, opCode, indexName, mappingVersion, start, end);
    }

    /**
     * Decode a k nearest neighbor request from the client.
     * @param buffer                    The ByteBuffer containing bytes sent by the client.
     *                                  The request type has already been read.
     * @return                          The key for which the search should be done and the number
     *                                  of neighbours.
     */
    @Override
    public GetKNNRequest<K> decodeGetKNN(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K key = decodeKey(buffer);
        int k = buffer.getInt();
        return new GetKNNRequest<>(requestId, opCode, indexName, mappingVersion, key, k);
    }

    @Override
    public GetIteratorBatchRequest<K> decodeGetBatch(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        String iteratorId = new String(readValue(buffer));
        int size = buffer.getInt();

        boolean isRanged = (buffer.getInt() != 0);
        if (isRanged) {
            K start = decodeKey(buffer);
            K end = decodeKey(buffer);
            return new GetIteratorBatchRequest<>(requestId, opCode, indexName, mappingVersion, iteratorId, size, start, end);
        }
        return new GetIteratorBatchRequest<>(requestId, opCode, indexName, mappingVersion, iteratorId, size);
    }


    @Override
    public CreateRequest decodeCreate(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        int dim = buffer.getInt();
        int depth = buffer.getInt();
        return new CreateRequest(requestId, opCode, indexName, mappingVersion, dim, depth);
    }

    @Override
    public DeleteRequest<K> decodeDelete(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K key = decodeKey(buffer);
        assert !buffer.hasRemaining();

        return new DeleteRequest<>(requestId, opCode, indexName, mappingVersion, key);
    }

    @Override
    public BaseRequest decodeBase(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        return new BaseRequest(requestId, opCode, indexName, mappingVersion);
    }

    @Override
    public MapRequest decodeMap(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        String mapString = new String(readValue(buffer));
        Map<String, String> contents = splitter.split(mapString);
        return new MapRequest(requestId, opCode, indexName, mappingVersion, contents);
    }

    @Override
    public InitBalancingRequest decodeInitBalancing(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        int size = buffer.getInt();
        int dim = buffer.getInt();
        int depth = buffer.getInt();
        return new InitBalancingRequest(requestId, opCode, indexName, mappingVersion, size, dim, depth);
    }

    @Override
    public PutBalancingRequest<K> decodePutBalancing(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();

        K key = decodeKey(buffer);
        byte[] value = readValue(buffer);
        return new PutBalancingRequest<>(requestId, opCode, indexName, mappingVersion, key, value);
    }

    @Override
    public CommitBalancingRequest decodeCommitBalancing(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        String mapString = new String(readValue(buffer));
        if (Strings.isNullOrEmpty(mapString)) {
            return new CommitBalancingRequest(requestId, opCode, indexName, mappingVersion);
        } else {
            Map<String, String> contents = splitter.split(mapString);
            return new CommitBalancingRequest(requestId, opCode, indexName, mappingVersion, contents);
        }
    }

    /**
     * Decode a key from the current ByteBuffer.
     *
     * The decoding is performed in the following steps:
     *  - the integer size of the key byte array is read from the buffer.
     *  - the byte array containing the key is then read.
     *  - the key is decoded using the keyDecoder.
     * @param buffer                        The ByteBuffer containing the key.
     * @return                              The decoded key.
     */
    private K decodeKey(ByteBuffer buffer) {
        int keyBytesSize = buffer.getInt();
        byte[] keyBytes = new byte[keyBytesSize];
        buffer.get(keyBytes);
        return keyDecoder.decode(keyBytes);
    }

    /**
     * Decode a value from the current ByteBuffer.
     *
     * The decoding is performed in the following steps:
     *  - the integer size of the value byte array is read from the buffer.
     *  - the byte array containing the value is then read.
     *  - the value is decoded using the valueDecoder.
     * @param buffer                        The ByteBuffer containing the key.
     * @return                              The decoded key.
     */
    private byte[] readValue(ByteBuffer buffer) {
        int valueBytesSize = buffer.getInt();
        byte[] valueBytes = new byte[valueBytesSize];
        buffer.get(valueBytes);
        return valueBytes;
    }

    public RollbackBalancingRequest decodeRollbackBalancing(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        String indexName = new String(readValue(buffer));
        int mappingVersion = buffer.getInt();
        String mapString = new String(readValue(buffer));
        if (Strings.isNullOrEmpty(mapString)) {
            return new RollbackBalancingRequest(requestId, opCode, indexName, mappingVersion);
        } else {
            Map<String, String> contents = splitter.split(mapString);
            return new RollbackBalancingRequest(requestId, opCode, indexName, mappingVersion, contents);
        }
    }
}