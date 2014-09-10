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

package de.elakito.misc.xml.tokenize;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class XMLTokenIteratorGroupingTest extends Assert {
    private static final byte[] DATA = (
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "<c:C attr='2'/>"
        + "<c:C attr='3'>orange</c:C>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "<c:C attr='6'/>"
        + "<c:C attr='7'>pear</c:C>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>").getBytes();

    private static final String[] RESULTS_WRAPPED_SIZE1 = {
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='2'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='3'>orange</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='6'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='7'>pear</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>"};
    
    private static final String[] RESULTS_WRAPPED_SIZE2 = {
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "<c:C attr='2'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='3'>orange</c:C>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "<c:C attr='6'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='7'>pear</c:C>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>"};
    
    private static final String[] RESULTS_WRAPPED_SIZE3 = {
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "<c:C attr='2'/>"
        + "<c:C attr='3'>orange</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "<c:C attr='6'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='7'>pear</c:C>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>"};
    
    private static final String[] RESULTS_WRAPPED_SIZE4 = {
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "<c:C attr='2'/>"
        + "<c:C attr='3'>orange</c:C>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "<c:C attr='6'/>"
        + "<c:C attr='7'>pear</c:C>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>"};
    
    private static final String[] RESULTS_WRAPPED_SIZE5 = {
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='1' xmlns:c='urn:c'>"
        + "<c:C attr='1'>peach</c:C>"
        + "<c:C attr='2'/>"
        + "<c:C attr='3'>orange</c:C>"
        + "<c:C attr='4'/>"
        + "</c:B>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='5'>mango</c:C>"
        + "</c:B>"
        + "</g:A>",
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:A xmlns:g='urn:g'>"
        + "<c:B attr='2' xmlns:c='urn:c'>"
        + "<c:C attr='6'/>"
        + "<c:C attr='7'>pear</c:C>"
        + "<c:C attr='8'/>"
        + "</c:B>"
        + "</g:A>"};
    
    private static final String[] RESULTS_INJECTED_SIZE1 = {
        "<c:C attr='1' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">peach</c:C>",
        "<c:C attr='2' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>",
        "<c:C attr='3' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">orange</c:C>",
        "<c:C attr='4' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>",
        "<c:C attr='5' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">mango</c:C>",
        "<c:C attr='6' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>",
        "<c:C attr='7' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">pear</c:C>",
        "<c:C attr='8' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"};

    private static final String[] RESULTS_INJECTED_SIZE2 = {
        "<group>"
        + "<c:C attr='1' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">peach</c:C>"
        + "<c:C attr='2' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>",
        "<group>"
        + "<c:C attr='3' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">orange</c:C>"
        + "<c:C attr='4' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>",
        "<group>"
        + "<c:C attr='5' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">mango</c:C>"
        + "<c:C attr='6' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>",
        "<group>"
        + "<c:C attr='7' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">pear</c:C>"
        + "<c:C attr='8' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>"};

    private static final String[] RESULTS_INJECTED_SIZE3 = {
        "<group>"
        + "<c:C attr='1' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">peach</c:C>"
        + "<c:C attr='2' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='3' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">orange</c:C>"
        + "</group>",
        "<group>"
        + "<c:C attr='4' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='5' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">mango</c:C>"
        + "<c:C attr='6' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>",
        "<group>"
        + "<c:C attr='7' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">pear</c:C>"
        + "<c:C attr='8' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>"};

    private static final String[] RESULTS_INJECTED_SIZE4 = {
        "<group>"
        + "<c:C attr='1' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">peach</c:C>"
        + "<c:C attr='2' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='3' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">orange</c:C>"
        + "<c:C attr='4' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>",
        "<group>"
        + "<c:C attr='5' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">mango</c:C>"
        + "<c:C attr='6' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='7' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">pear</c:C>"
        + "<c:C attr='8' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>"};

    private static final String[] RESULTS_INJECTED_SIZE5 = {
        "<group>"
        + "<c:C attr='1' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">peach</c:C>"
        + "<c:C attr='2' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='3' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">orange</c:C>"
        + "<c:C attr='4' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='5' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">mango</c:C>"
        + "</group>",
        "<group>"
        + "<c:C attr='6' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "<c:C attr='7' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\">pear</c:C>"
        + "<c:C attr='8' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
        + "</group>"};

        private Map<String, String> nsmap;
        
    
    @Before
    public void setup() {
        nsmap = new HashMap<String, String>();
        nsmap.put("g", "urn:g");
        nsmap.put("c", "urn:c");
    }

    // wrapped mode
    @Test
    public void testExtractWrappedSize1() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'w', 1, new ByteArrayInputStream(DATA), "utf-8", RESULTS_WRAPPED_SIZE1);
    }

    @Test
    public void testExtractWrappedSize2() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'w', 2, new ByteArrayInputStream(DATA), "utf-8", RESULTS_WRAPPED_SIZE2);
    }

    @Test
    @org.junit.Ignore
    // not working for now as the context extraction across two ancestor paths is not working
    public void testExtractWrappedSize3() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'w', 3, new ByteArrayInputStream(DATA), "utf-8", RESULTS_WRAPPED_SIZE3);
    }

    @Test
    public void testExtractWrappedSize4() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'w', 4, new ByteArrayInputStream(DATA), "utf-8", RESULTS_WRAPPED_SIZE4);
    }

    @Test
    @org.junit.Ignore
    // not working for now as the context extraction across two ancestor paths is not working
    public void testExtractWrappedSize5() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'w', 5, new ByteArrayInputStream(DATA), "utf-8", RESULTS_WRAPPED_SIZE5);
    }

    // injected mode
    @Test
    public void testExtractInjectedSize1() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'i', 1, new ByteArrayInputStream(DATA), "utf-8", RESULTS_INJECTED_SIZE1);
    }

    @Test
    public void testExtractInjectedSize2() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'i', 2, new ByteArrayInputStream(DATA), "utf-8", RESULTS_INJECTED_SIZE2);
    }

    @Test
    public void testExtractInjectedSize3() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'i', 3, new ByteArrayInputStream(DATA), "utf-8", RESULTS_INJECTED_SIZE3);
    }

    @Test
    public void testExtractInjectedSize4() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'i', 4, new ByteArrayInputStream(DATA), "utf-8", RESULTS_INJECTED_SIZE4);
    }

    @Test
    public void testExtractInjectedSize5() throws Exception {
        invokeAndVerify("//c:C", 
            nsmap, 'i', 5, new ByteArrayInputStream(DATA), "utf-8", RESULTS_INJECTED_SIZE5);
    }

    @Test
    @org.junit.Ignore
    // not working for now as the context extraction for one left-over token is not working
    public void testExtractWrappedLeftOver() throws Exception {
    	final byte[] data = ("<?xml version='1.0' encoding='UTF-8'?><g:A xmlns:g='urn:g'><c:B attr='1' xmlns:c='urn:c'>"
                + "<c:C attr='1'>peach</c:C>"
    			+ "<c:C attr='2'/>"
    			+ "<c:C attr='3'>orange</c:C>"
    			+ "</c:B></g:A>").getBytes();
    	final String[] results = {"<?xml version='1.0' encoding='UTF-8'?><g:A xmlns:g='urn:g'><c:B attr='1' xmlns:c='urn:c'>"
    		    + "<c:C attr='1'>peach</c:C><c:C attr='2'/>"
    			+ "</c:B></g:A>",
    			"<?xml version='1.0' encoding='UTF-8'?><g:A xmlns:g='urn:g'><c:B attr='1' xmlns:c='urn:c'>"
    			+ "<c:C attr='3'>orange</c:C>"
    			+ "</c:B></g:A>",
    	};
        invokeAndVerify("//c:C", 
            nsmap, 'w', 2, new ByteArrayInputStream(data), "utf-8", results);
    }

    private static void invokeAndVerify(String path, Map<String, String> nsmap, char mode, int grouping,
        InputStream in, String charset, String[] expected) 
        throws Exception {

        XMLTokenIterator tokenizer = new XMLTokenIterator(path, nsmap, mode, grouping, in, charset);

        List<String> results = new ArrayList<String>();
        while (tokenizer.hasNext()) {
            String token = (String)tokenizer.next();            
            System.out.println("#### result: " + token);
            results.add(token);
        }
        ((Closeable)tokenizer).close();

        assertEquals("token count", expected.length, results.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals("mismatch [" + i + "]", expected[i], results.get(i));
        }
    }
}
