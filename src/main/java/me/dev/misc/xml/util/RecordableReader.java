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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 
 */
public class RecordableReader extends FilterReader {
    private TrimmableCharArrayWriter buf;
    private boolean recording;

    public RecordableReader(Reader in) {
        super(in);
        this.buf = new TrimmableCharArrayWriter();
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
    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = super.read(cbuf, off, len);
        if (n > 0 && recording) {
            buf.write(cbuf, off, n);
        }
        return n;
    }

    public String getText(int pos) {
        recording = false;
        String t = new String(buf.getCharArray(), 0, pos);
        buf.trim(pos, 0);
        return t;
    }
    
    public char[] getChars(int pos) {
        recording = false;
        char[] c = buf.toCharArray(pos);
        buf.trim(pos, 0);
        return c;
    }
    
    public void record() {
        recording = true;
    }

    int size() {
        return buf.size();
    }
}
