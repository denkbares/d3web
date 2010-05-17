/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.core.knowledge.terminology;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.abstraction.inference.PSMethodQuestionSetter;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.info.Num2ChoiceSchema;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.CaseQuestionChoice;
import de.d3web.core.session.values.Choice;
import de.d3web.core.utilities.Tester;
import de.d3web.core.utilities.Utils;

/**
 * Storage for Questions with predefined answers (alternatives). Abstract
 * because you can choose from multiple/single choices (answers).<BR>
 * Part of the Composite design pattern (see QASet for further description)
 * 
 * @author joba, Christian Betz
 * @see QASet
 */
public abstract class QuestionChoice extends Question {

	protected List<Choice> alternatives;

	public QuestionChoice(String id) {
		super(id);
		this.setAlternatives(new LinkedList<Choice>());
	}

	/**
	 * Gives you all the answers (alternatives) and does not care about any
	 * rules which could possibly suppress an answer.
	 * 
	 * @param theCase currentCase
	 * @return a List of all alternatives that are not suppressed by any
	 *         RuleSuppress
	 **/
	public List<Choice> getAllAlternatives() {
		return alternatives;
	}

	private Choice findAlternative(List<? extends Answer> alternativesArg, final String id) {
		return (Choice) Utils.findIf(alternativesArg, new Tester() {

			public boolean test(Object testObj) {
				if ((testObj instanceof Choice)
						&& (((Choice) testObj).getId().equalsIgnoreCase(id))) {
					return true;
				}
				else {
					return false;
				}
			}
		});
	}

	/**
	 * if theCase == null, find the alternative in all alternatives, else find
	 * the alternative in all currently (depend on the case) available
	 * alternatives
	 * 
	 * @return Answer (either instanceof AnswerChoice or AnswerUnknown)
	 **/
	public Answer getAnswer(Session theCase, String id) {
		if (id == null) return null;
		if (theCase == null) return findAlternative(alternatives, id);
		else return findAlternative(getAlternatives(theCase), id);
	}

	public Choice findChoice(String choiceID) {
		if (choiceID == null) {
			return null;
		}
		else {
			return findAlternative(alternatives, choiceID);
		}
	}

	/**
	 * Gives you only the possible answers (alternatives) which are not
	 * suppressed by any rule.
	 * 
	 * @param theCase currentCase
	 * @return a Vector of all alternatives that are not suppressed by any
	 *         RuleSuppress
	 **/
	public List<Answer> getAlternatives(Session theCase) {
		CaseQuestionChoice caseQ = (CaseQuestionChoice) theCase.getCaseObject(this);
		List<Answer> suppVec = caseQ.getMergedSuppressAlternatives();
		List<Answer> result = new LinkedList<Answer>();
		Iterator<Choice> e = alternatives.iterator();
		while (e.hasNext()) {
			Answer elem = e.next();
			if (!suppVec.contains(elem)) result.add(elem);
		}
		return result;
	}

	/**
	 * sets the answer alternatives from which a user or rule can choose one or
	 * more to answer this question.
	 */
	public void setAlternatives(List<Choice> alternatives) {
		if (alternatives != null) {
			this.alternatives = alternatives;
			Iterator<Choice> iter = this.alternatives.iterator();
			while (iter.hasNext()) {
				iter.next().setQuestion(this);
			}
		}
		else setAlternatives(new LinkedList<Choice>());

	}

	public void addAlternative(Choice answer) {
		if ((answer != null) && (!getAllAlternatives().contains(answer))) {
			alternatives.add(answer);
			answer.setQuestion(this);
		}
	}

	@Override
	public String toString() {
		String res = super.toString();
		return res;
	}

	public String verbalizeWithoutValue(Session theCase) {
		String res = "\n " + super.toString();
		Iterator<Answer> iter = getAlternatives(theCase).iterator();
		while (iter.hasNext())
			res += "\n  " + iter.next().toString();
		return res;
	}

	/**
	 * @return the Num2ChoiceSchema that has been set to this question, null, if
	 *         no such schema exists.
	 */
	public Num2ChoiceSchema getSchemaForQuestion() {
		KnowledgeSlice schemaCol =
				getKnowledge(PSMethodQuestionSetter.class, PSMethodQuestionSetter.NUM2CHOICE_SCHEMA);
		if (schemaCol != null) {
			return (Num2ChoiceSchema) schemaCol;
		}
		else {
			return null;
		}
	}
}