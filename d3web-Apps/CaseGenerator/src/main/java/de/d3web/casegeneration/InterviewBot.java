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

package de.d3web.casegeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.interviewmanager.NextUnansweredQuestionFormStrategy;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.Rating;
import de.d3web.empiricaltesting.SequentialTestCase;

/**
 * This class generates sequential test cases simulating an interview with the
 * d3web interview manager by exhaustively traversing all possible answers of
 * the currently active question.
 * <P>
 * Generated can be configured in the following manner:
 * <P>
 * <UL>
 * <LI>Maximal number of cases to be generated
 * <LI>Initial findings: set before every case starts
 * <LI>Known answers for questions: these answers are set, when the question is
 * presented during problem solving
 * <LI>Forbidden answers for specific questions are not used in any generated
 * case
 * <LI>Rating strategy: determines how the state/score of the currently derived
 * solutions is stored in the case
 * </UL>
 * <P>
 * <b>Planned extensions:</b>
 * <UL>
 * <LI>[in progress] Consider whole qcontainers in one step
 * <LI>Only vary diagnostically relevant questions
 * <LI>Consider global conditions, that terminate the traversal
 * </UL>
 * 
 * @author joba
 * 
 */
// the class is instantiated by the included Builder
public final class InterviewBot {

	// default 0 means no restriction in the number of cases to be generated
	private long maxCases;
	// praefix of a sequential test case
	private String sequentialTestCasePraefix;
	// praefix of a part of a sequential test case
	private String ratedTestCasePraefix;
	// findings that are already set for all generated cases
	private List<Finding> initFindings;
	// the knowledge used to generate the cases
	private KnowledgeBase knowledge;
	// the method of how to determine the rating of a solution
	private RatingStrategy ratingStrategy;

	// the strategy to select questions and possible values
	private BotStrategy strategy = new DefaultBotStrategy();

	// the generated cases
	private List<SequentialTestCase> cases;
	// the number of generated cases
	private int casesCounter = 1;

	/**
	 * Generates a collection of {@link SequentialTestCase} instances based on
	 * the settings defined by the Builder.
	 * 
	 * @return a collection of {@link SequentialTestCase} instances
	 * @throws UnsupportedDataTypeException when the knowledge bases uses a
	 *         question type not supported by the generator
	 */
	public List<SequentialTestCase> generate() throws UnsupportedDataTypeException {
		init();

		Session session = createCase(initFindings);
		SequentialTestCase stc = createInitalCase(session);
		traverse(stc, session);

		return cases;
	}

	private void init() {
		casesCounter = 1;
		cases = new LinkedList<SequentialTestCase>();
	}

	private void traverse(SequentialTestCase thisCase, Session thisSession) throws UnsupportedDataTypeException {
		// Termination of the recursion: no more questions to ask
		// or maximum number of cases generated.
		InterviewObject[] interviewItems = strategy.getNextSequenceItems(thisSession);
		if (interviewItems == null || interviewItems.length == 0) {
			store(thisCase);
			return;
		}

		// do noting more if we reached the maximum number of cases
		if (maxCases > 0 && casesCounter > maxCases) return;

		// Get all possible answers (combinations) for the question
		FactSet[] factSets = strategy.getNextSequenceAnswers(thisSession, interviewItems);

		// Iterate over all possible answers of the next question
		for (FactSet factSet : factSets) {
			Session nextSession = createCase(thisCase);
			factSet.apply(nextSession);
			SequentialTestCase nextSequentialCase = packNewSequence(
					thisCase,
					toFindings(factSet),
					toRatedSolutions(nextSession));

			// step down in recursion with the next suitable question to ask
			traverse(nextSequentialCase, nextSession);
		}
	}

	private SequentialTestCase packNewSequence(SequentialTestCase sqCase, List<Finding> findings, List<RatedSolution> solutions) {
		RatedTestCase ratedCase = new RatedTestCase();
		ratedCase.addFindings(findings);
		ratedCase.addExpected(solutions);
		ratedCase.inverseSortSolutions();

		String namePraefix = sqCase.getName() + "_";
		String nameSuffix = ratedTestCasePraefix + sqCase.getCases().size();
		ratedCase.setName(namePraefix + nameSuffix);

		SequentialTestCase newSequentialCase = sqCase.flatClone();
		newSequentialCase.setName(sequentialTestCasePraefix + dateToString());
		newSequentialCase.add(ratedCase);
		return newSequentialCase;
	}

	private SequentialTestCase createInitalCase(Session session) {
		SequentialTestCase stc = new SequentialTestCase();
		stc.setName(sequentialTestCasePraefix);
		if (!initFindings.isEmpty()) {
			RatedTestCase ratedTestCase = new RatedTestCase();
			ratedTestCase.addFindings(initFindings);
			ratedTestCase.addExpected(toRatedSolutions(session));
			ratedTestCase.inverseSortSolutions();
			ratedTestCase.setName(ratedTestCasePraefix + "0");
			stc.add(ratedTestCase);
		}
		return stc;
	}

	/**
	 * Give the sequential test case a name and add it to the collection of
	 * generated cases.
	 * 
	 * @param theSeqCase the {@link SequentialTestCase} instance to be added to
	 *        the collection of generated cases
	 */
	private void store(SequentialTestCase theSeqCase) {
		theSeqCase.setName(sequentialTestCasePraefix + casesCounter);
		cases.add(theSeqCase);
		casesCounter++;
	}

	private List<Finding> toFindings(FactSet factSet) {
		List<Finding> findings = new LinkedList<Finding>();
		for (Fact fact : factSet.getValueFacts()) {
			TerminologyObject object = fact.getTerminologyObject();
			if (object instanceof Question) {
				Finding finding = new Finding((Question) object, fact.getValue());
				findings.add(finding);
			}
		}
		return findings;
	}

	private List<RatedSolution> toRatedSolutions(Session session) {
		List<RatedSolution> ratedSolutions = new LinkedList<RatedSolution>();
		for (Solution solution : session.getKnowledgeBase().getManager().getSolutions()) {
			Rating rating = ratingStrategy.getRatingFor(solution, session);
			if (rating.isProblemSolvingRelevant()) {
				RatedSolution ratedSolution = new RatedSolution(solution, rating);
				ratedSolutions.add(ratedSolution);
			}
		}
		return ratedSolutions;
	}

	private Session createCase(SequentialTestCase sqCase) {
		Session session = SessionFactory.createSession(knowledge);
		for (RatedTestCase c : sqCase.getCases()) {
			for (Finding finding : c.getFindings()) {
				setCaseValue(session, finding.getQuestion(), finding.getValue());
			}
		}
		return session;
	}

	private Session createCase(List<Finding> findings) {
		Session session = SessionFactory.createSession(knowledge);
		session.getInterview().setFormStrategy(new NextUnansweredQuestionFormStrategy());
		for (Finding finding : findings) {
			setCaseValue(session, finding.getQuestion(), finding.getValue());
		}
		return session;
	}

	private void setCaseValue(Session session, Question q, Value v) {

		Fact fact = FactFactory.createUserEnteredFact(q, v);

		session.getBlackboard().addValueFact(fact);
	}

	/**
	 * This builder creates a configured interview bot.
	 * 
	 * @author joba
	 * 
	 */
	public static class Builder {

		private long maxCases = 0;
		private String sequentialTestCasePraefix = "STC";
		private String ratedTestCasePraefix = "RTC";
		private List<Finding> initFindings = new LinkedList<Finding>();
		private RatingStrategy ratingStrategy = new StateRatingStrategy();
		private final Map<Question, List<Value>> allowedAnswers = new HashMap<Question, List<Value>>();
		private final Map<Question, List<Value>> forbiddenAnswers = new HashMap<Question, List<Value>>();
		private int maxAnswerCombinations = 0;
		private final Map<QuestionMC, Integer> maxAnswerCombinationsForQuestion = new HashMap<QuestionMC, Integer>();
		private final Map<QuestionMC, Collection<Set<Choice>>> forbiddenAnswerCombinations = new HashMap<QuestionMC, Collection<Set<Choice>>>();
		private final Map<QuestionMC, Collection<Set<Choice>>> allowedAnswerCombinations = new HashMap<QuestionMC, Collection<Set<Choice>>>();
		private KnowledgeBase knowledge;

		public Builder(KnowledgeBase knowledge) {
			this.knowledge = knowledge;
		}

		public Builder maxCases(long val) {
			maxCases = val;
			return this;
		}

		public Builder sequentialTestCasePraefix(String val) {
			sequentialTestCasePraefix = val;
			return this;
		}

		public Builder ratedTestCasePraefix(String val) {
			ratedTestCasePraefix = val;
			return this;
		}

		public Builder initFindings(List<Finding> additinalFindings) {
			initFindings.addAll(additinalFindings);
			return this;
		}

		public Builder initFindings(Finding... additinalFindings) {
			Collections.addAll(this.initFindings, additinalFindings);
			return this;
		}

		public Builder knowledgeBase(KnowledgeBase val) {
			knowledge = val;
			return this;
		}

		public Builder ratingStrategy(RatingStrategy val) {
			ratingStrategy = val;
			return this;
		}

		public Builder knownAnswer(Finding f) {
			return knownAnswer(f.getQuestion(), f.getValue());
		}

		public Builder knownAnswer(Question question, Value value) {
			allowedAnswers.put(question, Arrays.asList(value));
			return this;
		}

		public Builder allowedAnswer(Finding f) {
			return allowedAnswer(f.getQuestion(), f.getValue());
		}

		public Builder allowedAnswer(Question question, Value value) {
			List<Value> answers = allowedAnswers.get(question);
			if (answers == null) {
				answers = new ArrayList<Value>();
			}
			answers.add(value);
			allowedAnswers.put(question, answers);
			return this;
		}

		public Builder forbiddenAnswer(Finding f) {
			return forbiddenAnswer(f.getQuestion(), f.getValue());
		}

		public Builder forbiddenAnswer(Question question, Value value) {
			List<Value> answers = forbiddenAnswers.get(question);
			if (answers == null) {
				answers = new ArrayList<Value>();
			}
			answers.add(value);
			forbiddenAnswers.put(question, answers);
			return this;
		}

		public Builder maxAnswerCombinations(int val) {
			maxAnswerCombinations = val;
			return this;
		}

		public Builder maxAnswerCombinations(QuestionMC q, int val) {
			maxAnswerCombinationsForQuestion.put(q, Integer.valueOf(val));
			return this;
		}

		public Builder forbiddenAnswerCombination(FindingMC f) throws Exception {
			return forbiddenAnswerCombination(f.getQuestion(), f.getAnswers());
		}

		public Builder forbiddenAnswerCombination(QuestionMC question, Choice[] choices) throws Exception {
			Collection<Set<Choice>> answers = forbiddenAnswerCombinations.get(question);
			if (answers == null) {
				answers = new LinkedList<Set<Choice>>();
			}
			answers.add(new HashSet<Choice>(Arrays.asList(choices)));
			forbiddenAnswerCombinations.put(question, answers);
			return this;
		}

		public Builder allowedAnswerCombination(FindingMC f) {
			return allowedAnswerCombination(f.getQuestion(), f.getAnswers());
		}

		public Builder allowedAnswerCombination(QuestionMC question, Choice[] choices) {
			Collection<Set<Choice>> answers = allowedAnswerCombinations.get(question);
			if (answers == null) {
				answers = new LinkedList<Set<Choice>>();
			}
			answers.add(new HashSet<Choice>(Arrays.asList(choices)));
			allowedAnswerCombinations.put(question, answers);
			return this;
		}

		public InterviewBot build() {
			// create strategy
			DefaultBotStrategy strategy = new DefaultBotStrategy();
			strategy.setAllowedAnswers(this.allowedAnswers);
			strategy.setForbiddenAnswers(this.forbiddenAnswers);
			strategy.setMaxChoiceCombinations(this.maxAnswerCombinations);
			strategy.setMaxChoiceCombinationsForQuestion(this.maxAnswerCombinationsForQuestion);
			strategy.setForbiddenChoiceCombinations(this.forbiddenAnswerCombinations);
			strategy.setRequiredChoiceCombinations(this.allowedAnswerCombinations);
			// create interview bot
			InterviewBot bot = new InterviewBot(this);
			bot.maxCases = this.maxCases;
			bot.sequentialTestCasePraefix = this.sequentialTestCasePraefix;
			bot.ratedTestCasePraefix = this.ratedTestCasePraefix;
			bot.initFindings = this.initFindings;
			bot.knowledge = this.knowledge;
			bot.ratingStrategy = this.ratingStrategy;
			bot.strategy = strategy;
			return bot;
		}
	}

	private InterviewBot() {
	}

	private InterviewBot(Builder builder) {
	}

	private String dateToString() {
		return "" + Calendar.getInstance().getTimeInMillis();
	}

}