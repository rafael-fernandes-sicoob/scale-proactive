/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Cube;
import org.objectweb.proactive.examples.nbody.common.Deployer;
import org.objectweb.proactive.examples.nbody.common.Displayer;


public class Start {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** edge's size of the cube representing the universe */
    public static final int UNIVERSE_CUBE_EDGE_SIZE = 200;

    /** position of the cube representing the universe */
    public static final int UNIVERSE_CUBE_POSITION = -100;

    public static void main(String[] args) {
        org.objectweb.proactive.examples.nbody.common.Start.main(args);
    }

    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Deployer deployer) {
        logger.info("[NBODY] RUNNING barnes-hut 3D VERSION");

        Node[] nodes = deployer.getWorkerNodes();
        logger.info("[NBODY] " + totalNbBodies + " Planets are deployed on " + nodes.length +
            " possible nodes");

        // Creation of the universe
        Cube universe = new Cube(UNIVERSE_CUBE_POSITION, UNIVERSE_CUBE_POSITION, UNIVERSE_CUBE_POSITION,
            UNIVERSE_CUBE_EDGE_SIZE, UNIVERSE_CUBE_EDGE_SIZE, UNIVERSE_CUBE_EDGE_SIZE);

        // Creation of all the planets
        List<Planet> lplanets = new ArrayList<Planet>(totalNbBodies);
        for (int i = 0; i < totalNbBodies; i++)
            lplanets.add(new Planet(universe)); // the limits of planet's position is given by the cube universe

        // For Deploying : 
        // D = Domain
        // O = OctTree
        // M = Maestro
        // BM = BigMaestro
        // We distribute the objects like that :
        // For exemple 20 computers
        // D D D D D D D D O M D D D D D D D D O M D D D D O M... BM

        // We create only one OctTree for 8 Domains
        int nbOctTree = (totalNbBodies - 1) / 8 + 1;
        List<Object> listOctTree = new ArrayList<Object>(nbOctTree);

        for (int i = 0; i < nbOctTree; i++) {
            try {
                // the creation
                // it also initialise the OctTree
                // creation of its sons and computation of the values of mass and mass center of
                // all the tree's nodes
                // useless boolean, just for pick up a different constructor
                listOctTree.add(PAActiveObject.newActive(OctTree.class, new Object[] { lplanets, universe,
                        Boolean.valueOf(true) }, nodes[(10 * i + 8) % nodes.length]));
            } catch (ActiveObjectCreationException e) {
                deployer.abortOnError(e);
            } catch (NodeException e) {
                deployer.abortOnError(e);
            }
        }

        // Add the references on the OctTree to the deployer
        deployer.addAoReferences(listOctTree.toArray());

        // creation of the Domains
        // the number of Domains is the same that the number of planets (bodies)
        Domain[] domainArray = new Domain[totalNbBodies];
        int cmp = -1; // for pick up the good OctTree
        int fornod = -1; // for good node

        try {
            for (int i = 0; i < totalNbBodies; i++) {
                fornod++;
                if (i % 8 == 0) {
                    cmp++;
                }
                if (fornod % 10 == 8) {
                    fornod += 2;
                }

                domainArray[i] = (Domain) PAActiveObject
                        .newActive(Domain.class.getName(), new Object[] { new Integer(i), lplanets.get(i),
                                listOctTree.get(cmp) }, nodes[fornod % nodes.length]);
            }
        } catch (ActiveObjectCreationException e) {
            deployer.abortOnError(e);
        } catch (NodeException e) {
            deployer.abortOnError(e);
        }

        // Add the references on the Domain to the deployer
        deployer.addAoReferences(domainArray);

        // Create an array of Maestro, which will orchestrate the whole simulation, 
        // each of them synchronize only 8 Domain
        // and are themselves synchronized by the BigMaestro
        Maestro[] maestroArray = new Maestro[nbOctTree];

        try {
            for (int i = 0; i < nbOctTree; i++) {
                maestroArray[i] = PAActiveObject.newActive(Maestro.class, new Object[] { new Integer(i),
                        domainArray, deployer }, nodes[(10 * i + 9) % nodes.length]);
            }
        } catch (ActiveObjectCreationException e) {
            deployer.abortOnError(e);
        } catch (NodeException e) {
            deployer.abortOnError(e);
        }

        // Add the references on the Maestro to the deployer
        deployer.addAoReferences(maestroArray);

        // Create a BigMaestro, which will orchestrate the whole simulation, 
        // synchronizing all the Maestro
        BigMaestro bigMaestro = null;

        try {
            bigMaestro = PAActiveObject.newActive(BigMaestro.class, new Object[] { maestroArray,
                    new Integer(maxIter), deployer }, nodes[nodes.length - 1]);
        } catch (ActiveObjectCreationException e) {
            deployer.abortOnError(e);
        } catch (NodeException e) {
            deployer.abortOnError(e);
        }

        // Add the references on the BigMaestro to the deployer
        deployer.addAoReference(bigMaestro);

        // init workers

        // Initialisation of the Maestro
        // For having a connection with the BigMaestro
        for (int i = 0; i < nbOctTree; i++)
            maestroArray[i].init(bigMaestro);

        // Initialisation of all the Domain
        // For having a connection with their Maestro (1 Maestro for 8 Domain)
        for (int i = 0; i < totalNbBodies; i++)
            domainArray[i].init(displayer, maestroArray[i / 8]);
    }
}
