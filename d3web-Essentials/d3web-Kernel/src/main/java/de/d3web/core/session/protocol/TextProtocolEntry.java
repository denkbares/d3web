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

package de.d3web.core.session.protocol;

import java.text.DateFormat;
import java.util.Date;

import com.denkbares.utils.EqualsUtils;
import com.denkbares.utils.HashCodeUtils;

/**
 * Implementation of {@link ProtocolEntry} to store pain text messages into the
 * session protocol.
 * 
 * @author volker_belli
 * @created 19.10.2010
 */
public class TextProtocolEntry implements ProtocolEntry {

	private final Date date;
	private final String message;

	/**
	 * Creates a new protocol entry for a specified message to the specified
	 * time. The timeMillis is the number of milliseconds since the standard
	 * base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT
	 * 
	 * @param timeMillis the date of the entry
	 * @param message the message to be stored
	 * @throws NullPointerException if null has been specified as message
	 */
	public TextProtocolEntry(long timeMillis, String message) {
		this(new Date(timeMillis), message);
	}

	/**
	 * Creates a new protocol entry for a specified message to the specified
	 * date.
	 * 
	 * @param date the date of the entry
	 * @param message the message to be stored
	 * @throws NullPointerException if null has been specified as date or
	 *         message
	 */
	public TextProtocolEntry(Date date, String message) {
		this.date = date;
		this.message = message;
		if (date == null) {
			throw new NullPointerException("specified date of protocol entry is null");
		}
		if (message == null) {
			throw new NullPointerException("specified message of protocol entry is null");
		}
	}

	@Override
	public Date getDate() {
		return this.date;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		int result = HashCodeUtils.SEED;
		result = HashCodeUtils.hash(result, date);
		result = HashCodeUtils.hash(result, getMessage());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TextProtocolEntry other = (TextProtocolEntry) obj;
		return EqualsUtils.equals(this.date, other.date)
				&& EqualsUtils.equals(this.getMessage(), other.getMessage());
	}

	@Override
	public String toString() {
		return "[" +
				DateFormat.getInstance().format(date) +
				"] " +
				this.getMessage();
	}

}
