package ch.ethz.globis.distindex.phtree;

import ch.ethz.globis.disindex.codec.ByteRequestEncoder;
import ch.ethz.globis.disindex.codec.ByteResponseDecoder;
import ch.ethz.globis.disindex.codec.field.SerializingEncoderDecoder;
import ch.ethz.globis.distindex.client.DistributedIndex;
import ch.ethz.globis.distindex.client.io.DefaultMessageService;
import ch.ethz.globis.distindex.client.mapping.NonDistributedMapping;
import ch.ethz.globis.disindex.codec.api.FieldEncoderDecoder;

public class DistributedPHTree<V> extends DistributedIndex<long[], V> {

    public DistributedPHTree(String host, int port, Class<V> clazz) {
        FieldEncoderDecoder<long[]> keyEncoder = new SerializingEncoderDecoder<>(long[].class);
        FieldEncoderDecoder<V> valueEncoder = new SerializingEncoderDecoder<>(clazz);
        encoder = new ByteRequestEncoder<>(keyEncoder, valueEncoder);
        decoder = new ByteResponseDecoder<>(valueEncoder);

        service = new DefaultMessageService(port);
        keyMapping = new NonDistributedMapping(host);
    }
}