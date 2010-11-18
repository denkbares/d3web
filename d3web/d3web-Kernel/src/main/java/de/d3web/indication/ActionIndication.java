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

package de.d3web.indication;

import java.util.ArrayList;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.Indication;
import de.d3web.core.knowledge.Indication.State;
import de.d3web.core.knowledge.terminology.QASet;

/**
 * ActionIndication represents the general indication of a QASet. Creation date:
 * (21.02.2002 13:14:06)
 * 
 * @author Christian Betz
 */
public class ActionIndication extends ActionNextQASet {


	private static final Indication INDICATION = new Indication(State.INDICATED);

	@Override
	protected Indication getIndication() {
		return INDICATION;
	}

	@Override
	public PSAction copy() {
		ActionIndication a = new ActionIndication();
		a.setQASets(new ArrayList<QASet>(getQASets()));
		return a;
	}

}