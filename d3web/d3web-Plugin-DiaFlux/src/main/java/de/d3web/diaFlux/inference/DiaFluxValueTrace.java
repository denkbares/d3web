/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.d3web.diaFlux.inference;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.inference.PropagationListener;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionObjectSource;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;


/**
 * This class traces the values the terminology objects of nodes had, when they
 * were snapshotted.
 * 
 * @author Reinhard Hatko
 * @created 02.04.2012
 */
public class DiaFluxValueTrace implements PropagationListener, SessionObject, SessionObjectSource<DiaFluxValueTrace> {

	private final Session session;
	private final Map<Node, Value> tracedValues = new HashMap<Node, Value>();

	public static final SessionObjectSource<DiaFluxValueTrace> SOURCE = new SessionObjectSource<DiaFluxValueTrace>() {

		@Override
		public DiaFluxValueTrace createSessionObject(Session session) {
			return new DiaFluxValueTrace(session);
		}
	};

	public DiaFluxValueTrace(Session session) {
		this.session = session;
		session.getPropagationManager().addListener(this);
	}

	public Session getSession() {
		return session;
	}
	
	@Override
	public DiaFluxValueTrace createSessionObject(Session session) {
		return new DiaFluxValueTrace(session);
	}

	@Override
	public void propagationStarted(Collection<PropagationEntry> entries) {

	}

	@Override
	public void postPropagationStarted(Collection<PropagationEntry> entries) {
		DiaFluxCaseObject caseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		// we clear the current trace if the last snapshot is out-dated.
		// we do not if the propagation time is still the same (so we are in the
		// same propagation cycle from the users perspective)
		Date lastTime = caseObject.getLatestSnaphotTime();
		long thisTime = session.getPropagationManager().getPropagationTime();
		if (lastTime == null || lastTime.getTime() < thisTime) {
			this.tracedValues.clear();
		}

		// Do not trace, if no snapshots have been entered
		Collection<SnapshotNode> enteredSnapshots = caseObject.getActivatedSnapshots(session);
		if (enteredSnapshots.isEmpty()) {
			return;
		}

		List<FlowRun> runs = caseObject.getRuns();
		for (FlowRun flowRun : runs) {
			for (Node node : flowRun.getActiveNodes()) {
				TerminologyObject termObject = getTermObject(node);
				if (termObject != null) {
					Value value = session.getBlackboard().getValue((ValueObject) termObject);
					tracedValues.put(node, value);
				}

			}
		}

	}

	private static TerminologyObject getTermObject(Node node) {
		if (node instanceof ActionNode) {
			PSAction action = ((ActionNode) node).getAction();
			List<? extends TerminologyObject> objects = action.getBackwardObjects();
			if (!objects.isEmpty()) {
				// There should be only 1 backward object
				return objects.get(0);
			}
		}
		return null;
	}


	@Override
	public void propagationFinished(Collection<PropagationEntry> entries) {

	}

	public Map<Node, Value> getTracedValues() {
		return Collections.unmodifiableMap(tracedValues);
	}

	/**
	 * Returns the value the associated terminology object of the node had at
	 * the time the snapshot was taken, or null, if no value for this node has
	 * been recorded.
	 * 
	 * @created 04.04.2012
	 * @param node
	 * @return s
	 */
	public Value getValue(Node node) {
		return tracedValues.get(node);
	}

	public String getValueString(Node node) {
		TerminologyObject termObject = getTermObject(node);
		if (termObject == null) {
			// Node does not reference TermObject
			return null;
		}

		Value tracedValue = tracedValues.get(node);
		Value value = session.getBlackboard().getValue((ValueObject) termObject);
		if (tracedValue == null) {
			// Node does reference TermObject, but was not active during
			// snapshot
			return termObject.getName() + " = '" + value.toString() + "'";
		}
		else {
			// Node was active...
			String tooltip = termObject.getName() + " = '" + tracedValue.toString() + "'";
			// add current value, if not equal to value during snapshot
			if (!tracedValue.equals(value)) {
				tooltip += " (Current: '" + value.toString() + "')";
			}

			return tooltip;

		}

	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == getClass();
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
