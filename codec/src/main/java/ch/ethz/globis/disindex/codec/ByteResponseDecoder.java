/*
This file is part of PH-Tree:
A multi-dimensional indexing and storage structure.

Copyright (C) 2011-2015
Eidgenössische Technische Hochschule Zürich (ETH Zurich)
Institute for Information Systems
GlobIS Group
Bogdan Vancea, Tilmann Zaeschke
zaeschke@inf.ethz.ch or zoodb@gmx.de

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.globis.disindex.codec;

import java.nio.ByteBuffer;

import ch.ethz.globis.disindex.codec.api.FieldDecoder;
import ch.ethz.globis.disindex.codec.api.ResponseDecoder;
import ch.ethz.globis.distindex.api.IndexEntry;
import ch.ethz.globis.distindex.api.IndexEntryList;
import ch.ethz.globis.distindex.operation.response.BaseResponse;
import ch.ethz.globis.distindex.operation.response.IntegerResponse;
import ch.ethz.globis.distindex.operation.response.MapResponse;
import ch.ethz.globis.distindex.operation.response.Response;
import ch.ethz.globis.distindex.operation.response.ResponseCode;
import ch.ethz.globis.distindex.operation.response.ResultResponse;
import ch.ethz.globis.distindex.util.SerializerUtil;

/**
 * Decodes response messages sent by the server to the client.
 *
 * @param <K>                   The type of the key.
 * @param <V>                   The type of the value.
 */
public class ByteResponseDecoder<K, V> implements ResponseDecoder<K, V> {

    FieldDecoder<K> keyDecoder;
    FieldDecoder<V> valueDecoder;

    public ByteResponseDecoder(FieldDecoder<K> keyDecoder, FieldDecoder<V> valueDecoder) {
        this.keyDecoder = keyDecoder;
        this.valueDecoder = valueDecoder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Response> R decode(byte[] payload, Class<R> clazz) {
        Byte code = ResponseCode.getCode(clazz);
        if (code == null) {
            throw new IllegalArgumentException("Illegal response type specified");
        }
        R response = null;
        switch (code) {
            case ResponseCode.BASE:
                response = (R) decodeBase(payload);
                break;
            case ResponseCode.INTEGER:
                response = (R) decodeInteger(payload);
                break;
            case ResponseCode.MAP:
                response = (R) decodeMap(payload);
                break;
            case ResponseCode.RESULT:
                response = (R) decodeResult(payload);
                break;
        }
        return response;
    }

    @Override
    public Response decodeBase(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        byte status = buffer.get();
        return new BaseResponse(opCode, requestId, status);
    }

    @Override
    public IntegerResponse decodeInteger(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        byte status = buffer.get();
        int value = buffer.getInt();
        return new IntegerResponse(opCode, requestId, status, value);
    }

    @Override
    public MapResponse decodeMap(byte[] payload) {
       return SerializerUtil.getInstance().deserialize(payload);
    }

    @Override
    public ResultResponse<K, V> decodeResult(byte[] payload) {
        return decodeResult(ByteBuffer.wrap(payload));
    }

    @Override
    public V decodeValue(byte[] payload) {
        return valueDecoder.decode(payload);
    }

    public ResultResponse<K, V> decodeResult(ByteBuffer buffer) {
        byte opCode = buffer.get();
        int requestId = buffer.getInt();
        byte status = buffer.get();
        int nrEntries = buffer.getInt();

        int keysBytesSize, valueBytesSize;
        byte[] keyBytes, valueBytes;
        K key;
        V value;
        IndexEntryList<K, V> entries = new IndexEntryList<>();
        for (int i = 0; i < nrEntries; i++) {
            keysBytesSize = buffer.getInt();
            keyBytes = new byte[keysBytesSize];
            buffer.get(keyBytes);
            key = keyDecoder.decode(keyBytes);

            valueBytesSize = buffer.getInt();
            valueBytes = new byte[valueBytesSize];
            buffer.get(valueBytes);
            value = valueDecoder.decode(valueBytes);

            entries.add(new IndexEntry<>(key, value));
        }

        String iteratorId = readString(buffer);
        return new ResultResponse<>(opCode, requestId, status, entries, iteratorId);
    }

    private String readString(ByteBuffer buffer) {
        int strBytesNr = buffer.getInt();
        byte[] strBytes = new byte[strBytesNr];
        buffer.get(strBytes);
        return new String(strBytes);
    }
}