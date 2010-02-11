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

package de.d3web.abstraction.formula;

import de.d3web.core.session.XPSCase;

/**
 * Minimum-Term.
 * Creation date: (14.08.2000 16:41:32)
 * @author Norman Brümmer
 */
public class Min extends FormulaNumberArgumentsTerm implements FormulaNumberElement{


	private static final long serialVersionUID = -7206289688436849915L;

	/** 
	 * Creates a new FormulaTerm with null-arguments.
	 */
	public Min() {
		this(null, null);
	}
	
	
	/**
	 *	Creates a new FormulaTerm min(arg1, arg2)
	 *	@param arg1 first argument of the term
	 *	@param arg2 second argument of the term 
	 **/
	public Min(FormulaNumberElement arg1, FormulaNumberElement arg2) {
		setArg1(arg1);
		setArg2(arg2);
		setSymbol("min");
	}

	/**
	 * @return the minimum of the evaluated arguments
	 */
	public Double eval(XPSCase theCase) {
		if (super.eval(theCase) == null)
			return null;
		else
			return new Double(
				Math.min(
					getEvaluatedArg1().doubleValue(),
					getEvaluatedArg2().doubleValue()));
	}
}