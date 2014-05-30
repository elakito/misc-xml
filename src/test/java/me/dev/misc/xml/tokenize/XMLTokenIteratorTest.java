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

package me.dev.misc.xml.tokenize;

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
public class XMLTokenIteratorTest extends Assert {
    private static final byte[] DATA = (
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
        + "<c:parent some_attr='1' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
        + "<c:child some_attr='a' anotherAttr='a'></c:child>"
        + "<c:child some_attr='b' anotherAttr='b'/>"
        + "</c:parent>"
        + "<c:parent some_attr='2' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
        + "<c:child some_attr='c' anotherAttr='c'></c:child>"
        + "<c:child some_attr='d' anotherAttr='d'/>"
        + "</c:parent>"
        + "</grandparent>"
        + "<grandparent><uncle>ben</uncle><aunt/>"
        + "<c:parent some_attr='3' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
        + "<c:child some_attr='e' anotherAttr='e'></c:child>"
        + "<c:child some_attr='f' anotherAttr='f'/>"
        + "</c:parent>"
        + "</grandparent>"
        + "</g:greatgrandparent>").getBytes();

    private static final byte[] DATA_NS_MIXED = (
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<g:greatgrandparent xmlns:g='urn:g'><grandparent>"
        + "<parent some_attr='1' xmlns:c='urn:c' xmlns=\"urn:c\">"
        + "<child some_attr='a' anotherAttr='a'></child>"
        + "<x:child xmlns:x='urn:c' some_attr='b' anotherAttr='b'/>"
        + "</parent>"
        + "<c:parent some_attr='2' xmlns:c='urn:c'>"
        + "<child some_attr='c' anotherAttr='c' xmlns='urn:c'></child>"
        + "<c:child some_attr='d' anotherAttr='d'/>"
        + "</c:parent>"
        + "</grandparent>"
        + "</g:greatgrandparent>").getBytes();

    private static final String[] RESULTS_CHILD_WRAPPED = {
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='1' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='a' anotherAttr='a'></c:child>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='1' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='b' anotherAttr='b'/>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='2' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='c' anotherAttr='c'></c:child>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='2' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='d' anotherAttr='d'/>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle>ben</uncle><aunt/>"
            + "<c:parent some_attr='3' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='e' anotherAttr='e'></c:child>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle>ben</uncle><aunt/>"
            + "<c:parent some_attr='3' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='f' anotherAttr='f'/>"
            + "</c:parent></grandparent></g:greatgrandparent>"
    };

    private static final String[] RESULTS_CHILD = {
        "<c:child some_attr='a' anotherAttr='a' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"></c:child>",
        "<c:child some_attr='b' anotherAttr='b' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"/>",
        "<c:child some_attr='c' anotherAttr='c' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"></c:child>",
        "<c:child some_attr='d' anotherAttr='d' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"/>",
        "<c:child some_attr='e' anotherAttr='e' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"></c:child>",
        "<c:child some_attr='f' anotherAttr='f' xmlns:g=\"urn:g\" xmlns:d=\"urn:d\" xmlns:c=\"urn:c\"/>"
    };

    private static final String[] RESULTS_CHILD_MIXED = {
        "<child some_attr='a' anotherAttr='a' xmlns=\"urn:c\" xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"></child>",
        "<x:child xmlns:x='urn:c' some_attr='b' anotherAttr='b' xmlns='urn:c' xmlns:g='urn:g' xmlns:c='urn:c'/>",
        "<child some_attr='c' anotherAttr='c' xmlns='urn:c' xmlns:g='urn:g' xmlns:c='urn:c'></child>",
        "<c:child some_attr='d' anotherAttr='d' xmlns:g=\"urn:g\" xmlns:c=\"urn:c\"/>"
    };

    private static final String[] RESULTS_CHILD_MIXED_WRAPPED = {
        "<?xml version='1.0' encoding='UTF-8'?><g:greatgrandparent xmlns:g='urn:g'><grandparent>"
        + "<parent some_attr='1' xmlns:c='urn:c' xmlns=\"urn:c\">"
        + "<child some_attr='a' anotherAttr='a'></child></parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?><g:greatgrandparent xmlns:g='urn:g'><grandparent>"
        + "<parent some_attr='1' xmlns:c='urn:c' xmlns=\"urn:c\">"
        + "<x:child xmlns:x='urn:c' some_attr='b' anotherAttr='b'/></parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?><g:greatgrandparent xmlns:g='urn:g'><grandparent>"
        + "<c:parent some_attr='2' xmlns:c='urn:c'>"
        + "<child some_attr='c' anotherAttr='c' xmlns='urn:c'></child></c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?><g:greatgrandparent xmlns:g='urn:g'><grandparent>"
        + "<c:parent some_attr='2' xmlns:c='urn:c'>"
        + "<c:child some_attr='d' anotherAttr='d'/></c:parent></grandparent></g:greatgrandparent>"
    };

    private static final String[] RESULTS_PARENT_WRAPPED = {
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='1' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='a' anotherAttr='a'></c:child>"
            + "<c:child some_attr='b' anotherAttr='b'/>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "<c:parent some_attr='2' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='c' anotherAttr='c'></c:child>"
            + "<c:child some_attr='d' anotherAttr='d'/>"
            + "</c:parent></grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle>ben</uncle><aunt/>"
            + "<c:parent some_attr='3' xmlns:c='urn:c' xmlns:d=\"urn:d\">"
            + "<c:child some_attr='e' anotherAttr='e'></c:child>"
            + "<c:child some_attr='f' anotherAttr='f'/>"
            + "</c:parent></grandparent></g:greatgrandparent>",
    };

    private static final String[] RESULTS_PARENT = {
        "<c:parent some_attr='1' xmlns:c='urn:c' xmlns:d=\"urn:d\" xmlns:g='urn:g'>"
            + "<c:child some_attr='a' anotherAttr='a'></c:child>"
            + "<c:child some_attr='b' anotherAttr='b'/>"
            + "</c:parent>",
        "<c:parent some_attr='2' xmlns:c='urn:c' xmlns:d=\"urn:d\" xmlns:g='urn:g'>"
            + "<c:child some_attr='c' anotherAttr='c'></c:child>"
            + "<c:child some_attr='d' anotherAttr='d'/>"
            + "</c:parent>",
        "<c:parent some_attr='3' xmlns:c='urn:c' xmlns:d=\"urn:d\" xmlns:g='urn:g'>"
            + "<c:child some_attr='e' anotherAttr='e'></c:child>"
            + "<c:child some_attr='f' anotherAttr='f'/>"
            + "</c:parent>",
    };

    private static final String[] RESULTS_AUNT_WRAPPED = {
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle/><aunt>emma</aunt>"
            + "</grandparent></g:greatgrandparent>",
        "<?xml version='1.0' encoding='UTF-8'?>"
            + "<g:greatgrandparent xmlns:g='urn:g'><grandparent><uncle>ben</uncle><aunt/>"
            + "</grandparent></g:greatgrandparent>"
    };    

    private static final String[] RESULTS_AUNT = {
        "<aunt xmlns:g=\"urn:g\">emma</aunt>",
        "<aunt xmlns:g=\"urn:g\"/>"
    };    

    private static final String[] RESULTS_NULL = {
    };

    private Map<String, String> nsmap;
    
    @Before
    public void setup() {
        nsmap = new HashMap<String, String>();
        nsmap.put("G", "urn:g");
        nsmap.put("C", "urn:c");
    }

    @Test
    public void testExtractChild() throws Exception {
        invokeAndVerify("//C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);
    }

    @Test
    public void testExtractChildInjected() throws Exception {
        invokeAndVerify("//C:child", 
               nsmap, false, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD);
    }

    @Test
    public void testExtractChildNSMixed() throws Exception {
        invokeAndVerify("//*:child", 
               nsmap, true, new ByteArrayInputStream(DATA_NS_MIXED), "utf-8", RESULTS_CHILD_MIXED_WRAPPED);
    }

    @Test
    public void testExtractChildNSMixedInjected() throws Exception {
        invokeAndVerify("//*:child", 
               nsmap, false, new ByteArrayInputStream(DATA_NS_MIXED), "utf-8", RESULTS_CHILD_MIXED);
    }

    @Test
    public void testExtractAnyChild() throws Exception {
        invokeAndVerify("//*:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);
    }

    @Test
    public void testExtractCxxxd() throws Exception {
        invokeAndVerify("//C:c*d", 
               nsmap, false, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD);
    }

    @Test
    public void testExtractUnqualifiedChild() throws Exception {
        invokeAndVerify("//child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_NULL);
    }

    @Test
    public void testExtractChildWithAncestorGGPdGP() throws Exception {
        invokeAndVerify("/G:greatgrandparent/grandparent//C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);
    }

    @Test
    public void testExtractChildWithAncestorGGPdP() throws Exception {
        invokeAndVerify("/G:greatgrandparent//C:parent/C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);

    }

    @Test
    public void testExtractChildWithAncestorGPddP() throws Exception {
        invokeAndVerify("//grandparent//C:parent/C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);

    }

    @Test
    public void testExtractChildWithAncestorGPdP() throws Exception {
        invokeAndVerify("//grandparent/C:parent/C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);

    }
    @Test
    public void testExtractChildWithAncestorP() throws Exception {
        invokeAndVerify("//C:parent/C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);

    }

    @Test
    public void testExtractChildWithAncestorGGPdGPdP() throws Exception {
        invokeAndVerify("/G:greatgrandparent/grandparent/C:parent/C:child", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_CHILD_WRAPPED);
    }
    
    @Test
    public void textExtractParent() throws Exception {
        invokeAndVerify("//C:parent", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_PARENT_WRAPPED);
    }
    
    @Test
    public void textExtractParentInjected() throws Exception {
        invokeAndVerify("//C:parent", 
               nsmap, false, new ByteArrayInputStream(DATA), "utf-8", RESULTS_PARENT);
    }
    
    @Test
    public void textExtractAuntWC1() throws Exception {
        invokeAndVerify("//a*t", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_AUNT_WRAPPED);
    }

    @Test
    public void textExtractAuntWC2() throws Exception {
        invokeAndVerify("//au?t", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_AUNT_WRAPPED);
    }

    @Test
    public void textExtractAunt() throws Exception {
        invokeAndVerify("//aunt", 
               nsmap, true, new ByteArrayInputStream(DATA), "utf-8", RESULTS_AUNT_WRAPPED);
    }

    @Test
    public void textExtractAuntInjected() throws Exception {
        invokeAndVerify("//aunt", 
               nsmap, false, new ByteArrayInputStream(DATA), "utf-8", RESULTS_AUNT);
    }

    private static void invokeAndVerify(String path, Map<String, String> nsmap, boolean wrap,
                                        InputStream in, String charset, String[] expected) 
        throws Exception {

        XMLTokenIterator tokenizer = new XMLTokenIterator(path, nsmap, wrap, in, charset);

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

