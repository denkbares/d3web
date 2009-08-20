package de.d3web.kernel.dialogControl.exceptions;

import java.util.LinkedList;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;

/**
 * This type of Question will be returned by a moveTo... method of
 * DialogController, if the history limit has been reached
 * 
 * @see ExceptionQuestionNext
 * @see ExceptionQuestionPrevious
 * @author Norman Brümmer
 */
public abstract class ExceptionQuestion extends Question {

    public ExceptionQuestion() {
	super();
    }

    /**
     * fulfils the super-contructor
     */
    public ExceptionQuestion(KnowledgeBase kb, String id, String text) {
	super(id);
	setKnowledgeBase(kb);
	setText(text);
    }

    /**
     * fulfils the super-contructor
     */
    public ExceptionQuestion(KnowledgeBase kb, String id, String text,
	    List children) {
	super(id);
	setKnowledgeBase(kb);
	setText(text);
	setChildren(children);
    }

    /**
     * does nothing here
     */
    public void addContraReason(Object source, XPSCase theCase) {/*
								     * does
								     * nothing
								     */
    }

    /**
     * does nothing here
     */
    public void addProReason(Object source, XPSCase theCase) {/* does nothing */
    }

    /**
     * Won´t return a XPSCaseObject, because this is just a "marker"-Class
     * 
     * @return null
     */
    public XPSCaseObject createCaseObject() {
	return null;
    }

    /**
     * nothing to expand
     * 
     * @return false
     */
    public boolean expand(List onList, XPSCase theCase) {
	return false;
    }

    /**
     * this marker class must not return any answer
     * 
     * @return null
     */
    public de.d3web.kernel.domainModel.Answer getAnswer(Object value) {
	return null;
    }

    /**
     * has no value
     * 
     * @return an empty List
     */
    public List getValue(XPSCase theCase) {
	return new LinkedList();
    }

    /**
     * this marker class never gets a value
     * 
     * @return false
     */
    public boolean hasValue(XPSCase theCase) {
	return false;
    }

    /**
     * @return true
     */
    public boolean isDone(XPSCase theCase) {
	return true;
    }

    /**
     * has no pro or contra reasons. just a marker class
     */
    public void removeContraReason(Object source,
	    de.d3web.kernel.XPSCase theCase) {
    }

    /**
     * has no pro or contra reasons. just a marker class
     */
    public void removeProReason(Object source, de.d3web.kernel.XPSCase theCase) {
    }

    /**
     * does nothing because this marker class needs no value
     */
    public void setValue(de.d3web.kernel.XPSCase theCase,
	    java.lang.Object[] values) {
    }

    public String toString() {
	return "ExQ";
    }

}