/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.server.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.test.utils.QpidTestCase;

public class MapJsonSerializerTest extends QpidTestCase
{
    private MapJsonSerializer _serializer;

    protected void setUp() throws Exception
    {
        super.setUp();
        _serializer = new MapJsonSerializer();

    }

    public void testSerializeDeserialize()
    {
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("string", "Test String");
        testMap.put("integer", new Integer(10));
        testMap.put("long", new Long(Long.MAX_VALUE));
        testMap.put("boolean", Boolean.TRUE);

        String jsonString = _serializer.serialize(testMap);
        Map<String, Object> deserializedMap = _serializer.deserialize(jsonString);

        assertEquals(deserializedMap, testMap);
    }

}
