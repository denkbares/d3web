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

package de.d3web.kernel.domainModel.formula;

import java.util.Collection;
/**
 * This interface discribes a general FormulaElement	
 * Used Pattern: Composite
 * <p>
 * NOTE: All implementing classes should contain a default-constructor.
 *
 * Creation date: (14.08.2000 15:40:15)
 * @author Norman Brümmer
 */
public interface FormulaElement extends java.io.Serializable {


	/**
	 * @return a list of FormulaElements, if it is a complex type. Otherwise e.g. a Double
	 */
	public Collection<Object> getTerminalObjects();
}