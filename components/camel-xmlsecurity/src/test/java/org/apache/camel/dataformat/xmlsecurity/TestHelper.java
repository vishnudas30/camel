/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.xmlsecurity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.log4j.Logger;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import org.junit.Assert;

public class TestHelper {
    
    protected static final String NS_XML_FRAGMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<ns1:cheesesites xmlns:ns1=\"http://cheese.xmlsecurity.camel.apache.org/\">" 
        + "<netherlands>"
        + "<source>cow</source>"
        + "<cheese>gouda</cheese>"
        + "</netherlands>"
        + "<italy>"
        + "<source>cow</source>"
        + "<cheese>gorgonzola</cheese>"
        + "</italy>"
        + "<france>"
        + "<source>goat</source>"
        + "<cheese>brie</cheese>"
        + "</france>"
        + "</ns1:cheesesites>";
    
    protected static final String XML_FRAGMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<cheesesites>"
        + "<netherlands>"
        + "<source>cow</source>"
        + "<cheese>gouda</cheese>"
        + "</netherlands>"
        + "<italy>"
        + "<source>cow</source>"
        + "<cheese>gorgonzola</cheese>"
        + "</italy>"
        + "<france>"
        + "<source>goat</source>"
        + "<cheese>brie</cheese>"
        + "</france>"
        + "</cheesesites>";
    
    static final boolean HAS_3DES;
    static {
        boolean ok = false;
        try {
            org.apache.xml.security.Init.init();
            XMLCipher.getInstance(XMLCipher.TRIPLEDES_KeyWrap);
            ok = true;
        } catch (XMLEncryptionException e) {
            e.printStackTrace();
        }
        HAS_3DES = ok;
    }
    
    Logger log = Logger.getLogger(TestHelper.class);

    protected void sendText(final String fragment, CamelContext context) throws Exception {
        ProducerTemplate template = context.createProducerTemplate();
        template.start();
        template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                // Set the property of the charset encoding
                exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
                Message in = exchange.getIn();
                in.setBody(fragment);
                log.info("xmlFragment: " + fragment);
            }
        });
    }
      
    protected void testEncryption(String fragment, CamelContext context) throws Exception {
        MockEndpoint resultEndpoint = context.getEndpoint("mock:encrypted", MockEndpoint.class);
        resultEndpoint.setExpectedMessageCount(1);
        context.start();
        sendText(fragment, context);
        resultEndpoint.assertIsSatisfied(100);
        Exchange exchange = resultEndpoint.getExchanges().get(0);
        Document inDoc = getDocumentForInMessage(exchange);
        if (log.isDebugEnabled()) {
            logMessage(exchange, inDoc);
        }
        Assert.assertTrue("The XML message has no encrypted data.", hasEncryptedData(inDoc));
    }
    

    protected void testEncryption(CamelContext context) throws Exception {
        testEncryption(XML_FRAGMENT, context);
    }
    
    
    protected void testDecryption(String fragment, CamelContext context) throws Exception {
        MockEndpoint resultEndpoint = context.getEndpoint("mock:decrypted", MockEndpoint.class);
        resultEndpoint.setExpectedMessageCount(1);
        // verify that the message was encrypted before checking that it is decrypted
        testEncryption(fragment, context);

        resultEndpoint.assertIsSatisfied(100);
        Exchange exchange = resultEndpoint.getExchanges().get(0);
        Document inDoc = getDocumentForInMessage(exchange);
        if (log.isDebugEnabled()) {
            logMessage(exchange, inDoc);
        }
        Assert.assertFalse("The XML message has encrypted data.", hasEncryptedData(inDoc));
        
        // verify that the decrypted message matches what was sent
        Document fragmentDoc = createDocumentfromInputStream(new ByteArrayInputStream(fragment.getBytes()), context);
        Diff xmlDiff = XMLUnit.compareXML(fragmentDoc, inDoc);
        
        Assert.assertTrue("The decrypted document does not match the control document.", xmlDiff.identical());            
    }
    
    protected void testDecryption(CamelContext context) throws Exception {
        testDecryption(XML_FRAGMENT, context);
    }
    
    private boolean hasEncryptedData(Document doc) throws Exception {
        NodeList nodeList = doc.getElementsByTagNameNS("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
        return nodeList.getLength() > 0;
    }
    
    private void logMessage(Exchange exchange, Document inDoc) throws Exception {
        XmlConverter converter = new XmlConverter();
        String xmlStr = converter.toString(inDoc, exchange);
        log.debug(xmlStr);   
    }
    
    private Document getDocumentForInMessage(Exchange exchange) {
        byte[] body = exchange.getIn().getBody(byte[].class);
        Document d = createDocumentfromInputStream(new ByteArrayInputStream(body), exchange.getContext());
        return d;
    }
    
    private Document createDocumentfromInputStream(InputStream is, CamelContext context) {
        return context.getTypeConverter().convertTo(Document.class, is);
    }

}
