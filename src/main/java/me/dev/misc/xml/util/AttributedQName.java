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

import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * An extended QName to be used to pattern matching on the local part.
 */
public class AttributedQName extends QName {
    private Pattern lcpattern;
    
    public AttributedQName(String localPart) {
        super(localPart);
        checkWildcard(localPart);
    }

    public AttributedQName(String namespaceURI, String localPart, String prefix) {
        super(namespaceURI, localPart, prefix);
        checkWildcard(localPart);
    }

    public AttributedQName(String namespaceURI, String localPart) {
        super(namespaceURI, localPart);
        checkWildcard(localPart);
    }

    public boolean matches(QName qname) {
        return getNamespaceURI().equals(qname.getNamespaceURI())
            && (lcpattern != null 
            ? lcpattern.matcher(qname.getLocalPart()).matches() 
            : getLocalPart().equals(qname.getLocalPart()));
    }
    
    private void checkWildcard(String p) {
        boolean wc = false;
        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (c == '?' || c == '*') {
                wc = true;
                break;
            }
        }
        if (wc) {
            lcpattern = Pattern.compile(p.replace("*", ".*").replace("?", "."));
        }
    }
}
