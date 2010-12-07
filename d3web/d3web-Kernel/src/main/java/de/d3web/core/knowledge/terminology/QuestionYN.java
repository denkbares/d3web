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

import java.util.Arrays;

import de.d3web.core.manage.AnswerFactory;

/**
 * This is a simple extension of the QuestionChoice with only has two possible
 * values, that are restricted to a YES and NO answer. <Br>
 * This class is now immutable (only get() access) Creation date: (28.09.00
 * 16:51:21)
 * 
 * @author Joachim Baumeister
 */
public class QuestionYN extends QuestionOC {

	protected final static String YES_STRING = "Yes";
	protected final static String NO_STRING = "No";

	private final Choice answerChoiceYes;
	private final Choice answerChoiceNo;

	/**
	 * Creates a new Yes-No Question, which is a simple QuestionChoice with only
	 * instance-dependent two alternatives (YES, NO).
	 * 
	 * @see QuestionOC
	 * @see Question
	 * @see NamedObject
	 */
	public QuestionYN(String id) {
		this(id, YES_STRING, NO_STRING);
	}

	public QuestionYN(String id, String yesText, String noText) {
		super(id);
		answerChoiceYes = AnswerFactory.createAnswerYes(id + "YES", yesText);
		answerChoiceNo = AnswerFactory.createAnswerNo(id + "NO", noText);
		setAlternatives(Arrays.asList(new Choice[] {
				answerChoiceYes, answerChoiceNo }));
	}

	/**
	 * Get the YES answer choice of this QuestionYN
	 */
	public Choice getAnswerChoiceYes() {
		return answerChoiceYes;
	}

	/**
	 * Get the NO answer choice of this QuestionYN
	 */
	public Choice getAnswerChoiceNo() {
		return answerChoiceNo;
	}

	@Override
	public void addAlternative(Choice answer) {
		throw new UnsupportedOperationException("Adding choices to a QuestionYN is not allowed");
	}

	@Override
	public void addAlternative(Choice answer, int pos) {
		throw new UnsupportedOperationException("Adding choices to a QuestionYN is not allowed");
	}

}