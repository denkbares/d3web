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

/*
 * Created on 20.06.2003
 */
package de.d3web.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author hoernlein
 */
public class IfTask extends Task {

	private boolean bool;
	private String target;

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void execute() throws BuildException {
		System.out.print("bool=\"" + bool + "\", ");
		if (bool) {
			System.out.println("executing \"" + target + "\".");
			project.executeTarget(target);
		}
		else {
			System.out.println("not executing \"" + target + "\".");
		}
	}

}
