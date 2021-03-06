/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.diaFlux.test;

import org.junit.Test;

/**
 * 
 * @author Reinhard Hatko
 * @created 06.12.2010
 */
public class AutostartTest extends AbstractDiaFluxTest {

	private static final String FILE = "AutostartTest.d3web";

	public AutostartTest() {
		super(FILE);
	}

	@Test
	public void testActivated() throws Exception {
		assertActiveNodes(Flow1, start1, start2);
	}

	@Test
	public void testNotActivated() throws Exception {
		assertInactiveNodes(Flow2, start1, start2);
	}

}
