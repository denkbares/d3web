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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.diaFlux.inference.IPath;

/**
 * 
 * @author Reinhard Hatko Created on: 04.11.2009
 */
public class FlowSet implements KnowledgeSlice, CaseObjectSource, Iterable<Flow> {

	// hardcoded it ATM due to comment in Interface
	private final String id = "Flowset";
	private final Map<String, Flow> map;

	public FlowSet() {
		this.map = new HashMap<String, Flow>();
	}

	@Override
	public SessionObject createCaseObject(Session session) {

		Map<Flow, IPath> flowdatas = new HashMap<Flow, IPath>();

		for (Flow flow : map.values()) {
			IPath flowdata = (IPath) flow.createCaseObject(session);
			flowdatas.put(flow, flowdata);
		}

		return new DiaFluxCaseObject(this, flowdatas);
	}

	public boolean contains(String id) {
		return map.containsKey(id);
	}

	public boolean containsValue(Flow flow) {
		return map.containsValue(flow);
	}

	public Flow get(String id) {
		return map.get(id);
	}

	public Flow getByName(String name) {

		for (Flow flow : getFlows()) {
			if (flow.getName().equals(name)) return flow;
		}

		return null;
	}

	public Set<String> getIDs() {
		return map.keySet();
	}

	public Flow put(Flow flow) {
		return map.put(flow.getId(), flow);
	}

	public Flow remove(String id) {
		return map.remove(id);
	}

	public int size() {
		return map.size();
	}

	public Collection<Flow> getFlows() {
		return map.values();
	}

	@Override
	public Iterator<Flow> iterator() {
		return getFlows().iterator();
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
		// TODO
		return true;
	}

	@Override
	public void remove() {
		// TODO
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		FlowSet other = (FlowSet) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (map == null) {
			if (other.map != null) return false;
		}
		else if (!map.equals(other.map)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "FlowSet [id=" + id + ", map=" + map + "]";
	}

}
