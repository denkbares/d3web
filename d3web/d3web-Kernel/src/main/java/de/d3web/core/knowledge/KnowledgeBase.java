/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.core.knowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.d3web.core.inference.KnowledgeKind;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSConfig;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.manage.KnowledgeBaseUtils;

/**
 * The KnowledgeBase stores all terminology objects (Question, Solution, etc.)
 * and provides interfaces to access the problem-solving/dialog knowledge of the
 * domain.
 * 
 * New terminology objects should be added here and should be created using the
 * KnowlegdeBaseManagement factory. For the creation of problem-solving/dialog
 * knowledge, you should use the factories provided with the particular
 * KnowledgeSlices (e.g. XCLModel).
 * 
 * @author joba
 */
public class KnowledgeBase implements NamedObject {

	private final InfoStore infoStore = new DefaultInfoStore();

	private String kbID;

	// stores all qasets contained in the init questionnaires together with
	// their respective priority
	// TODO: vb: move this into a knowledge slice of PSMethodInit and indicate
	// them by that problem solver
	private Map<QASet, Integer> initQuestions = new HashMap<QASet, Integer>();

	private final List<Resource> resouces = new ArrayList<Resource>();

	private final List<PSConfig> psConfigs = new ArrayList<PSConfig>();

	private QASet rootQASet = null;

	private Solution rootSolution = null;

	private TerminologyManager manager = new TerminologyManager(this);

	private KnowledgeStore knowledgeStore = new DefaultKnowledgeStore();

	/**
	 * @return the unique identifier of this KnowledgeBase instance.
	 */
	@Override
	public String getId() {
		return kbID;
	}

	/**
	 * Sets a unique identifier for this KnowledgeBase instance.
	 * 
	 * @param id a unique identifier
	 * @author joba
	 * @date 15.04.2010
	 */
	public void setId(String id) {
		kbID = id;
	}

	/**
	 * Creates a new knowledge base instance. For the general creation of a
	 * knowledge base and its corresponding objects we recommend to use the
	 * {@link KnowledgeBaseUtils} class.
	 */
	public KnowledgeBase() {
		initQuestions = new HashMap<QASet, Integer>();
	}

	/**
	 * Collects and returns all {@link KnowledgeSlice} instances that are stored
	 * in this {@link KnowledgeBase}.
	 * 
	 * @return a {@link Collection} of all {@link KnowledgeSlice} instances
	 *         contained in the {@link KnowledgeBase}
	 */
	public Collection<KnowledgeSlice> getAllKnowledgeSlices() {
		Set<KnowledgeSlice> allKnowledgeSlices = new HashSet<KnowledgeSlice>(
				Arrays.asList(getKnowledgeStore().getKnowledge()));
		for (TerminologyObject to : manager.getAllTerminologyObjects()) {
			allKnowledgeSlices.addAll(Arrays.asList(to.getKnowledgeStore().getKnowledge()));
		}
		return allKnowledgeSlices;
	}

	/**
	 * Collects and returns all {@link KnowledgeSlice} instances that are stored
	 * in this {@link KnowledgeBase} for a specified problem-solver and
	 * specified access key.
	 * 
	 * @return Collection containing objects of type {@link KnowledgeSlice} for
	 *         the specified problem-solver and access key
	 * 
	 * @param problemsolver the specified problem-solver
	 * @param kind the specified access key how the knowledge is stored in the
	 *        problem-solver
	 * @return a {@link Collection} of {@link KnowledgeSlice} instances
	 *         containing all knowledge for the specified problem-solver and
	 *         access key
	 */
	public <T extends KnowledgeSlice> Collection<T> getAllKnowledgeSlicesFor(KnowledgeKind<T> kind) {
		Collection<T> slices = new LinkedList<T>();
		T ks = getKnowledgeStore().getKnowledge(kind);
		if (ks != null) slices.add(ks);
		for (TerminologyObject to : manager.getAllTerminologyObjects()) {
			ks = to.getKnowledgeStore().getKnowledge(kind);
			if (ks != null) slices.add(ks);
		}
		return slices;
	}

	/**
	 * Returns the ordered {@link List} of all initial questions (
	 * {@link Question})/questionnaires ({@link QContainer}) . These
	 * questions/questionnaires are prompted first when starting a new dialog.
	 * 
	 * @return a list of the initial questions/questionnaires
	 */
	public List<QASet> getInitQuestions() {
		Map<QASet, Integer> sortedInit = sortByValue(this.initQuestions);
		return new ArrayList<QASet>(sortedInit.keySet());
	}

	private static Map<QASet, Integer> sortByValue(Map<QASet, Integer> map) {
		List<Map.Entry<QASet, Integer>> list = new LinkedList<Map.Entry<QASet, Integer>>(
				map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<QASet, Integer>>() {

			@Override
			public int compare(Entry<QASet, Integer> o1, Entry<QASet, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
				// ((Comparable) ((Map.Entry) (o1)).getValue())
				// .compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		Map<QASet, Integer> result = new LinkedHashMap<QASet, Integer>();
		for (Map.Entry<QASet, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * The solutions contained in this {@link KnowledgeBase} are organized in a
	 * hierarchy. This method returns the root solution.
	 * 
	 * @return the root solution of this {@link KnowledgeBase}
	 * @author joba
	 * @date 15.04.2010
	 */
	public Solution getRootSolution() {
		return rootSolution;
	}

	/**
	 * The questionnaires and contained questions are organized in a hierarchy.
	 * This method returns the root object (usually a {@link QContainer}).
	 * 
	 * @return the root {@link QASet} instance of this {@link KnowledgeBase}
	 */
	public QASet getRootQASet() {
		return rootQASet;
	}

	/**
	 * Defines the ordered list of initial questions/questionnaires. This
	 * {@link List} of {@link QASet}s is prompted at the beginning of every new
	 * problem-solving session.
	 * 
	 * @param initQuestions the collection of init questions/questionnaires
	 * @author joba
	 * @date 15.04.2010
	 */
	public void setInitQuestions(List<? extends QASet> initQuestions) {
		this.initQuestions.clear();
		Integer priority = 1;
		for (QASet qaSet : initQuestions) {
			this.initQuestions.put(qaSet, priority);
			priority++;
		}
	}

	/**
	 * Adds the specified {@link Question} or {@link QContainer} to the list of
	 * init questions. This {@link List} of {@link QASet}s is prompted at the
	 * beginning of every new problem-solving session. Priority: A lower number
	 * means a higher priority, i.e., qasets with the lowest numbers are asked
	 * first.
	 * 
	 * @created 25.10.2010
	 * @param qaset the specified init question to be added
	 * @param priority a priority used to sort the specified question into the
	 *        list of already existing init questionnaires
	 * @return true, when the specified priority was already used before
	 */
	public boolean addInitQuestion(QASet qaset, int priority) {
		boolean alreadyused = this.initQuestions.values().contains(priority);
		this.initQuestions.put(qaset, priority);
		return alreadyused;
	}

	/**
	 * Removes the specified {@link Question} or {@link QContainer} from the
	 * list of init questions.
	 * 
	 * @created 25.10.2010
	 * @param qaset the specified init question to be removed
	 * @return true, when the specified qaset was successfully removed; false
	 *         otherwise
	 */
	public boolean removeInitQuestion(QASet qaset) {
		if (this.initQuestions.keySet().contains(qaset)) {
			this.initQuestions.remove(qaset);
			return true;
		}
		return false;
	}

	/**
	 * Returns a compact {@link String} representation of this
	 * {@link KnowledgeBase} instance (usually only the ID).
	 * 
	 * @return a compact {@link String} representation of this instance
	 */
	@Override
	public String toString() {
		return "KnowledgeBase ID: " + kbID;
	}

	/**
	 * Inserts a new resource for this {@link KnowledgeBase} instance. For
	 * example, a resource is a multimedia file attached to the
	 * {@link KnowledgeBase}.
	 * <p>
	 * The path is represented from its root path without any trailing "/". The
	 * path folder separator used is always "/", independent from the underlying
	 * file system.
	 * 
	 * @param resource a new resource
	 * @author joba
	 * @date 15.04.2010
	 */
	public void addResouce(Resource resource) {
		this.resouces.add(resource);
	}

	/**
	 * Returns all resources stored in this {@link KnowledgeBase} instance. For
	 * example, a multi-media is a resource.
	 * 
	 * @return all resources of this knowledge base
	 * @author joba
	 * @date 15.04.2010
	 */
	public List<Resource> getResources() {
		return Collections.unmodifiableList(this.resouces);
	}

	/**
	 * Returns a stored resource for a specified pathname accessor (a relative
	 * path that must not start with "/").
	 * 
	 * @param pathname the specified pathname accessor.
	 * @return a resource stored by the specified accessor
	 * @author joba
	 * @date 15.04.2010
	 */
	public Resource getResource(String pathname) {
		for (Resource resource : resouces) {
			if (pathname.equalsIgnoreCase(resource.getPathName())) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * Returns the configurations of all registered problem-solvers.
	 * 
	 * @return the list of problem-solver configurations sorted by priority
	 */
	public List<PSConfig> getPsConfigs() {
		// the list is sorted
		Collections.sort(psConfigs);
		return Collections.unmodifiableList(psConfigs);
	}

	/**
	 * Inserts a new problem-solver configuration.
	 * 
	 * @param psConfig the new problem-solver configuration
	 * @author joba
	 * @date 15.04.2010
	 */
	public void addPSConfig(PSConfig psConfig) {
		psConfigs.add(psConfig);
	}

	/**
	 * Removes a specified problem-solver configuration.
	 * 
	 * @param psConfig the specified problem-solver configuration
	 * @author joba
	 * @date 15.04.2010
	 */
	public void removePSConfig(PSConfig psConfig) {
		psConfigs.remove(psConfig);
	}

	public void setRootQASet(QASet rootQASet) {
		this.rootQASet = rootQASet;
		if (!manager.getQASets().contains(rootQASet)) {
			manager.putTerminologyObject(rootQASet);
		}
	}

	public void setRootSolution(Solution rootSolution) {
		this.rootSolution = rootSolution;
		if (!manager.getSolutions().contains(rootSolution)) {
			manager.putTerminologyObject(rootSolution);
		}
	}

	@Override
	public String getName() {
		return getInfoStore().getValue(MMInfo.PROMPT);
	}

	@Override
	public InfoStore getInfoStore() {
		return infoStore;
	}

	public TerminologyManager getManager() {
		return manager;
	}

	public KnowledgeStore getKnowledgeStore() {
		return knowledgeStore;
	}

}