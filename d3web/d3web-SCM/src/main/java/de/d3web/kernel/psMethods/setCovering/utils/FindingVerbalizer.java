package de.d3web.kernel.psMethods.setCovering.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.kernel.domainModel.ruleCondition.CondNum;
import de.d3web.kernel.domainModel.ruleCondition.CondNumIn;
import de.d3web.kernel.domainModel.ruleCondition.CondQuestion;
import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
import de.d3web.kernel.psMethods.setCovering.PredictedFinding;

/**
 * Verbalizes Findings for different Applications (e.g. for Dialog)
 * 
 * @author bruemmer
 */
public class FindingVerbalizer {

	private static FindingVerbalizer instance = null;

	public static FindingVerbalizer getInstance() {
		if (instance == null) {
			instance = new FindingVerbalizer();
		}
		return instance;
	}

	private FindingVerbalizer() {
	}

	public List retrieveNamedObjectAndAnswers(PredictedFinding finding, AbstractCondition condition) {
		List answers = new LinkedList();
		if (condition instanceof CondQuestion) {
			finding.setNamedObject(((CondQuestion) condition).getQuestion());
			answers.addAll(getAnswersFromCondQuestion(finding, (CondQuestion) condition));
		} else if (condition instanceof NonTerminalCondition) {
			Iterator iter = ((NonTerminalCondition) condition).getTerms().iterator();
			while (iter.hasNext()) {
				AbstractCondition cond = (AbstractCondition) iter.next();
				answers.addAll(retrieveNamedObjectAndAnswers(finding, cond));
			}
		}
		return answers;
	}

	public static List getAnswersFromCondQuestion(PredictedFinding finding, CondQuestion condition) {
		List answers = new LinkedList();

		if (condition instanceof CondNumIn) {
			CondNumIn condNumIn = (CondNumIn) condition;
			answers.add(NumericalIntervalMapper.getInstance().map(condNumIn.getInterval()));

		} else if (condition instanceof CondNum) {
			CondNum condNum = (CondNum) condition;
			AnswerNum ansNum = ((QuestionNum) finding.getNamedObject()).getAnswer(null, condNum
					.getAnswerValue());
			if (ansNum != null) {
				answers.add(ansNum);
			}
		} else if (condition instanceof CondEqual) {
			CondEqual condEqual = (CondEqual) condition;
			answers.addAll(condEqual.getValues());
		}
		return answers;
	}

	public String verbalizeForDialog(PredictedFinding predFinding) {
		return verbalizeCondition(predFinding, predFinding.getCondition());
	}

	private String verbalizeCondition(PredictedFinding finding, AbstractCondition condition) {
		StringBuffer sb = new StringBuffer();
		if (condition instanceof CondQuestion) {
			List answers = getAnswersFromCondQuestion(finding, (CondQuestion) condition);
			if (answers.size() == 1) {
				sb.append(((Answer) answers.get(0)).verbalizeValue(null));
			} else {
				sb.append(answers.toString());
			}
		} else if (condition instanceof NonTerminalCondition) {

			if (condition instanceof CondNot) {
				sb.append("("
						+ finding.getSymbolVerbalizer()
								.resolveSymbolForCurrentLocale(CondNot.class) + " ");
				sb.append(verbalizeCondition(finding, (AbstractCondition) ((CondNot) condition)
						.getTerms().get(0))
						+ ")");
			} else {
				String symbol = finding.getSymbolVerbalizer().resolveSymbolForCurrentLocale(
						condition.getClass());
				NonTerminalCondition nonTermCondition = (NonTerminalCondition) condition;
				Iterator termIter = nonTermCondition.getTerms().iterator();
				sb.append("(");
				while (termIter.hasNext()) {
					AbstractCondition cond = (AbstractCondition) termIter.next();
					sb.append(verbalizeCondition(finding, cond));
					if (termIter.hasNext()) {
						sb.append(" " + symbol + " ");
					}
				}
				sb.append(")");
			}
		}
		return sb.toString();
	}
}