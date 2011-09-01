/*
 * Copyright (C) 2009 denkbares GmbH
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
package de.d3web.costbenefit.model;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.session.Session;
import de.d3web.costbenefit.inference.CostFunction;
import de.d3web.costbenefit.inference.DefaultCostFunction;
import de.d3web.costbenefit.inference.PSMethodCostBenefit;

/**
 * This model provides all functions on targets, nodes and paths for the search
 * algorithms. It represents the actual state of a search.
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public class SearchModel {

	private final Set<Target> targets = new HashSet<Target>();

	private Target bestBenefitTarget;
	private Target bestCostBenefitTarget;
	private CostFunction costFunction;
	private final Session session;

	public SearchModel(Session session) {
		this.session = session;
		PSMethod problemsolver = session.getPSMethodInstance(PSMethodCostBenefit.class);
		PSMethodCostBenefit ps = (PSMethodCostBenefit) problemsolver;
		if (ps != null) {
			costFunction = ps.getCostFunction();
		}
		else {
			costFunction = new DefaultCostFunction();
			Logger.getLogger(this.getClass().getName()).throwing(
					this.getClass().getName(),
					"Kein Costbenefit-Problemlöser im Fall. Es wird die Standartkostenfunktion verwendet.",
					null);
		}
	}

	@Override
	public SearchModel clone() {
		SearchModel copy = new SearchModel(session);
		copy.bestBenefitTarget =
				this.bestBenefitTarget == null ? null : bestBenefitTarget.clone();
		copy.bestCostBenefitTarget =
				this.bestCostBenefitTarget == null ? null : bestCostBenefitTarget.clone();
		for (Target target : targets) {
			copy.addTarget(target.clone());
		}
		return copy;
	}

	/**
	 * Merges the information from the specified {@link SearchModel} into this
	 * SearchModel. The specified one is left untouched. This object is altered
	 * and contains the merged results of the two searches.
	 * 
	 * @created 01.09.2011
	 * @param other the search model to merge into this object
	 */
	public void merge(SearchModel other) {
		// do all the checks inside this loop
		// to avoid multiple clones for the same target
		for (Target original : other.targets) {
			// create one clone target
			Target copy = original.clone();
			// check for best benefit one
			if (original == other.bestBenefitTarget) {
				checkTarget(copy);
			}
			// check for best cost benefit one
			// (else, because we have no need to check it twice)
			else if (original == other.bestCostBenefitTarget) {
				checkTarget(copy);
			}
			// and replace the one in the targets list
			// if the copied one is better
			Target existing = getExistingTarget(copy);
			if (existing != null && existing.getCosts() > copy.getCosts()) {
				targets.remove(existing);
				targets.add(copy);
			}
		}
	}

	/**
	 * Return the existing target that equals to the specified one.
	 * Unfortunately the Set have no such function to acces the existing one.
	 */
	private Target getExistingTarget(Target blueprint) {
		if (targets.contains(blueprint)) {
			for (Target target : targets) {
				if (target.equals(blueprint)) return target;
			}
		}
		return null;
	}

	/**
	 * Adds a new target
	 * 
	 * @param target
	 */
	public void addTarget(Target target) {
		targets.add(target);
	}

	public void checkTarget(Target t) {
		if (bestBenefitTarget == null || t.getBenefit() > bestBenefitTarget.getBenefit()) {
			bestBenefitTarget = t;
		}
		// only check for best cost/benefit it the target has been reached yet
		if (t.getMinPath() != null) {
			if (bestCostBenefitTarget == null
					|| t.getCostBenefit() < bestCostBenefitTarget.getCostBenefit()) {
				bestCostBenefitTarget = t;
			}
		}
	}

	/**
	 * Maximizes the benefit of a target
	 * 
	 * @param t
	 * @param benefit
	 */
	public void maximizeBenefit(Target t, double benefit) {
		if (t.getBenefit() < benefit) {
			t.setBenefit(benefit);
			checkTarget(t);
		}
	}

	/**
	 * @return the Target with the best CostBenefit
	 */
	public Target getBestCostBenefitTarget() {
		return bestCostBenefitTarget;
	}

	/**
	 * Returns the benefit of the bestBenefitTarget The best benefit not
	 * necessarily is the benefit used for the best cost/benefit relation
	 * 
	 * @return
	 */
	public double getBestBenefit() {
		if (bestBenefitTarget == null) return 0f;
		return bestBenefitTarget.getBenefit();
	}

	public Set<Target> getTargets() {
		return targets;
	}

	/**
	 * Returns the CostBenefit of the BestCostBenefitTarget
	 * 
	 * @return
	 */
	public double getBestCostBenefit() {
		if (bestCostBenefitTarget == null) {
			return Float.MAX_VALUE;
		}
		return bestCostBenefitTarget.getCostBenefit();
	}

	/**
	 * Returns the best unreached benefit. This can be used to calculate the
	 * best reachable CostBenefit.
	 * 
	 * @return
	 */
	public double getBestUnreachedBenefit() {
		double benefit = 0;
		for (Target t : targets) {
			if (t.getMinPath() == null) {
				benefit = Math.max(benefit, t.getBenefit());
			}
		}
		return benefit;
	}

	/**
	 * Checks if the actual path reaches at least one target.
	 * 
	 * @param actual
	 * @return
	 */
	public boolean isTarget(Path actual) {
		if (actual.isEmpty()) return false;
		for (Target t : targets) {
			if (t.isReached(actual)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if this model has at least one target
	 * 
	 * @return
	 */
	public boolean hasTargets() {
		return !targets.isEmpty();
	}

	/**
	 * Returns the CostFunction
	 * 
	 * @return
	 */
	public CostFunction getCostFunction() {
		return costFunction;
	}

	/**
	 * Checks if at least one target is reached.
	 * 
	 * @return
	 */
	public boolean isAnyTargetReached() {
		return (bestCostBenefitTarget != null);
	}

	public Session getSession() {
		return session;
	}

}
