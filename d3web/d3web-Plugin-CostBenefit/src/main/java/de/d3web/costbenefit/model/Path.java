/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.costbenefit.model;

import java.util.List;

import de.d3web.core.knowledge.terminology.QContainer;

/**
 * Represents a path
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 10.08.2011
 */
public interface Path {

	/**
	 * Returns the {@link List} of all {@link QContainer}s of the path in the
	 * correct (forward) order.
	 * 
	 * @return a list of questionnaires, representing this path
	 */
	List<QContainer> getPath();

	/**
	 * Returns the total costs of this path. This may differ from the static
	 * costs of the questionnaires accessed by {@link #getPath()}, if a specific
	 * cost function is used.
	 * 
	 * @return costs of the path
	 */
	double getCosts();

	/**
	 * Checks if the path is empty.
	 * 
	 * @return if the path is empty
	 */
	public boolean isEmpty();
}
