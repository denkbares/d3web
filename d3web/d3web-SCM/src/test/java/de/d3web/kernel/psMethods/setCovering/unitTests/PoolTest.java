/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.kernel.psMethods.setCovering.unitTests;

import java.util.Collections;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.d3web.kernel.psMethods.setCovering.Hypothesis;
import de.d3web.kernel.psMethods.setCovering.SCDiagnosis;
import de.d3web.kernel.psMethods.setCovering.pools.HypothesisPool;
import de.d3web.kernel.psMethods.setCovering.pools.SetPool;

/**
 * This is a test class for all used pools
 * 
 * @author bates
 * 
 */
public class PoolTest extends TestCase {

	private SetPool setPool = null;
	private HypothesisPool hypothesisPool = null;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PoolTest.suite());
	}

	public static TestSuite suite() {
		return new TestSuite(PoolTest.class);
	}

	public void setUp() {
		setPool = SetPool.getInstance();
		setPool.initialize();
		hypothesisPool = HypothesisPool.getInstance();
	}

	public void testSetPool() {
		// at the beginning the pool must have the specified element count
		assertEquals("wrong pool size at beginning", 0, setPool.getCurrentSetCount());

		// when retrieving an empty Set from the empty pool, GENERATION_COUNT
		// new Sets will be created.
		// so GENERATION_COUNT-1 Sets are left.
		setPool.getEmptySet();
		assertEquals("wrong Set count", SetPool.GENERATION_COUNT - 1, setPool.getCurrentSetCount());

		// checking, if the pool will be refilled automatically
		int currentCount = setPool.getCurrentSetCount();
		for (int i = 0; i < currentCount + 1; ++i) {
			setPool.getEmptySet();
		}
		assertEquals("pool size not correct (has to be refilled!)", currentCount, setPool
				.getCurrentSetCount());
	}

	public void testHypothesisPool() {
		// at the beginning the pool must have the specified element count
		int count = hypothesisPool.getCurrentHypothesesCount();
		assertEquals("wrong hypotheses count at beginning", HypothesisPool.GENERATION_COUNT, count);

		// when retrieving a Hypothesis from the empty pool, GENERATION_COUNT
		// new hypotheses will be created.
		// so GENERATION_COUNT-1 Hypotheses are left.
		SCDiagnosis scDiagnosis = new SCDiagnosis();
		Hypothesis hypothesis = hypothesisPool.getHypothesis(new Object[]{scDiagnosis});
		assertEquals("wrong hypothesis count", HypothesisPool.GENERATION_COUNT - 1, hypothesisPool
				.getCurrentHypothesesCount());

		// check, if the Diagnosis is placed correctly
		assertTrue("element set in hypothesis must not be null or empty", (hypothesis
				.getSCDiagnoses() != null)
				&& !hypothesis.getSCDiagnoses().isEmpty());
		assertTrue("scDiagnosis not placed correctly", hypothesis.getSCDiagnoses().contains(
				scDiagnosis));

		// checking, if the pool will be refilled automatically
		int currentCount = hypothesisPool.getCurrentHypothesesCount();
		for (int i = 0; i < currentCount + 1; ++i) {
			hypothesisPool.getHypothesis(Collections.EMPTY_SET);
		}
		assertEquals("pool size not correct (has to be refilled!)", currentCount, hypothesisPool
				.getCurrentHypothesesCount());
	}

}
