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

package de.d3web.kernel.dialogControl;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.Rule;
import de.d3web.kernel.psMethods.PSMethod;
/**
 * The cases view to the dialog: it only needs to add qasets.<br/>
 * <b>Attention:</b> This needs to be refactored in order to represent the new ActionNextQASet-Mechanism.
 * Also, userIndication now should work by using PSMethodUserSelected as a context.
 * So the next one is to respect these comments ;) <br>
 *
 * Creation date: (21.02.2002 15:36:04)
 * @author Christian Betz
 */
public interface QASetManager {

	/**
	 * @return the main QASet agenda
	 */
	public List getQASetQueue();
	
	/**
	 * @return List of all QContainers, that have been (partially) processed during 
	 * answering the case (system-indicated and user-selected ones)
	 */
	public List getProcessedContainers();

	/**
	 * @return true iff there is a valid QASet the manager can move forward to
	 */
	public boolean hasNextQASet();

	/**
	 * @see PSMethod#propagate(XPSCase, NamedObject, Object[])
	 */
	public void propagate(NamedObject no, Rule rule, PSMethod psm);

}