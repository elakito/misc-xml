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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class XMLEventReaderInputStream extends InputStream {
	private XMLEventReader reader;
	private XMLEventWriter writer;
	private TrimmableByteArrayOutputStream chunk;
    private byte[] buffer;
    private String charset;
    private int bpos;

    private static final int BUFFER_SIZE = 4096;

	public XMLEventReaderInputStream(XMLEventReader reader, String charset) {
		this.reader = reader;
        this.buffer = new byte[BUFFER_SIZE];
        this.chunk = new TrimmableByteArrayOutputStream();
        this.charset = charset;
        this.writer = StaxUtils.createXMLEventWriter(chunk, charset);
	}

	public XMLEventReaderInputStream(XMLEventReader reader) {
		this(reader, "utf-8");
	}

	@Override
	public int read() throws IOException {
        byte[] ba = new byte[1];
        return read(ba, 0, 1) == 1 ? ba[0] : -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
        int tlen = 0;
        while (len > 0) {
            int n = ensureBuffering(len);
            if (n < 0) {
                break;
            }
            int clen = len > n ? n : len;
            System.arraycopy(buffer, 0, b, off, clen);
            System.arraycopy(buffer, clen, buffer, 0, buffer.length - clen);
            bpos -= clen;
            len -= clen;
            off += clen;
            tlen += clen;
        }

        return tlen > 0 ? tlen : -1;
	}

	private int ensureBuffering(int size) throws IOException {
        if (size < bpos) {
            return bpos;
        }
        // refill the buffer as more buffering is requested than the current buffer status
        try {

            if (chunk.size() < buffer.length) {
            	while (reader.hasNext()) {
            		writer.add(reader.nextEvent());

            		// check if the chunk is full
            		final int csize = buffer.length - bpos;
            		if (chunk.size() > csize) {
            			System.arraycopy(chunk.getByteArray(), 0, buffer, bpos, csize);
            			bpos = buffer.length;
            			chunk.trim(csize, 0);
            			return buffer.length;
            		}
            	}
            }
            final int csize = chunk.size() < buffer.length - bpos ? chunk.size() : buffer.length - bpos; 
			if (csize > 0) {
				System.arraycopy(chunk.getByteArray(), 0, buffer, bpos, csize);
				bpos += csize;
				chunk.trim(csize, 0);
				return bpos;
			} else {
				return bpos > 0 ? bpos : -1;
			}
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
    }
}
