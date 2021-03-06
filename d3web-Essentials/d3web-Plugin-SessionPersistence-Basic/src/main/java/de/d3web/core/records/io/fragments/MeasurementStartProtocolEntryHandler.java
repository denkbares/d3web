/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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

package de.d3web.core.records.io.fragments;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.w3c.dom.Element;

import com.denkbares.strings.Strings;
import de.d3web.core.io.Persistence;
import de.d3web.core.io.fragments.FragmentHandler;
import de.d3web.core.io.utilities.XMLUtil;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.session.protocol.MeasurementStartProtocolEntry;

/**
 * FragmentHandler for {@link MeasurementStartProtocolEntry}
 *
 * @author Jonas Müller
 * @created 15.02.18
 */
public class MeasurementStartProtocolEntryHandler implements FragmentHandler<SessionRecord> {
	private static final String ELEMENT_NAME = "entry";
	private static final String ELEMENT_TYPE = "measurementStart";
	private static final String ATTR_QUESTION_NAME = "questionName";
	private static final String ATTR_DATE = "date";

	@Override
	public Object read(Element element, Persistence<SessionRecord> persistence) throws IOException {
		try {
			String dateString = element.getAttribute(ATTR_DATE);
			Date date = Strings.readDate(dateString, Strings.DATE_FORMAT_COMPATIBILITY);
			String questionName = element.getAttribute(ATTR_QUESTION_NAME);
			return new MeasurementStartProtocolEntry(questionName, date);
		}
		catch (ParseException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Element write(Object object, Persistence<SessionRecord> persistence) throws IOException {
		MeasurementStartProtocolEntry entry = (MeasurementStartProtocolEntry) object;
		String dateString = Strings.writeDate(entry.getDate());

		Element element = persistence.getDocument().createElement(ELEMENT_NAME);
		element.setAttribute("type", ELEMENT_TYPE);
		element.setAttribute(ATTR_DATE, dateString);
		element.setAttribute(ATTR_QUESTION_NAME, entry.getQuestionName());
		return element;
	}

	@Override
	public boolean canRead(Element element) {
		return XMLUtil.checkNameAndType(element, ELEMENT_NAME, ELEMENT_TYPE);
	}

	@Override
	public boolean canWrite(Object object) {
		return object instanceof MeasurementStartProtocolEntry;
	}
}
