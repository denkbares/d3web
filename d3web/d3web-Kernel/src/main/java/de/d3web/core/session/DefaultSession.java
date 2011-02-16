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

package de.d3web.core.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import de.d3web.core.inference.DefaultPropagationManager;
import de.d3web.core.inference.PSConfig;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PropagationManager;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.DefaultBlackboard;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.core.session.interviewmanager.DefaultInterview;
import de.d3web.core.session.interviewmanager.FormStrategy;
import de.d3web.core.session.interviewmanager.Interview;
import de.d3web.core.session.interviewmanager.NextUnansweredQuestionFormStrategy;
import de.d3web.core.session.protocol.DefaultProtocol;
import de.d3web.core.session.protocol.Protocol;
import de.d3web.plugin.Autodetect;
import de.d3web.plugin.Extension;
import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginConfig;
import de.d3web.plugin.PluginEntry;
import de.d3web.plugin.PluginManager;

/**
 * The {@link DefaultSession} is the default implementation of {@link Session}.
 * Here, the {@link Blackboard}, {@link Interview}, and
 * {@link PropagationManager} are managed, that together represent the behavior
 * of a {@link Session}.
 * 
 * @author joba
 * @see SessionObject
 */
public class DefaultSession implements Session {

	private final KnowledgeBase kb;
	private final DefaultPropagationManager propagationController;
	private final Interview interview;

	private final Map<CaseObjectSource, SessionObject> dynamicStore;

	private final Blackboard blackboard;
	private Protocol protocol = new DefaultProtocol();

	private final Collection<PSMethod> usedPSMethods;

	private final Date created;
	private Date edited;

	private final String name;
	private final InfoStore infoStore = new SessionInfoStore(this);

	protected DefaultSession(String name, KnowledgeBase knowledgebase, Date creationDate) {
		this(name, knowledgebase, creationDate, true);
	}

	protected DefaultSession(String name, KnowledgeBase knowledgebase, Date creationDate, boolean psm) {
		// check that we have a valid date
		if (creationDate == null) {
			creationDate = new Date();
		}
		if (name != null) {
			this.name = name;
		}
		else {
			this.name = createNewCaseName();
		}
		this.created = creationDate;
		this.edited = creationDate;
		this.kb = knowledgebase;
		this.blackboard = new DefaultBlackboard(this);
		this.dynamicStore = new HashMap<CaseObjectSource, SessionObject>();
		// add problem-solving methods used for this case
		this.usedPSMethods = new TreeSet<PSMethod>(new PSMethodComparator());
		this.propagationController = new DefaultPropagationManager(this);

		// Interview should be defined very late, since it uses blackboard
		this.interview = new DefaultInterview(this);
		this.interview.setFormStrategy(new NextUnansweredQuestionFormStrategy());
		this.protocol = new DefaultProtocol();
		if (psm) {
			// register some common problem solving methods
			// first add the methods
			for (PSMethod method : SessionFactory.getDefaultPSMethods()) {
				addUsedPSMethod(method);
			}
			addPlugedPSMethods(knowledgebase);

			// after adding the ps methods, we init inside a propagation,
			// because it may also
			// add facts to the blackboard that require the start date of the
			// case
			PropagationManager propagationContoller = getPropagationManager();
			propagationContoller.openPropagation(this.created.getTime());
			try {
				for (PSMethod method : this.usedPSMethods) {
					method.init(this);
				}
			}
			finally {
				propagationContoller.commitPropagation();

			}
		}

		// TODO: add knowlegde base id, name, version
		// into the case header (dc markup?, properties?)

	}

	private String createNewCaseName() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Adds the PSMethods from Plugins
	 * 
	 * @created 24.09.2010
	 * @param knowledgebase {@link KnowledgeBase}
	 */
	private void addPlugedPSMethods(KnowledgeBase knowledgebase) {
		// get PluginConfiguration
		PluginConfig pc = PluginConfig.getPluginConfig(knowledgebase);
		// add plugged PS with default config, only if none instance of this
		// plugin was configured in the kb
		// psMethods with state deactivated are not inserted
		for (Extension e : PluginManager.getInstance().getExtensions(
				"d3web-Kernel-ExtensionPoints", PSMethod.EXTENSIONPOINT_ID)) {
			PSMethod psMethod = (PSMethod) e.getNewInstance();
			boolean found = false;
			for (PSConfig psConfig : knowledgebase.getPsConfigs()) {
				PSMethod psm = psConfig.getPsMethod();
				if (psm == null || psMethod == null) {
					continue;
				}
				if (psm.getClass().equals(psMethod.getClass())) {
					found = true;
					break;
				}
			}
			if (found) {
				continue;
			}
			// get PluginEntry, if none is found, one will be created
			PluginEntry pluginEntry = pc.getPluginEntry(e.getPluginID());
			if (pluginEntry == null) {
				Plugin plugin = PluginManager.getInstance().getPlugin(e.getPluginID());
				pluginEntry = new PluginEntry(plugin, false, true);
				pc.addEntry(pluginEntry);
			}
			// get autodetect of the psMethod
			Autodetect auto = pluginEntry.getAutodetect();
			// add the newly created configuration
			PSConfig psConfig = new PSConfig(PSConfig.PSState.autodetect, psMethod, auto,
					e.getID(), e.getPluginID(), e.getPriority());
			knowledgebase.addPSConfig(psConfig);
		}
		// adding preconfigured psmethods from the kb to the case
		for (PSConfig psConfig : knowledgebase.getPsConfigs()) {
			checkStateAndInsertPSM(knowledgebase, psConfig);
		}
	}

	DefaultSession(String id, KnowledgeBase knowledgebase, FormStrategy formStrategy, Date creationDate) {
		this(id, knowledgebase, creationDate);
		getInterview().setFormStrategy(formStrategy);
	}

	private void checkStateAndInsertPSM(KnowledgeBase kb, PSConfig psConfig) {
		if (psConfig.getPsState() == PSConfig.PSState.autodetect) {
			Autodetect auto = psConfig.getAutodetect();
			// if it is set to autodetect and there is no implementation
			// the psmethod is added
			if (auto == null) {
				addUsedPSMethod(psConfig.getPsMethod());
			}
			else {
				if (auto.check(kb)) {
					addUsedPSMethod(psConfig.getPsMethod());
				}
			}
		}
		else if (psConfig.getPsState() == PSConfig.PSState.active) {
			addUsedPSMethod(psConfig.getPsMethod());
		}
	}

	/*
	 * @see de.d3web.kernel.domainModel.IDReference#getId()
	 */
	@Override
	@Deprecated
	public String getId() {
		return getName();
	}

	@Override
	public Interview getInterview() {
		return interview;
	}

	@Override
	public Protocol getProtocol() {
		return this.protocol;
	}

	/**
	 * Adds a new PSMethod to the used PSMethods of this case and intiliazes it.
	 * 
	 */
	private void addUsedPSMethod(PSMethod psmethod) {
		touch();
		if (usedPSMethods.contains(psmethod)) {
			return;
		}
		this.usedPSMethods.add(psmethod);
	}

	/**
	 * @return the unique and dynamic user case object for the specified
	 *         (static) knowledge base object.
	 */
	@Override
	public SessionObject getCaseObject(CaseObjectSource cos) {
		SessionObject co = dynamicStore.get(cos);
		if (co == null) {
			co = cos.createCaseObject(this);
			dynamicStore.put(cos, co);
		}
		return co;
	}

	/**
	 * @return knowledge base used in the case.
	 */
	@Override
	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	/**
	 * @return the instance of a specified PSMethod class definition, which is
	 *         used in this Session; null if the PSMethod-class is not used in
	 *         this case.
	 * @param psmethodClass java.lang.Class
	 */
	@Override
	public <T extends PSMethod> T getPSMethodInstance(Class<T> context) {
		for (PSMethod psm : usedPSMethods) {
			if (psm.getClass().equals(context)) {
				return context.cast(psm);
			}
		}
		return null;
	}

	@Override
	public List<PSMethod> getPSMethods() {
		return new ArrayList<PSMethod>(usedPSMethods);
	}

	@Override
	public Blackboard getBlackboard() {
		return blackboard;
	}

	// ******************** event notification *********************

	private final Collection<SessionEventListener> listeners = new LinkedList<SessionEventListener>();

	/**
	 * this listener will be notified, if some value has been set in this case
	 */
	@Override
	public void addListener(SessionEventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(SessionEventListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	@Override
	public PropagationManager getPropagationManager() {
		return propagationController;
	}

	public void notifyListeners(TerminologyObject o) {
		for (SessionEventListener listener : listeners) {
			listener.notify(this, o, this);
		}
	}

	// ******************** /event notification ********************

	@Override
	public void touch() {
		edited = new Date();

	}

	@Override
	public void touch(Date date) {
		edited = date;
	}

	@Override
	public Date getCreationDate() {
		return created;
	}

	@Override
	public Date getLastChangeDate() {
		return edited;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InfoStore getInfoStore() {
		return infoStore;
	}

	/**
	 * Is used to sort PSMethods by priority
	 * 
	 * @author Markus Friedrich (denkbares GmbH)
	 * @created 01.02.2011
	 */
	private static class PSMethodComparator implements Comparator<PSMethod> {

		@Override
		public int compare(PSMethod o1, PSMethod o2) {
			if (o1 == o2) {
				return 0;
			}
			else if (o1.getClass().equals(o2.getClass())) {
				throw new IllegalArgumentException(
						"Adding two variants of one psmethod to one session is not forbidden.");
			}
			else if (o1.getPriority() < o2.getPriority()) {
				return -1;
			}
			else if (o1.getPriority() > o2.getPriority()) {
				return 1;
			}
			else {
				return o1.getClass().toString().compareTo(o2.getClass().toString());
			}
		}

	}

}