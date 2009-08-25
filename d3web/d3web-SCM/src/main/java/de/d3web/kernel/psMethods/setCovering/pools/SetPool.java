package de.d3web.kernel.psMethods.setCovering.pools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This is a pool for java.util.Set-Objects. It helps to reuse Sets instead of
 * creating a new one every time a Set is needed.
 * 
 * @author bates
 * 
 */
public class SetPool {

	public static final int GENERATION_COUNT = 100;
	
	//Since the sets-list causes strange NullPointer and NoSuchElementExceptions after some thousand runs - switched off 
	public final static boolean POOL_ACTIVE = false;

	private static SetPool instance = null;
	private List sets = null;

	private SetPool() {
		sets = new LinkedList();
		generateSets();
	}

	/**
	 * @return the one and only instance of this Pool
	 */
	public static SetPool getInstance() {
		if (instance == null) {
			instance = new SetPool();
		}
		return instance;
	}

	public void initialize() {
		sets.clear();
	}

	private void generateSets() {
		for (int i = 0; i < GENERATION_COUNT; ++i) {
			sets.add(new HashSet());
		}
	}

	/**
	 * @return an empty Set from this pool
	 */
	public Set getEmptySet() {
		//if(!POOL_ACTIVE) { return new HashSet();}
		if (sets.isEmpty()) {
			generateSets();
		}
		Set ret = (Set) sets.get(0);
		sets.remove(0);
		return ret;
	}

	/**
	 * @param elements
	 *            elements to fill the Set with
	 * @return an empty set filled with the given elements
	 */
	public Set getFilledSet(Object[] elements) {
		if (elements == null) {
			throw new IllegalArgumentException("elements must not be null!");
		}
		Set ret = getEmptySet();
		for (int i = 0; i < elements.length; ++i) {
			ret.add(elements[i]);
		}
		return ret;
	}

	/**
	 * By using this method you can give a used Set back to the pool. It will be
	 * emptied and put to internal Set-Stack.
	 * 
	 * @param set
	 *            Set to push back
	 */
	public void free(Set set) {
		set.removeAll(set);
		sets.add(set);
	}

	/**
	 * @return the current size of the internal Set-stack
	 */
	public int getCurrentSetCount() {
		return sets.size();
	}

}
