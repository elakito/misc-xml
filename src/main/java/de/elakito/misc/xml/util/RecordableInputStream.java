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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * 
 */
public class RecordableInputStream extends FilterInputStream {
    private TrimmableByteArrayOutputStream buf;
    private String charset;
    private boolean recording;

    public RecordableInputStream(InputStream in, String charset) {
        super(in);
        this.buf = new TrimmableByteArrayOutputStream();
        this.charset = charset;
        this.recording = true;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c > 0 && recording) {
            buf.write(c);
        }
        return c;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0 && recording) {
            buf.write(b, off, n);
        }
        return n;
    }

    public String getText(int pos) {
        String t = null;
        recording = false;
        try {
            if (charset == null) {
                t = new String(buf.getByteArray(), 0, pos);
            } else {
                t = new String(buf.getByteArray(), 0, pos, charset);
            }
        } catch (UnsupportedEncodingException e) {
            // ignore it as this should have be caught while scanning.
        } finally {
            buf.trim(pos, 0);
        }
        return t;
    }
    
    public byte[] getBytes(int pos) {
        recording = false;
        byte[] b = buf.toByteArray(pos);
        buf.trim(pos, 0);
        return b;
    }
    
    public void record() {
        recording = true;
    }

    int size() {
        return buf.size();
    }
}
