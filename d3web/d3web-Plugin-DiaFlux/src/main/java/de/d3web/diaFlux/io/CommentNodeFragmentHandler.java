/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.diaFlux.io;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.CommentNode;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;


/**
 * 
 * @author Reinhard Hatko
 * @created 11.11.2010
 */
public class CommentNodeFragmentHandler extends
		AbstractNodeFragmentHandler {

	private static final String COMMENT = "Comment";

	@Override
	public Object read(KnowledgeBase kb, Element element) throws IOException {
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");

		return FlowFactory.getInstance().createCommentNode(id, name);
	}

	@Override
	public Element write(Object object, Document doc) throws IOException {
		INode node = (INode) object;
		Element nodeElement = createNodeElement(node, doc);

		Element commentElement = doc.createElement(COMMENT);
		nodeElement.appendChild(commentElement);

		commentElement.appendChild(doc.createTextNode(node.getName()));

		return nodeElement;
	}

	@Override
	public boolean canRead(Element element) {
		return element.getElementsByTagName(COMMENT).getLength() == 1;
	}

	@Override
	public boolean canWrite(Object object) {
		return object instanceof CommentNode;
	}

}