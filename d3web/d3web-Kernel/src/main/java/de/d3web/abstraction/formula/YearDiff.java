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
 * 
 * /* Created on 10.10.2003
 */
package de.d3web.abstraction.formula;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Session;

/**
 * A Term which has 2 Dates as Arguments.
 * 
 * @author Tobias vogele
 */
public class YearDiff implements FormulaNumberElement {

	private FormulaDateElement arg1;
	private FormulaDateElement arg2;

	/**
	 * Here the evaluation value will be stored while trying to evaluate the
	 * term. It warrents, that the evaluation will be done only once.
	 */
	private Date evaluatedArg1 = null;

	/**
	 * Look above.
	 */
	private Date evaluatedArg2 = null;

	/**
	 * Creates a new term with its two arguments
	 * 
	 * @param arg1 first argument
	 * @param arg2 second argument
	 */
	public YearDiff(FormulaDateElement arg1, FormulaDateElement arg2) {
		setArg1(arg1);
		setArg2(arg2);
	}

	/**
	 * Evaluates the arguments.
	 * 
	 * @return true, if no argument is null.
	 */
	private boolean evaluateArguments(Session session) {
		if (getArg1() == null || getArg2() == null) {
			return false;
		}
		evaluatedArg1 = getArg1().eval(session);
		evaluatedArg2 = getArg2().eval(session);

		return evaluatedArg1 != null && evaluatedArg2 != null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.d3web.kernel.domainModel.formula.FormulaElement#getTerminalObjects()
	 */
	@Override
	public Collection<? extends TerminologyObject> getTerminalObjects() {
		Collection<? extends TerminologyObject> c1 = getArg1().getTerminalObjects();
		Collection<? extends TerminologyObject> c2 = getArg2().getTerminalObjects();
		Collection<TerminologyObject> both = new ArrayList<TerminologyObject>(c1.size() + c2.size());
		both.addAll(c1);
		both.addAll(c2);
		return both;
	}

	/**
	 * @return
	 */
	public FormulaDateElement getArg1() {
		return arg1;
	}

	/**
	 * @param arg1
	 */
	public void setArg1(FormulaDateElement arg1) {
		this.arg1 = arg1;
	}

	public FormulaDateElement getArg2() {
		return arg2;
	}

	/**
	 * @param arg2
	 */
	public void setArg2(FormulaDateElement arg2) {
		this.arg2 = arg2;
	}

	public String getSymbol() {
		return "YEARDIFF";
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer();
		buffy.append("(");
		if (getArg1() != null) {
			buffy.append(getArg1());
		}
		buffy.append(" ").append(getSymbol()).append(" ");
		// if (getArg2() != null) { //Commented out to satisfy SONAR
		// }
		buffy.append(")");
		return buffy.toString();
	}

	@Override
	public Double eval(Session session) {
		evaluateArguments(session);
		GregorianCalendar cal1 = new GregorianCalendar();
		cal1.setTime(evaluatedArg1);
		GregorianCalendar cal2 = new GregorianCalendar();
		cal2.setTime(evaluatedArg2);

		double yearDiff = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);

		if (cal1.get(Calendar.MONTH) < cal2.get(Calendar.MONTH)) {
			yearDiff--;
		}
		else if ((cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
				&& (cal1.get(Calendar.DAY_OF_MONTH) < cal2.get(Calendar.DAY_OF_MONTH))) {
			yearDiff--;
		}

		return new Double(yearDiff);
	}
}
