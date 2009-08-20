package de.d3web.kernel.dialogControl.exceptions;

import java.util.List;

import de.d3web.kernel.domainModel.KnowledgeBase;

/**
 * This is a marker class that will be returned by moveToPrevious method of DialogController
 * if the history limit has been reached
 * @author Norman Brümmer
 */
public class ExceptionQuestionPrevious extends ExceptionQuestion {

	public ExceptionQuestionPrevious() {
		super();
	}

	public ExceptionQuestionPrevious(
		KnowledgeBase kb,
		String id,
		String text) {
		super(kb, id, text);
	}

	public ExceptionQuestionPrevious(
		KnowledgeBase kb,
		String id,
		String text,
		List children) {
		super(kb, id, text, children);
	}
	
	/**
	 * statically equals test (compares the classes!)
	 */
	public boolean equals(Object other) {
		return other.getClass() == this.getClass();
	}	
}