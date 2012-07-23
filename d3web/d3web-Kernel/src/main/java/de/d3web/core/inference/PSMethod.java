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

package de.d3web.core.inference;

import java.util.Collection;

import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;

/**
 * Interface representing the access to problem-solving methods. Each
 * {@link Session} has a list of currently used problem-solvers. They are
 * notified, if some value (question or solution) has changed. <br>
 * Creation date: (28.08.00 17:22:54)
 * 
 * @author joba
 */
public interface PSMethod {

	/**
	 * Each PSMethod can have one or more Types, describing which kind of
	 * PSMethod it is.
	 * 
	 * Each type is described in it's own comment.
	 * 
	 * @author Markus Friedrich (denkbares GmbH)
	 * @created 21.09.2010
	 */
	public enum Type {
		/**
		 * a source psm adds facts and does not react on other facts, e.g.
		 * PSMetodUserSelected
		 */
		source,
		/**
		 * a strategic solver adds indication facts
		 */
		strategic,
		/**
		 * a psmethod of type problem adds facts, based on other propagation
		 * entries
		 */
		problem,
		/**
		 * a consumer does not add facts, it is used to get informed about
		 * propagation entries
		 */
		consumer
	}

	public final static String EXTENSIONPOINT_ID = "PSMethod";

	/**
	 * Initialization method for this PSMethod; will be called when a new
	 * {@link Session} instance is created.
	 */
	void init(Session session);

	/**
	 * Propagates the specified changes of the specified {@link Session} to this
	 * problem-solver instance.
	 * 
	 * @param session the specified {@link Session} instance
	 * @param changes the changes that should be propagated to this
	 *        problem-solver
	 */
	void propagate(Session session, Collection<PropagationEntry> changes);

	/**
	 * Merges the facts created by this problem-solver to the final value. The
	 * method will receive a non-empty set of facts created by this
	 * problem-solver to merge it to the final value. The method may rely on
	 * that every fact has a unique source. The method may also rely on that all
	 * facts are created by their own. Therefore it may cast the facts to the
	 * implementation class it uses for creating facts.
	 * 
	 * @param facts the facts to be merged
	 * 
	 * @return the merged fact
	 */
	Fact mergeFacts(Fact[] facts);

	/**
	 * Returns if the psmethod has the specified type. Note: A PSMethod can have
	 * more then one type!
	 * 
	 * @created 21.09.2010
	 * @param type Type to be checked
	 * @return true if the PSMethod has the specified type
	 */
	boolean hasType(Type type);

	/**
	 * Returns the Priority of the PSMethod
	 * 
	 * @created 01.02.2011
	 * @return
	 */
	double getPriority();
}