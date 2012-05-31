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
package cc.denkbares.testing;

/**
 * A message contains a type which is one of SUCCESS, FAILURE, or ERROR and an
 * (optional) message text.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 21.05.2012
 */
public class Message {

	private final Type type;
	private final String message;

	public Message(Type type) {
		this(type, null);
	}

	public Type getType() {
		return type;
	}

	public String getText() {
		return message;
	}

	public Message(Type type, String message) {
		this.type = type;
		this.message = message;
	}

	public boolean isSuccess() {
		return type == Type.SUCCESS;
	}

	public enum Type {
		SUCCESS,
		FAILURE,
		ERROR
	}
}
