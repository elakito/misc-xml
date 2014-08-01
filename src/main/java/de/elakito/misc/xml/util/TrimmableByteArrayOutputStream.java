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

import java.io.ByteArrayOutputStream;

class TrimmableByteArrayOutputStream extends ByteArrayOutputStream {
    public void trim(int head, int tail) {
        System.arraycopy(buf, head, buf, 0, count - head - tail);
        count -= head + tail;
    }
    
    public byte[] toByteArray(int len) {
        byte[] b = new byte[len];
        System.arraycopy(buf, 0, b, 0, len);
        return b;
    }

    byte[] getByteArray() {
        return buf;
    }
}
