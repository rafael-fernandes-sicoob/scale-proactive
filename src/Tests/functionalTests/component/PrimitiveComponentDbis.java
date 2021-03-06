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
package functionalTests.component;

import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


/**
 * @author The ProActive Team
 */
public class PrimitiveComponentDbis extends PrimitiveComponentD {
    @Override
    public void bindFc(String clientItfName, Object serverItf) {
        if (clientItfName.startsWith(I2_ITF_NAME)) {
            i2Map.put(clientItfName, (I2) serverItf);
        } else {
            logger.error("Binding impossible : wrong client interface name");
        }
    }

    @Override
    public String[] listFc() {
        Set<String> itf_names = i2Map.keySet();
        return itf_names.toArray(new String[itf_names.size()]);
    }

    @Override
    public void unbindFc(String clientItf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (i2Map.containsKey(clientItf)) {
            i2Map.remove(clientItf);
        } else {
            logger.error("client interface not found");
        }
    }

    @Override
    public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
        if (i2Map.containsKey(clientItf)) {
            return i2Map.get(clientItf);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot find " + clientItf + " interface");
            }
            return null;
        }
    }
}
