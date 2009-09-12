/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.persistence.xml.loader;

import java.util.Hashtable;
import java.util.Iterator;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.persistence.xml.loader.KBLoader;

/**
 * This is a dummy KBLoader for ConditionFactory. It will only be used for
 * searching Answers and Questions from the given KnowledgeBase
 * 
 * @author bruemmer
 */
public class KBLoaderDummy extends KBLoader {

	private Hashtable answerChoiceById = null;

	private boolean answersAlreadyUpdated = false;

	public KBLoaderDummy(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	/**
	 * Updates the "answers"-Hashtable by adding all answers which are contained
	 * in "kb" but not in "answers".
	 * 
	 * @param answers
	 *            Hashtable to update
	 * @param kb
	 *            KnowledgeBase
	 * @return Hashtable (updated "answers")
	 */
	private Hashtable updateAnswersHashtable(KnowledgeBase kb) {
		answerChoiceById = new Hashtable();
		Iterator iter = kb.getQuestions().iterator();
		while (iter.hasNext()) {
			Question q = (Question) iter.next();
			if (q instanceof QuestionChoice) {
				Iterator answerIter = ((QuestionChoice) q).getAllAlternatives().iterator();
				while (answerIter.hasNext()) {
					Answer answer = (Answer) answerIter.next();
					if ((q instanceof QuestionChoice) && (answer instanceof AnswerChoice)) {
						answerChoiceById.put(answer.getId(), answer);
					}
				}
			}
		}
		return answerChoiceById;
	}

	public Object search(String id) {
		Object ret = null;

		ret = searchAnswer(id);
		if (ret == null) {
			ret = searchKnowledgeSlice(id);
		}
		if (ret == null) {
			ret = searchQContainer(id);
		}
		if (ret == null) {
			ret = searchQuestion(id);
		}
		if (ret == null) {
			ret = searchDiagnosis(id);
		}

		return ret;
	}

	/**
	 * searches for an answer matching the given id
	 */
	public Answer searchAnswer(String id) {
		id = clean(id);
		if (!answersAlreadyUpdated) {
			updateAnswersHashtable(knowledgeBase);
			answersAlreadyUpdated = true;
		}
		return (Answer) answerChoiceById.get(id);
	}

	/**
	 * searches for a diagnosis matching the given id
	 */
	public Diagnosis searchDiagnosis(String id) {
		id = clean(id);
		return knowledgeBase.searchDiagnosis(id);
	}

	/**
	 * searches for a diagnosis matching the given id
	 */
	public KnowledgeSlice searchKnowledgeSlice(String id) {
		id = clean(id);
		Iterator iter = knowledgeBase.getAllKnowledgeSlices().iterator();
		while (iter.hasNext()) {
			KnowledgeSlice slice = (KnowledgeSlice) iter.next();
			if (slice.getId().equals(id)) {
				return slice;
			}
		}
		return null;
	}

	/**
	 * searches for a container matching the given id
	 */
	public QContainer searchQContainer(String id) {
		id = clean(id);
		return knowledgeBase.searchQContainers(id);
	}

	/**
	 * searches for a question matching the given id
	 */
	public Question searchQuestion(String id) {
		id = clean(id);

		return knowledgeBase.searchQuestions(id);
	}

}
