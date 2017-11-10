package com.alkisum.android.vlcremote.utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Utility class for XML operations.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Xml {

    /**
     * Xml constructor.
     */
    private Xml() {

    }

    /**
     * Build XML document from string.
     *
     * @param xml String to build the XML document from
     * @return Built XML document
     * @throws IOException                  XML document cannot be created
     *                                      from XML string
     * @throws SAXException                 SAX error or warning
     * @throws ParserConfigurationException Serious configuration error
     */
    public static Document buildDocFromString(final String xml) throws
            IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Get value from status XML doc.
     *
     * @param doc         Doc to get value from
     * @param elementName Element name to search
     * @param attribute   Attribute to search
     * @return The value found
     * @throws XPathExpressionException Error in XPath expression
     */
    public static String getValueFromStatus(final Document doc,
                                            final String elementName,
                                            final String attribute)
            throws XPathExpressionException {

        // Create XPath
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        StringBuilder expression = new StringBuilder();

        // Build XPath from element name and attribute value (if exists)
        expression.append("//").append(elementName);
        if (attribute != null) {
            expression.append("[@name=\'").append(attribute).append("\']");
        }
        expression.append("/text()");
        XPathExpression xPathExpression = xPath.compile(expression.toString());

        // Return result from XPath expression
        return xPathExpression.evaluate(doc);
    }
}
