/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 30.05.2012
 */
public abstract class AbstractTest<T> implements de.d3web.testing.Test<T> {
	
	private final List<TestParameter> parameters = new ArrayList<TestParameter>();
	
	
	@Override
	public List<TestParameter> getParameterSpecification() {
		return Collections.unmodifiableList(parameters);
	}
	
	protected void addParameter(String name, TestParameter.Type type, TestParameter.Mode mode, String description) {
		parameters.add(new TestParameter(name,type,mode, description));
	}
	
	protected int getNumberOfMandatoryParameters() {
		int count = 0;
		for (TestParameter p : parameters) {
			if(p.getMode().equals(TestParameter.Mode.Mandatory)) {
				count ++;
			}
		}
		return count;
	}

	@Override
	public ArgsCheckResult checkArgs(String[] args) {

		ArgsCheckResult r = new ArgsCheckResult(args);
		if(args.length < getNumberOfMandatoryParameters()) {
			r.setError(0,
				"Not enough arguments for execution of test '" + this.getClass().getSimpleName()
						+ "'. Expected number of arguments: "
						+ parameters.size() + " - found: " + args.length);
			return r;
		}
		
		ArgsCheckResult result = new ArgsCheckResult(args);
		boolean problemFound = false;
		for (int i =  0; i < args.length; i++) {
			TestParameter parameter = parameters.get(i);
			boolean ok = parameter.checkParameterValue(args[i]);
			if(!ok) {
				problemFound = true;
			result.setError(i,
					"Argument passend as '" + parameter.getName()
							+ "' is not a valid "+ parameter.getType().toString() + " argument: \"" + args[i]+"\"");
			}
		}
		if(problemFound)return result;
		
		
		if (args.length > parameters.size()) {
			ArgsCheckResult res = new ArgsCheckResult(args);
			res.setWarning(args.length - 1,
					"Too many arguments passend for test '" + this.getClass().getSimpleName()
							+ "': Maximum expected number of arguments: "
							+ parameters.size() + " - found: " + args.length);
			return res;
		}
		
		
		return r;
	}
	
}
