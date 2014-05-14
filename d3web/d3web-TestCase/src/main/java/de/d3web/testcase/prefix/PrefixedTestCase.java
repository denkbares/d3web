package de.d3web.testcase.prefix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.DefaultFinding;
import de.d3web.testcase.model.Finding;
import de.d3web.testcase.model.TestCase;

/**
 * This class allows to add a {@link TestCase} as a prefix to another
 * {@link TestCase}. The start date will be the one of the prefix and the last
 * finding and check of the prefix will be merged with the first
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.02.2013
 */
public class PrefixedTestCase implements TestCase {

	private boolean initialized = false;
	private final TestCase prefix;
	private final TestCase testCase;
	private long dateDiff = 0;
	private Date mergedDate = null;
	private LinkedHashSet<Date> mergedChronology = null;

	public PrefixedTestCase(TestCase prefix, TestCase testCase) {
		this.prefix = prefix;
		this.testCase = testCase;
	}

	/**
	 * Returns the prefix part of this PrefixedTestCase
	 */
	public TestCase getPrefix() {
		return prefix;
	}

	/**
	 * Returns the actual TestCase of this PrefixedTestCase. Meaning: The part after the prefix.
	 */
	public TestCase getTestCase() {
		return testCase;
	}

	private void lazyInit() {
		if (!initialized) {
			init();
		}
	}

	private void init() {
		List<Date> prefixChronology = new ArrayList<Date>(prefix.chronology());
		Collection<Date> testCaseChronology = testCase.chronology();
		if (testCaseChronology.isEmpty()) return;
		Date lastPrefixDate = prefixChronology.get(prefixChronology.size() - 1);
		Date firstTestCaseDate = testCaseChronology.iterator().next();
		dateDiff = firstTestCaseDate.getTime() - lastPrefixDate.getTime();
		mergedDate = lastPrefixDate;
		initialized = true;
	}

	private Date toPrefixDate(Date testCaseDate) {
		return new Date(testCaseDate.getTime() - dateDiff);
	}

	private Date toTestCaseDate(Date prefixDate) {
		return new Date(prefixDate.getTime() + dateDiff);
	}

	private DefaultFinding toPrefixFinding(Finding testCaseFinding) {
		return new DefaultFinding(
				testCaseFinding.getTerminologyObject(),
				testCaseFinding.getValue(),
				toPrefixDate(testCaseFinding.getDate()));
	}

	@Override
	public Collection<Date> chronology() {
		lazyInit();
		if (this.mergedChronology != null) {
			return this.mergedChronology;
		}
		Collection<Date> prefixChronology = prefix.chronology();
		Collection<Date> testCaseChronology = testCase.chronology();
		mergedChronology = new LinkedHashSet<Date>(prefixChronology.size()
				+ testCaseChronology.size());
		mergedChronology.addAll(prefixChronology);
		for (Date testCaseDate : testCaseChronology) {
			mergedChronology.add(toPrefixDate(testCaseDate));
		}
		return mergedChronology;
	}

	@Override
	public Collection<Finding> getFindings(Date date, KnowledgeBase kb) {
		lazyInit();
		if (!chronology().contains(date)) return Collections.emptyList();
		if (date.before(mergedDate)) {
			return prefix.getFindings(date, kb);
		}
		else if (date.after(mergedDate)) {
			Collection<Finding> testCaseFindings = testCase.getFindings(toTestCaseDate(date), kb);
			Collection<Finding> transformedFindings = new ArrayList<Finding>(
					testCaseFindings.size());
			for (Finding testCaseFinding : testCaseFindings) {
				transformedFindings.add(toPrefixFinding(testCaseFinding));
			}
			return transformedFindings;
		}
		else {
			Collection<Finding> prefixFindings = prefix.getFindings(date, kb);
			Collection<Finding> testCaseFindings = testCase.getFindings(toTestCaseDate(date), kb);
			LinkedHashMap<TerminologyObject, Finding> mergedFindings = new LinkedHashMap<TerminologyObject, Finding>();
			for (Finding prefixFinding : prefixFindings) {
				mergedFindings.put(prefixFinding.getTerminologyObject(), prefixFinding);
			}
			for (Finding testCaseFinding : testCaseFindings) {
				mergedFindings.put(testCaseFinding.getTerminologyObject(),
						toPrefixFinding(testCaseFinding));
			}
			return mergedFindings.values();
		}
	}

	@Override
	public Finding getFinding(Date date, TerminologyObject object) {
		lazyInit();
		if (!chronology().contains(date)) return null;
		if (date.before(mergedDate)) {
			return prefix.getFinding(date, object);
		}
		else {
			Finding testCaseFinding = testCase.getFinding(toTestCaseDate(date), object);
			return testCaseFinding == null ? null : toPrefixFinding(testCaseFinding);
		}
	}

	@Override
	public Collection<Check> getChecks(Date date, KnowledgeBase kb) {
		lazyInit();
		if (!chronology().contains(date)) return Collections.emptyList();
		if (date.before(mergedDate)) {
			return prefix.getChecks(date, kb);
		}
		else if (date.after(mergedDate)) {
			return testCase.getChecks(toTestCaseDate(date), kb);
		}
		else {
			Collection<Check> prefixChecks = prefix.getChecks(date, kb);
			Collection<Check> testCaseChecks = testCase.getChecks(toTestCaseDate(date), kb);
			ArrayList<Check> mergedChecks = new ArrayList<Check>(prefixChecks.size()
					+ testCaseChecks.size());
			mergedChecks.addAll(prefixChecks);
			mergedChecks.addAll(testCaseChecks);
			return mergedChecks;
		}
	}

	@Override
	public Date getStartDate() {
		lazyInit();
		return prefix.getStartDate();
	}

	@Override
	public Collection<String> check(KnowledgeBase kb) {
		lazyInit();
		Collection<String> prefixMessages = prefix.check(kb);
		Collection<String> testCaseMessages = testCase.check(kb);
		ArrayList<String> mergedMessages = new ArrayList<String>(prefixMessages.size()
				+ testCaseMessages.size());
		mergedMessages.addAll(prefixMessages);
		mergedMessages.addAll(testCaseMessages);
		return mergedMessages;
	}

}
