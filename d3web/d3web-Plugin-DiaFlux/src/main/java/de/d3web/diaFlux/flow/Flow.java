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

package de.d3web.diaFlux.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.diaFlux.inference.Path;

/**
 * @author Reinhard Hatko
 * 
 */
public class Flow implements CaseObjectSource, KnowledgeSlice {

	private final List<IEdge> edges;
	private final List<INode> nodes;
	private final String name;
	private final String id;
	private String origin;
	private boolean autostart;

	public Flow(String id, String name, List<INode> nodes, List<IEdge> edges) {

		if (nodes == null) throw new IllegalArgumentException("nodes is null");

		if (edges == null) throw new IllegalArgumentException("edges is null");

		if (name == null) throw new IllegalArgumentException("name is null");

		this.nodes = Collections.unmodifiableList(nodes);
		this.edges = Collections.unmodifiableList(edges);
		this.name = name;
		this.id = id;
		this.autostart = false;

		checkFlow();
	}

	/**
	 * Checks the consistency of nodes and edges and sets backreference to flwo
	 * in nodes
	 */
	private void checkFlow() {
		for (INode node : nodes) {
			node.setFlow(this);
		}

	}

	@Override
	public SessionObject createCaseObject(Session session) {

		Map<INode, INodeData> nodedata = new HashMap<INode, INodeData>(getNodes().size());

		for (INode nodeDecl : getNodes()) {
			nodedata.put(nodeDecl, (INodeData) nodeDecl.createCaseObject(session));
		}

		Map<IEdge, EdgeData> edgeData = new HashMap<IEdge, EdgeData>(getEdges().size());

		for (IEdge edge : getEdges()) {
			edgeData.put(edge, (EdgeData) edge.createCaseObject(session));
		}

		return new Path(this, nodedata, edgeData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Flow other = (Flow) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public List<IEdge> getEdges() {
		return edges;
	}

	public List<INode> getNodes() {
		return nodes;
	}

	public boolean isAutostart() {
		return autostart;
	}

	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	public List<StartNode> getStartNodes() {
		return getNodesOfType(StartNode.class);
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public List<EndNode> getExitNodes() {
		return getNodesOfType(EndNode.class);
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getNodesOfType(Class<T> clazz) {
		List<T> result = new LinkedList<T>();

		for (INode node : nodes) {
			if (clazz.isAssignableFrom(node.getClass())) result.add((T) node);
		}

		return result;
	}

	@Override
	public String toString() {
		return "Flow [" + getName() + ", " + nodes.size() + " nodes, " + edges.size() + " edges] "
				+ "@" + Integer.toHexString(hashCode());
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Class<? extends PSMethod> getProblemsolverContext() {
		return FluxSolver.class;
	}

	@Override
	public boolean isUsed(Session theCase) {
		return true;
	}

	@Override
	public void remove() {
	}

}
