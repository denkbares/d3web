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

package de.d3web.core.inference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Session;

/**
 * @author jochen
 * 
 *         abstract PSMethod checking for rules relevant for the corresponding
 *         problem-solver-subclass
 * 
 */
public abstract class PSMethodRulebased extends PSMethodAdapter {

	private static final Map<Class<? extends PSMethodRulebased>, KnowledgeKind<RuleSet>> forwardKinds = new HashMap<>();
	private static final Map<Class<? extends PSMethodRulebased>, KnowledgeKind<RuleSet>> backwardKinds = new HashMap<>();

	public PSMethodRulebased(KnowledgeKind<RuleSet> forward, KnowledgeKind<RuleSet> backward) {
		forwardKinds.put(getClass(), forward);
		backwardKinds.put(getClass(), backward);
	}

	public static KnowledgeKind<RuleSet> getForwardKind(Class<? extends PSMethodRulebased> clazz) {
		KnowledgeKind<RuleSet> forward = forwardKinds.get(clazz);
		if (forward == null) {
			throw new NoSuchElementException("Forward KnowledgeKind not present");
		}
		return forward;
	}

	public static KnowledgeKind<RuleSet> getBackwardKind(Class<? extends PSMethodRulebased> clazz) {
		KnowledgeKind<RuleSet> backward = backwardKinds.get(clazz);
		if (backward == null) {
			throw new NoSuchElementException("Backward KnowledgeKind not present");
		}
		return backward;
	}

	/**
	 * Check if TerminologyObject has rules connected with this problem-solver
	 * and check them, if available
	 */
	protected final void propagate(Session session, TerminologyObject nob) {
		if (nob != null) {
			KnowledgeSlice slices = nob.getKnowledgeStore().getKnowledge(
					getForwardKind(this.getClass()));
			if (slices instanceof RuleSet) {
				RuleSet rs = (RuleSet) slices;
				for (Rule rule : rs.getRules()) {
					rule.check(session);
				}
			}
		}
	}

	@Override
	public void propagate(Session session, Collection<PropagationEntry> changes) {
		// for rules we check all rules sequentially
		for (PropagationEntry change : changes) {
			if (!change.isStrategic() && change.hasChanged()) {
				this.propagate(session, change.getObject());
			}
		}
	}

	@Override
	public Set<TerminologyObject> getPotentialDerivationSources(TerminologyObject derivedObject) {
		return getSourceObjects(derivedObject, null);
	}

	@Override
	public Set<TerminologyObject> getActiveDerivationSources(TerminologyObject derivedObject, Session session) {
		if (session == null) throw new NullPointerException();
		return getSourceObjects(derivedObject, session);
	}

	private Set<TerminologyObject> getSourceObjects(TerminologyObject derivedObject, Session session) {
		Set<TerminologyObject> result = new HashSet<>();
		RuleSet rules = derivedObject.getKnowledgeStore().getKnowledge(getBackwardKind(this.getClass()));
		if (rules == null) return Collections.emptySet();
		for (Rule rule : rules.getRules()) {
			// if the rule is known not to be fired, ignore the rule
			if (session != null && !rule.hasFired(session)) continue;
			// add precondition values
			Condition condition = rule.getCondition();
			if (condition != null) {
				result.addAll(condition.getTerminalObjects());
			}
			// add action formula values
			PSAction action = rule.getAction();
			if (action != null) {
				result.addAll(action.getForwardObjects());
			}
		}
		return result;
	}

	@Override
	public double getPriority() {
		return 2.5;
	}

}
