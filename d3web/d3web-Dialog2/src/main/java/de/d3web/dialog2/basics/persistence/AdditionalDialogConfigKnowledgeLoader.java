/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.dialog2.basics.persistence;

import java.net.URL;

import de.d3web.kernel.domainModel.KnowledgeBase;

/**
 * Classes implementing this interface can be registered at
 * DialogPersistenceManager. Alike AuxilliaryPersistenceHandler, they are
 * loading further knowledge to store in the knowledgebase-object. However (in
 * contrast to AuxilliaryPersistenceHandler), the further knowledge does not
 * have to (must not) be specified within the knowledgebase (jar) file, but on
 * other locations. AdditionalDialogConfigKnowledgeLoader are intended to load
 * configurational knowledge, which is to use within the dialog, but which shall
 * not be stored within the knowledgebase (jar) file.
 * 
 * @author gbuscher
 */
public interface AdditionalDialogConfigKnowledgeLoader {

    public String getId();

    public void loadAdditionalDialogConfigKnowledge(KnowledgeBase kb,
	    String kbId, URL filename);

}
