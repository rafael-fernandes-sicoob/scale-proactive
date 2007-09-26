/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.system;

import java.io.File;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.exceptions.EnvironmentException;


@PublicAPI
public interface SkeletonSystem {

    /**
     * The working space is a directory to which files can be written and read.
     *
     * The files stored in this directory are distroyed after the execution
     * of the muscle function. To keep files persistent use the method {@link #newPersistent(File) registerAsPersistent}.
     *
     * @return A File object representing the location of the working space.
     * @throws EnvironmentException
     */
    public WSpace getWorkingSpace() throws EnvironmentException;

    public void execCommand(File command, String arguments);
}
