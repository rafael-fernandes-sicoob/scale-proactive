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

import java.io.Serializable;


/**
 * Class implementing physical gravitation force between bodies.
 */
public class Force implements Serializable {
    double x = 0;
    double y = 0;
    double z = 0;
    final double G = 9.81;
    final double RMIN = 1;

    public Force() {
    }

    /**
     * From 2 interacting bodies 1 & 2, adds the force resulting from their interaction.
     * The force is the force that applies on 1, caused by 2
     * @param p1 the information of the boody on which the force is applied.
     * @param p2 the information of the body which caused the generation of a force.
     */
    public void add(Planet p1, Planet p2) {
        if (p2 != null) { // indeed, P2 null means no force must be added 
            double a = p2.x - p1.x;
            double b = p2.y - p1.y;
            double c = p2.z - p1.z;
            double length = Math.sqrt(a * a + b * b + c * c);
            if (length < p1.diameter + p2.diameter) {
                length = p1.diameter + p2.diameter;
            }
            double cube = length * length; // *length; 
            double coeff = G * p2.mass / cube; // * p1.mass removed, because division removed as well

            // Watch out : no minus sign : we want to have force of 2 on 1!
            x += coeff * a;
            y += coeff * b;
            z += coeff * c;
        }
    }

    // FIXME : the code below is not used, and should be in order to make things more efficient.

    /**
     * Adds up the force of the parameter force to this.
     * @param f the force to be added to this
     */
    public void add(Force f) {
        x += f.x;
        y += f.y;
        z += f.z;
    }

    @Override
    public String toString() {
        return "<" + (int) x + " " + (int) y + " " + (int) z + ">";
    }
}
