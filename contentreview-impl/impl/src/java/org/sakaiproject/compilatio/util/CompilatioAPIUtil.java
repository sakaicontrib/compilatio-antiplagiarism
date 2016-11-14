/**
 * Copyright (c) 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.compilatio.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a utility class for wrapping the physical https calls to the Turn It
 * In Service.
 * 
 * @author sgithens
 *
 */
public class CompilatioAPIUtil {
	private static final Log log = LogFactory.getLog(CompilatioAPIUtil.class);

	public static Document callCompilatioReturnDocument(String apiURL, Map<String, String> parameters, String secretKey,
			int timeout, Proxy proxy, boolean isMultipart) throws TransientSubmissionException, SubmissionException {

		SOAPConnectionFactory soapConnectionFactory;
		Document xmlDocument = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();

			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyAction = soapBody.addChildElement(parameters.get("action"));
			parameters.remove("action");
			// api key
			SOAPElement soapBodyKey = soapBodyAction.addChildElement("key");
			soapBodyKey.addTextNode(secretKey);

			Set<Entry<String, String>> ets = parameters.entrySet();
			Iterator<Entry<String, String>> it = ets.iterator();
			while (it.hasNext()) {
				Entry<String, String> param = it.next();
				SOAPElement soapBodyElement = soapBodyAction.addChildElement(param.getKey());
				soapBodyElement.addTextNode(param.getValue());
			}

			//System.out.print("Request SOAP Message:");
			//soapMessage.writeTo(System.out);
			//System.out.println();
			SOAPMessage soapResponse = soapConnection.call(soapMessage, apiURL);

			// print SOAP Response
			System.out.print("Response SOAP Message:");
			soapResponse.writeTo(System.out);

			// loading the XML document
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			DocumentBuilderFactory builderfactory = DocumentBuilderFactory.newInstance();
			builderfactory.setNamespaceAware(true);

			String responseIdDocument = null;
			DocumentBuilder builder = builderfactory.newDocumentBuilder();
			xmlDocument = builder.parse(new InputSource(new StringReader(out.toString())));
			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();
			XPathExpression xPathExpression = xPath.compile("//idDocument");
			NodeList nodeListBook = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeListBook.getLength(); i++) {
				responseIdDocument = nodeListBook.item(i).getTextContent();
				System.out.println("Title " + (i + 1) + ": " + responseIdDocument);
			}
			soapConnection.close();

		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		return xmlDocument;

	}

	public static String callCompilatioReturnURL(String apiURL, Map<String, String> parameters, String secretKey,
			int timeout, Proxy proxy, boolean isMultipart) throws TransientSubmissionException, SubmissionException {

		SOAPConnectionFactory soapConnectionFactory;
		String stringReturn = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();

			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyAction = soapBody.addChildElement(parameters.get("action"));
			// api key
			SOAPElement soapBodyKey = soapBodyAction.addChildElement("key");
			soapBodyKey.addTextNode(secretKey);

			Set<Entry<String, String>> ets = parameters.entrySet();
			Iterator<Entry<String, String>> it = ets.iterator();
			while (it.hasNext()) {
				Entry<String, String> param = it.next();
				SOAPElement soapBodyElement = soapBodyAction.addChildElement(param.getKey());
				soapBodyElement.addTextNode(param.getValue());
			}

			System.out.print("Request SOAP Message:");
			soapMessage.writeTo(System.out);
			System.out.println();
			SOAPMessage soapResponse = soapConnection.call(soapMessage, apiURL);

			// print SOAP Response
			System.out.print("Response SOAP Message:");
			soapResponse.writeTo(System.out);

			// loading the XML document
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			DocumentBuilderFactory builderfactory = DocumentBuilderFactory.newInstance();
			builderfactory.setNamespaceAware(true);

			stringReturn = out.toString();

			soapConnection.close();

		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		return stringReturn;

	}

	public static Map packMap(Object... vargs) {
		Map map = new HashMap();
		if (vargs.length % 2 != 0) {
			throw new IllegalArgumentException("You need to supply an even number of vargs for the key-val pairs.");
		}
		for (int i = 0; i < vargs.length; i += 2) {
			map.put(vargs[i], vargs[i + 1]);
		}
		return map;
	}

	private static String encodeParam(String name, String value, String boundary) {
		return "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n";
	}

}
