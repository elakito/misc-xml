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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.elakito.misc.xml.util.AttributedQName;
import de.elakito.misc.xml.util.RecordableReader;
import de.elakito.misc.xml.util.StaxUtils;

/**
 * An iterator to extract a specific XML content/token. The token to be extracted
 * is specified using a path notation that looks like a unix path but uses QNames
 * as in xpath. There are four extraction modes: inject, wrap, unwrap, and text.
 * the inject mode 'i' injects the namespace bindings to the extracted node.
 * The wrap mode 'w' wraps the extracted node with its ancestor elements.
 * The unwrap 'u' mode unwraps the start ane end tags from the extracted node.
 * The text mode 't' concatenates only the text nodes of the extract node.
 */
public class XMLTokenIterator implements Iterator<Object>, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(XMLTokenIterator.class);

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("xmlns(:\\w+|)\\s*=\\s*('[^']*'|\"[^\"]*\")");

    private AttributedQName[] splitpath;
    private int index;
    private char mode;
    private int group;
    private RecordableReader in;
    private XMLStreamReader reader;
    private List<QName> path;
    private List<Map<String, String>> namespaces;
    private List<String> segments;
    private List<QName> segmentlog;
    private List<String> tokens;
    private int code;
    private int consumed;
    private boolean backtrack;
    private int trackdepth = -1;
    private int depth;

    private Object nextToken;

    /**
     * Constructs an XML token iterator.
     * 
     * @param path the unix like path notation using the QNames
     * @param nsmap the namespace binding map
     * @param mode the extraction mode. One of 'i', 'w', and 'u', representing inject, wrap, and unwrap 
     * @param in the input stream
     * @param charset the character encoding
     * @throws XMLStreamException
     * @throws UnsupportedEncodingException 
     */
    public XMLTokenIterator(String path, Map<String, String> nsmap, char mode, InputStream in, String charset) 
            throws XMLStreamException, UnsupportedEncodingException {
        this(path, nsmap, mode, 1, in, charset); 
    }
    
    /**
     * Constructs an XML token iterator.
     * 
     * @param path the unix like path notation using the QNames
     * @param nsmap the namespace binding map
     * @param mode the extraction mode. One of 'i', 'w', and 'u', representing inject, wrap, and unwrap
     * @param group the number of tokens to be grouped together  
     * @param in the input stream
     * @param charset the character encoding
     * @throws XMLStreamException
     * @throws UnsupportedEncodingException 
     */
    public XMLTokenIterator(String path, Map<String, String> nsmap, char mode, int group, InputStream in, String charset) 
            throws XMLStreamException, UnsupportedEncodingException {
        // woodstox's getLocation().etCharOffset() does not return the offset correctly for InputStream, so use Reader instead.
        this(path, nsmap, mode, group, new InputStreamReader(in, charset));
    }
    
    /**
     * Constructs an XML token iterator.
     * 
     * @param path the unix like path notation using the QNames
     * @param nsmap the namespace binding map
     * @param mode the extraction mode. One of 'i', 'w', and 'u', representing inject, wrap, and unwrap 
     * @param in the input reader
     * @throws XMLStreamException
     */
    public XMLTokenIterator(String path, Map<String, String> nsmap, char mode, Reader in) throws XMLStreamException {
        this(path, nsmap, mode, 1, in);
    }
    
    /**
     * Constructs an XML token iterator.
     * 
     * @param path the unix like path notation using the QNames
     * @param nsmap the namespace binding map
     * @param mode the extraction mode. One of 'i', 'w', and 'u', representing inject, wrap, and unwrap
     * @param group the number of tokens to be grouped together
     * @param in the input reader
     * @throws XMLStreamException
     */
    public XMLTokenIterator(String path, Map<String, String> nsmap, char mode, int group, Reader in) throws XMLStreamException {
        final String[] sl = path.substring(1).split("/");
        this.splitpath = new AttributedQName[sl.length];
        for (int i = 0; i < sl.length; i++) {
            String s = sl[i];
            if (s.length() > 0) {
                int d = s.indexOf(':');
                String pfx = d > 0 ? s.substring(0, d) : "";
                this.splitpath[i] = 
                    new AttributedQName(
                        "*".equals(pfx) ? "*" : nsmap == null ? "" : nsmap.get(pfx), d > 0 ? s.substring(d + 1) : s, pfx);
            }
        }
        this.mode = mode != 0 ? mode : 'i';
        this.group = group > 0 ? group : 1;
        this.in = new RecordableReader(in);
        // use a local staxutils to create a stream reader. This can be replaced if other means is available
        this.reader = StaxUtils.createXMLStreamReader(this.in);

        LOG.trace("reader.class = {}", reader.getClass());

        int coff = reader.getLocation().getCharacterOffset();
        if (coff != 0) {
            LOG.error("XMLStreamReader {} not supporting Location");
            throw new XMLStreamException("reader not supporting Location");
        }

        this.path = new ArrayList<QName>();
        // wrapped mode needs the segments and the injected mode needs the namespaces
        if (this.mode == 'w') {
            this.segments = new ArrayList<String>();
            this.segmentlog = new ArrayList<QName>();
        } else if (this.mode == 'i') {
            this.namespaces = new ArrayList<Map<String, String>>();
        }

        // when grouping the tokens, allocate the storage to temporarily store tokens. 
        if (this.group > 1) {
                this.tokens = new ArrayList<String>();
        }
        // pre-fetch the initial token to make the iterator gets started.
        this.nextToken = getNextToken();
    }
    
    private boolean isDoS() {
        return splitpath[index] == null;
    }
    
    private AttributedQName current() {
        return splitpath[index + (isDoS() ? 1 : 0)];
    }
    
    private AttributedQName ancestor() {
        return index == 0 ? null : splitpath[index - 1];
    }

    private void down() {
        if (isDoS()) {
            index++;
        }
        index++;
    }
    
    private void up() {
        index--;
    }
    
    private boolean isBottom() {
        return index == splitpath.length - (isDoS() ? 2 : 1);
    }
    
    private boolean isTop() {
        return index == 0;
    }
    
    private int readNext() throws XMLStreamException {
        int c = code;
        if (c > 0) {
            code = 0;
        } else {
            c = reader.next();
        }
        return c;
    }
    
    private String getCurrenText() {
        int pos = reader.getLocation().getCharacterOffset();
        String txt = in.getText(pos - consumed);
        consumed = pos;
        // keep recording
        in.record();
        return txt;
    }

    private void pushName(QName name) {
        path.add(name);
    }

    private QName popName() {
        return path.remove(path.size() - 1);
    }

    private void pushSegment(QName qname, String token) {
        segments.add(token);
        segmentlog.add(qname);
    }

    private String popSegment() {
        return segments.remove(segments.size() - 1);
    }
    
    private QName peekLog() {
        return segmentlog.get(segmentlog.size() - 1);
    }
    
    private QName popLog() {
        return segmentlog.remove(segmentlog.size() - 1);
    }

    private void pushNamespaces(XMLStreamReader reader) {
        Map<String, String> m = new HashMap<String, String>();
        if (namespaces.size() > 0) {
            m.putAll(namespaces.get(namespaces.size() - 1));
        }
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            m.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
        namespaces.add(m);
    }

    private void popNamespaces() {
        namespaces.remove(namespaces.size() - 1);
    }

    private Map<String, String> getCurrentNamespaceBindings() {
        return namespaces.get(namespaces.size() - 1);
    }

    private void readCurrent(boolean incl) throws XMLStreamException {
        int d = depth;
        while (d <= depth) {
            int code = reader.next();
            if (code == XMLStreamReader.START_ELEMENT) {
                depth++;
            } else if (code == XMLStreamReader.END_ELEMENT) {
                depth--;
            }
        }
        // either look ahead to the next token or stay at the end element token
        if (incl) {
            code = reader.next();
        } else {
            code = reader.getEventType();
            if (code == XMLStreamReader.END_ELEMENT) {
                // revert the depth count to avoid double counting the up event
                depth++;
            }
        }
    }

    private String getCurrentToken() throws XMLStreamException {
        readCurrent(true);
        popName();
        
        String token = createContextualToken(getCurrenText());
        if (mode == 'i') {
            popNamespaces();
        }
        return token;
    }

    private String createContextualToken(String token){
        StringBuilder sb = new StringBuilder();
        if (mode == 'w' && group == 1) {
            for (int i = 0; i < segments.size(); i++) {
                sb.append(segments.get(i));
            }
            sb.append(token);
            for (int i = path.size() - 1; i >= 0; i--) {
                QName q = path.get(i);
                sb.append("</").append(makeName(q)).append(">");
            }

        } else if (mode == 'i') {
            final String stag = token.substring(0, token.indexOf('>') + 1);
            Set<String> skip = new HashSet<String>();
            Matcher matcher = NAMESPACE_PATTERN.matcher(stag);
            char quote = 0;
            while (matcher.find()) {
                String prefix = matcher.group(1);
                if (prefix.length() > 0) {
                    prefix = prefix.substring(1);
                }
                skip.add(prefix);
                if (quote == 0) {
                    quote = matcher.group(2).charAt(0);
                }
            }
            if (quote == 0) {
                quote = '"';
            }
            boolean empty = stag.endsWith("/>"); 
            sb.append(token.substring(0, stag.length() - (empty ? 2 : 1)));
            for (Entry<String, String> e : getCurrentNamespaceBindings().entrySet()) {
                if (!skip.contains(e.getKey())) {
                    sb.append(e.getKey().length() == 0 ? " xmlns" : " xmlns:")
                    .append(e.getKey()).append("=").append(quote).append(e.getValue()).append(quote);
                }
            }
            sb.append(token.substring(stag.length() - (empty ? 2 : 1)));
        } else if (mode == 'u') {
            int bp = token.indexOf(">");
            int ep = token.lastIndexOf("</");
            if (bp > 0 && ep > 0) {
                sb.append(token.substring(bp + 1, ep));
            }
        } else if (mode == 't') {
            int bp = 0;
            for (;;) {
                int ep = token.indexOf('>', bp);
                bp = token.indexOf('<', ep);
                if (bp < 0) {
                    break;
                }
                sb.append(token.substring(ep + 1, bp));
            }
        } else {
            return token;
        }
        return sb.toString();
    }

    private String getGroupedToken() {
        StringBuilder sb = new StringBuilder();
        if (mode == 'w') {
            // for wrapped
            for (int i = 0; i < segments.size(); i++) {
                sb.append(segments.get(i));
            }
            for (String s : tokens) {
                sb.append(s);
            }
            for (int i = path.size() - 1; i >= 0; i--) {
                QName q = path.get(i);
                sb.append("</").append(makeName(q)).append(">");
            }
        } else {
            // for injected, unwrapped, text
            sb.append("<group>");
            for (String s : tokens) {
                sb.append(s);
            }
            sb.append("</group>");
        }
        tokens.clear();
        return sb.toString();
    }

    private String getNextToken() throws XMLStreamException {
        int xcode = 0;
        while (xcode != XMLStreamConstants.END_DOCUMENT) {
            xcode = readNext();

            switch (xcode) {
            case XMLStreamConstants.START_ELEMENT:
                depth++;
                QName name = reader.getName();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("se={}; depth={}; trackdepth={}", new Object[]{name, depth, trackdepth});
                }

                String token = getCurrenText();
                LOG.trace("token={}", token);
                if (!backtrack && mode == 'w') {
                    pushSegment(name, token);
                }
                pushName(name);
                if (mode == 'i') {
                    pushNamespaces(reader);
                }
                backtrack = false;
                if (current().matches(name)) {
                    // mark the position of the match in the segments list
                    if (isBottom()) {
                        // final match
                        token = getCurrentToken();
                        backtrack = true;
                        trackdepth = depth;
                        if (group > 1) {
                            tokens.add(token);
                            if (group == tokens.size()) {
                                return getGroupedToken();
                            }
                        } else {
                            return token;
                        }
                    } else {
                        // intermediary match
                        down();
                    }
                } else if (isDoS()){
                    // continue
                } else {
                    // skip
                    readCurrent(false);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
            	if ((backtrack || (trackdepth > 0 && depth == trackdepth))
            	    && (mode == 'w' && group > 1 && tokens.size() > 0)) {
            		// flush the left over using the current context
            		code = XMLStreamConstants.END_ELEMENT;
            		return getGroupedToken();
            	}

                depth--;
                QName endname = reader.getName();
                LOG.trace("ee={}", endname);
                popName();
                if (mode == 'i') {
                    popNamespaces();
                }
                
                int pc = 0;
                if (backtrack || (trackdepth > 0 && depth == trackdepth - 1)) {
                    // reactivate backtracking if not backtracking and update the track depth
                    backtrack = true;
                    trackdepth--;
                    if (mode == 'w') {
                        while (!endname.equals(peekLog())) {
                            pc++;
                            popLog();
                        }
                    }
                }

                if (backtrack) {
                    if (mode == 'w') {
                        for (int i = 0; i < pc; i++) {
                            popSegment();
                        }
                    }

                    if ((ancestor() == null && !isTop())
                        || (ancestor() != null && ancestor().matches(endname))) {
                        up();
                    }
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                LOG.trace("depth={}", depth);
                if (group > 1 && tokens.size() > 0) {
                    // flush the left over before really going EoD
                    code = XMLStreamConstants.END_DOCUMENT;
                    return getGroupedToken();
                }
                break;
            }
        }
        return null;
    }

    private static String makeName(QName qname) {
        String pfx = qname.getPrefix();
        return pfx.length() == 0 ? qname.getLocalPart() : qname.getPrefix() + ":" + qname.getLocalPart();
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextToken != null;
    }

    @Override
    public Object next() {
        Object o = nextToken;
        try {
            nextToken = getNextToken();
        } catch (XMLStreamException e) {
            nextToken = null;
            throw new RuntimeException(e);
        }
        return o;
    }

    @Override
    public void remove() {
        // nop
    }

}
