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
package org.objectweb.proactive.ic2d.gui.components.model;

import org.objectweb.fractal.gui.model.ConfigurationNotifier;

import java.util.Iterator;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveConfigurationNotifier extends ConfigurationNotifier
    implements ProActiveConfigurationListener {
    public void virtualNodeChanged(ProActiveComponent component, String oldValue) {
        Iterator i = listeners.values().iterator();
        while (i.hasNext()) {
            ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
            l.virtualNodeChanged(component, oldValue);
        }
    }

    public void exportedVirtualNodeChanged(ProActiveComponent component,
        String virtualNodeName, String oldValue) {
        Iterator i = listeners.values().iterator();
        while (i.hasNext()) {
            ProActiveConfigurationListener l = (ProActiveConfigurationListener) i.next();
            l.exportedVirtualNodeChanged(component, virtualNodeName, oldValue);
        }
    }
}
