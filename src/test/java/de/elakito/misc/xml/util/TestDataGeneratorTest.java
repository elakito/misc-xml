package de.elakito.misc.xml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestDataGeneratorTest extends Assert {
    // the sample data provided by mic-cust.com
    private static final String HEAD =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<PartMessage xmlns=\"http://www.mic-cust.com/test/parts/v1.1\">";

    private static final String PART =
        "    <Part>"
        + "        <company>XX</company>"
        + "        <plant>XX</plant>"
        + "        <partNumber>XX</partNumber>"
        + "        <partType>M</partType>"
        + "        <PartDetails>"
        + "            <validFrom>2014-06-20</validFrom>"
        + "            <description1>Test Description XXX</description1>"
        + "            <description2>Test Description XXX</description2>"
        + "            <descriptionType></descriptionType>"
        + "            <language>1</language>"
        + "            <alias1>       3,7B</alias1>"
        + "            <aliasType1></aliasType1>"
        + "            <alias2></alias2>"
        + "            <aliasType2></aliasType2>"
        + "            <bom></bom>"
        + "            <bomVariant></bomVariant>"
        + "            <quantity>1.000</quantity>"
        + "            <quantityUom>01</quantityUom>"
        + "            <weightUom></weightUom>"
        + "            <volumeUom></volumeUom>"
        + "            <originCountry></originCountry>"
        + "            <prefIndicator></prefIndicator>"
        + "            <prefOriginCountry></prefOriginCountry>"
        + "            <diffusionCountry></diffusionCountry>"
        + "            <diffusionState></diffusionState>"
        + "            <assemblyCountry></assemblyCountry>"
        + "            <assemblyState></assemblyState>"
        + "            <resaleGoods></resaleGoods>"
        + "            <chipDelivery></chipDelivery>"
        + "            <system>XX</system>"
        + "            <reason>XX</reason>"
        + "            <responsiblePerson></responsiblePerson>"
        + "            <info></info>"
        + "            <status></status>"
        + "            <CustomerFields>"
        + "                <customerField1></customerField1>"
        + "                <customerField2></customerField2>"
        + "                <customerField3></customerField3>"
        + "                <customerField4></customerField4>"
        + "                <customerField5></customerField5>"
        + "                <customerField6></customerField6>"
        + "                <customerField7></customerField7>"
        + "                <customerField8></customerField8>"
        + "                <customerField9></customerField9>"
        + "                <customerField10></customerField10>"
        + "                <text1></text1>"
        + "                <text2></text2>"
        + "                <text3></text3>"
        + "                <text4></text4>"
        + "                <text5></text5>"
        + "                <text6></text6>"
        + "                <text7></text7>"
        + "                <text8></text8>"
        + "                <text9></text9>"
        + "                <text10></text10>"
        + "                <number10>7</number10>"
        + "            </CustomerFields>"
        + "        </PartDetails>"
        + "        <PartClassifications>"
        + "            <validFrom>2014-06-20</validFrom>"
        + "            <classificationType>XX</classificationType>"
        + "            <classificationRegion>XX</classificationRegion>"
        + "            <tariffNumber>XX</tariffNumber>"
        + "            <confidenceLevel>XX</confidenceLevel>"
        + "            <workQueue></workQueue>"
        + "            <responsiblePerson></responsiblePerson>"
        + "            <noteType></noteType>"
        + "            <noteText></noteText>"
        + "            <reference></reference>"
        + "            <reason>XX</reason>"
        + "        </PartClassifications>"
        + "        <PartDescriptions>"
        + "            <validFrom>2014-06-20</validFrom>"
        + "            <language>2</language>"
        + "            <description>XX</description>"
        + "            <system></system>"
        + "        </PartDescriptions>"
        + "    </Part>";

    private static final String TAIL =
        "</PartMessage>";

    private static final String VPART =
        "    <Part n=\"{0}\">"
        + "        <company>{1}</company>"
        + "        <plant>{2}</plant>"
        + "        <partNumber>{3}</partNumber>"
        + "        <partType>{4}</partType>"
        + "        <PartDetails>Some Test Part</PartDetails>"
        + "    </Part>";


    @Test
    public void testSampleData() throws Exception {
        InputStream in = 
            TestDataGenerator.createTokenDataInputStream(PART, 3, HEAD, TAIL, null, "utf-8");
        
        String target = buildTargetTestData(PART, 3, HEAD, TAIL, null);
        String result = buildResultTestData(in, "utf-8");

        assertEquals(target, result);
    }

    @Test
    public void testSampleVariableData() throws Exception {
        List<Object[]> vlist = new ArrayList<Object[]>();
        vlist.add(new Object[]{"1", "apple", "fremont", "123", "1976"});
        vlist.add(new Object[]{"2", "orange", "bristol", "581", "1994"});
        vlist.add(new Object[]{"3", "peach", "osaka", "321", "2011"});
        InputStream in = 
            TestDataGenerator.createTokenDataInputStream(VPART, 3, HEAD, TAIL, vlist.iterator(), "utf-8");
        
        String target = buildTargetTestData(VPART, 3, HEAD, TAIL, vlist.iterator());
        String result = buildResultTestData(in, "utf-8");

        assertEquals(target, result);
    }

    private String buildTargetTestData(String part, int r, String head, String tail, Iterator<Object[]> values)
        throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append(HEAD);
        for (int i = 0; i < r; i++) {
            sb.append(values != null ? MessageFormat.format(part,  values.next()) : part);
        }
        sb.append(TAIL);
        return sb.toString();
    }

    private String buildResultTestData(InputStream in, String charset) throws IOException {
        byte[] buf = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (;;) {
            int n = in.read(buf, 0, buf.length);
            if (n < 0) {
                break;
            }
            baos.write(buf, 0, n);
        }

        return baos.toString(charset);
    }
}
