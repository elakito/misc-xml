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

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class RecordableReaderTest extends Assert {
    private static final String DATA;
    
    static {
        StringBuilder sb = new StringBuilder();
        // [0x20 - 0x7e]
        int radix = 0x7f - 0x20;
        for (int i = 0; i < 512; i++) {
            sb.append((char) (i % radix + 0x20));
        }
        
        // [0xa1 - 0xff]
        radix = 0x100 - 0xa1;
        for (int i = 0; i < radix; i++) {
            sb.append((char) (i + 0xa1));
        }
        DATA = sb.toString();
    }

    @Test
    public void testReadAndGetTextsBufferPurge() throws Exception {
        RecordableReader rin = new RecordableReader(new StringReader(DATA));
        assertEquals(0, rin.size());
        char[] buf = new char[64];
        
        // 8 * 64 = 512
        for (int i = 0; i < 8; i++) {
            // read in 64 bytes
            int n = rin.read(buf, 0, buf.length);
            assertEquals(64, n);
            assertEquals(64, rin.size());

            int offset = i * 64;
            // consume the first 32 bytes
            String text = rin.getText(32);
            assertEquals(DATA.substring(offset, offset + 32), text);
            assertEquals(32, rin.size());

            // consume the other 32 bytes
            text = rin.getText(32);
            assertEquals(DATA.substring(offset + 32, offset + 64), text);
            assertEquals(0, rin.size());

            rin.record();
        }
        
        rin.close();
    }
    @Test
    public void testReadAndGetTextsAutoStopRecord() throws Exception {
        RecordableReader rin = new RecordableReader(new StringReader(DATA));
        assertEquals(0, rin.size());
        char[] buf = new char[64];
        
        // read 64 bytes
        int n = rin.read(buf, 0, buf.length);
        assertEquals(64, n);
        assertEquals(64, rin.size());

        // consume the 64 bytes
        String text = rin.getText(64);
        
        assertEquals(DATA.substring(0, 64), text);
        assertEquals(0, rin.size());

        // read the next 64 bytes
        n = rin.read(buf, 0, buf.length);
        assertEquals(64, n);
        assertEquals(0, rin.size());
        
        // turn back on the recording and read the next 64 bytes
        rin.record();
        n = rin.read(buf, 0, buf.length);
        assertEquals(64, n);
        assertEquals(64, rin.size());
        
        // consume the 64 bytes
        text = rin.getText(64);
        
        // 64 * 2 = 128
        assertEquals(DATA.substring(128, 192), text);
        assertEquals(0, rin.size());

        rin.close();
    }
}
