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
package org.objectweb.proactive.extensions.timitspmd.util;

import java.io.Serializable;


/**
 * Represent a counter used for timing procedures
 *
 * @author The ProActive Team
 *
 */

public class TimerCounter implements Serializable {

    /**
     *
     */
    private int id;
    private String name;
    private HierarchicalTimer timer;

    /**
     * Create a counter with his name as it will be shown on output charts. The
     * counter is disabled by default.
     *
     * @param s
     */
    public TimerCounter(String s) {
        this.name = s;
        this.id = -1;
        this.timer = FakeTimer.getInstance();
    }

    /**
     * Set the counter id
     *
     * @param n
     */
    public void setId(int n) {
        this.id = n;
    }

    /**
     * Set the counter name
     *
     * @param s
     */
    public void setName(String s) {
        this.name = s;
    }

    /**
     * Get the counter id
     *
     * @return counter id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get the counter name
     *
     * @return counter name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Used by the Timer to register the counter
     *
     * @param timer
     */
    public void setTimer(HierarchicalTimer timer) {
        this.timer = timer;
    }

    /**
     * Used by the Timer to know if a counter can be migrated
     */
    public boolean isMigratable() {
        return false;
    }

    /**
     * Start the counter.<br>
     * Note that an IllegalStateException can occur if the counter has been
     * started two times.
     */
    public void start() {
        this.timer.start(this.id);
    }

    /**
     * Stop the counter.<br>
     * Note that an IllegalStateException can occur if you stop the counter
     * without starting it.
     */
    public void stop() {
        this.timer.stop(this.id);
    }

    /**
     * Set a specific time value (in milliseconds) to the counter.<br>
     * Counter's parents will be modified to keep integrity
     *
     * @param t
     *            time value to set in millis
     */
    public void setValue(int t) {
        this.timer.setValue(this.id, t);
    }

    /**
     * Add a specific time value (in milliseconds) to the counter.<br>
     * Counter's parents will be modified to keep integrity
     *
     * @param t
     *            time value to add in millis
     */
    public void addValue(int t) {
        this.timer.addValue(this.id, t);
    }

    /**
     * Keep in mind that this method take a "little bit of time".<br>
     * Theorically, you don't need to use this method.
     *
     * @return true if counter is started, false otherwise
     */
    public boolean isStarted() {
        return this.timer.isStarted(this.id);
    }

    /**
     * @return the elapsed time since the last start
     */
    public int getElapsedTime() {
        return this.timer.getElapsedTime(this.id);
    }

    /**
     * @return the time value in the current hierarchy
     */
    public int getHierarchicalTime() {
        return this.timer.getHierarchicalTime(this.id);
    }

    /**
     * @return the total time value (sum of all usage of this counter in all
     *         hierarchies)
     */
    public int getTotalTime() {
        return this.timer.getTotalTime(this.id);
    }

    /**
     * Reset the counter
     */
    public void reset() {
        this.timer.resetCounter(this.id);
    }
}
