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
package ch.ethz.globis.distindex.operation.request;

import java.util.HashMap;
import java.util.Map;

public class MapRequest extends BaseRequest {

    Map<String, String> contents;

    public MapRequest(int id, byte opCode, String indexId, int mappingVersion) {
        super(id, opCode, indexId, mappingVersion);
        this.contents = new HashMap<>();
    }

    public MapRequest(int id, byte opCode, String indexId, int mappingVersion, Map<String, String> contents) {
        super(id, opCode, indexId, mappingVersion);
        this.contents = contents;
    }

    public void addParamater(String key, Object value) {
        contents.put(key, String.valueOf(value));
    }

    public String getParameter(String key) {
        return contents.get(key);
    }

    public Map<String, String> getContents() {
        return contents;
    }
}