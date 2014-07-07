/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package me.dev.misc.xml.util;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This is a local stax utility class for providing some stax related utility methods.
 */
public final class StaxUtils {
    private static final XMLInputFactory factory = XMLInputFactory.newFactory();
    
    private StaxUtils() {
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in) {
        try {
            return factory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding) {
        try {
            return factory.createXMLStreamReader(in, encoding);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }

    }
    public static XMLStreamReader createXMLStreamReader(Reader in) {
        try {
            return factory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }
    }
}
