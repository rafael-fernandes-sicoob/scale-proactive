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
package functionalTests.group;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;


public class A implements InitActive, RunActive, EndActive, java.io.Serializable {

    private String name = "anonymous";
    private boolean onewayCallReceived = false;

    public A() {
    }

    public A(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void onewayCall() {
        this.onewayCallReceived = true;
    }

    public void onewayCall(A a) {
        this.onewayCallReceived = true;
    }

    public boolean isOnewayCallReceived() {
        return this.onewayCallReceived;
    }

    public A asynchronousCall() {
        return new A(this.name + "_Clone");
    }

    public A asynchronousCall(A a) {
        return new A(a.getName() + "_Clone");
    }

    public String getHostName() {
        try { //return the name of the Host
            return URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getName failed";
        }
    }

    public String getNodeName() {
        try {
            //return the name of the Node
            return PAActiveObject.getBodyOnThis().getNodeURL().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getNodeName failed";
        }
    }

    public void moveTo(String nodeURL) throws Exception {
        // System.out.println(" I am going to migate");
        PAMobileAgent.migrateTo(nodeURL);
        // System.out.println("migration done");
    }

    public void endBodyActivity() throws Exception {
        PAActiveObject.terminateActiveObject(true);
    }

    public void initActivity(Body body) {
        // System.out.println("Initialization of the Activity");
    }

    public void runActivity(Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            // The synchro policy is FIFO
            service.blockingServeOldest();
        }
    }

    public void endActivity(Body body) {
        // System.out.println("End of the activity of this Active Object");
    }

    public A asynchronousCallException() throws Exception {
        throw new Exception();
    }
}