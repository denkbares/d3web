package de.d3web.utilities;

import java.util.Set;



/**
 * A HashMap that conatin IdentiyHashSets as values.
 * 
 * @author Peter Klügl
 *
 */
public class IdentitySetMap<Key, Type> extends AbstractSetMap<Key, Type>{

	private static final long serialVersionUID = -7932985293685168247L;


	public boolean add(Key key, Type object) {
		Set<Type> coll = get(key);
		if(coll == null) {
			coll = new IdentityHashSet<Type>();
			put(key, coll);
		}
		return coll.add(object);
	}

		
}
