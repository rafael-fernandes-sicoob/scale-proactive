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
package org.objectweb.proactive.core.group.spmd;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


/**
 * <p>
 * This class provides a static method to build (an deploy) an 'SPMD' group of active objects
 * with all references between them to communicate.
 * </p><p>
 * For instance, the following code builds objects of type <code>A</code> on nodes
 * <code>node1,node2,...</code>, with parameters <code>param1,param2,...</code>
 * and build for each object created its diffusion group to communicate with the others.
 * </p>
 * <pre>
 * Object[] params = {param1,param2,...};
 * Node[] nodes = {node1,node2,...};
 *
 * A group  =  (A) ProSPMD.newSPMDGroup("A", params, nodes);
 * </pre>
 *
 * @version 1.0,  2003/10/09
 * @since   ProActive 1.0.3
 * @author Laurent Baduel
 */
public class ProSPMD {
	/**
	 * Creates an object representing a spmd group (a typed group) and creates all members with params on the node.
	 * @param <code>className</code> the name of the (upper) class of the group's member.
	 * @param <code>params</code> the array that contain the parameters used to build the group's member.
	 * @param <code>nodeName</code> the name (String) of the node where the members are created.
	 * @return a typed group with its members.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the node was null and that the DefaultNode cannot be created
	 */
	public static Object newSPMDGroup(String className, Object[][] params, String nodeName)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		Node[] nodeList = new Node[1]; 
		nodeList[0] = NodeFactory.getNode(nodeName);
		return ProSPMD.newSPMDGroup(className, params, nodeList);
	}



	/**
	 * Creates an object representing a spmd group (a typed group) and creates members with params cycling on nodeList.
	 * @param <code>className</code> the name of the (upper) class of the group's member.
	 * @param <code>params</code> the array that contain the parameters used to build the group's member.
	 * @param <code>nodeListString</code> the names of the nodes where the members are created.
	 * @return a typed group with its members.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the node was null and that the DefaultNode cannot be created
	 */
	public static Object newSPMDGroup(String className, Object[][] params, String[] nodeListString)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		Node[] nodeList = new Node[nodeListString.length]; 
		for (int i = 0 ; i < nodeListString.length ; i++)
			nodeList[i] = NodeFactory.getNode(nodeListString[i]);
		return ProSPMD.newSPMDGroup(className, params, nodeList);
	}

	/**
	 * Creates an object representing a spmd group (a typed group) and creates all members with params on the node.
	 * @param <code>className</code> the name of the (upper) class of the group's member.
	 * @param <code>params</code> the array that contain the parameters used to build the group's member.
	 * @param <code>node</code> the node where the members are created.
	 * @return a typed group with its members.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the node was null and that the DefaultNode cannot be created
	 */
	public static Object newSPMDGroup(String className, Object[][] params, Node node)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		Node[] nodeList = new Node[1]; 
		nodeList[0] = node;
		return ProSPMD.newSPMDGroup(className, params, nodeList);
	}


	/**
	 * Creates an object representing a spmd group (a typed group) and creates members with params cycling on nodeList.
	 * @param <code>className</code> the name of the (upper) class of the group's member.
	 * @param <code>params</code> the array that contain the parameters used to build the group's member.
	 * If <code>params</code> is <code>null</code>, builds an empty group. 
	 * @param <code>nodeList</code> the nodes where the members are created.
	 * @return a typed group with its members.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the node was null and that the DefaultNode cannot be created
	 */
	public static Object newSPMDGroup(String className, Object[][] params, Node[] nodeList)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {

	Object result = ProActiveGroup.newGroup(className);
	Group g = ProActiveGroup.getGroup(result);

	if (params != null) {
		for (int i=0 ; i < params.length ; i++) {
			g.add(ProActive.newActive(className, params[i], nodeList[i % nodeList.length]));
		}
	}
	((ProxyForGroup) g).setSPMDGroup(result);
	return result;
	}

	/**
	 * Creates an object representing a spmd group (a typed group) and creates members with params cycling on the nodes of the virtual node.
	 * @param <code>className</code> the name of the (upper) class of the group's member.
	 * @param <code>params</code> the array that contain the parameters used to build the group's member.
	 * If <code>params</code> is <code>null</code>, builds an empty group. 
	 * @param <code>virtualNode</code> the virtual where the members are created.
	 * @return a typed group with its members.
	 * @throws ActiveObjectCreationException if a problem occur while creating the stub or the body
	 * @throws ClassNotFoundException if the Class corresponding to <code>className</code> can't be found.
	 * @throws ClassNotReifiableException if the Class corresponding to <code>className</code> can't be reify.
	 * @throws NodeException if the node was null and that the DefaultNode cannot be created
	 */
	public static Object newSPMDGroup(String className, Object[][] params, VirtualNode virtualNode)
	throws ClassNotFoundException, ClassNotReifiableException, ActiveObjectCreationException, NodeException {
		return ProSPMD.newSPMDGroup(className,params,virtualNode.getNodes());
	}

//    /**
//     * Set the SPMD group for this
//     * @param o - the new SPMD group
//     */
//    public static void setSPMDGroupOnThis(Object o) {
//        AbstractBody body = (AbstractBody) ProActive.getBodyOnThis();
//        body.setSPMDGroup(o);
//    }

	/**
	 * Returns the SPMD group of this
	 * @return the SPMD group of this
	 */
	public static Object getSPMDGroup() {
		AbstractBody body = (AbstractBody) ProActive.getBodyOnThis();
		return body.getSPMDGroup();
	}

	/**
	 * Returns the size of the SPMD group of this 
	 * @return a size (int)
	 */
	public static int getMySPMDGroupSize() {
		return ProActiveGroup.getGroup(ProSPMD.getSPMDGroup()).size();
	}
	
	/**
	 * Returns the rank (position) of the object in the Group
	 * @return the index of the object
	 */
	public static int getMyRank() {
		return ProActiveGroup.getGroup(ProSPMD.getSPMDGroup()).indexOf(ProActive.getStubOnThis());
	}

	/**
	 * Strongly synchronizes all the members of the spmd group
	 * @param barrierName the name of the barrier (used as unique identifier)
	 */
	public static void barrier (String barrierName) {
		((AbstractBody) ProActive.getBodyOnThis()).sendSPMDGroupCall(new MethodCallBarrier(barrierName));
	}

	/**
	 * Strongly synchronizes all the members of the group.
	 * Beware ! The caller object HAVE TO BE IN THE GROUP <code>group</code>
	 * @param barrierName the name of the barrier (used as unique identifier)
	 * @param group the typed group the barrier is invoked on 
	 */
	public static void barrier (String barrierName, Object group) {
		try {
			((ProxyForGroup) ProActiveGroup.getGroup(group)).reify(new MethodCallBarrier(barrierName,ProActiveGroup.size(group))); }
		catch (InvocationTargetException e) {
			System.err.println("Unable to invoke a method call to control groups");
			e.printStackTrace();
		}
	}

}
