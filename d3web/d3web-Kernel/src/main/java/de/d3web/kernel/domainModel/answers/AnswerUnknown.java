package de.d3web.kernel.domainModel.answers;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.supportknowledge.Property;

/**
 * special answer: "unknown"
 * Creation date: (13.09.2000 13:51:37)
 * @author norman
 */
public class AnswerUnknown extends Answer {
	public final static String UNKNOWN_ID = "MaU";
	
	public final static String UNKNOWN_VALUE = "-?-";

	/**
	 * Creates a new AnswerUnknown object
	 */
	public AnswerUnknown() {
	    super();
	}
	
	public AnswerUnknown(String id) {
	    super(id);
	}

	/**
	 * @return true iff otherAnswer is of same type and not null
	 */
	public boolean equals(Object otherAnswer) {
		if (otherAnswer == null)
			return false;
		return this == otherAnswer || otherAnswer instanceof AnswerUnknown;
	}

	/**
	 * Creation date: (15.09.2000 11:07:48)
	 * @return AnswerUnknown.UNKNOWN_VALUE
	 */
	public Object getValue(XPSCase theCase) {

		String result = null;
		if (getQuestion() != null) {
			try {
				result =
					(String) getQuestion().getProperties().getProperty(
						Property.UNKNOWN_VERBALISATION);
			} catch (RuntimeException e) {
			}
		}

		if (result != null) {
			return result;
		}

		if (theCase != null) {
			try {
				result =
					(String) theCase
						.getKnowledgeBase()
						.getProperties()
						.getProperty(
						Property.UNKNOWN_VERBALISATION);
			} catch (RuntimeException e) {
			}
		}
		if (result != null) {
			return result;
		}

		return AnswerUnknown.UNKNOWN_VALUE;
	}

	/**
	 * Creation date: (13.09.2000 13:54:21)
	 * @return true
	 */
	public boolean isUnknown() {
		return true;
	}

	public String getId() {
		return UNKNOWN_ID;
	}

	/**
	 * Creation date: (15.03.2001 17:00:52)
	 * @return true, iff o is instanceof AnswerUnknown
	 */
	public static boolean isUnknownAnswer(Object o) {
		return o instanceof AnswerUnknown;
	}

	public String toString() {
		return UNKNOWN_VALUE;
	}
	
	/* (non-Javadoc)
	 * @see de.d3web.kernel.domainModel.Answer#hashCode()
	 */
	public int hashCode() {
		return getId().hashCode();
	}

}