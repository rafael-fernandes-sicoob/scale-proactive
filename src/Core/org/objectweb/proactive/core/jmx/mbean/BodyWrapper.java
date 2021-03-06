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
package org.objectweb.proactive.core.jmx.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.debug.debugger.DebugInfo;
import org.objectweb.proactive.core.debug.debugger.RequestQueueInfo;
import org.objectweb.proactive.core.gc.GarbageCollector;
import org.objectweb.proactive.core.gc.ObjectGraph;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of a BodyWrapperMBean.
 * <p>
 * Such wrapper is NOT created if the reified object of a body implements
 * {@link org.objectweb.proactive.ProActiveInternalObject}.
 *
 * @author The ProActive Team
 */
public class BodyWrapper extends NotificationBroadcasterSupport implements Serializable, BodyWrapperMBean {

    /**
     * The name of the attribute used to know if the reified object implements
     * {@link java.io.Serializable}
     */
    public static final String IS_REIFIED_OBJECT_SERIALIZABLE_ATTRIBUTE_NAME = "IsReifiedObjectSerializable";

    /** JMX Logger */
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
    private transient Logger notificationsLogger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** Unique id of the active object */
    private UniqueID id;

    /** The body wrapped in this MBean */
    private AbstractBody body;

    /** The url of node containing this active object */
    private transient String nodeUrl;

    /** The name of the body of the active object */
    private String bodyName;

    /**
     * A <code>Boolean</code> attribute used to know if the reified object
     * implements {@link java.io.Serializable}
     */
    private boolean isReifiedObjectSerializable;

    // -- JMX Datas --

    /** Timeout between updates */
    private long updateFrequence = 300;

    /** Used by the JMX notifications */
    private long counter = 1;

    /**
     * A list of jmx notifications. The current MBean sends a list of
     * notifications in order to not overload the network
     */
    private transient ConcurrentLinkedQueue<Notification> notifications;

    /**
     * Empty constructor required by JMX
     */
    public BodyWrapper() {
        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new BodyWrapper MBean, representing an active object.
     *
     * @param oname
     *            The JMX name of this wrapper
     * @param body
     *            The wrapped active object's body
     */
    public BodyWrapper(ObjectName oname, AbstractBody body) {
        this.objectName = oname;
        this.id = body.getID();
        this.body = body;
        this.nodeUrl = body.getNodeURL();
        this.isReifiedObjectSerializable = body.getReifiedObject() instanceof Serializable;
        this.notifications = new ConcurrentLinkedQueue<Notification>();
        this.launchNotificationsThread();
    }

    public UniqueID getID() {
        return this.id;
    }

    public String getName() {
        if (this.bodyName == null) {
            this.bodyName = this.body.getName();
        }
        return this.bodyName;
    }

    public ObjectName getObjectName() {
        if (this.objectName == null) {
            this.objectName = FactoryName.createActiveObjectName(this.id);
        }
        return this.objectName;
    }

    public String getNodeUrl() {
        return this.nodeUrl;
    }

    public void sendNotification(String type) {
        this.sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        ObjectName source = getObjectName();
        if (notificationsLogger.isDebugEnabled()) {
            notificationsLogger.debug("[" + type + "]#[BodyWrapper.sendNotification] source=" + source +
                ", userData=" + userData);
        }

        Notification notification = new Notification(type, source, counter++, System.nanoTime() / 1000); // timeStamp in microseconds
        notification.setUserData(userData);
        // If the migration is finished, we need to inform the
        // JMXNotificationManager
        if (type.equals(NotificationType.migrationFinished)) {
            sendNotifications();
            notifications.add(notification);
            sendNotifications(NotificationType.migrationMessage);
        } else {
            notifications.add(notification);
        }
    }

    public void migrateTo(String nodeUrl) throws MigrationException {
        if (!(this.body instanceof Migratable)) {
            throw new MigrationException("Object cannot Migrate");
        }
        Node node = null;
        try {
            node = NodeFactory.getNode(nodeUrl);
        } catch (NodeException e) {
            throw new MigrationException("Cannot find node " + nodeUrl, e);
        }
        PAMobileAgent.migrateTo(body, node, true, Request.NFREQUEST_IMMEDIATE_PRIORITY);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------------
    //

    /**
     * Creates a new thread which sends JMX notifications. A BodyWrapperMBean
     * keeps all the notifications, and the NotificationsThread sends every
     * 'updateFrequence' a list of notifications.
     */
    private void launchNotificationsThread() {
        Thread t = new Thread("JMXNotificationThread for " + BodyWrapper.this.objectName) {
            @Override
            public void run() {
                // first we wait for the creation of the body
                while (!BodyWrapper.this.body.isActive() && BodyWrapper.this.body.isAlive()) {
                    try {
                        Thread.sleep(updateFrequence);
                    } catch (InterruptedException e) {
                        logger.error("The JMX notifications sender thread was interrupted", e);
                    }
                }

                // and once the body is activated, we can forward the
                // notifications
                while (BodyWrapper.this.body.isActive()) {
                    try {
                        Thread.sleep(updateFrequence);
                        sendNotifications();
                    } catch (InterruptedException e) {
                        logger.error("The JMX notifications sender thread was interrupted", e);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Sends a notification containing all stored notifications.
     */
    private void sendNotifications() {
        this.sendNotifications(null);
    }

    /**
     * Sends a notification containing all stored notifications.
     *
     * @param userMessage
     *            The message to send with the set of notifications.
     */
    private void sendNotifications(String userMessage) {
        if (this.notifications == null) {
            this.notifications = new ConcurrentLinkedQueue<Notification>();
        }

        // not sure if the synchronize is needed here, let's see ...
        // synchronized (notifications) {
        if (!this.notifications.isEmpty()) {
            ObjectName source = getObjectName();
            Notification n = new Notification(NotificationType.setOfNotifications, source, counter++,
                userMessage);
            n.setUserData(this.notifications);
            super.sendNotification(n);
            this.notifications.clear();
            // }
        }
    }

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("[Serialisation.writeObject]#Serialization of the MBean :" + objectName);
        }

        // Send the notifications before migrates.
        if (!this.notifications.isEmpty()) {
            sendNotifications();
        }

        // Unregister the MBean from the MBean Server.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(this.objectName)) {
            try {
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                logger.error("The objectName " + objectName +
                    " was not found during the serialization of the MBean", e);
            } catch (MBeanRegistrationException e) {
                logger.error("The MBean " + objectName +
                    " can't be unregistered from the MBean server during the serialization of the MBean", e);
            }
        }

        // Default Serialization
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Warning loggers is transient
        logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
        notificationsLogger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

        if ((logger != null) && logger.isDebugEnabled()) {
            logger.debug("[Serialisation.readObject]#Deserialization of the MBean");
        }

        in.defaultReadObject();

        // Warning objectName is transient
        this.objectName = FactoryName.createActiveObjectName(this.id);

        // Warning nodeUrl is transient
        // We get the url of the new node.
        this.nodeUrl = this.body.getNodeURL();
        logger.debug("BodyWrapper.readObject() nodeUrl=" + nodeUrl);

        // Warning notifications is transient
        this.notifications = new ConcurrentLinkedQueue<Notification>();

        // Register the MBean into the MBean Server
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        } catch (InstanceAlreadyExistsException e) {
            logger.error("A Mean is already registered with this objectName " + objectName, e);
        } catch (MBeanRegistrationException e) {
            logger.error("The MBean " + objectName +
                " can't be registered on the MBean server during the deserialization of the MBean", e);
        } catch (NotCompliantMBeanException e) {
            logger.error("Exception throws during the deserialization of the MBean", e);
        }

        launchNotificationsThread();
    }

    public ProActiveSecurityManager getSecurityManager(Entity user) {
        try {
            return this.body.getProActiveSecurityManager(user);
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setSecurityManager(Entity user, PolicyServer policyServer) {
        try {
            this.body.setProActiveSecurityManager(user, policyServer);
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns a list of outgoing active object references.
     */
    public Collection<UniqueID> getReferenceList() {
        return ObjectGraph.getReferenceList(this.id);
    }

    public String getDgcState() {
        return GarbageCollector.getDgcState(this.id);
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#getTimersSnapshotFromBody()
     */
    public Object[] getTimersSnapshotFromBody() throws Exception {
        final org.objectweb.proactive.core.util.profiling.TimerProvidable container = org.objectweb.proactive.core.util.profiling.TimerWarehouse
                .getTimerProvidable(this.id);
        if (container == null) {
            throw new NullPointerException("The timers container is null, the body is not timed.");
        }
        return new Object[] { container.getSnapshot(), // The array of timers
                System.nanoTime() // The nano timestamp on this machine used
        // to stop all timers at the caller side
        };
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#getIsReifiedObjectSerializable()
     */
    public boolean getIsReifiedObjectSerializable() {
        return this.isReifiedObjectSerializable;
    }

    //
    // -- STEPBYSTEP METHODS -----------------------------------------------
    //
    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#enableStepByStep()
     */
    public void enableStepByStep() {
        body.getDebugger().setStepByStep(true);
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#disableStepByStep()
     */
    public void disableStepByStep() {
        body.getDebugger().resume();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#nextStep()
     */
    public void nextStep() {
        body.getDebugger().nextStep();
    }

    public void nextStep(long id) {
        body.getDebugger().nextStep(id);
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#nextStep(Collection)
     */
    public void nextStep(Collection<Long> ids) {
        body.getDebugger().nextStep(ids);
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#getState()
     */
    public DebugInfo getDebugInfo() throws ProActiveException {
        return body.getDebugger().getDebugInfo();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#slowMotion(long)
     */
    public void slowMotion(long slowMotionDelay) {
        body.getDebugger().slowMotion(slowMotionDelay);
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#initBreakpointTypes()
     */
    public void initBreakpointTypes() {
        body.getDebugger().initBreakpointTypes();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#updateBreakpointTypes(Map<String, Boolean> types)
     */
    public void updateBreakpointTypes(Map<String, Boolean> values) {
        body.getDebugger().updateBreakpointTypes(values);
    }

    //
    // -- EXTENDED DEBUGGER ------------------------------------------------
    //
    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#enableExtendedDebugger()
     */
    public void enableExtendedDebugger() {
        body.getDebugger().enableExtendedDebugger();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#enableExtendedDebugger()
     */
    public void disableExtendedDebugger() {
        body.getDebugger().disableExtendedDebugger();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#unblockConnection()
     */
    public void unblockConnection() {
        body.getDebugger().unblockConnection();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#getRequestQueueInfo()
     */
    public RequestQueueInfo getRequestQueueInfo() {
        return body.getDebugger().getRequestQueueInfo();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#moveUpRequest(long)
     */
    public void moveUpRequest(final long sequenceNumber) {
        body.getDebugger().moveUpRequest(sequenceNumber);
    }

    /**
     * see {@link org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#moveDownRequest(long)}
     */
    public void moveDownRequest(final long sequenceNumber) {
        body.getDebugger().moveDownRequest(sequenceNumber);
    }
}
