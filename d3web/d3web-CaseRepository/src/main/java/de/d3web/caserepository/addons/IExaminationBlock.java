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

/*
 * Created on 22.09.2003
 */
package de.d3web.caserepository.addons;

import java.util.List;

import de.d3web.caserepository.ISolutionContainer;
import de.d3web.core.terminology.Diagnosis;
import de.d3web.core.terminology.QContainer;

/**
 * 22.09.2003 17:19:55
 * @author hoernlein
 */
public interface IExaminationBlock extends ISolutionContainer {
	
	public String getTitle();
	public void setTitle(String title);
	public String getId();

	public void addContent(QContainer q);
	public void removeContent(QContainer q);
	
	public List<QContainer> getContents();
	
    public String getCommentFor(Diagnosis d);
    public void setCommentFor(Diagnosis d, String s);
    
}
