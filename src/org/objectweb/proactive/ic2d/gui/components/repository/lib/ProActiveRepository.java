/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import org.objectweb.fractal.gui.graph.model.GraphModel;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.repository.lib.BasicRepository;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveRepository extends BasicRepository {
    public String storeComponent(final Component component,
        final GraphModel graph, final Object hints) throws Exception {
        ProActiveAdlWriter writer = new ProActiveAdlWriter();
        writer.setStorage(storage);
        writer.setForceInternal("inline".equals(hints));
        return writer.saveTemplate(component, graph);
    }
}
