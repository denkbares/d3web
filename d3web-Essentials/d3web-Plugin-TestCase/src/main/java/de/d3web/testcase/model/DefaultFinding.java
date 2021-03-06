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
package de.d3web.testcase.model;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Value;

/**
 * A simple implementation for {@link Finding} (Without wrapping anything)
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 24.01.2012
 */
public class DefaultFinding implements Finding {

	private final TerminologyObject object;
	private final Value value;

	public DefaultFinding(TerminologyObject object, Value value) {
		this.object = object;
		this.value = value;
	}

	@Override
	public TerminologyObject getTerminologyObject() {
		return object;
	}

	@Override
	public Value getValue() {
		return value;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + object.getName() + " = " + value + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DefaultFinding that = (DefaultFinding) o;

		if (!object.equals(that.object)) return false;
		return value.equals(that.value);

	}

	@Override
	public int hashCode() {
		int result = object.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}
}
