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

package de.d3web.kernel.psMethods.parentQASet;

import java.util.Collection;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.psMethods.PSMethodAdapter;
import de.d3web.kernel.psMethods.PropagationEntry;

/**
 * This is a psmethod to mark QContainers (QASets) which are (contra-)indicated
 * due to the (contra-)indication of a parent-QContainer (QASet).
 * @author Georg
 */
public class PSMethodParentQASet extends PSMethodAdapter {

	private static PSMethodParentQASet instance = null;

	private PSMethodParentQASet() {
		super();
		setContributingToResult(false);
	}

	/**
	 * @return the one and only instance of this PSMethod (Singleton)
	 */
	public static PSMethodParentQASet getInstance() {
		if (instance == null) {
			instance = new PSMethodParentQASet();
		}
		return instance;
	}


	public DiagnosisState getState(XPSCase theCase, Diagnosis diagnosis) {
		return null;
	}

	public void propagate(XPSCase theCase, Collection<PropagationEntry> changes) {
	}
}
