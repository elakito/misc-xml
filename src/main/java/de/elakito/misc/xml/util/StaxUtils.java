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

package de.elakito.misc.xml.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * This is a local stax utility class for providing some stax related utility methods.
 */
public final class StaxUtils {
    private static final XMLInputFactory infactory = XMLInputFactory.newFactory();
    private static final XMLOutputFactory outfactory = XMLOutputFactory.newFactory();
    
    private StaxUtils() {
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in) {
        try {
            return infactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding) {
        try {
            return infactory.createXMLStreamReader(in, encoding);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }

    }

    public static XMLStreamReader createXMLStreamReader(Reader in) {
        try {
            return infactory.createXMLStreamReader(in);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream reader.", e);
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out) {
        try {
            return outfactory.createXMLStreamWriter(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream writer.", e);
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding) {
        try {
            return outfactory.createXMLStreamWriter(out, encoding);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream writer.", e);
        }

    }

    public static XMLStreamWriter createXMLStreamWriter(Writer out) {
        try {
            return outfactory.createXMLStreamWriter(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't create a stream writer.", e);
        }
    }
}
