package de.d3web.kernel.domainModel.answers;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Answer;

/**
 * Answer (alternative) class for choice questions
 * Creation date: (13.09.2000 14:32:50)
 * @author norman
 */
public class AnswerChoice extends Answer {
	private String text;

	/**
	 * Creates a new AnswerChoice object
	 */
	public AnswerChoice() {
		super();
	}
	
	public AnswerChoice(String id) {
	    super(id);
	}

	public String getText() {
		return text;
	}

	/**
	 * Creation date: (15.09.2000 11:03:33)
	 * @return the value of this answer object depending on the current case
	 */
	public Object getValue(XPSCase theCase) {
		return getText();
	}

	/**
	 * Creation date: (28.09.00 17:50:31)
	 * @return true, if this is an as AnswerNo configured answer (false here)
	 */
	public boolean isAnswerNo() {
		return false;
	}

	/**
	 * Creation date: (28.09.00 17:50:14)
	 * @return true, if this is an as AnswerYes configured answer (false here)
	 */
	public boolean isAnswerYes() {
		return false;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Creation date: (15.09.2000 12:07:31)
	 * @return String representation of the answer
	 */
	public String toString() {
		return getText();
	}

	/**
	 * Firstly compares for equal reference, then for equal 
	 * class instance, then for equal getText() values.
	 * <BR>
	 * 2002-05-29 joba: added for better comparisons
	 * */
	public boolean equals(Object other) {
		if (this == other)
			return true;
		else if (other instanceof AnswerChoice)
			return ((AnswerChoice) other).getId().equals(this.getId());
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see de.d3web.kernel.domainModel.Answer#hashCode()
	 */
	public int hashCode() {
		return getId().hashCode() + getText().hashCode();
	}

}
