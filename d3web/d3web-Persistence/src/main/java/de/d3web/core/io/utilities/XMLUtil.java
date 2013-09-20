/*
 * Copyright (C) 2009 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.core.io.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.d3web.core.io.NoSuchFragmentHandlerException;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.knowledge.terminology.info.Property.Autosave;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.core.utilities.Triple;
import de.d3web.scoring.Score;

/**
 * Provides useful static functions for xml persistence handlers
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public final class XMLUtil {

	public static final String INFO_STORE = "infoStore";

	/**
	 * Appends a question element to a parent element. Appends no element, if
	 * the question is null.
	 * 
	 * @param parent Element where the question element should be appended
	 * @param q Question which should be represented by the newly appended
	 *        element
	 * @throws IOException if the question has no ID
	 */
	public static void appendQuestionLinkElement(Element parent, QASet q) throws IOException {
		Document doc = parent.getOwnerDocument();
		if (q != null) {
			Element e = doc.createElement("Question");
			if (q.getName() != null) {
				e.setAttribute("name", q.getName());
				parent.appendChild(e);
			}
		}
	}

	/**
	 * Checks if the element has the specified element name and an attribute
	 * type with the value typeToCheck
	 * 
	 * @param element Element be checked
	 * @param elementname element name to check
	 * @param typeToCheck Value for the Attribute type
	 * @return true, if the given element fulfills the requirements, false
	 *         otherwise
	 */
	public static boolean checkNameAndType(Element element, String elementname, String typeToCheck) {
		String nodeName = element.getNodeName();
		String type = element.getAttribute("type");
		if (nodeName != null && nodeName.equals(elementname) && type != null
				&& type.equals(typeToCheck)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the element is named "Condition" and if has an attribute type
	 * with the value typeToCheck
	 * 
	 * @param element to be checked
	 * @param typeToCheck Value for the Attribute type
	 * @return true, if the given element fulfills the requirements, false
	 *         otherwise
	 */
	public static boolean checkCondition(Element element, String typeToCheck) {
		return checkNameAndType(element, "Condition", typeToCheck);
	}

	/**
	 * Creates a condition element with the id of the named object and the given
	 * type
	 * 
	 * @param doc Document, where the Element should be created
	 * @param nob TerminologyObject, whose ID should be used
	 * @param type type of the condition
	 * @return condition element
	 */
	public static Element writeCondition(Document doc, TerminologyObject nob, String type) {
		Element element = doc.createElement("Condition");
		element.setAttribute("type", type);
		if (nob != null) {
			element.setAttribute("name", nob.getName());
		}
		else {
			element.setAttribute("name", "");
		}
		return element;
	}

	/**
	 * Creates a condition element with the id of the named object, the given
	 * type and value
	 * 
	 * @param doc Document, where the Element should be created
	 * @param nob TerminologyObject, whose ID should be used
	 * @param type type of the condition
	 * @param value value of the condition
	 * @return condition element
	 */
	public static Element writeCondition(Document doc, TerminologyObject nob, String type, String value) {
		Element element = writeCondition(doc, nob, type);
		if (value != null) {
			element.setAttribute("value", value);
		}
		return element;
	}

	/**
	 * Creates a condition element with the id of the named object, the given
	 * type and value. The value is stored in a child element.
	 * 
	 * @param doc Document, where the Element should be created
	 * @param nob TerminologyObject, whose ID should be used
	 * @param type type of the condition
	 * @param value value of the condition
	 * @return condition element
	 */
	public static Element writeConditionWithValueNode(Document doc, TerminologyObject nob, String type, String value) throws IOException {
		Element valueElement = doc.createElement("Value");
		valueElement.setTextContent(value);
		return writeConditionWithValueNode(doc, nob, type, valueElement);
	}

	/**
	 * Creates a condition element with the id of the named object, the given
	 * type and value. The value node is inserted as a child element in the
	 * newly created node.
	 * 
	 * @param doc Document, where the Element should be created
	 * @param nob TerminologyObject, whose ID should be used
	 * @param type type of the condition
	 * @param value value of the condition
	 * @return condition element
	 */
	public static Element writeConditionWithValueNode(Document doc, TerminologyObject nob, String type, Element value) {
		Element element = writeCondition(doc, nob, type);
		element.appendChild(value);
		return element;
	}

	/**
	 * Creates a condition element with the id of the named object, the given
	 * type and values
	 * 
	 * @param doc Document, where the Element should be created
	 * @param nob TerminologyObject, whose ID should be used
	 * @param type type of the condition
	 * @param values List of answers, which are used as values
	 * @return condition element
	 * @throws IOException if one of the elements in values is neither a Choice
	 *         nor a Unknown Answer
	 */
	public static Element writeCondition(Document doc, TerminologyObject nob, String type,
			Value value) throws IOException {
		Element element = writeCondition(doc, nob, type);
		if (value != null) {
			String s = getValue(nob, value);
			element.setAttribute("value", s);
		}
		return element;
	}

	public static String getValue(TerminologyObject nob, Object answer) throws IOException {
		if (answer instanceof ChoiceValue) {
			ChoiceValue v = (ChoiceValue) answer;
			Choice choice = v.getChoice((QuestionChoice) nob);
			return choice.getName();
		}
		else if (answer instanceof Unknown) {
			return Unknown.UNKNOWN_ID;
		}
		else if (answer instanceof MultipleChoiceValue) {
			return ChoiceID.encodeChoiceIDs(((MultipleChoiceValue) answer).getChoiceIDs());
		}
		// for num answers, not the ID, but their actual value is returned
		else if (answer instanceof NumValue) {
			return ((NumValue) answer).getValue().toString();
		}
		else if (answer instanceof TextValue) {
			return ((TextValue) answer).getValue().toString();
		}
		else {
			throw new IOException(
					"The given value is neighter a Choice nor a Unknown nor a text value.");
		}
	}

	/**
	 * Creates a condition element with the specified type
	 * 
	 * @param doc Document, where the Element should be created
	 * @param type type of the condition
	 * @return condition element
	 */
	public static Element writeCondition(Document doc, String type) {
		Element element = doc.createElement("Condition");
		element.setAttribute("type", type);
		return element;
	}

	/**
	 * Appends an Element TargetQASets containing elements for the given qaSets
	 * 
	 * @param element Element the TargetQASets Element should be appended
	 * @param qaSets List of QASet being represented by the appended element
	 */
	public static void appendTargetQASets(Element element, List<QASet> qaSets) {
		Document doc = element.getOwnerDocument();
		if (!qaSets.isEmpty()) {
			Element targetQASets = doc.createElement("TargetQASets");
			for (QASet qaset : qaSets) {
				Element qasetElement = doc.createElement("QASet");
				qasetElement.setAttribute("name", qaset.getName());
				targetQASets.appendChild(qasetElement);
			}
			element.appendChild(targetQASets);
		}
	}

	/**
	 * Extract the qasets stored in the given element
	 * 
	 * @param element Element representing the QASets
	 * @param kb knowledge base containing the qasets
	 * @return List of represented QASets
	 */
	public static List<QASet> getTargetQASets(Element element, KnowledgeBase kb) {
		List<QASet> ret = new LinkedList<QASet>();
		if (element.getNodeName().equalsIgnoreCase("targetQASets")) {
			NodeList qasets = element.getChildNodes();
			for (int k = 0; k < qasets.getLength(); ++k) {
				Node q = qasets.item(k);
				if (q.getNodeName().equalsIgnoreCase("QASet")) {
					String id = q.getAttributes().getNamedItem("name").getNodeValue();
					QASet qset = (QASet) kb.getManager().search(id);
					ret.add(qset);
				}
			}
		}
		return ret;
	}

	/**
	 * Adds the children of the TerminologyObject to the specified element
	 * 
	 * @param namedObject TerminologyObject, whose children should be appended
	 *        as Elements
	 * @param element Element representing the namedObject, where the children
	 *        will be appended
	 */
	public static void appendChildren(TerminologyObject namedObject, Element element) {
		Document doc = element.getOwnerDocument();
		TerminologyObject[] children = namedObject.getChildren();
		if (children.length != 0) {
			Element childrenElement = doc.createElement("Children");
			for (TerminologyObject child : children) {
				Element childElement = doc.createElement("Child");
				childElement.setAttribute("name", child.getName());
				childrenElement.appendChild(childElement);
			}
			element.appendChild(childrenElement);
		}
	}

	/**
	 * Adds the children given from the xml structure to the TerminologyObject
	 * 
	 * @param kb KnowledgeBase containing the children
	 * @param namedObject where the children should be appended
	 * @param element representing the namedObject and containing the children
	 *        as childnodes
	 */
	public static void appendChildren(KnowledgeBase kb, TerminologyObject namedObject, Element element) throws IOException {
		List<Element> children = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child.getNodeName().equals("Children")) {
				children = XMLUtil.getElementList(child.getChildNodes());
			}
		}
		if (children != null) {
			for (Element child : children) {
				String id = child.getAttribute("name");
				TerminologyObject no = kb.getManager().search(
						id);
				if (namedObject instanceof QASet && no instanceof QASet) {
					((QASet) namedObject).addChild(((QASet) no));
				}
				else if (namedObject instanceof Solution && no instanceof Solution) {
					((Solution) namedObject).addChild(((Solution) no));
				}
				else {
					throw new IOException("The types of " + namedObject + " and " + no
							+ " don't match.");
				}
			}
		}
	}

	/**
	 * Extracts primitive Values form a string
	 * 
	 * @param textContent Sting containing the primitive value
	 * @param clazz Name of the Class
	 * @return Extracted Value
	 * @throws IOException if the class is not supported
	 */
	public static Object getPrimitiveValue(String textContent, String clazz) throws IOException {
		if (clazz.equals(String.class.getName())) {
			return textContent;
		}
		else if (clazz.equals(Integer.class.getName())) {
			return Integer.parseInt(textContent);
		}
		else if (clazz.equals(Double.class.getName())) {
			return Double.parseDouble(textContent);
		}
		else if (clazz.equals(Boolean.class.getName())) {
			return Boolean.parseBoolean(textContent);
		}
		else if (clazz.equals(URL.class.getName())) {
			return new URL(textContent);
		}
		else if (clazz.equals(Float.class.getName())) {
			return Float.parseFloat(textContent);
		}
		else {
			throw new IOException("Class " + clazz + " is not supported");
		}
	}

	/**
	 * Filters all elements of a NodeList and returns them in a collection.
	 * 
	 * @param list Nodelist containing all types of nodes (text nodes etc.)
	 * @return a list containing all elements from nodelist, but not containing
	 *         other nodes such as text nodes etc.
	 */
	public static List<Element> getElementList(NodeList list) {
		List<Element> col = new ArrayList<Element>(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				col.add((Element) list.item(i));
			}
		}
		return col;
	}

	/**
	 * Filters all elements of a NodeList and returns them in a collection. The
	 * list will only contain that elements of the NodeList that match the
	 * specified node name. The name selection is case insensitive.
	 * 
	 * @param list Nodelist containing all types of nodes (text nodes etc.)
	 * @param nodeName the name of the elements to be selected (case
	 *        insensitive)
	 * @return a list containing all elements from nodelist, but not containing
	 *         other nodes such as text nodes etc.
	 */
	public static List<Element> getElementList(NodeList list, String nodeName) {
		List<Element> col = new ArrayList<Element>();
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item instanceof Element && item.getNodeName().equalsIgnoreCase(nodeName)) {
				col.add((Element) item);
			}
		}
		return col;
	}

	/**
	 * Appends all entries with the given autosave of the {@link InfoStore} to
	 * the specified father. If autosave is null, all entries will be appended
	 * 
	 * @created 08.11.2010
	 * @param father {@link Element}
	 * @param infoStore {@link InfoStore}
	 * @param autosave {@link Autosave}
	 * @throws IOException
	 */
	public static void appendInfoStoreEntries(Element father, InfoStore infoStore, Autosave autosave) throws IOException {
		Document doc = father.getOwnerDocument();
		for (Triple<Property<?>, Locale, Object> entry : sortEntries(infoStore.entries())) {
			if (autosave == null || (autosave != null && entry.getA().hasState(autosave))) {
				Element entryElement = doc.createElement("entry");
				father.appendChild(entryElement);
				entryElement.setAttribute("property", entry.getA().getName());
				Locale language = entry.getB();
				if (language != InfoStore.NO_LANGUAGE) {
					entryElement.setAttribute("lang", language.toString());
				}
				try {
					entryElement.appendChild(PersistenceManager.getInstance().writeFragment(
							entry.getC(), doc));
				}
				catch (NoSuchFragmentHandlerException e) {
					if (entry.getA().canParseValue()) {
						entryElement.setTextContent(entry.getC().toString());
					}
					else {
						throw e;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @created 11.11.2010
	 * @param entries
	 * @return
	 */
	public static List<Triple<Property<?>, Locale, Object>> sortEntries(Collection<Triple<Property<?>, Locale, Object>> entries) {
		LinkedList<Triple<Property<?>, Locale, Object>> ret = new LinkedList<Triple<Property<?>, Locale, Object>>(
				entries);
		Collections.sort(ret, new Comparator<Triple<Property<?>, Locale, Object>>() {

			@Override
			public int compare(Triple<Property<?>, Locale, Object> arg0, Triple<Property<?>, Locale, Object> arg1) {
				if (arg0 == arg1) return 0;
				// if the property is different, compare the names
				if (arg0.getA().getName() != arg1.getA().getName()) {
					return arg0.getA().getName().compareTo(arg1.getA().getName());
				}
				// the next criteria is the locale
				else if (arg0.getB() != arg1.getB()) {
					if (arg0.getB() == InfoStore.NO_LANGUAGE) {
						return 1;
					}
					else if (arg1.getB() == InfoStore.NO_LANGUAGE) {
						return -1;
					}
					else {
						return arg0.getB().toString().compareTo(arg1.getB().toString());
					}
				}
				// finally compare the content using its toString()
				else {
					if (arg0.getC() == arg1.getC()) {
						return 0;
					}
					if (arg0.getC() == null) {
						return 1;
					}
					else if (arg1.getC() == null) {
						return -1;
					}
					else {
						return arg0.getC().toString().compareTo(arg1.getC().toString());
					}
				}
			}
		});
		return ret;
	}

	public static void appendInfoStore(Element idObjectElement, NamedObject idObject, Autosave autosave) throws IOException {
		InfoStore infoStore = idObject.getInfoStore();
		if (infoStore != null && !infoStore.isEmpty()) {
			Element infoStoreElement = idObjectElement.getOwnerDocument().createElement(INFO_STORE);
			appendInfoStoreEntries(infoStoreElement, infoStore, autosave);
			if (infoStoreElement.getChildNodes().getLength() > 0) {
				idObjectElement.appendChild(infoStoreElement);
			}
		}
	}

	/**
	 * Reads all children of the {@link Element} father and adds the created
	 * entries to the infostore
	 * 
	 * @created 08.11.2010
	 * @param infoStore {@link InfoStore}
	 * @param father {@link Element}
	 * @param kb {@link KnowledgeBase}
	 * @throws IOException
	 */
	public static void fillInfoStore(InfoStore infoStore, Element father, KnowledgeBase kb) throws IOException {
		for (Element child : getElementList(father.getChildNodes())) {
			Property<Object> property;
			try {
				property = Property.getUntypedProperty(child.getAttribute("property"));
			}
			catch (NoSuchElementException e) {
				Logger.getLogger(XMLUtil.class.getName()).log(
						Level.WARNING,
						"Property '" + child.getAttribute("property") +
								"' is not supported. Propably the corresponding plugin " +
								"is missing. This property will be lost when saving " +
								"the knowledge base.");
				continue;
			}
			List<Element> childNodes = XMLUtil.getElementList(child.getChildNodes());
			Object value = "";
			if (childNodes.size() > 0) {
				value = PersistenceManager.getInstance().readFragment(
						childNodes.get(0), kb);
			}
			else {
				String s = child.getTextContent();
				try {
					value = property.parseValue(s);
				}
				catch (NoSuchMethodException e) {
					throw new IOException(e);
				}
			}

			String language = child.getAttribute("lang");
			if (language.length() > 0) {
				String[] split = language.split("_", 3);
				Locale locale;
				if (split.length < 2) {
					locale = new Locale(language);
				}
				else if (split.length == 2) {
					locale = new Locale(split[0], split[1]);
				}
				else {
					locale = new Locale(split[0], split[1], split[2]);
				}
				infoStore.addValue(property, locale, value);
			}
			else {
				infoStore.addValue(property, value);
			}
		}
	}

	/**
	 * Creates an XML {@link Document} from the given {@link InputStream}.
	 * 
	 * @param stream the XML input stream
	 * @return Document the document created from the stream
	 * @throws IOException if the stream cannot be read or does not contains
	 *         valid XML content or the XML parser cannot be configured
	 */
	public static Document streamToDocument(InputStream stream) throws IOException {
		return streamToDocument(stream, null);
	}

	/**
	 * Creates an XML {@link Document} from the given {@link InputStream}.
	 * 
	 * @param stream the XML input stream
	 * @param resolver is a {@link EntityResolver} to specify how entities given
	 *        in the {@link Document} should be resolved
	 * @return Document the document created from the stream
	 * @throws IOException if the stream cannot be read or does not contains
	 *         valid XML content or the XML parser cannot be configured
	 */
	public static Document streamToDocument(InputStream stream, EntityResolver resolver) throws IOException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = null;
		try {
			parser = fac.newDocumentBuilder();
			if (resolver != null) parser.setEntityResolver(resolver);
			return parser.parse(new InputSource(stream));
		}
		catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Creates an XML {@link Document} from the {@link File}.
	 * 
	 * @param stream the xml input stream
	 * @return Document the document created from the stream
	 * @throws IOException when an error occurs
	 */
	public static Document fileToDocument(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			return streamToDocument(in);
		}
		finally {
			in.close();
		}
	}

	/**
	 * Creates an empty Document
	 * 
	 * @return newly created document
	 * @throws IOException when an error occurs
	 */
	public static Document createEmptyDocument() throws IOException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = fac.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		}
		return builder.newDocument();
	}

	/**
	 * Writes the Document to the given OutputStream
	 * 
	 * @param doc input document
	 * @param stream outout stream
	 * @throws IOException when an error occurs
	 */
	public static void writeDocumentToOutputStream(Document doc, OutputStream stream) throws IOException {
		Source source = new DOMSource(doc);
		Result result = new StreamResult(stream);
		Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty("method", "xml");
			if (doc.getXmlEncoding() == null) {
				xformer.setOutputProperty("encoding", "UTF-8");
			}
			else {
				xformer.setOutputProperty("encoding", doc.getXmlEncoding());
			}
			xformer.setOutputProperty("omit-xml-declaration", "no");
			xformer.setOutputProperty("indent", "yes");
			xformer.transform(source, result);
		}
		catch (TransformerConfigurationException e) {
			new IOException(e.getMessage());
		}
		catch (TransformerFactoryConfigurationError e) {
			new IOException(e.getMessage());
		}
		catch (TransformerException e) {
			new IOException(e.getMessage());
		}
	
	}

	/**
	 * @return the Score matching the given String (e.g. "n7" to Score.N7)
	 * @throws IOException when an error occurs
	 */
	public static Score getScore(String value) throws IOException {
		Score score = null;
		if (value.equalsIgnoreCase("n7")) {
			score = Score.N7;
		}
		else if (value.equalsIgnoreCase("n6")) {
			score = Score.N6;
		}
		else if (value.equalsIgnoreCase("n5")) {
			score = Score.N5;
		}
		else if (value.equalsIgnoreCase("n5x")) {
			score = Score.N5x;
		}
		else if (value.equalsIgnoreCase("n4")) {
			score = Score.N4;
		}
		else if (value.equalsIgnoreCase("n3")) {
			score = Score.N3;
		}
		else if (value.equalsIgnoreCase("n2")) {
			score = Score.N2;
		}
		else if (value.equalsIgnoreCase("n1")) {
			score = Score.N1;
		}
		else if (value.equalsIgnoreCase("p1")) {
			score = Score.P1;
		}
		else if (value.equalsIgnoreCase("p2")) {
			score = Score.P2;
		}
		else if (value.equalsIgnoreCase("p3")) {
			score = Score.P3;
		}
		else if (value.equalsIgnoreCase("p4")) {
			score = Score.P4;
		}
		else if (value.equalsIgnoreCase("p5")) {
			score = Score.P5;
		}
		else if (value.equalsIgnoreCase("p5x")) {
			score = Score.P5x;
		}
		else if (value.equalsIgnoreCase("p6")) {
			score = Score.P6;
		}
		else if (value.equalsIgnoreCase("p7")) {
			score = Score.P7;
		}
		else if (value.equalsIgnoreCase("pp")) {
			throw new IOException(
					"knowledgebase uses pp-rules! - this will cause NullPointerException in rule firing");
		}
		return score;
	}
}
