/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.test.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestPersistence;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.stc.STCWrapper;
import de.d3web.testing.Message;
import de.d3web.testing.TestObjectProviderManager;

/**
 * Test the behavior of the class TestCaseTest
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 30.07.2012
 */
public class TestCaseTestTester {

	@Test
	public void testTestCaseTestFalse() {
		try {
			InitPluginManager.init();
			TestObjectProviderManager.registerTestObjectProvider(new JUnitTestKnowledgeBaseProvider());
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		de.d3web.test.TestCaseTest test = new de.d3web.test.TestCaseTest();

		List<TestCase> testcases = readTestCases(new File(
				"./src/test/resources/Demo_-_Test_Cases_testcase.xml"));

		TestCase case1 = testcases.get(0);
		Message result1 = test.execute(case1, new String[] { "Car faults diagnosis" });
		assertEquals(result1.getText(), Message.Type.SUCCESS, result1.getType());

		TestCase case2 = testcases.get(1);
		Message result2 = test.execute(case2, new String[] { "Car faults diagnosis" });
		assertEquals(result2.getText(), Message.Type.SUCCESS, result2.getType());

	}

	@Test
	public void testTestCaseTestTrue() {
		try {
			InitPluginManager.init();
			TestObjectProviderManager.registerTestObjectProvider(new JUnitTestKnowledgeBaseProvider());
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		de.d3web.test.TestCaseTest test = new de.d3web.test.TestCaseTest();

		List<TestCase> testcases = readTestCases(new File(
				"./src/test/resources/Demo_-_Test_Cases_testcase2.xml"));
		// Leaking air intake system: Derived: UNCLEAR; expected: UNCLEAR;
		TestCase case1 = testcases.get(0);
		Message result1 = test.execute(case1, new String[] { "Car faults diagnosis" });
		assertEquals(result1.getText(), Message.Type.FAILURE, result1.getType());
	}

	private List<TestCase> readTestCases(File file) {
		List<SequentialTestCase> stcaList = readSTC(file);
		List<TestCase> result = new ArrayList<TestCase>();
		for (SequentialTestCase sequentialTestCase : stcaList) {
			TestCase tc = new STCWrapper(sequentialTestCase);
			result.add(tc);
		}
		return result;
	}

	private List<SequentialTestCase> readSTC(File file) {
		try {
			KnowledgeBase lkb = new LazyKnowledgeBase();
			URL url = file.toURI().toURL();
			List<SequentialTestCase> cases = TestPersistence.getInstance().loadCases(url, lkb);
			return cases;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<SequentialTestCase>(0);
	}

}

class LazyKnowledgeBase extends KnowledgeBase {

	private final LazyTerminologyManager manager;

	public LazyKnowledgeBase() {
		manager = new LazyTerminologyManager(this);
	}

	@Override
	public TerminologyManager getManager() {
		return manager;
	}

	private static class LazyTerminologyManager extends TerminologyManager {

		private final KnowledgeBase knowledgeBase;

		public LazyTerminologyManager(KnowledgeBase knowledgeBase) {
			super(knowledgeBase);
			this.knowledgeBase = knowledgeBase;
		}

		@Override
		public Question searchQuestion(String questionName) {
			Question question = super.searchQuestion(questionName);
			if (question == null) {
				question = new QuestionText(knowledgeBase, questionName);
			}
			return question;
		}

		@Override
		public Solution searchSolution(String solutionName) {
			Solution solution = super.searchSolution(solutionName);
			if (solution == null) {
				solution = new Solution(knowledgeBase, solutionName);
			}
			return solution;
		}

	}
}
