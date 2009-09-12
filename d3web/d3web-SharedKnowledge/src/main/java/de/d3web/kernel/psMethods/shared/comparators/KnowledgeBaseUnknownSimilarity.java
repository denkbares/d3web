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

package de.d3web.kernel.psMethods.shared.comparators;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.shared.PSMethodShared;
/**
 * Insert the type's description here.
 * Creation date: (19.02.2002 13:46:08)
 * @author: Norman Brümmer
 */
public class KnowledgeBaseUnknownSimilarity implements KnowledgeSlice{
	private String id = null;

	private double similarity = 0.1;
	private de.d3web.kernel.domainModel.KnowledgeBase knowledgeBase = null;



/**
 * KnowledgeBaseUnknownSimilarity constructor comment.
 */
public KnowledgeBaseUnknownSimilarity() {
	super();
}



/**
 * Provide a unique id for each part of knowledge.
 * Creation date: (19.02.2002 13:49:21)
 * @return java.lang.String
 */
public java.lang.String getId() {
	return id;
}



/**
 * Insert the method's description here.
 * Creation date: (19.02.2002 13:52:41)
 * @return de.d3web.kernel.domainModel.KnowledgeBase
 */
public de.d3web.kernel.domainModel.KnowledgeBase getKnowledgeBase() {
	return knowledgeBase;
}



/**
 * Returns the class of the PSMethod in which this
 * KnowledgeSlice makes sense.
 * Creation date: (19.02.2002 13:46:55)
 * @return java.lang.Class PSMethod class
 */
public java.lang.Class getProblemsolverContext() {
	return de.d3web.kernel.psMethods.shared.PSMethodShared.class;
}



/**
 * Insert the method's description here.
 * Creation date: (19.02.2002 13:50:07)
 * @return double
 */
public double getSimilarity() {
	return similarity;
}



/**
 * Has this knowledge already been used? (e.g. did a rule fire?)
 */
public boolean isUsed(de.d3web.kernel.XPSCase theCase) {
	return true;
}



/**
 * Provide a unique id for each part of knowledge.
 * Creation date: (19.02.2002 13:46:55)
 * @return java.lang.String
 */
public void setId(String _id) {
	id = _id;
}



/**
 * Insert the method's description here.
 * Creation date: (19.02.2002 13:52:41)
 * @param newKnowledgeBase de.d3web.kernel.domainModel.KnowledgeBase
 */
public void setKnowledgeBase(
	de.d3web.kernel.domainModel.KnowledgeBase newKnowledgeBase) {

	knowledgeBase = newKnowledgeBase;

	if (knowledgeBase != null) {
		knowledgeBase.addKnowledge(
			getProblemsolverContext(),
			this,
			PSMethodShared.SHARED_SIMILARITY);

	}
}



/**
 * Insert the method's description here.
 * Creation date: (19.02.2002 13:50:07)
 * @param newSimilarity double
 */
public void setSimilarity(double newSimilarity) {
	similarity = newSimilarity;
}



public void remove() {
	if (knowledgeBase != null) {
		knowledgeBase.removeKnowledge(
			getProblemsolverContext(),
			this,
			PSMethodShared.SHARED_SIMILARITY);

	}
}
}