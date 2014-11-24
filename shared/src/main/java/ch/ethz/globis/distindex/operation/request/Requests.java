package ch.ethz.globis.distindex.operation.request;

import ch.ethz.globis.distindex.operation.OpCode;
import ch.ethz.globis.distindex.operation.request.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Requests {

    private static final AtomicInteger ID = new AtomicInteger(0);

    public static <K> GetRequest<K> newGet(K key) {
        return new GetRequest<>(nextId(), OpCode.GET, "", key);
    }

    public static <K, V> PutRequest<K, V> newPut(K key, V value) {
        return new PutRequest<>(nextId(), OpCode.PUT, "", key, value);
    }

    public static <K> GetRangeRequest<K> newGetRange(K start, K end) {
        return new GetRangeRequest<>(nextId(), OpCode.GET_RANGE, "", start, end);
    }

    public static <K> GetRangeRequest<K> newGetRange(K start, K end, double distance) {
        return new GetRangeRequest<>(nextId(), OpCode.GET_RANGE, "", start, end, distance);
    }

    public static <K> GetKNNRequest<K> newGetKNN(K key, int k) {
        return new GetKNNRequest<>(nextId(), OpCode.GET_KNN, "", key, k);
    }

    public static <K> GetIteratorBatchRequest<K> newGetBatch(String iteratorId, int size) {
        return new GetIteratorBatchRequest<>(nextId(), OpCode.GET_BATCH, "", iteratorId, size);
    }

    public static <K> GetIteratorBatchRequest<K> newGetBatch(String iteratorId, int size, K start, K end) {
        return new GetIteratorBatchRequest<>(nextId(), OpCode.GET_BATCH, "", iteratorId, size, start, end);
    }

    public static <K> DeleteRequest<K> newDelete(K key) {
        return new DeleteRequest<>(nextId(), OpCode.DELETE, "", key);
    }

    public static CreateRequest newCreate(int dim, int depth) {
        return new CreateRequest(nextId(), OpCode.CREATE_INDEX, "", dim, depth);
    }

    public static MapRequest newMap(byte opCode) {
        return new MapRequest(nextId(), opCode, "");
    }

    public static BaseRequest newGetSize() {
        return new BaseRequest(nextId(), OpCode.GET_SIZE, "");
    }

    public static BaseRequest newGetDim() {
        return new BaseRequest(nextId(), OpCode.GET_DIM, "");
    }

    public static BaseRequest newGetDepth() {
        return new BaseRequest(nextId(), OpCode.GET_DEPTH, "");
    }

    private static int nextId() {
        return ID.incrementAndGet();
    }

    public static <K> ContainsRequest<K> newContains(K key) {
        return new ContainsRequest<>(nextId(), OpCode.CONTAINS, "", key);
    }
}