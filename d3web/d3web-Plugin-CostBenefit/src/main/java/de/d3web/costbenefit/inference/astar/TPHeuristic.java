/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.costbenefit.inference.astar;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.Conditions;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.abnormality.Abnormality;
import de.d3web.core.knowledge.terminology.info.abnormality.DefaultAbnormality;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.utilities.Pair;
import de.d3web.costbenefit.Util;
import de.d3web.costbenefit.inference.StateTransition;
import de.d3web.costbenefit.inference.ValueTransition;
import de.d3web.costbenefit.model.Path;
import de.d3web.costbenefit.model.SearchModel;
import de.d3web.costbenefit.model.Target;

/**
 * Uses a "slice model" to calculate a more precise distance
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 30.09.2011
 */
public class TPHeuristic extends DividedTransitionHeuristic {

	public static class TPHeuristicSessionObject extends DividedTransitionHeuristicSessionObject {

		/**
		 * Stores for each CondEqual a Pair of Conditions and QContainers. If
		 * the condition is false when starting a search and none of the
		 * QContainers is in the actual path, the conditions can be added to the
		 * condequal if there are no conflicts (e.g. same termobjects in an
		 * CondOr and CondEqual)
		 */
		private Map<Condition, Pair<List<Condition>, Set<QContainer>>> preconditionCache = new HashMap<Condition, Pair<List<Condition>, Set<QContainer>>>();

		/**
		 * Stores a List of Pairs of Conditions and QContainers establishing a
		 * state where the conditions of the targetQContainer is applicable
		 */
		private Map<QContainer, List<Pair<List<Condition>, Set<QContainer>>>> targetCache = new HashMap<QContainer, List<Pair<List<Condition>, Set<QContainer>>>>();

		private Collection<Question> cachedAbnormalQuestions = new HashSet<Question>();
	}

	private static final Logger log = Logger.getLogger(TPHeuristic.class.getName());

	@Override
	public void init(SearchModel model) {
		TPHeuristicSessionObject sessionObject = (TPHeuristicSessionObject) model.getSession().getSessionObject(
				this);
		// KB has to be remembered before super.init
		KnowledgeBase oldkb = sessionObject.knowledgeBase;
		super.init(model);
		// initgeneral in only called when the kb or the list of cached abnormal
		// questions changes
		if (model.getSession().getKnowledgeBase() != oldkb) {
			initGeneralCache(sessionObject, model);
			// knowledbase gets updated in super.init(model)
			sessionObject.cachedAbnormalQuestions = calculateAnsweredAbnormalQuestions(model);
		}
		else {
			Set<Question> answeredAbnormalQuestions = calculateAnsweredAbnormalQuestions(model);
			if (!answeredAbnormalQuestions.equals(sessionObject.cachedAbnormalQuestions)) {
				sessionObject.cachedAbnormalQuestions = answeredAbnormalQuestions;
				initGeneralCache(sessionObject, model);
			}
		}
		initTargetCache(sessionObject, model);
	}

	private static Set<Question> calculateAnsweredAbnormalQuestions(SearchModel model) {
		Set<Question> result = new HashSet<Question>();
		Blackboard blackboard = model.getSession().getBlackboard();
		for (StateTransition st : model.getTransitionalStateTransitions()) {
			for (Question q : Util.getQuestionOCs(st.getQcontainer())) {
				DefaultAbnormality abnormality = q.getInfoStore().getValue(
						BasicProperties.DEFAULT_ABNORMALITIY);
				Value value = blackboard.getValue(q);
				if (UndefinedValue.isNotUndefinedValue(value)) {
					boolean abnormal = (abnormality == null)
							? true
							: abnormality.getValue(value) == Abnormality.A5;
					if (abnormal) {
						result.add(q);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Inits preconditions per target.
	 * 
	 * Conditions can be used as Preconditions, if they are of type CondEqual,
	 * they are not already contained in the activation condition of the target
	 * and their term object is not contained in any partial condition of the
	 * activation condition other than CondEqual. They can also be used, if they
	 * are not type of CondEqual and all their term objects are not part of the
	 * activation condition.
	 * 
	 * @created 05.10.2011
	 * @param sessionObject actual SessionObject
	 * @param model {@link SearchModel}
	 */
	private static void initTargetCache(TPHeuristicSessionObject sessionObject, SearchModel model) {
		long time = System.currentTimeMillis();
		sessionObject.targetCache.clear();
		Session session = model.getSession();
		for (Target target : model.getTargets()) {
			// iterate over all QContainers of all targets
			for (QContainer qcon : target.getQContainers()) {
				// skip qcontainer if it is already initialized
				if (sessionObject.targetCache.get(qcon) != null) continue;
				StateTransition st = StateTransition.getStateTransition(qcon);
				// qcontainers without Statetransition are handled separately
				if (st == null) continue;
				Condition activationCondition = st.getActivationCondition();
				if (activationCondition == null) continue;
				Collection<TerminologyObject> forbiddenTermObjects = getForbiddenObjects(activationCondition);
				List<Condition> conditions = getPrimitiveConditions(activationCondition);
				List<Pair<List<Condition>, Set<QContainer>>> additionalConditions = new LinkedList<Pair<List<Condition>, Set<QContainer>>>();
				List<Condition> conditionsToExamine = conditions;
				Set<Condition> alreadyExaminedConditions = new HashSet<Condition>();
				while (!conditionsToExamine.isEmpty()) {
					List<Pair<List<Condition>, Set<QContainer>>> temppairs = getPairs(
							sessionObject, session,
							conditionsToExamine, conditions,
							activationCondition.getTerminalObjects(), forbiddenTermObjects, true);
					additionalConditions.addAll(temppairs);
					conditionsToExamine = getPrimitiveConditions(temppairs,
							alreadyExaminedConditions);
				}
				sessionObject.targetCache.put(qcon, additionalConditions);
			}
		}
		log.info("Target init: " + (System.currentTimeMillis() - time) + "ms");
	}

	private static List<Condition> getPrimitiveConditions(List<Pair<List<Condition>, Set<QContainer>>> temppairs, Set<Condition> alreadyExaminedConditions) {
		List<Condition> list = new LinkedList<Condition>();
		for (Pair<List<Condition>, Set<QContainer>> p : temppairs) {
			for (Condition cond : p.getA()) {
				if (cond instanceof CondEqual) {
					if (!alreadyExaminedConditions.contains(cond)) list.add(cond);
				}
				else if (cond instanceof CondOr) {
					if (checkCondOr((CondOr) cond)) {
						if (!alreadyExaminedConditions.contains(cond)) list.add(cond);
					}
				}
				else if (cond instanceof CondNot) {
					if (checkCondNot((CondNot) cond)) {
						if (!alreadyExaminedConditions.contains(cond)) list.add(cond);
					}
				}
			}
		}
		alreadyExaminedConditions.addAll(list);
		return list;
	}

	private static List<Pair<List<Condition>, Set<QContainer>>> getPairs(TPHeuristicSessionObject sessionObject, Session session, Collection<? extends Condition> targetConditions, List<Condition> originalPrimitiveConditions, Collection<? extends TerminologyObject> collection, Collection<TerminologyObject> forbiddenTermObjects, boolean skipTrueConds) {
		List<Pair<List<Condition>, Set<QContainer>>> additionalConditions = new LinkedList<Pair<List<Condition>, Set<QContainer>>>();
		for (Condition cond : targetConditions) {
			if (skipTrueConds && Conditions.isTrue(cond, session)) continue;
			Pair<List<Condition>, Set<QContainer>> generalPair = sessionObject.preconditionCache.get(cond);
			if (generalPair == null) continue;
			List<Condition> checkedConditions = new LinkedList<Condition>();
			for (Condition precondition : generalPair.getA()) {
				if (Conditions.isTrue(precondition, session)) continue;
				if (precondition instanceof CondEqual) {
					CondEqual condEqual = (CondEqual) precondition;
					// condequals must not be already contained and must
					// not have a forbidden termobject
					if (!originalPrimitiveConditions.contains(precondition)
							&& !forbiddenTermObjects.contains(condEqual.getQuestion())) {
						checkedConditions.add(condEqual);
					}
				}
				// all other conditions must not have any term object
				// already contained in the activation condition
				else if (Collections.disjoint(collection,
						precondition.getTerminalObjects())) {
					checkedConditions.add(precondition);
				}
			}
			if (!checkedConditions.isEmpty()) {
				additionalConditions.add(new Pair<List<Condition>, Set<QContainer>>(
						checkedConditions, generalPair.getB()));
			}
		}
		return additionalConditions;
	}

	private static void initGeneralCache(TPHeuristicSessionObject sessionObject, SearchModel model) {
		long time = System.currentTimeMillis();
		sessionObject.preconditionCache.clear();
		KnowledgeBase kb = model.getSession().getKnowledgeBase();
		Collection<StateTransition> transitiveStateTransitions = model.getTransitionalStateTransitions();
		Collection<StateTransition> stateTransitions = new LinkedList<StateTransition>();
		Set<QContainer> blockedQContainers = model.getBlockedQContainers();
		// filter StateTransitions that cannot be applied due to final questions
		for (StateTransition st : kb.getAllKnowledgeSlicesFor(StateTransition.KNOWLEDGE_KIND)) {
			QContainer qcontainer = st.getQcontainer();
			if (!blockedQContainers.contains(qcontainer)) {
				stateTransitions.add(st);
			}
		}
		// collect all CondEqual preconditions of all statetransitions (CondAnd
		// are splitted, other condition types can not be used to extend
		// condition
		Set<Condition> preconditions = new HashSet<Condition>();
		for (StateTransition st : stateTransitions) {
			preconditions.addAll(getPrimitiveConditions(st.getActivationCondition()));
		}
		for (Condition condition : preconditions) {
			// collect all conditions of state transitions establishing a state
			// that enables
			// execution of the condEqual
			LinkedList<List<Condition>> neededConditions = new LinkedList<List<Condition>>();
			Set<QContainer> transitionalQContainer = new HashSet<QContainer>();
			for (StateTransition st : transitiveStateTransitions) {
				for (ValueTransition vt : st.getPostTransitions()) {
					if (vt.getQuestion() == condition.getTerminalObjects().iterator().next()) {
						Value v = getValue(sessionObject, vt);
						if (checkValue(condition, v)) {
							neededConditions.add(flattenCondAnds(st.getActivationCondition()));
							transitionalQContainer.add(st.getQcontainer());
							break;
						}
					}
				}
			}
			// TODO: add a hashmap finalquestion -> conditions which must be
			// recalculated
			// put all common conditions in the cache
			sessionObject.preconditionCache.put(condition,
					new Pair<List<Condition>, Set<QContainer>>(
							getCommonConditions(neededConditions),
							transitionalQContainer));
		}
		log.info("General init: " + (System.currentTimeMillis() - time) + "ms");
	}

	/**
	 * Checks if the the Value v can be used to fullfill the condition. This
	 * method can only handle primitive conditions @see getPrimitiveConditions
	 * 
	 * @created 04.07.2012
	 * @param condition specified condition
	 * @param v Value
	 * @return true if the value can fullfill the condition
	 */
	public static boolean checkValue(Condition condition, Value v) {
		if (condition instanceof CondEqual) {
			return ((CondEqual) condition).getValue().equals(v);
		}
		else if (condition instanceof CondOr && (v instanceof ChoiceValue)) {
			List<ChoiceID> choiceIDs = getChoiceIDs((CondOr) condition);
			ChoiceValue cv = (ChoiceValue) v;
			return choiceIDs.contains(cv.getChoiceID());
		}
		else if (condition instanceof CondNot && (v instanceof ChoiceValue)) {
			ChoiceValue cv = (ChoiceValue) v;
			CondNot condNot = (CondNot) condition;
			CondEqual condEqual = (CondEqual) condNot.getTerms().get(0);
			return !(cv.getChoiceID().equals(((ChoiceValue) condEqual.getValue()).getChoiceID()));
		}
		return false;
	}

	private static List<Condition> getCommonConditions(LinkedList<List<Condition>> neededConditions) {
		// take the first list and check if all other lists contain it's
		// conditions
		List<Condition> first = neededConditions.poll();
		List<Condition> commonConditions = new LinkedList<Condition>();
		if (first != null) {
			first: for (Condition c : first) {
				for (List<Condition> ref : neededConditions) {
					if (!ref.contains(c)) {
						continue first;
					}
				}
				// all reference condition list contain c
				commonConditions.add(c);
			}
		}
		return commonConditions;
	}

	/**
	 * Returns all primitive subconditions of the specified condition. CondAnd
	 * are splitted. CondOr are defined as primitive, when their subterms are
	 * all CondEqual of QuestionOC. CondNot are primitive, when they have
	 * exactly one CondEqual of a QuestionOC as subcondition. CondEqual are
	 * always primitive.
	 * 
	 * @param cond specified Condition
	 * @return list of primitive conditions
	 */
	public static List<Condition> getPrimitiveConditions(Condition cond) {
		List<Condition> conds = new LinkedList<Condition>();
		if (cond instanceof CondEqual) {
			conds.add(cond);
		}
		else if (cond instanceof CondOr) {
			CondOr condOr = (CondOr) cond;
			if (checkCondOr(condOr)) {
				conds.add(condOr);
			}
		}
		else if (cond instanceof CondNot) {
			CondNot condNot = (CondNot) cond;
			if (checkCondNot(condNot)) {
				conds.add(condNot);
			}
		}
		else if (cond instanceof CondAnd) {
			CondAnd condAnd = (CondAnd) cond;
			for (Condition c : condAnd.getTerms()) {
				conds.addAll(getPrimitiveConditions(c));
			}
		}
		return conds;
	}

	/**
	 * Checks if all terms of the specified CondOr are {@link CondEqual}s of
	 * QuestionOC
	 * 
	 * @created 22.03.2012
	 */
	private static boolean checkCondOr(CondOr condOr) {
		Question question = null;
		boolean accept = true;
		for (Condition c : condOr.getTerms()) {
			if (!(c instanceof CondEqual)) {
				accept = false;
				break;
			}
			CondEqual condEqual = (CondEqual) c;
			if (question == null) {
				question = condEqual.getQuestion();
			}
			else {
				if (question != condEqual.getQuestion()) {
					accept = false;
					break;
				}
			}
			if (!(condEqual.getQuestion() instanceof QuestionOC && condEqual.getValue() instanceof ChoiceValue)) {
				accept = false;
				break;
			}
		}
		return accept;
	}

	/**
	 * Checks if the specified {@link CondNot} contains only one CondEqual of a
	 * ChoiceValue
	 * 
	 * @created 22.03.2012
	 */
	private static boolean checkCondNot(CondNot condNot) {
		Condition negatedCondition = condNot.getTerms().get(0);
		if (negatedCondition instanceof CondEqual) {
			CondEqual condEqual = (CondEqual) negatedCondition;
			return (condEqual.getValue() instanceof ChoiceValue);
		}
		return false;
	}

	private static List<ChoiceID> getChoiceIDs(CondOr condOr) {
		List<ChoiceID> choices = new LinkedList<ChoiceID>();
		for (Condition c : condOr.getTerms()) {
			CondEqual condEqual = (CondEqual) c;
			ChoiceValue cv = (ChoiceValue) condEqual.getValue();
			choices.add(cv.getChoiceID());
		}
		return choices;
	}

	private static List<Condition> flattenCondAnds(Condition cond) {
		List<Condition> conds = new LinkedList<Condition>();
		if (cond instanceof CondAnd) {
			CondAnd condAnd = (CondAnd) cond;
			for (Condition c : condAnd.getTerms()) {
				conds.addAll(flattenCondAnds(c));
			}
		}
		else if (cond != null) {
			conds.add(cond);
		}
		return conds;
	}

	@Override
	public double getDistance(SearchModel model, Path path, State state, QContainer target) {
		StateTransition stateTransition = StateTransition.getStateTransition(target);
		// if there is no condition, the target can be indicated directly
		if (stateTransition == null || stateTransition.getActivationCondition() == null) return 0;
		Condition condition = getTransitiveCondition(model.getSession(), path, stateTransition,
				state.getSession());
		List<Condition> flattenedConditions = flattenCondAnds(condition);
		condition = new CondAnd(flattenedConditions);
		DividedTransitionHeuristicSessionObject sessionObject = model.getSession().getSessionObject(
				this);
		double result = estimatePathCosts(sessionObject, state, condition)
				+ calculateUnusedNegatives(sessionObject, path);

		return result;
	}

	/**
	 * Creates a transitive condition for the specified stateTransition based on
	 * the actual path. Important note: The heuristic has to be initialized
	 * based on a search model, containing the qcontainer of the StateTransition
	 * as a target. If no model is created yet, use the method
	 * calculateTransitiveCondition(Session, QContainer)
	 * 
	 * @created 05.07.2012
	 * @param sessionContainingObject the actual session
	 * @param path actual path
	 * @param stateTransition specified {@link StateTransition}
	 * @return transitive activation condition
	 */
	public Condition getTransitiveCondition(Session sessionContainingObject, Path path, StateTransition stateTransition, Session sessionRepresentingTheActualState) {
		Condition precondition = stateTransition.getActivationCondition();
		Map<Question, CondEqual> originalFullfilledCondEqual = new HashMap<Question, CondEqual>();
		List<Condition> originalPrimitiveConditions = getPrimitiveConditions(precondition);
		for (Condition condition : originalPrimitiveConditions) {
			if (condition instanceof CondEqual
					&& Conditions.isTrue(condition, sessionRepresentingTheActualState)) {
				CondEqual condEqual = (CondEqual) condition;
				originalFullfilledCondEqual.put(condEqual.getQuestion(), condEqual);
			}
		}
		TPHeuristicSessionObject sessionObject = (TPHeuristicSessionObject) sessionContainingObject.getSessionObject(this);
		// use a set to filter duplicated conditions
		Set<Condition> conditions = new HashSet<Condition>();
		List<Pair<List<Condition>, Set<QContainer>>> list = sessionObject.targetCache.get(stateTransition.getQcontainer());
		if (list == null) {
			return precondition;
		}
		for (Pair<List<Condition>, Set<QContainer>> p : list) {
			// if no qcontainer was on the path, add the conditions
			if (!path.contains(p.getB())) {
				conditions.addAll(p.getA());
			}
		}
		Condition condition;
		if (conditions.size() > 0) {
			// TODO eval: check if a conditionequal of the original condition is
			// true and a conditionequal conflicting with it is added, add
			// conditions needed to establish the original condition, maybe even
			// add conditions needed to establish those conditions, if the are
			// not true and not conflicting?
			List<Condition> conditionsToUse = new LinkedList<Condition>();
			// add the original condition
			conditionsToUse.add(precondition);
			Set<Condition> nonCondEqual = new HashSet<Condition>();
			Set<CondEqual> conflictingOriginalConds = new HashSet<CondEqual>();
			for (Condition additionalCondition : conditions) {
				if (Conditions.isTrue(additionalCondition, sessionRepresentingTheActualState)) continue;
				if (additionalCondition instanceof CondEqual) {
					conditionsToUse.add(additionalCondition);
					CondEqual conflicting = originalFullfilledCondEqual.get(((CondEqual) additionalCondition).getQuestion());
					if (conflicting != null) {
						conflictingOriginalConds.add(conflicting);
					}
				}
				else {
					nonCondEqual.add(additionalCondition);
				}
			}
			Set<Condition> blacklist = new HashSet<Condition>();
			for (Condition additionalCondition : nonCondEqual) {
				if (blacklist.contains(additionalCondition)) {
					continue;
				}
				// using a HashSet to fasten Collections.disjoint
				Set<TerminologyObject> objectsOfAdditionalCondition = new HashSet<TerminologyObject>(
						additionalCondition.getTerminalObjects());
				// the term objects of the other conditions must be disjunct
				boolean disjunct = true;
				// NOTE: Collect all reference TerminologyObjects and doing
				// only one disjoint operation is slower (cannot abort, must
				// Iterate all conditions)
				for (Condition reference : conditions) {
					if (reference != additionalCondition) {
						if (!Collections.disjoint(reference.getTerminalObjects(),
									objectsOfAdditionalCondition)) {
							// adding of this condition could violate the
							// optimism, continue with next condition
							disjunct = false;
							// reference condition cannot be used either
							blacklist.add(reference);
							break;
						}
					}
				}
				// if the terms objects are disjunct from all other
				// additional conditions, the condition can be added
				if (disjunct) {
					conditionsToUse.add(additionalCondition);
				}
			}
			condition = new CondAnd(conditionsToUse);
			if (conflictingOriginalConds.size() > 0) {
				List<Pair<List<Condition>, Set<QContainer>>> pairs = getPairs(sessionObject,
						sessionContainingObject, conflictingOriginalConds,
						getPrimitiveConditions(condition), condition.getTerminalObjects(),
						getForbiddenObjects(condition), false);
				for (Pair<List<Condition>, Set<QContainer>> pair : pairs) {
					// add the condition, even if one of their qcontainers is on
					// the path -> the condition is conflicting, so the
					// qcontainer will have to be visited again to fullfill the
					// condition again
					conditionsToUse.addAll(pair.getA());
				}
				condition = new CondAnd(conditionsToUse);
			}
		}
		else {
			condition = precondition;
		}
		return condition;
	}

	/**
	 * Returns all term objects of a condition, being part of a condition other
	 * than CondEqual or CondAnd
	 * 
	 * TODO replace forbidden objects by forbidden values!
	 * 
	 * @created 05.10.2011
	 * @param condition Condition
	 */
	private static Collection<TerminologyObject> getForbiddenObjects(Condition cond) {
		List<TerminologyObject> terms = new LinkedList<TerminologyObject>();
		if (cond instanceof CondEqual) {
			return terms;
		}
		else if (cond instanceof CondAnd) {
			CondAnd condAnd = (CondAnd) cond;
			for (Condition c : condAnd.getTerms()) {
				terms.addAll(getForbiddenObjects(c));
			}
			return terms;
		}
		else {
			terms.addAll(cond.getTerminalObjects());
			return terms;
		}
	}

	/**
	 * Calculates a transitive condition for the specified target based on the
	 * session.
	 * 
	 * @created 04.07.2012
	 * @param session actual session
	 * @param target specified target
	 * @return transitive condition
	 */
	public static Condition calculateTransitiveCondition(Session session, QContainer target) {
		TPHeuristic heuristic = new TPHeuristic();
		SearchModel model = new SearchModel(session);
		model.addTarget(new Target(target));
		heuristic.init(model);
		StateTransition stateTransition = StateTransition.getStateTransition(target);
		if (stateTransition == null) return new CondAnd(Collections.<Condition> emptyList());
		Path path = new AStarPath(null, null, 0);
		return heuristic.getTransitiveCondition(session, path, stateTransition, session);
	}

	@Override
	public TPHeuristicSessionObject createSessionObject(Session session) {
		return new TPHeuristicSessionObject();
	}
}
