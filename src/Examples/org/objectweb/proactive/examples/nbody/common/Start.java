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
package org.objectweb.proactive.examples.nbody.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * <P>
 * This launches the different versions of the nbody example
 * <ul>
 * <li> simple : simplest version, one-to-one communication and master</li>
 * <li> groupcom : group communication and master</li>
 * <li> groupdistrib : odd-even-synchronization</li>
 * <li> groupoospmd : oospmd synchronization</li>
 * <li> barneshut : an implementation of the Barnes-Hutalgorithm</li>
 * </ul>
 * </P>
 *
 * @author The ProActive Team
 * @version 1.0,  2005/04
 * @since   ProActive 2.2
 */
public class Start implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private GCMApplication descriptorPad;

    /**
     * Options should be "java Start xmlFile [-nodisplay|-displayft] totalNbBodies maxIter"
     * Parameters can be <ul>
     * <li> -nodisplay, which is not compulsory, specifies whether a graphic display is to be created.</li>
     * <li> -displayft, which is not compulsory, specifies whether a fault-generating panel should be created.</li>
     * <li> xmlFile is the xml deployment file..</li>
     * <li> totalNbBodies  The number of Planets in the System.</li>
     * <li> maxIter The number of iterations before the program stops.</li>
     * </ul>
     */
    public static void main(String[] args) {
        new Start().run(args);
    }

    public void run(String[] args) {
        int input = 0;
        boolean display = true;
        boolean displayft = false;
        boolean ddd = false;
        int totalNbBodies = 4;
        int maxIter = 10000;
        String xmlFileName;

        // Set arguments as read on command line
        switch (args.length) {
            case 0:
                usage();
                logger.info("No xml descriptor specified - aborting");
                System.exit(1);
            case 2:
                if (args[1].equals("-nodisplay")) {
                    display = false;
                    break;
                } else if (args[1].equals("-displayft")) {
                    displayft = true;
                    break;
                } else if (args[1].equals("-3d")) {
                    ddd = true;
                    break;
                } else if (args[1].equals("-3dft")) {
                    displayft = true;
                    ddd = true;
                    break;
                }
            case 3:
                totalNbBodies = Integer.parseInt(args[1]);
                maxIter = Integer.parseInt(args[2]);
                break;
            case 4:
                if (args[1].equals("-nodisplay")) {
                    display = false;
                    totalNbBodies = Integer.parseInt(args[2]);
                    maxIter = Integer.parseInt(args[3]);
                    break;
                } else if (args[1].equals("-displayft")) {
                    displayft = true;
                    totalNbBodies = Integer.parseInt(args[2]);
                    maxIter = Integer.parseInt(args[3]);
                    break;
                } else if (args[1].equals("-3d")) {
                    ddd = true;
                    totalNbBodies = Integer.parseInt(args[2]);
                    maxIter = Integer.parseInt(args[3]);
                    break;
                } else if (args[1].equals("-3dft")) {
                    ddd = true;
                    displayft = true;
                    totalNbBodies = Integer.parseInt(args[2]);
                    maxIter = Integer.parseInt(args[3]);
                    break;
                }

                // else : don't break, which means go to the default case
            default:
                usage();
        }

        // testing java3d installation
        if (ddd) {
            try {
                Class.forName("com.sun.j3d.utils.behaviors.mouse.MouseRotate");
                Class.forName("org.objectweb.proactive.examples.nbody.common.NBody3DFrame");
            } catch (Exception e) {
                ddd = false;
                logger.warn("Java 3D not installed, switching to 2D");
            }
        }

        logger.info("        Running with options set to " + totalNbBodies + " bodies, " + maxIter +
            " iterations, display " + display);
        xmlFileName = args[0];

        logger.info(" 1: Simplest version, one-to-one communication and master");
        logger.info(" 2: Group communication and master");
        logger.info(" 3: Group communication, odd-even-synchronization");
        logger.info(" 4: Group communication, oospmd synchronization");
        logger.info(" 5: Barnes-Hut");
        logger.info("Choose which version you want to run [12345] : ");
        try {
            while (true) {
                // Read a character from keyboard
                input = System.in.read();
                if (input >= 49 && input <= 53 || input == -1) {
                    break;
                }
            }
        } catch (IOException ioe) {
            abort(ioe);
        }

        logger.info("Thank you!");

        // If need be, create a displayer
        Displayer displayer = null;
        Deployer deployer = null;
        if (display) {
            try {
                GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(new File(xmlFileName));
                gcmad.startDeployment();
                GCMVirtualNode workers = gcmad.getVirtualNode("Workers");

                if (workers == null)
                    throw new ProActiveException("Failed to acquire \"Workers\" virtual node");

                deployer = PAActiveObject.newActive(Deployer.class, new Object[] { gcmad, workers });

                displayer = PAActiveObject.newActive(Displayer.class, new Object[] {
                        Integer.valueOf(totalNbBodies), Boolean.valueOf(displayft), deployer,
                        new BooleanWrapper(ddd) });
            } catch (ProActiveException e) {
                abort(e);
            }
        }

        switch (input) {
            case 49:
                org.objectweb.proactive.examples.nbody.simple.Start.main(totalNbBodies, maxIter, displayer,
                        deployer);
                break;
            case 50:
                org.objectweb.proactive.examples.nbody.groupcom.Start.main(totalNbBodies, maxIter, displayer,
                        deployer);
                break;
            case 51:
                org.objectweb.proactive.examples.nbody.groupdistrib.Start.main(totalNbBodies, maxIter,
                        displayer, deployer);
                break;
            case 52:
                org.objectweb.proactive.examples.nbody.groupoospmd.Start.main(totalNbBodies, maxIter,
                        displayer, deployer);
                break;
            case 53:
                org.objectweb.proactive.examples.nbody.barneshut.Start.main(totalNbBodies, maxIter,
                        displayer, deployer);
                break;
        }
    }

    /**
     * Shows what are the possible options to this program.
     */
    private void usage() {
        String options = "[-nodisplay | -displayft | -3d | -3dft] totalNbBodies maxIter";
        logger.info("        Usage : nbody.[bat|sh] " + options);
        logger.info("        from the command line, it would be   java Start xmlFile " + options);
    }

    /**
     * Stop with an error.
     * @param e the Exception which triggered the abrupt end of the program
     */
    private void abort(Exception e) {
        System.err.println("This is an unhandled behavior!");
        e.printStackTrace();
    }
}
