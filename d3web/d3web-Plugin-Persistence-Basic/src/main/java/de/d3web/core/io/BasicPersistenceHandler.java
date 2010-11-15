/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg denkbares GmbH
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

package de.d3web.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.io.fragments.PropertiesHandler;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.io.utilities.IDObjectComparator;
import de.d3web.core.io.utilities.Util;
import de.d3web.core.io.utilities.XMLUtil;
import de.d3web.core.knowledge.InfoStoreUtil;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.Num2ChoiceSchema;
import de.d3web.core.knowledge.terminology.info.Property.Autosave;

/**
 * PersistenceHandler for reading and writing basic knowledge Creation date:
 * (06.06.2001 15:26:13)
 * 
 * @author Michael Scharvogel, Norman Brümmer, Markus Friedrich (denkbares GmbH)
 */
public class BasicPersistenceHandler implements
		KnowledgeReader,
		KnowledgeWriter {

	public static final String BASIC_PERSISTENCE_HANDLER = "basic";

	private float saveSchemas(Element father, KnowledgeBase kb, ProgressListener listener, float time, int abstime) throws IOException {

		final MethodKind methodKind = PSMethodAbstraction.NUM2CHOICE_SCHEMA;
		final Class<? extends PSMethod> context = PSMethodAbstraction.class;

		Iterator<Question> questionsIter = kb.getQuestions().iterator();
		while (questionsIter.hasNext()) {
			Question question = questionsIter.next();
			KnowledgeSlice o = question.getKnowledge(context, methodKind);
			if (o != null) {
				Num2ChoiceSchema schema = (Num2ChoiceSchema) o;
				listener.updateProgress(time++ / abstime, "Saving knowledge base: Schemas");
				father.appendChild(PersistenceManager.getInstance().writeFragment(schema,
						father.getOwnerDocument()));
			}
		}
		return time;
	}

	/**
	 * @return the ID of this PersistenceHandler
	 */
	public String getId() {
		return BASIC_PERSISTENCE_HANDLER;
	}

	private float saveInitQuestions(Element father, KnowledgeBase kb, ProgressListener listener, float time, int abstime) throws IOException {
		List<? extends QASet> theList = kb.getInitQuestions();
		Document doc = father.getOwnerDocument();
		if (theList != null) {
			Iterator<? extends QASet> iter = theList.iterator();
			Element initQuestionsNode = doc.createElement("InitQuestions");
			while (iter.hasNext()) {
				listener.updateProgress(time++ / abstime, "Saving knowledge base: init QA sets");
				QASet o = iter.next();
				XMLUtil.appendQuestionLinkElement(initQuestionsNode, o);
			}
			father.appendChild(initQuestionsNode);
		}
		return time;
	}

	@Override
	public void read(KnowledgeBase kb, InputStream stream, ProgressListener listener) throws IOException {
		listener.updateProgress(0, "Loading knowledge base");
		Document doc = Util.streamToDocument(stream);
		List<Element> childNodes = XMLUtil.getElementList(doc.getChildNodes());
		if (childNodes.size() != 1) {
			throw new IOException("Document has more than one child.");
		}
		Element father = childNodes.get(0);
		Node idNode = father.getAttributes().getNamedItem("id");
		if (idNode != null) {
			String idString = idNode.getNodeValue();
			if (idString != null && !idString.equals("null")
					&& !idString.trim().equals("")) {
				kb.setId(idString);
			}
		}
		List<Element> kbchildren = XMLUtil.getElementList(father.getChildNodes());
		// splitting the kb children and calculating the absolute time to load
		int abstime = 0;
		List<Element> knowledgeslicesNodes = null;
		List<Element> qASetNodes = new ArrayList<Element>();
		List<Element> diagnosisNodes = null;
		List<Element> initquestionnodes = null;
		FragmentManager pm = PersistenceManager.getInstance();
		String rootQASetID = null;
		String rootSolutionID = null;
		PropertiesHandler ph = new PropertiesHandler();
		for (Element child : kbchildren) {
			String name = child.getNodeName();
			if (name.equalsIgnoreCase("knowledgeslices")) {
				knowledgeslicesNodes = XMLUtil.getElementList(child.getChildNodes());
				abstime += knowledgeslicesNodes.size();
			}
			// former way of saving Questions
			else if (name.equalsIgnoreCase("Questions")) {
				qASetNodes.addAll(XMLUtil.getElementList(child.getChildNodes()));
			}
			else if (name.equalsIgnoreCase("diagnoses")) {
				diagnosisNodes = XMLUtil.getElementList(child.getChildNodes());
				abstime += diagnosisNodes.size();
			}
			// former way of saving QContainers
			else if (name.equalsIgnoreCase("QContainers")) {
				qASetNodes.addAll(XMLUtil.getElementList(child.getChildNodes()));
			}
			else if (name.equalsIgnoreCase("QASets")) {
				qASetNodes.addAll(XMLUtil.getElementList(child.getChildNodes()));
			}
			else if (name.equalsIgnoreCase("InitQuestions")
								// former name in previous versions
					|| name.equalsIgnoreCase("InitQASets")) {
				initquestionnodes = XMLUtil.getElementList(child.getChildNodes());
				abstime += initquestionnodes.size();
			}
			else if (name.equals("rootQASet")) {
				rootQASetID = child.getTextContent();
			}
			else if (name.equals("rootSolution")) {
				rootSolutionID = child.getTextContent();
			}
			else if (name.equals(XMLUtil.INFO_STORE)) {
				XMLUtil.fillInfoStore(kb.getInfoStore(), child, kb);
			}
			// read old persistence format
			else if (ph.canRead(child)) {
				InfoStoreUtil.copyEntries(ph.read(kb, child), kb.getInfoStore());
			}
		}
		abstime += qASetNodes.size();
		float time = 0;

		Map<Element, NamedObject> hierarchiemap = new HashMap<Element, NamedObject>();

		for (Element child : qASetNodes) {
			listener.updateProgress(time++ / abstime, "Building qasets");
			QASet q = (QASet) pm.readFragment(child, kb);
			hierarchiemap.put(child, q);
		}

		if (diagnosisNodes != null) {
			for (Element child : diagnosisNodes) {
				listener.updateProgress(time++ / abstime, "Building diagnosis");
				Solution diag = (Solution) pm.readFragment(child, kb);
				hierarchiemap.put(child, diag);
			}
		}

		// defining roots:
		if (rootQASetID != null) {
			kb.setRootQASet(kb.searchQASet(rootQASetID));
		}
		else {
			kb.setRootQASet(getRootQASet(kb));
		}
		if (rootSolutionID != null) {
			kb.setRootSolution(kb.searchSolution(rootSolutionID));
		}
		else {
			kb.setRootSolution(getRootSolution(kb));
		}

		// appending children
		for (Element e : hierarchiemap.keySet()) {
			XMLUtil.appendChildren(kb, hierarchiemap.get(e), e);
		}

		List<QASet> qaSets = new LinkedList<QASet>();
		for (Element child : initquestionnodes) {
			listener.updateProgress(time++ / abstime, "Loading knowledge base: init QA sets");
			if (child.getNodeName().equalsIgnoreCase("QContainer")
					|| child.getNodeName().equalsIgnoreCase("Question")
					|| child.getNodeName().equalsIgnoreCase("QASet")) {
				String id = child.getAttributes().getNamedItem("ID")
						.getNodeValue();
				QASet item = (QASet) kb.search(id);
				if (item != null) {
					qaSets.add(item);
				}
			}
		}
		kb.setInitQuestions(qaSets);

		// creating rules and schemas (rules are written into basic.xml in
		// former persistence versions
		for (Element child : knowledgeslicesNodes) {
			listener.updateProgress(time++ / abstime, "Loading knowledge base: knowledge slices");
			pm.readFragment(child, kb);
		}
		listener.updateProgress(1, "Loading knowledge base");
	}

	@Override
	public void write(KnowledgeBase kb, OutputStream stream, ProgressListener listener) throws IOException {
		Document doc = Util.createEmptyDocument();
		float time = 0;
		int abstime = getEstimatedSize(kb);

		listener.updateProgress(time++ / abstime, "Saving knowledge base");

		Element father = doc.createElement("KnowledgeBase");
		doc.appendChild(father);

		father.setAttribute("id", kb.getId());
		father.setAttribute("type", "basic");
		father.setAttribute("system", "d3web");

		listener.updateProgress(time++ / abstime, "Saving knowledge base: properties");
		XMLUtil.appendInfoStore(father, kb, Autosave.basic);

		time = saveInitQuestions(father, kb, listener, time, abstime);

		listener.updateProgress(time++ / abstime, "Saving knowledge base: costs");

		Element rootQASetElement = doc.createElement("rootQASet");
		rootQASetElement.setTextContent(kb.getRootQASet().getId());
		father.appendChild(rootQASetElement);

		Element rootSolutionElement = doc.createElement("rootSolution");
		rootSolutionElement.setTextContent(kb.getRootSolution().getId());
		father.appendChild(rootSolutionElement);

		Element qContainersElement = doc.createElement("QASets");
		Map<NamedObject, Element> possibleParents = new HashMap<NamedObject, Element>();
		List<QASet> qASets = kb.getQASets();
		Collections.sort(qASets, new IDObjectComparator());
		PersistenceManager pm = PersistenceManager.getInstance();
		for (QASet qASet : qASets) {
			listener.updateProgress(time++ / abstime, "Saving knowledge base: QASets");
			Element qContainerElement = pm.writeFragment(qASet, doc);
			qContainersElement.appendChild(qContainerElement);
			possibleParents.put(qASet, qContainerElement);
		}
		father.appendChild(qContainersElement);

		// Element questionsElement = doc.createElement("Questions");
		// for (Question q: kb.getQuestions()) {
		// listener.updateProgress(time++/abstime,
		// "Saving knowledge base: questions");
		// Element questionElement = pm.writeFragment(q, doc);
		// questionsElement.appendChild(questionElement);
		// possibleParents.put(q, questionElement);
		// }
		// father.appendChild(questionsElement);

		Element diagnosisElement = doc.createElement("Diagnoses");
		for (Solution diag : kb.getSolutions()) {
			listener.updateProgress(time++ / abstime, "Saving knowledge base: diagnosis");
			Element singleDiagnosisElement = pm.writeFragment(diag, doc);
			diagnosisElement.appendChild(singleDiagnosisElement);
			possibleParents.put(diag, singleDiagnosisElement);
		}
		father.appendChild(diagnosisElement);

		// appendChildren
		for (NamedObject parent : possibleParents.keySet()) {
			XMLUtil.appendChildren(parent, possibleParents.get(parent));
		}

		Element knowledgeSlicesElement = doc.createElement("KnowledgeSlices");
		father.appendChild(knowledgeSlicesElement);

		time = saveSchemas(knowledgeSlicesElement, kb, listener, time, abstime);

		Util.writeDocumentToOutputStream(doc, stream);
	}

	@Override
	public int getEstimatedSize(KnowledgeBase kb) {
		// DCMarkups are count as 1
		int time = 1;
		time += kb.getQuestions().size();
		time += kb.getQContainers().size();
		time += kb.getSolutions().size();
		time += kb.getInitQuestions().size();
		// Schemas
		final MethodKind methodKind = PSMethodAbstraction.NUM2CHOICE_SCHEMA;
		final Class<? extends PSMethod> context = PSMethodAbstraction.class;

		Iterator<Question> questionsIter = kb.getQuestions().iterator();
		while (questionsIter.hasNext()) {
			Question question = questionsIter.next();
			KnowledgeSlice o = question.getKnowledge(context, methodKind);
			if ((o != null)) {
				time++;
			}
		}
		return time;
	}

	private static QASet getRootQASet(KnowledgeBase kb) {
		List<QASet> noParents = new ArrayList<QASet>();
		Iterator<QASet> iter = kb.getQASets().iterator();
		while (iter.hasNext()) {
			QASet fk = iter.next();
			if (fk.getParents() == null || fk.getParents().length == 0) {
				noParents.add(fk);
			}
		}
		if (noParents.size() > 1) {
			Logger.getLogger(kb.getClass().getName()).warning(
					"more than one root node in qaset tree!");
			// [HOTFIX]:aha:multiple root / orphan handling
			QASet root = null;
			iter = noParents.iterator();
			while (iter.hasNext()) {
				QASet q = iter.next();
				if (q.getId().equals("Q000")) root = q;
			}
			return root;

		}
		else if (noParents.size() < 1) {
			Logger.getLogger(kb.getClass().getName()).severe(
					"no root node in qaset tree!");
			return null;
		}
		return noParents.get(0);
	}

	private static Solution getRootSolution(KnowledgeBase kb) {
		List<Solution> result = new ArrayList<Solution>();
		Iterator<Solution> iter = kb.getSolutions().iterator();
		while (iter.hasNext()) {
			Solution d = iter.next();
			if (d.getParents() == null || d.getParents().length == 0) {
				result.add(d);
			}
		}
		if (result.size() > 1) {
			Logger.getLogger(kb.getClass().getName()).warning(
					"more than one diagnosis root node!");

			// [HOTFIX]:aha:multiple root / orphan handling
			Solution root = null;
			iter = result.iterator();
			while (iter.hasNext()) {
				Solution d = iter.next();
				if (d.getId().equals("P000")) root = d;
			}
			return root;

		}
		else if (result.size() < 1) {
			Logger.getLogger(kb.getClass().getName()).severe(
					"no root node in diagnosis tree!");
			return null;
		}
		return result.get(0);
	}
}