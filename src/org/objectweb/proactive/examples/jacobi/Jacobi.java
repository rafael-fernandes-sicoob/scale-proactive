/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.examples.jacobi;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.NodeException;

/**
 * @author Laurent Baduel
 */
public class Jacobi {

	/**
	 * Number of columns of SubMatrix
	 */
	public static final int WIDTH = 3;

	/**
	 * Number of lines of SubMatrix
	 */
	public static final int HEIGHT = 3;

	/**
	 * Max number of iterations
	 */
	public static final int ITERATIONS = 20;
	
	/**
	 * Min diff to stop 
	 */
	public static final double MINDIFF = 0.001;
	
	/**
	 * Default external border value
	 */
	public static final double DEFAULT_BORDER_VALUE = 0;


	public static void main (String[] args) {
		
		ProActiveDescriptor proActiveDescriptor=null;
		String[] nodes = null;
		try {
			proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+args[0]);
		} catch (ProActiveException e) {
			System.err.println("** ProActiveException **");
			e.printStackTrace();
		}
		proActiveDescriptor.activateMappings(); 
		VirtualNode vn = proActiveDescriptor.getVirtualNode("matrixNode");
		 try {
			nodes = vn.getNodesURL();
		} catch (NodeException e) {
			System.err.println("** NodeException **");
			e.printStackTrace();
		}

		Object[][] params = new Object[Jacobi.WIDTH * Jacobi.HEIGHT][];
		for (int i = 0 ; i < params.length ; i++) {
			params[i] = new Object[1];
			params[i][0] = "SubMatrix"+i;
		}
		
		SubMatrix matrix = null;
		try {
			matrix = (SubMatrix) ProSPMD.newSPMDGroup(SubMatrix.class.getName(), params, nodes); }
		catch (ClassNotFoundException e) { System.out.println("** ClassNotFoundException **"); }
		catch (ClassNotReifiableException e) { System.out.println("** ClassNotReifiableException **"); }
		catch (ActiveObjectCreationException e) { System.out.println("** ActiveObjectCreationException **"); }
		catch (NodeException e) { System.out.println("** NodeException **"); }

		matrix.compute();
	}

}
