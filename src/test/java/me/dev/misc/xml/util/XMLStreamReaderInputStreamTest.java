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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class XMLStreamReaderInputStreamTest extends Assert {
	@Test
	public void testSampleShortUTF8() throws Exception {
		XMLStreamReader reader = 
				StaxUtils.createXMLStreamReader(XMLStreamReaderInputStreamTest.class.getResourceAsStream("soap_req.xml"), "utf-8");
		XMLStreamReaderInputStream xsris = new XMLStreamReaderInputStream(reader, "utf-8");
		verifyResult(XMLStreamReaderInputStreamTest.class.getResourceAsStream("soap_req_target.xml"), xsris);
	}

	@Test
	public void testSampleShortLatin() throws Exception {
		XMLStreamReader reader = 
				StaxUtils.createXMLStreamReader(XMLStreamReaderInputStreamTest.class.getResourceAsStream("soap_req_latin.xml"), "iso-8859-1");
		XMLStreamReaderInputStream xsris = new XMLStreamReaderInputStream(reader, "iso-8859-1");
		verifyResult(XMLStreamReaderInputStreamTest.class.getResourceAsStream("soap_req_latin_target.xml"), xsris);
	}

	@Test
	public void testSampleLong() throws Exception {
		XMLStreamReader reader = 
				StaxUtils.createXMLStreamReader(XMLStreamReaderInputStreamTest.class.getResourceAsStream("hello_world.wsdl"), "utf-8");
		XMLStreamReaderInputStream xsris = new XMLStreamReaderInputStream(reader, "utf-8");
		verifyResult(XMLStreamReaderInputStreamTest.class.getResourceAsStream("hello_world_target.wsdl"), xsris);
	}

	private void verifyResult(InputStream yours, InputStream mine) {
		byte[] tmp1 = new byte[512];
		byte[] tmp2 = new byte[512];
		for (;;) {
			int n1 = 0;
			int n2 = 0;
			try {
				n1 = yours.read(tmp1, 0, tmp1.length);
				n2 = mine.read(tmp2, 0, tmp2.length);
			} catch (IOException e) {
				fail("unable to read data");
			}
			assertEquals(n1, n2);
			if (n2 < 0) {
				break;
			}
			assertTrue(Arrays.equals(tmp1,  tmp2));
		}
	}
}
