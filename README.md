misc-xml
========

Place for putting some xml related coding experience.

### Content ###
Currently. it contains
- XMLTokenIterator: An iterator that can iterate over an XML document to extract 
  a series of elements matched by the specified path expression. The patch expression
  is similar to an xpath expression. However it differs from xpath, in which the path
  may consist of a series of QNames and a unix-like wildcard can be used for the QNames.

  For examples, "//c:child", "/root/c:parent/c:item", "/root//c:item", "//*:child", "//*:c*d"
  are all valid path expressions.

  The mode parameter determines how the extracted tokens are constructed.

| mode | description |
| ---- | ----------- |
| i    | injecting the contextual namespace bindings into the extracted token (default) |
| w    | wrapping the extracted token in its ancestor context |
| u    |  unwrapping the extracted token to its child content |
| t    |  extracting the text content of the specified element |


```java  
        XMLTokenIterator tokenizer = new XMLTokenIterator(path, nsmap, mode, in, charset);

        List<String> results = new ArrayList<String>();
        while (tokenizer.hasNext()) {
            String token = (String)tokenizer.next();            
            System.out.println("#### result: " + token);
            results.add(token);
        }
        ((Closeable)tokenizer).close();
```

### Note ###

A slightly modified version of XMLTokenIterator has been contributed to Apache Camel with [CAMEL-7468](https://issues.apache.org/jira/browse/CAMEL-7468).

There is a dedicated package for testing and comparing its performance at [https://github.com/elakito/testzone/tree/master/tests/camel_xmltokenize_comaprison](https://github.com/elakito/testzone/tree/master/tests/camel_xmltokenize_comaprison)

A slightly modified version of these input transformers have been contributed to Apache Camel with [CAMEL-10070](https://issues.apache.org/jira/browse/CAMEL-10070)

