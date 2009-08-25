package de.d3web.kernel.verbalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import de.d3web.kernel.psMethods.verbalizer.XclVerbalizer;

/**
 * This is the key class of the d3web-verbalizer. It handles the verbalization
 * of Knowledge objects. The VerbalizationManager can be ordered to "verbalize"
 * an object in a specified format (Plain Text/HTML/XML). The manager chooses
 * the registered verbalizer with the highest priority, that can perform this
 * task. Verbalizers can be registered/deregistered in the VerbalizationManager.
 * 
 * @author lemmerich
 * @date june 2008
 * 
 */
public class VerbalizationManager {

	//the standard priorities to choose, if several verbalizers could perform the same rendering
	public static final int PRIORITY_HIGH = 1000;
	public static final int PRIORITY_MEDIUM = 500;
	public static final int PRIORITY_LOW = 100;
	private static final int PRIORITY_MIN = 0;
	
	private static String[] specialStrings =  {"\n", "\r",
		".", ",", ";", "!", "@", "|", "#", "~",
		"{", "}", "[", "]", "(", ")", 
		"<", ">", "=", 
		"-", "+", "*", "/",
		"WENN", "IF",
		"DANN", "THEN",
		"UND", "AND",
		"ODER", "OR",
		"NICHT", "NOT",
		"VERBERGE", "HIDE",
		"AUSSER", "EXCEPT",
		"UNBEKANNT", "UNKNOWN",
		"SOFORT", "INSTANT", 
		"MINMAX",
		"KONTEXT", "CONTEXT",
		"SOWIE",
		"ENTFERNEN", "DELETE",
		"IN",
		"ALLE", "ALL",
		"SET",
		"&REF"};
	
	/**
	 * possible types of targets, to which can be rendered by a verbalizer
	 */
	public enum RenderingFormat {
		HTML, XML, PLAIN_TEXT
	};

	/**
	 * The singleton instance of VerbalizationManager
	 */
	private static VerbalizationManager instance;

	/**
	 * A list of Verbalizers registered in the Manager
	 */
	private ArrayList<Verbalizer> registeredVerbalizers = new ArrayList<Verbalizer>();

	/**
	 * This Hashmap saves the priorities of the Verbalizers
	 */
	private Map<Verbalizer, Integer> priorityHash;

	/**
	 * private constructor for singleton instantiation
	 */
	private VerbalizationManager() {
		priorityHash = new HashMap<Verbalizer, Integer>();
		register(new DefaultVerbalizer(), PRIORITY_MIN);
		// register(new RuleVerbalizer(), PRIORITY_LOW);
		register(new ConditionVerbalizer());
		register(new RuleActionVerbalizer());
		register(new DiagnosisVerbalizer());
		register(new QuestionVerbalizer());
		register(new AnswerVerbalizer());
		register(new XclVerbalizer());

	}

	/**
	 * Returns the singleton instance of VerbalizationManager. Lazy
	 * instantiation on demand.
	 * 
	 * @return the singleton instance of VerbalizationManager
	 */
	public static VerbalizationManager getInstance() {
		if (instance == null) {
			instance = new VerbalizationManager();

		}
		return instance;
	}

	/**
	 * registers a verbalizer in the Manager with medium Priority
	 * 
	 * @param verbalizer
	 *            the verbalizer to be registered
	 */
	public void register(Verbalizer verbalizer) {
		register(verbalizer, PRIORITY_MEDIUM);
	}

	/**
	 * registers a verbalizer in the Manager with given priority
	 * 
	 * @param verbalizer
	 *            the verbalizer to be registered
	 */
	public void register(Verbalizer verbalizer, int priority) {
		registeredVerbalizers.add(verbalizer);
		priorityHash.put(verbalizer, priority);
		Collections.sort(registeredVerbalizers, new VerbalizerComparator());
	}

	/**
	 * removes a verbalizer from the Manager
	 * 
	 * @param verbalizer
	 *            the verbalizer to be deregistered
	 */
	public void deregister(Verbalizer verbalizer) {
		registeredVerbalizers.remove(verbalizer);
		priorityHash.remove(verbalizer);
	}

	/**
	 * returns the priority of v in the VerbalizationManager, -1, if not
	 * registered
	 * 
	 * @param v
	 * @return the priority of v in the VerbalizationManager, -1, if not
	 *         registered
	 */
	public int getPriority(Verbalizer v) {
		if (!priorityHash.containsKey(v)) {
			return -1;
		}
		return priorityHash.get(v);
	}

	public void setPriority(Verbalizer v, int newPriority) {
		priorityHash.put(v, newPriority);
	}

	private static boolean containsSpecialStrings(String s) {
		for (int i = 0; i < specialStrings.length; i++) {
			if (s.contains(specialStrings[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static String quoteIfNecessary(String s) {
		if (containsSpecialStrings(s)) {
			return "\"" + s + "\"";
		}
		return s;
	}
	
	/**
	 * Returns a verbalization (String representation) of the given object in
	 * the target format without using additional parameters by calling an appropriate Verbalizer
	 * 
	 * 
	 * @param o
	 *            the Object to be verbalized
	 * @param targetFormat
	 *            The output format of the verbalization (HTML/XML/PlainText...)
	 * @return A String representation of given object o in the target format
	 */
	public String verbalize(Object o, RenderingFormat renderingTargetFormat) {
		return verbalize(o, renderingTargetFormat, null);
	}

	/**
	 * Returns a verbalization (String representation) of the given object in
	 * the target format using additional parameters by calling an appropriate Verbalizer. 
	 * 
	 * 
	 * @param o
	 *            the Object to be verbalized
	 * @param targetFormat
	 *            The output format of the verbalization (HTML/XML/PlainText...)
	 * @param parameter
	 *            additional parameters used to adapt the verbalization (e.g.,
	 *            singleLine, etc...)
	 * @return A String representation of given object o in the target format
	 */
	public String verbalize(Object o, RenderingFormat renderingTargetFormat, Map<String, Object> parameter) {
		// go through the verbalizers (sorted by priorities)
		for (Verbalizer v : registeredVerbalizers) {

			// can handle v the class of this object?
			boolean classCanBeHandled = false;
			for (Class c : v.getSupportedClassesForVerbalization()) {
				if (c.isInstance(o))
					classCanBeHandled = true;
			}
			// if the class cant be handled try next verbalizer
			if (!classCanBeHandled)
				continue;

			// class can be handled, now check, if the renderingType is
			// supported by this verbalizer
			if (Arrays.asList(v.getSupportedRenderingTargets()).contains(renderingTargetFormat)) {
				return v.verbalize(o, renderingTargetFormat, parameter);
			}
			// else continue with next verbalization
		}

		// this should never happen, as the DefaultVerbalizer can handle
		// everything!
		return null;
	}

	/**
	 * A private comparator, that compares Verbalizers: compares the priority of
	 * both verbalizers in the VerbalizerManager
	 * 
	 * @author lemmerich
	 * 
	 */
	private class VerbalizerComparator implements Comparator<Verbalizer> {

		@Override
		public int compare(Verbalizer o1, Verbalizer o2) {
			int priorityV1 = getPriority(o1);
			int priorityV2 = getPriority(o2);
			return priorityV2 - priorityV1;
		}

	}

}
