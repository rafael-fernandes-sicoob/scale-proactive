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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.examples.nbody.common.Deployer;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * Synchronization of the computation of the Domain
 */
@ActiveObject
public class Maestro implements Serializable {

    /** Counts the number of Domain that have respond */
    private int nbFinished = 0;

    /** a unique number to differentiate this Maestro from the others */
    private int identification;

    /** List of the Planets associed at this Maestro */
    private List<Planet> lPlanets;

    /** References on all the Active Domain associed at this Maestro */
    private Domain[] domainArray;

    /** Reference on the BigMaestro that synchronize all the Maestro */
    private BigMaestro bigMaestro;

    /**
     * Required by ProActive
     */
    public Maestro() {
    }

    /**
     * Create a new master for the simulation, which pilots 8 domains maximum.
     * @param aDomainArray all the Domain of the simulation.
     */
    public Maestro(Integer id, Domain[] aDomainArray, Deployer deployer) {
        identification = id.intValue();

        // We put only the Domain that correspond with this Maestro
        if (aDomainArray.length < 8 * (identification + 1)) {
            domainArray = new Domain[aDomainArray.length - 8 * identification];
        } else {
            domainArray = new Domain[8];
        }
        for (int i = 0; i < 8; i++) {
            if (identification * 8 + i < aDomainArray.length) {
                domainArray[i] = aDomainArray[identification * 8 + i];
            }
        }

        // Initialization of the list of planets
        lPlanets = new ArrayList<Planet>(domainArray.length);
        for (int i = 0; i < domainArray.length; i++)
            lPlanets.add(null);
    }

    /** Initialization of the reference to the Bigmaestro */
    public void init(BigMaestro big) {
        bigMaestro = big;
    }

    /**
     * Called by a Domain when computation is finished.
     * This method counts the calls, and communicates to the BigMaestro only when all have finished
     * @param id identification of the Domain
     * @param pl planet of the Domain, for the update
     */
    public void notifyFinished(int id, Planet pl) {
        nbFinished++; // one another have finished

        // update of the new planet's positions
        lPlanets.set(id % 8, pl);

        if (nbFinished == domainArray.length) {
            // notify the BigMaestro that the 8 Domain have finished
            bigMaestro.notifyFinished(identification, lPlanets);
        }
    }

    /** Launched by the igMaestro when the Domain can pass to the next iteration
     *  Recontact the domain
     */
    public void finished() {
        nbFinished = 0;
        for (int i = 0; i < domainArray.length; i++)
            domainArray[i].moveAndDraw();
    }
}
