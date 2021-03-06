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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.xml.sax.Attributes;


/**
 * $LastChangedDate: 2006-05-14 13:21:43 +0200 (Sun, 14 May 2006) $
 * $LastChangedRevision: 31 $
 *
 * Describes what is a planet.
 * This class doesn't have any methods (except accessors and modifiers)
 *
 * @author The ProActive Team
 *
 */
public class PlanetDescription implements Serializable {
    // Fields
    private double x;

    // Fields
    private double y;

    // Fields
    private double z;

    // Fields
    private double mass;

    // Fields
    private double diameter;
    private static transient int lastId = 0;
    private int id = lastId++;
    private UniverseDescription universe;
    protected static transient final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /**
     * Creates a PlanetDescription with random values inside given universe
     * @param universe description of the englobbing universe
     */
    public PlanetDescription(UniverseDescription universe) {
        this.universe = universe;
        x = RandomFactory.nextDouble(universe.getWidth());
        y = RandomFactory.nextDouble(universe.getHeight());
        z = RandomFactory.nextDouble(universe.getDepth());
        mass = RandomFactory.nextDouble(1000, 1000000);
        diameter = mass / 2000;
    }

    /**
     * Creates a PlanetDescription with values given by the XML attributes
     * in given universe
     * @param universe description of the englobbing universe
     * @param xmlAttributes XML parameters
     */
    public PlanetDescription(UniverseDescription universe, Attributes xmlAttributes) {
        this.universe = universe;
        for (int i = 0; i < xmlAttributes.getLength(); i++) {
            String name = xmlAttributes.getQName(i).toLowerCase();
            String value = xmlAttributes.getValue(i);

            if (name.compareTo("diameter") == 0) {
                setDiameter(value);
            } else if (name.compareTo("mass") == 0) {
                setMass(value);
            } else if (name.compareTo("x") == 0) {
                setX(value);
            } else if (name.compareTo("y") == 0) {
                setY(value);
            } else if (name.compareTo("z") == 0) {
                setZ(value);
            }
        }
    }

    /**
     * Accessor
     * @return coordonates (x, y, z) as a single Object
     */
    public Point3D getCoordonate() {
        return new Point3D(x, y, z);
    }

    /**
     * Accessor
     * @return diameter
     */
    public double getDiameter() {
        return diameter;
    }

    /**
     * Accessor
     * @return UUID
     */
    public int getId() {
        return id;
    }

    /**
     * Accessor
     * @return mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * Accessor
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * Accessor
     * @return y position
     */
    public double getY() {
        return y;
    }

    /**
     * Accessor
     * @return z position
     */
    public double getZ() {
        return z;
    }

    /**
     * Modifier
     * @param diameter new diameter
     */
    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    /**
     * Modifier
     * @param diameter new diameter
     */
    public void setDiameter(String diameter) {
        try {
            setDiameter(Double.parseDouble(diameter));
        } catch (NumberFormatException e) {
            logger.warn(diameter + "is not a correct diameter value");
        }
    }

    /**
     * Modifier
     * @param mass new mass
     */
    public void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * Modifier
     * @param mass new mass
     */
    public void setMass(String mass) {
        try {
            setMass(Double.parseDouble(mass));
        } catch (NumberFormatException e) {
            logger.warn(mass + "is not a correct mass value");
        }
    }

    /**
     * Modifier
     * @param x new x position
     */
    public void setX(double x) {
        if (x < 0 || x >= universe.getWidth()) {
            logger.error("planet is outside universe bounds (x=" + x + ")");
        } else {
            this.x = x;
        }
    }

    /**
     * Modifier
     * @param x new x position
     */
    public void setX(String x) {
        try {
            setX(Double.parseDouble(x));
        } catch (NumberFormatException e) {
            logger.warn(x + "is not a correct x value");
        }
    }

    /**
     * Modifier
     * @param y new y position
     */
    public void setY(double y) {
        if (y < 0 || y >= universe.getHeight()) {
            logger.error("planet is outside universe bounds (y=" + y + ")");
        } else {
            this.y = y;
        }
    }

    /**
     * Modifier
     * @param y new y position
     */
    public void setY(String y) {
        try {
            setY(Double.parseDouble(y));
        } catch (NumberFormatException e) {
            logger.warn(y + "is not a correct y value");
        }
    }

    /**
     * Modifier
     * @param z new z position
     */
    public void setZ(double z) {
        if (z < 0 || z >= universe.getDepth()) {
            logger.error("planet is outside universe bounds (z=" + z + ")");
        } else {
            this.z = z;
        }
    }

    /**
     * Modifier
     * @param z new z position
     */
    public void setZ(String z) {
        try {
            setZ(Double.parseDouble(z));
        } catch (NumberFormatException e) {
            logger.warn(z + "is not a correct z value");
        }
    }
}
