/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.core.records.filter;

import de.d3web.core.records.SessionRecord;

/**
 * Interface to filter {@link SessionRecord}s
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 07.03.2011
 */
public interface Filter {

	/**
	 * Returns true when the record matches the filters requirements
	 * 
	 * @created 07.03.2011
	 * @param record {@link SessionRecord}
	 * @return true if it matches the requirements, false otherwise
	 */
	boolean match(SessionRecord record);
}
