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

/*
 * Created on 09.10.2003
 */
package de.d3web.core.knowledge.terminology;

import de.d3web.core.knowledge.KnowledgeBase;

/**
 * A Question which asks for a date.
 * 
 * @author Tobias Vogele
 */
public class QuestionDate extends Question {

	/**
	 * These constants are used for Property.QUESTION_DATE_TYPE to specify the
	 * type of input.
	 */
	public final static String TYPE_DATE = "date";
	public final static String TYPE_TIME = "time";
	public final static String TYPE_DATE_TIME = "date_time";

	/**
	 * Creates a new QuestionDate and adds it to the knowledgebase, so no manual
	 * adding of the created object to the kb is needed
	 * 
	 * @param kb {@link KnowledgeBase} in which the QuestionDate should be
	 *        inserted
	 * @param name the name of the new QuestionDate
	 */
	public QuestionDate(KnowledgeBase kb, String name) {
		super(kb, name);
	}

	/**
	 * Creates a new QuestionDate, adds it to the knowledgebase and to the
	 * parent. No manual adding of the created object to the kb is needed
	 * 
	 * @param parent the parent {@link QASet}
	 * @param name the name of the new QuestionDate
	 */
	public QuestionDate(QASet parent, String name) {
		this(parent.getKnowledgeBase(), name);
		parent.addChild(this);
	}

}
