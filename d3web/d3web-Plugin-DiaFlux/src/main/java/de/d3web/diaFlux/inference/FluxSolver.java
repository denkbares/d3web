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

package de.d3web.diaFlux.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PostHookablePSMethod;
import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.Facts;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.EdgeMap;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.INodeData;
import de.d3web.diaFlux.flow.ISupport;
import de.d3web.diaFlux.flow.NodeList;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.diaFlux.flow.ValidSupport;

/**
 * 
 * @author Reinhard Hatko
 * @created: 10.09.2009
 * 
 */
public class FluxSolver implements PostHookablePSMethod {

	public static final MethodKind DIAFLUX = new MethodKind("DIAFLUX");
	public static final MethodKind NODE_REGISTRY = new MethodKind("NodeRegistry");

	public static final Level LEVEL = Level.OFF;

	public FluxSolver() {
		Logger.getLogger(getClass().getName()).setLevel(LEVEL);
	}

	@Override
	public void init(Session session) {

		if (!DiaFluxUtils.isFlowCase(session)) return;

		Logger.getLogger(FluxSolver.class.getName()).info(
				"Initing FluxSolver with case: " + session);

		for (Flow flow : DiaFluxUtils.getFlowSet(session)) {
			if (flow.isAutostart()) {
				for (StartNode startNode : flow.getStartNodes()) {

					activate(session, startNode, new ValidSupport());
				}

			}

		}

	}

	public static void activate(Session session, StartNode startNode, ISupport support) {

		Logger.getLogger(FluxSolver.class.getName()).info(
				"Activating startnode '" + startNode.getName() + "' of flow '"
						+ startNode.getFlow().getName() + "'.");

		DiaFluxUtils.getPath(startNode, session).activate(startNode, support, session);

	}

	public static boolean removeSupport(Session session, INode node, ISupport support) {
		INodeData nodeData = DiaFluxUtils.getNodeData(node, session);

		Logger.getLogger(FluxSolver.class.getName()).info(
				"Removing support '" + support + "' from node '" + node + "'.");

		boolean removed = nodeData.removeSupport(session, support);

		if (!removed) {
			Logger.getLogger(FluxSolver.class.getName()).severe(
					"Could not remove support '" + support + "' from node '" + node + "'.");

		}

		return removed;
	}

	public static boolean addSupport(Session session, INode node, ISupport support) {
		Logger.getLogger(FluxSolver.class.getName()).info(
				"Adding support '" + support + "' to node '" + node + "'.");

		INodeData nodeData = DiaFluxUtils.getNodeData(node, session);
		boolean added = nodeData.addSupport(session, support);

		if (!added) {
			Logger.getLogger(FluxSolver.class.getName()).severe(
					"Could not add support '" + support + "' to node '" + node + "'.");

		}

		return added;
	}

	@Override
	public void propagate(Session session, Collection<PropagationEntry> changes) {

		if (!DiaFluxUtils.isFlowCase(session)) return;

		Logger.getLogger(FluxSolver.class.getName()).info(
				"Start propagating: " + changes);

		try {
			session.getPropagationManager().openPropagation();

			for (PropagationEntry propagationEntry : changes) {

				// strategic entries do not matter so far...
				if (propagationEntry.isStrategic()) continue;

				TerminologyObject object = propagationEntry.getObject();
				EdgeMap slice = (EdgeMap) ((NamedObject) object).getKnowledge(FluxSolver.class,
						MethodKind.FORWARD);

				// TO does not occur in any edge
				if (slice == null) continue;

				// iterate over all edges that contain the changed TO
				for (IEdge edge : slice.getEdges()) {

					INode node = edge.getStartNode();
					IPath path = DiaFluxUtils.getPath(node, session);

					// if the node the edge starts at is supported
					if (path.getNodeData(node).isSupported()) {

						// ...propagate starting at this node
						path.propagate(session, node);
					}

				}

			}

			for (PropagationEntry propagationEntry : changes) {

				TerminologyObject object = propagationEntry.getObject();
				NodeList knowledge = (NodeList) ((NamedObject) object).getKnowledge(
						FluxSolver.class,
						MethodKind.BACKWARD);

				if (knowledge == null) continue;

				for (INode node : knowledge) {

					IPath path = DiaFluxUtils.getPath(node, session);

					if (path.getNodeData(node).isSupported()) {

						if (node instanceof ActionNode) {

							PSAction action = ((ActionNode) node).getAction();

							// TODO quick and dirty hack to reevaluate Formulas
							if (action.getClass().getName().equals(
									"cc.d3web.expression.eval.ExpressionAction")) {
								activate(session, node);
							}

						}

						// ...propagate starting at this node
						path.propagate(session, node);
					}

				}

			}

			Logger.getLogger(FluxSolver.class.getName()).info(
					"Finished propagating.");

		}
		finally {
			session.getPropagationManager().commitPropagation();
		}

	}

	@Override
	public void postPropagate(Session session) {

		if (!DiaFluxUtils.isFlowCase(session)) return;

		DiaFluxCaseObject caseObject = DiaFluxUtils.getDiaFluxCaseObject(session);

		// the list for all taken snapshots in this postpropagation
		List<SnapshotNode> takenSnapshots = new ArrayList<SnapshotNode>();

		// the list of current snapshots to take
		List<SnapshotNode> currentSnapshots = new ArrayList<SnapshotNode>();

		do {

			// do not iterate over returned list, as new nodes can be inserted
			// while flowing
			currentSnapshots.addAll(caseObject.getRegisteredSnapshots());
			currentSnapshots.removeAll(takenSnapshots);

			// At first:
			for (SnapshotNode node : currentSnapshots) {
				// take the snapshot at each registered SSN
				takeSnapshot(session, node);

			}

			// clear the current Snapshots
			// new ones can be reached during the propagation
			// starting from the SSNs
			caseObject.clearRegisteredSnapshots();

			// List<SnapshotNode> nodesToFlowFrom = new
			// ArrayList<SnapshotNode>(currentSnapshots);

			// do not flow from the nodes that have already been reached once in
			// this
			// postpropagation
			// nodesToFlowFrom.removeAll(takenSnapshots);

			// Then:
			for (SnapshotNode node : currentSnapshots) {
				// continue flowing from the SSN, as it has been
				// cancelled at this point in the previous propagation

				// TODO alternative: flow from nodes after SSN
				// but then, only conditionless edges can start at SSNs

				// for (IEdge edge : node.getOutgoingEdges()) {
				// INode nodeAfterSnapshot = edge.getEndNode();
				//
				// FluxSolver.addSupport(session, nodeAfterSnapshot, new
				// ValidSupport());
				//
				// DiaFluxUtils.getPath(nodeAfterSnapshot,
				// session).propagate(session,
				// nodeAfterSnapshot);
				// }

				// FluxSolver.addSupport(session, node, new ValidSupport());

				DiaFluxUtils.getPath(node, session).propagate(session, node);

			}

			// remember the SS that have been taken
			takenSnapshots.addAll(currentSnapshots);
			currentSnapshots.clear();

			// It's important to separate the snapshot taking and the flowing,
			// when taking more than 1 SS:
			// avoid to connect paths that are already snapshotted and are then
			// continued to those that have still to be snapshotted. This would
			// severely impact the snapshotting...

		} while (!takenSnapshots.containsAll(currentSnapshots));

	}

	public static void deactivate(Session session, INode node) {
		Logger.getLogger(FluxSolver.class.getName()).info("Deactivating node: " + node);

		node.deactivate(session);
	}

	public static void activate(Session session, INode node) {
		Logger.getLogger(FluxSolver.class.getName()).info("Activating node: " + node);

		node.activate(session);
	}

	public static void takeSnapshot(Session session, SnapshotNode node) {

		IPath path = DiaFluxUtils.getPath(node, session);

		Logger.getLogger(FluxSolver.class.getName()).info("Start taking snapshot on path: " + path);

		ArrayList<INode> nodes = new ArrayList<INode>();
		path.takeSnapshot(session, node, node, nodes);

		Logger.getLogger(FluxSolver.class.getName()).info(
				"Finished taking snapshot on over " + nodes.size() + " nodes.");

	}

	@Override
	public Fact mergeFacts(Fact[] facts) {

		return Facts.getLatestFact(facts);
	}

	@Override
	public boolean hasType(Type type) {
		return type == Type.strategic || type == Type.problem;
	}

}
