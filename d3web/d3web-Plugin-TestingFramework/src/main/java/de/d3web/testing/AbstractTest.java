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
 * Abstract implementation of a test that easily enables basic argument
 * checking.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 30.05.2012
 */
public abstract class AbstractTest<T> implements Test<T> {

	private final List<TestParameter> argParameters = new ArrayList<TestParameter>();
	private final List<TestParameter> ignoreParameters = new ArrayList<TestParameter>();

	@Override
	public List<TestParameter> getParameterSpecification() {
		return Collections.unmodifiableList(argParameters);
	}

	@Override
	public List<TestParameter> getIgnoreSpecification() {
		return Collections.unmodifiableList(ignoreParameters);
	}

	protected void addParameter(String name, TestParameter.Type type, TestParameter.Mode mode, String description) {
		argParameters.add(new TestParameter(name, type, mode, description));
	}

	protected void addIgnoreParameter(String name, TestParameter.Type type, TestParameter.Mode mode, String description) {
		ignoreParameters.add(new TestParameter(name, type, mode, description));
	}

	@Override
	public ArgsCheckResult checkArgs(String[] args) {
		return checkParameter(this, args, argParameters);
	}

	@Override
	public ArgsCheckResult checkIgnore(String[] args) {
		return checkParameter(this, args, ignoreParameters);
	}

	private static ArgsCheckResult checkParameter(Test<?> test, String[] args, List<TestParameter> parameters) {
		ArgsCheckResult r = new ArgsCheckResult(args);
		int minParamCount = getNumberOfMandatoryParameters(parameters);
		if (args.length < minParamCount) {
			r.setError(0,
					"Not enough arguments for execution of test '"
							+ test.getName()
							+ "'. Expected " + parameters.size() + " argument"
							+ (parameters.size() == 1 ? "" : "s") + ", but found "
							+ args.length + ".");
			return r;
		}

		for (int i = 0; i < args.length; i++) {

			// check whether array might be longer than registered number of
			// arguments
			if (i >= parameters.size()) {
				r.setError(args.length - 1,
						"Too many arguments passend for test '" + test.getName()
								+ "': Expected " + parameters.size() + " argument"
								+ (parameters.size() == 1 ? "" : "s") + ", but found "
								+ args.length + ".");
				return r;
			}

			// get parameter no. i and check arg value
			TestParameter parameter = parameters.get(i);
			boolean ok = parameter.checkParameterValue(args[i]);
			if (!ok) {
				r.setError(i,
						"Argument passend as '" + parameter.getName()
								+ "' is not a valid " + parameter.getType().toString()
								+ " argument: \"" + args[i] + "\".");
			}
		}

		return r;
	}

	@Override
	public String getName() {
		return TestManager.getTestName(this);
	}

	private static int getNumberOfMandatoryParameters(List<TestParameter> parameters) {
		int count = 0;
		for (TestParameter p : parameters) {
			if (p.getMode().equals(TestParameter.Mode.Mandatory)) {
				count++;
			}
		}
		return count;
	}

}
