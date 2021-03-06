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

import de.d3web.core.knowledge.KnowledgeBase;

/**
 * Storage for Questions with predefined multiple answers (alternatives). <BR>
 * Part of the Composite design pattern (see QASet for further description)
 * 
 * @author joba, Christian Betz
 * @see QASet
 */
public class QuestionMC extends QuestionChoice {

	/**
	 * Creates a new QuestionMC and adds it to the knowledgebase, so no manual
	 * adding of the created object to the kb is needed
	 * 
	 * @param kb {@link KnowledgeBase} in which the QuestionMC should be
	 *        inserted
	 * @param name the name of the new QuestionMC
	 */
	public QuestionMC(KnowledgeBase kb, String name) {
		super(kb, name);
	}

	/**
	 * Creates a new QuestionMC, adds it to the knowledgebase and to the parent.
	 * No manual adding of the created object to the kb is needed
	 * 
	 * @param parent the parent {@link QASet}
	 * @param name the name of the new QuestionMC
	 */
	public QuestionMC(QASet parent, String name) {
		this(parent.getKnowledgeBase(), name);
		parent.addChild(this);
	}

	/**
	 * Creates a new QuestionMC, adds it to the knowledgebase and to the parent.
	 * No manual adding of the created object to the kb is needed
	 * 
	 * @param parent the parent {@link QASet}
	 * @param name the name of the new QuestionMC
	 * @param choices the choices
	 */
	public QuestionMC(QASet parent, String name, Choice... choices) {
		super(parent.getKnowledgeBase(), name, choices);
		parent.addChild(this);
	}

	/**
	 * Creates a new QuestionMC, adds it to the knowledgebase and to the parent.
	 * No manual adding of the created object to the kb is needed
	 * 
	 * @param parent the parent {@link QASet}
	 * @param name the name of the new QuestionMC
	 * @param choices the strings of choice names
	 */
	public QuestionMC(QASet parent, String name, String... choices) {
		super(parent.getKnowledgeBase(), name, choices);
		parent.addChild(this);
	}

}
