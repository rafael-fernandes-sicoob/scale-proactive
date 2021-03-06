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
package org.objectweb.proactive.core.descriptor.legacyparser;

import java.io.File;
import java.util.Arrays;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.process.AbstractListProcessDecorator;
import org.objectweb.proactive.core.process.DependentListProcess;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.IndependentListProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcess.PriorityLevel;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.process.globus.GlobusProcess;
import org.objectweb.proactive.core.process.gridengine.GridEngineSubProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;
import org.objectweb.proactive.core.process.mpi.MPIProcess;
import org.objectweb.proactive.core.process.nordugrid.NGProcess;
import org.objectweb.proactive.core.process.oar.OARGRIDSubProcess;
import org.objectweb.proactive.core.process.oar.OARSubProcess;
import org.objectweb.proactive.core.process.pbs.PBSSubProcess;
import org.objectweb.proactive.core.process.prun.PrunSubProcess;
import org.objectweb.proactive.core.process.rsh.maprsh.MapRshProcess;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.SingleValueUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.utils.OperatingSystem;
import org.xml.sax.SAXException;


//import org.xml.sax.SAXException;
public class ProcessDefinitionHandler extends AbstractUnmarshallerDecorator implements
        ProActiveDescriptorConstants {
    protected String id;
    protected ProActiveDescriptorInternal proActiveDescriptor;
    protected ExternalProcess targetProcess;

    public ProcessDefinitionHandler(ProActiveDescriptorInternal proActiveDescriptor) {
        super(false);
        this.proActiveDescriptor = proActiveDescriptor;
        this.addHandler(JVM_PROCESS_TAG, new JVMProcessHandler(proActiveDescriptor));
        this.addHandler(RSH_PROCESS_TAG, new RSHProcessHandler(proActiveDescriptor));
        this.addHandler(MAPRSH_PROCESS_TAG, new MapRshProcessHandler(proActiveDescriptor));
        this.addHandler(SSH_PROCESS_TAG, new SSHProcessHandler(proActiveDescriptor));
        this.addHandler(RLOGIN_PROCESS_TAG, new RLoginProcessHandler(proActiveDescriptor));
        this.addHandler(BSUB_PROCESS_TAG, new BSubProcessHandler(proActiveDescriptor));
        this.addHandler(GLOBUS_PROCESS_TAG, new GlobusProcessHandler(proActiveDescriptor));
        this.addHandler(PRUN_PROCESS_TAG, new PrunProcessHandler(proActiveDescriptor));
        this.addHandler(PBS_PROCESS_TAG, new PBSProcessHandler(proActiveDescriptor));
        this.addHandler(GRID_ENGINE_PROCESS_TAG, new GridEngineProcessHandler(proActiveDescriptor));
        this.addHandler(OAR_PROCESS_TAG, new OARProcessHandler(proActiveDescriptor));
        this.addHandler(OARGRID_PROCESS_TAG, new OARGRIDProcessHandler(proActiveDescriptor));
        this.addHandler(MPI_PROCESS_TAG, new MPIProcessHandler(proActiveDescriptor));
        this.addHandler(DEPENDENT_PROCESS_SEQUENCE_TAG, new DependentProcessSequenceHandler(
            proActiveDescriptor));
        this.addHandler(SEQUENTIAL_PROCESS_TAG, new SequentialProcessHandler(proActiveDescriptor));
        ProcessListHandler handler = new ProcessListHandler(proActiveDescriptor);
        this.addHandler(PROCESS_LIST_TAG, handler);
        this.addHandler(PROCESS_LIST_BYHOST_TAG, handler);
        this.addHandler(NG_PROCESS_TAG, new NGProcessHandler(proActiveDescriptor));
        this.addHandler(CLUSTERFORK_PROCESS_TAG, new ClusterForkProcessHandler(proActiveDescriptor));
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(String,
     *UnmarshallerHandler)
     */
    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {

    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        ExternalProcess result = targetProcess;
        targetProcess = null;

        return result;
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(String,
     *Attributes)
     */
    public void startContextElement(String name, Attributes attributes) throws SAXException {
        id = attributes.getValue("id");
    }

    public class ProcessHandler extends AbstractUnmarshallerDecorator implements ProActiveDescriptorConstants {
        protected ProActiveDescriptorInternal proActiveDescriptor;
        protected boolean isRef;

        public ProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
            addHandler(ENVIRONMENT_TAG, new EnvironmentHandler());
            addHandler(PROCESS_REFERENCE_TAG, new ProcessReferenceHandler());
            addHandler(COMMAND_PATH_TAG, new CommandPathHanlder());
            addHandler(FILE_TRANSFER_DEPLOY_TAG, new FileTransferStructureHandler("deploy"));
            addHandler(FILE_TRANSFER_RETRIEVE_TAG, new FileTransferStructureHandler("retrieve"));
        }

        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String className = attributes.getValue("class");

            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException("Process defined without specifying the class");
            }

            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                //e.printStackTrace();
                throw new org.xml.sax.SAXException(e.getMessage());
            }

            String closeStream = attributes.getValue("closeStream");

            if (checkNonEmpty(closeStream) && closeStream.equals("yes")) {
                targetProcess.closeStream();
            }

            String hostname = attributes.getValue("hostname");

            if (checkNonEmpty(hostname)) {
                targetProcess.setHostname(hostname);
            }

            String username = attributes.getValue("username");

            if (checkNonEmpty(username)) {
                targetProcess.setUsername(username);
            }

            String priority = attributes.getValue("priority");

            if (checkNonEmpty(priority)) {
                ((JVMProcess) targetProcess).setPriority(PriorityLevel.valueOf(priority));
            }

            String os = attributes.getValue("os");

            if (checkNonEmpty(os)) {
                ((JVMProcess) targetProcess).setOperatingSystem(OperatingSystem.valueOf(os));
            }
        }

        //
        // -- implements UnmarshallerHandler
        // ------------------------------------------------------
        //
        public Object getResultObject() throws org.xml.sax.SAXException {
            return null;
        }

        //
        // -- PROTECTED METHODS
        // ------------------------------------------------------
        //
        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
            if (name.equals(ENVIRONMENT_TAG)) {
                targetProcess.setEnvironment((String[]) activeHandler.getResultObject());
            } else if (name.equals(PROCESS_REFERENCE_TAG)) {
                if (!(targetProcess instanceof ExternalProcessDecorator)) {
                    throw new org.xml.sax.SAXException(
                        "found a Process defined inside a non composite process");
                }

                ExternalProcessDecorator cep = (ExternalProcessDecorator) targetProcess;
                Object result = activeHandler.getResultObject();
                proActiveDescriptor.registerProcess(cep, (String) result);
            } else if (name.equals(COMMAND_PATH_TAG)) {
                targetProcess.setCommandPath((String) activeHandler.getResultObject());
            }
        }

        //
        // -- INNER CLASSES
        // ------------------------------------------------------
        //
        protected class EnvironmentHandler extends BasicUnmarshaller {
            private java.util.ArrayList<String> variables;

            public EnvironmentHandler() {
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
                variables = new java.util.ArrayList<String>();
            }

            @Override
            public Object getResultObject() throws org.xml.sax.SAXException {
                if (variables == null) {
                    isResultValid = false;
                } else {
                    int n = variables.size();
                    String[] result = new String[n];

                    if (n > 0) {
                        variables.toArray(result);
                    }

                    setResultObject(result);
                    variables.clear();
                    variables = null;
                }

                return super.getResultObject();
            }

            @Override
            public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
                if (name.equals(VARIABLE_TAG)) {
                    String vName = attributes.getValue("name");
                    String vValue = attributes.getValue("value");

                    if (checkNonEmpty(vName) && (vValue != null)) {
                        logger.info("Found environment variable name=" + vName + " value=" + vValue);
                        variables.add(vName + "=" + vValue);
                    }
                }
            }
        }

        // end inner class EnvironmentHandler
        protected class CommandPathHanlder extends BasicUnmarshaller {
            public CommandPathHanlder() {
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
                String path = attributes.getValue("value");

                if (checkNonEmpty(path)) {
                    setResultObject(path);
                } else {
                    throw new org.xml.sax.SAXException(
                        "The Id of the referenced definition cannot be set to an empty string");
                }
            }
        }

        protected class FileTransferStructureHandler extends PassiveCompositeUnmarshaller {

            /* Specifies the queue to use in the FileTransferStructure
             * For now this queues are:
             *                 FileTransferStructure.DEPLOY
             *                 FileTransferStructure.RETRIEVE
             */
            protected String fileTransferQueue;
            protected FileTransferWorkShop fileTransferStructure;

            public FileTransferStructureHandler(String queue) {
                super();
                fileTransferQueue = queue;
                fileTransferStructure = null; //defined later in the startContextElement

                addHandler(FILE_TRANSFER_COPY_PROTOCOL_TAG, new SingleValueUnmarshaller());
                addHandler(FILE_TRANSFER_SRC_INFO_TAG, new InfoAttributeHandler());
                addHandler(FILE_TRANSFER_DST_INFO_TAG, new InfoAttributeHandler());
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                if (name.equals(FILE_TRANSFER_COPY_PROTOCOL_TAG)) {
                    fileTransferStructure.setFileTransferCopyProtocol((String) activeHandler
                            .getResultObject());
                }
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
                if (fileTransferQueue.equalsIgnoreCase("deploy")) {
                    fileTransferStructure = targetProcess.getFileTransferWorkShopDeploy();
                } else { //if(fileTransferQueue.equalsIgnoreCase("retrieve"))
                    fileTransferStructure = targetProcess.getFileTransferWorkShopRetrieve();
                }

                String ftRefId = attributes.getValue("refid");
                if (!checkNonEmpty(ftRefId)) {
                    throw new org.xml.sax.SAXException(name + " defined without 'refid' attribute");
                }

                if (ftRefId.equalsIgnoreCase(FILE_TRANSFER_IMPLICT_KEYWORD)) {
                    fileTransferStructure.setImplicit(true);
                } else {
                    fileTransferStructure.setImplicit(false);
                    fileTransferStructure.addFileTransfer(proActiveDescriptor.getFileTransfer(ftRefId));
                }
            }

            protected class InfoAttributeHandler extends BasicUnmarshaller {
                @Override
                public void startContextElement(String name, Attributes attributes)
                        throws org.xml.sax.SAXException {
                    String[] parameter = { "prefix", "hostname", "username", "password" };

                    for (int i = 0; i < parameter.length; i++) {
                        String value = attributes.getValue(parameter[i]);

                        if (checkNonEmpty(value)) {
                            if (name.equals(FILE_TRANSFER_SRC_INFO_TAG)) {
                                fileTransferStructure.setFileTransferStructureSrcInfo(parameter[i], value);
                            } else if (name.equals(FILE_TRANSFER_DST_INFO_TAG)) {
                                fileTransferStructure.setFileTransferStructureDstInfo(parameter[i], value);
                            } else {
                                System.err.println("Error skipping unknown tag name:" + name);
                            }
                        }
                    }
                }
            } //end InfoAttributeHandler class
        } //end FileTransferStructure class        
    }

    public class ProcessListHandler extends ProcessHandler implements ProActiveDescriptorConstants {
        public ProcessListHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            int padding = 0;
            int repeat = 1;
            String className = attributes.getValue("class");

            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException("Process defined without specifying the class");
            }

            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                //e.printStackTrace();
                throw new org.xml.sax.SAXException(e.getMessage());
            }

            String closeStream = attributes.getValue("closeStream");

            String fixedName = attributes.getValue("fixedName");
            String list = attributes.getValue("list");
            String domain = attributes.getValue("domain");
            String spadding = attributes.getValue("padding");
            String hostlist = attributes.getValue("hostlist");
            String srepeat = attributes.getValue("repeat");

            if (checkNonEmpty(spadding)) {
                padding = Integer.parseInt(spadding);
            }

            if (checkNonEmpty(srepeat)) {
                repeat = Integer.parseInt(srepeat);
            }

            if (checkNonEmpty(fixedName) && checkNonEmpty(list)) {
                ((AbstractListProcessDecorator) targetProcess).setHostConfig(fixedName, list, domain,
                        padding, repeat);
            }

            if (checkNonEmpty(hostlist)) {
                ((AbstractListProcessDecorator) targetProcess).setHostList(hostlist, domain);
            }

            if (checkNonEmpty(closeStream) && closeStream.equals("yes")) {
                targetProcess.closeStream();
            }

            String username = attributes.getValue("username");

            if (checkNonEmpty(username)) {
                targetProcess.setUsername(username);
            }
        }
    }

    //end of inner class ProcessHandler
    protected class PrunProcessHandler extends ProcessHandler {
        public PrunProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(PRUN_OPTIONS_TAG, new PrunOptionHandler());

            //	System.out.println("ProcessDefinitionHandler.PrunProcessHandler()");
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            String queueName = (attributes.getValue("queue"));

            if (checkNonEmpty(queueName)) {
                ((PrunSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class PrunOptionHandler extends PassiveCompositeUnmarshaller {
            //  	private static final String HOSTLIST_ATTRIBUTE = "hostlist";
            //  	private static final String PROCESSOR_ATRIBUTE = "processor";
            //private LSFBSubProcess bSubProcess;
            public PrunOptionHandler() {
                //this.bSubProcess = (LSFBSubProcess)targetProcess;
                //      System.out.println("ProcessDefinitionHandler.PrunOptionHandler()");
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_PER_NODE_TAG, new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG, new SingleValueUnmarshaller());
                this.addHandler(OUTPUT_FILE, new SingleValueUnmarshaller());

                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);

                //    this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                // we know that it is a prun process since we are
                // in prun option!!!
                PrunSubProcess prunSubProcess = (PrunSubProcess) targetProcess;

                //    System.out.println("+++++ notifyEndActiveHandler " + name);
                if (name.equals(HOST_LIST_TAG)) {
                    prunSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    prunSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_PER_NODE_TAG)) {
                    prunSubProcess.setProcessorPerNodeNumber((String) activeHandler.getResultObject());

                    //                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    //                    prunSubProcess.setScriptLocation((String)
                    // activeHandler.getResultObject());
                    //                }
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    prunSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else if (name.equals(OUTPUT_FILE)) {
                    prunSubProcess.setOutputFile((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class PBSProcessHandler extends ProcessHandler {
        public PBSProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(PBS_OPTIONS_TAG, new PBSOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            String interactive = (attributes.getValue("interactive"));

            if (checkNonEmpty(interactive)) {
                ((PBSSubProcess) targetProcess).setInteractive(interactive);
            }

            String queueName = (attributes.getValue("queue"));

            if (checkNonEmpty(queueName)) {
                ((PBSSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class PBSOptionHandler extends PassiveCompositeUnmarshaller {
            public PBSOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_PER_NODE_TAG, new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG, new SingleValueUnmarshaller());
                this.addHandler(OUTPUT_FILE, new SingleValueUnmarshaller());

                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                PBSSubProcess pbsSubProcess = (PBSSubProcess) targetProcess;

                if (name.equals(HOST_LIST_TAG)) {
                    pbsSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    pbsSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_PER_NODE_TAG)) {
                    pbsSubProcess.setProcessorPerNodeNumber((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    pbsSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    pbsSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else if (name.equals(OUTPUT_FILE)) {
                    pbsSubProcess.setOutputFile((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class GridEngineProcessHandler extends ProcessHandler {
        public GridEngineProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(GRID_ENGINE_OPTIONS_TAG, new GridEngineOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((GridEngineSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class GridEngineOptionHandler extends PassiveCompositeUnmarshaller {
            public GridEngineOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PARALLEL_ENVIRONMENT_TAG, new SingleValueUnmarshaller());

                //   this.addHandler(PROCESSOR_TAG, new
                // SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG, new SingleValueUnmarshaller());

                //   this.addHandler(PRUN_OUTPUT_FILE, new
                // SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                GridEngineSubProcess geSubProcess = (GridEngineSubProcess) targetProcess;

                if (name.equals(HOST_LIST_TAG)) {
                    geSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    geSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PARALLEL_ENVIRONMENT_TAG)) {
                    geSubProcess.setParallelEnvironment((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    geSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    geSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class ClusterForkProcessHandler extends ProcessHandler {
        public ClusterForkProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws SAXException {
            super.startContextElement(name, attributes);
        }
    }

    protected class OARProcessHandler extends ProcessHandler {
        public OARProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(OAR_OPTIONS_TAG, new OAROptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            String interactive = (attributes.getValue("interactive"));

            if (checkNonEmpty(interactive)) {
                ((OARSubProcess) targetProcess).setInteractive(interactive);
            }

            String queueName = (attributes.getValue("queue"));

            if (checkNonEmpty(queueName)) {
                ((OARSubProcess) targetProcess).setQueueName(queueName);
            }

            String accessProtocol = (attributes.getValue("bookedNodesAccess"));

            if (checkNonEmpty(accessProtocol)) {
                ((OARSubProcess) targetProcess).setAccessProtocol(accessProtocol);
            }
        }

        protected class OAROptionHandler extends PassiveCompositeUnmarshaller {
            public OAROptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();

                //not yet supported
                //this.addHandler(OAR_PROPERTY_TAG, new
                // SingleValueUnmarshaller());
                this.addHandler(OAR_RESOURCE_TAG, new SingleValueUnmarshaller());

                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                OARSubProcess oarSubProcess = (OARSubProcess) targetProcess;

                if (name.equals(OAR_RESOURCE_TAG)) {
                    oarSubProcess.setResources((String) activeHandler.getResultObject());
                }
                //Not yet supported
                //                else if (name.equals(OAR_PROPERTY_TAG)) {
                //                    oarSubProcess.setProperties((String)
                // activeHandler.getResultObject());
                //                }
                else if (name.equals(SCRIPT_PATH_TAG)) {
                    oarSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class OARGRIDProcessHandler extends ProcessHandler {
        public OARGRIDProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(OARGRID_OPTIONS_TAG, new OARGRIDOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            String queueName = (attributes.getValue("queue"));

            if (checkNonEmpty(queueName)) {
                ((OARGRIDSubProcess) targetProcess).setQueueName(queueName);
            }

            String accessProtocol = (attributes.getValue("bookedNodesAccess"));

            if (checkNonEmpty(accessProtocol)) {
                ((OARGRIDSubProcess) targetProcess).setAccessProtocol(accessProtocol);
            }
        }

        protected class OARGRIDOptionHandler extends PassiveCompositeUnmarshaller {
            public OARGRIDOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();

                this.addHandler(OAR_RESOURCE_TAG, new SingleValueUnmarshaller());
                this.addHandler(OARGRID_WALLTIME_TAG, new SingleValueUnmarshaller());

                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                OARGRIDSubProcess oarGridSubProcess = (OARGRIDSubProcess) targetProcess;

                if (name.equals(OAR_RESOURCE_TAG)) {
                    oarGridSubProcess.setResources((String) activeHandler.getResultObject());
                } else if (name.equals(OARGRID_WALLTIME_TAG)) {
                    oarGridSubProcess.setWallTime((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    oarGridSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class JVMProcessHandler extends ProcessHandler {
        public JVMProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);

            UnmarshallerHandler pathHandler = new PathHandler();

            {
                CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
                cu.addHandler(ABS_PATH_TAG, pathHandler);
                cu.addHandler(REL_PATH_TAG, pathHandler);
                cu.addHandler(JVMPARAMETER_TAG, new SimpleValueHandler());
                this.addHandler(CLASSPATH_TAG, cu);
                this.addHandler(BOOT_CLASSPATH_TAG, cu);
                this.addHandler(JVMPARAMETERS_TAG, cu);
            }

            BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
            bch.addHandler(ABS_PATH_TAG, pathHandler);
            bch.addHandler(REL_PATH_TAG, pathHandler);
            bch.addHandler(JVMPARAMETER_TAG, new BasicUnmarshaller());

            //  this.addHandler(JVMPARAMETERS_TAG, bch);
            this.addHandler(JAVA_PATH_TAG, bch);
            this.addHandler(POLICY_FILE_TAG, bch);
            this.addHandler(LOG4J_FILE_TAG, bch);
            this.addHandler(PROACTIVE_PROPS_FILE_TAG, bch);
            this.addHandler(CLASSNAME_TAG, new SingleValueUnmarshaller());
            this.addHandler(PARAMETERS_TAG, new SingleValueUnmarshaller());
            this.addHandler(EXTENDED_JVM_TAG, new ExtendedJVMHandler());

            // this.addHandler(JVMPARAMETERS_TAG, new
            // SingleValueUnmarshaller());
        }

        //
        //  ----- PUBLIC METHODS
        // -----------------------------------------------------------------------------------
        //
        //
        //  ----- PROTECTED METHODS
        // -----------------------------------------------------------------------------------
        //
        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
            // the fact targetProcess is a JVMProcess is checked in
            // startContextElement
            //super.notifyEndActiveHandler(name,activeHandler);
            JVMProcess jvmProcess = (JVMProcess) targetProcess;

            if (name.equals(CLASSPATH_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();

                if (paths.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    char pathSeparator = jvmProcess.getOperatingSystem().pathSeparator();
                    sb.append(paths[0].trim());

                    //we call trim method to avoid whitespace at the beginning
                    // or end of a path
                    for (int i = 1; i < paths.length; i++) {
                        sb.append(pathSeparator);
                        sb.append(paths[i].trim());
                    }

                    jvmProcess.setClasspath(sb.toString());
                }
            } else if (name.equals(BOOT_CLASSPATH_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();

                if (paths.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    String pathSeparator = File.pathSeparator;
                    sb.append(paths[0].trim());

                    for (int i = 1; i < paths.length; i++) {
                        sb.append(pathSeparator);
                        sb.append(paths[i].trim());
                    }

                    jvmProcess.setBootClasspath(sb.toString());
                }
            } else if (name.equals(JVMPARAMETERS_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();
                if (paths.length > 0) {
                    jvmProcess.setJvmOptions(Arrays.asList(paths));
                }
            } else if (name.equals(JAVA_PATH_TAG)) {
                String jp = (String) activeHandler.getResultObject();
                jvmProcess.setJavaPath(jp.trim());
            } else if (name.equals(POLICY_FILE_TAG)) {
                jvmProcess.setPolicyFile(((String) activeHandler.getResultObject()).trim());
            } else if (name.equals(LOG4J_FILE_TAG)) {
                jvmProcess.setLog4jFile(((String) activeHandler.getResultObject()).trim());
            } else if (name.equals(PROACTIVE_PROPS_FILE_TAG)) {
                jvmProcess.addJvmOption("-Dproactive.configuration=" +
                    ((String) activeHandler.getResultObject()).trim());
            } else if (name.equals(CLASSNAME_TAG)) {
                jvmProcess.setClassname((String) activeHandler.getResultObject());
            } else if (name.equals(PARAMETERS_TAG)) {
                String[] params = ((String) activeHandler.getResultObject()).split(" ");
                jvmProcess.setParameters(Arrays.asList(params));
            } else if (name.equals(EXTENDED_JVM_TAG)) {
                try {
                    proActiveDescriptor.mapToExtendedJVM((JVMProcess) targetProcess, (String) activeHandler
                            .getResultObject());
                } catch (ProActiveException e) {
                    throw new SAXException(e);
                }
            } else {
                super.notifyEndActiveHandler(name, activeHandler);
            }
        }

        protected class ExtendedJVMHandler extends ProcessReferenceHandler {
            public ExtendedJVMHandler() {
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
                super.startContextElement(name, attributes);

                String overwrite = attributes.getValue("overwriteParameters");

                if ((overwrite != null) && overwrite.equals("yes")) {
                    ((JVMProcess) targetProcess).setOverwrite(true);
                }
            }
        }
    }

    // end of inner class JVMProcessHandler
    protected class RSHProcessHandler extends ProcessHandler {
        public RSHProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class RSHProcessHandler
    protected class MapRshProcessHandler extends ProcessHandler {
        public MapRshProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);

            UnmarshallerHandler pathHandler = new PathHandler();
            BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
            bch.addHandler(ABS_PATH_TAG, pathHandler);
            bch.addHandler(REL_PATH_TAG, pathHandler);
            this.addHandler(SCRIPT_PATH_TAG, bch);
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // we know that it is a maprsh process since we are
            // in map rsh handler!!!
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            super.startContextElement(name, attributes);

            String parallelize = attributes.getValue("parallelize");

            if (checkNonEmpty(parallelize)) {
                ((MapRshProcess) targetProcess).setParallelization("parallelize");
            }
        }

        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            if (name.equals(SCRIPT_PATH_TAG)) {
                ((MapRshProcess) targetProcess).setScriptLocation((String) activeHandler.getResultObject());
            } else {
                super.notifyEndActiveHandler(name, activeHandler);
            }
        }
    }

    //end of inner class MapRshProcessHandler
    protected class SSHProcessHandler extends ProcessHandler {
        public SSHProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class SSHProcessHandler
    protected class RLoginProcessHandler extends ProcessHandler {
        public RLoginProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class RLoginProcessHandler
    protected class BSubProcessHandler extends ProcessHandler {
        public BSubProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(BSUB_OPTIONS_TAG, new BsubOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // we know that it is a maprsh process since we are
            // in map rsh handler!!!
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            super.startContextElement(name, attributes);

            String interactive = (attributes.getValue("interactive"));

            if (checkNonEmpty(interactive)) {
                ((LSFBSubProcess) targetProcess).setInteractive(interactive);
            }

            String queueName = (attributes.getValue("queue"));

            if (checkNonEmpty(queueName)) {
                ((LSFBSubProcess) targetProcess).setQueueName(queueName);
            }
            String jobName = (attributes.getValue("jobname"));

            if (checkNonEmpty(jobName)) {
                ((LSFBSubProcess) targetProcess).setJobname(jobName);
            }
        }

        protected class BsubOptionHandler extends PassiveCompositeUnmarshaller {
            //  	private static final String HOSTLIST_ATTRIBUTE = "hostlist";
            //  	private static final String PROCESSOR_ATRIBUTE = "processor";
            //private LSFBSubProcess bSubProcess;
            public BsubOptionHandler() {
                //this.bSubProcess = (LSFBSubProcess)targetProcess;
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_TAG, new SingleValueUnmarshaller());
                this.addHandler(RES_REQ_TAG, new SimpleValueHandler());

                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                // we know that it is a bsub process since we are
                // in bsub option!!!
                LSFBSubProcess bSubProcess = (LSFBSubProcess) targetProcess;

                if (name.equals(HOST_LIST_TAG)) {
                    bSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_TAG)) {
                    bSubProcess.setProcessorNumber((String) activeHandler.getResultObject());
                } else if (name.equals(RES_REQ_TAG)) {
                    bSubProcess.setRes_requirement((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    bSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }

        // end inner class OptionHandler
    }

    // end of inner class BSubProcessHandler
    protected class GlobusProcessHandler extends ProcessHandler {
        public GlobusProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(GLOBUS_OPTIONS_TAG, new GlobusOptionHandler());
        }

        protected class GlobusOptionHandler extends PassiveCompositeUnmarshaller {
            public GlobusOptionHandler() {
                this.addHandler(COUNT_TAG, new SingleValueUnmarshaller());
                this.addHandler(GLOBUS_MAXTIME_TAG, new SingleValueUnmarshaller());
                this.addHandler(OUTPUT_FILE, new SingleValueUnmarshaller());
                this.addHandler(ERROR_FILE, new SingleValueUnmarshaller());
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                // we know that it is a globus process since we are
                // in globus option!!!
                GlobusProcess globusProcess = (GlobusProcess) targetProcess;

                if (name.equals(COUNT_TAG)) {
                    globusProcess.setCount((String) activeHandler.getResultObject());
                } else if (name.equals(GLOBUS_MAXTIME_TAG)) {
                    globusProcess.setMaxTime((String) activeHandler.getResultObject());
                } else if (name.equals(OUTPUT_FILE)) {
                    globusProcess.setStdout((String) activeHandler.getResultObject());
                } else if (name.equals(ERROR_FILE)) {
                    globusProcess.setStderr((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }

        //end of inner class GlobusOptionHandler
    }

    protected class NGProcessHandler extends ProcessHandler {
        public NGProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(NG_OPTIONS_TAG, new NGOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);
            String jobname = (attributes.getValue("jobname"));
            if (checkNonEmpty(jobname)) {
                ((NGProcess) targetProcess).setJobname(jobname);
            }

            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((NGProcess) targetProcess).setQueue(queueName);
            }
        }

        protected class NGOptionHandler extends PassiveCompositeUnmarshaller {
            public NGOptionHandler() {
                this.addHandler(COUNT_TAG, new SingleValueUnmarshaller());
                this.addHandler(OUTPUT_FILE, new SingleValueUnmarshaller());
                this.addHandler(ERROR_FILE, new SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                UnmarshallerHandler pathHandler = new PathHandler();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(EXECUTABLE_TAG, bch);
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                // we know that it is a globus process since we are
                // in globus option!!!
                NGProcess ngProcess = (NGProcess) targetProcess;

                if (name.equals(COUNT_TAG)) {
                    ngProcess.setCount((String) activeHandler.getResultObject());
                } else if (name.equals(OUTPUT_FILE)) {
                    ngProcess.setStdout((String) activeHandler.getResultObject());
                } else if (name.equals(ERROR_FILE)) {
                    ngProcess.setStderr((String) activeHandler.getResultObject());
                } else if (name.equals(EXECUTABLE_TAG)) {
                    ngProcess.setExecutable((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    // MPI Process Handler
    protected class MPIProcessHandler extends ProcessHandler {
        public MPIProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(MPI_PROCESS_OPTIONS_TAG, new MPIOptionHandler());
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);

            try {
                String mpiFileName = (attributes.getValue("mpiFileName"));
                if (checkNonEmpty(mpiFileName)) {
                    ((MPIProcess) targetProcess).setMpiFileName(mpiFileName);
                }
                String hostsFileName = (attributes.getValue("hostsFileName"));
                if (checkNonEmpty(hostsFileName)) {
                    ((MPIProcess) targetProcess).setHostsFileName(hostsFileName);
                }
                String mpiCommandOptions = (attributes.getValue("mpiCommandOptions"));
                if (checkNonEmpty(mpiCommandOptions)) {
                    ((MPIProcess) targetProcess).setMpiCommandOptions(mpiCommandOptions);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws SAXException {
            super.notifyEndActiveHandler(name, activeHandler);
        }

        protected class MPIOptionHandler extends PassiveCompositeUnmarshaller {
            public MPIOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(MPI_LOCAL_PATH_TAG, bch);
                this.addHandler(MPI_REMOTE_PATH_TAG, bch);
                this.addHandler(MPI_PROCESS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(MPI_NOLOCAL, new SingleValueUnmarshaller());
            }

            @Override
            public void startContextElement(String name, Attributes attributes)
                    throws org.xml.sax.SAXException {
            }

            @Override
            protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                    throws org.xml.sax.SAXException {
                try {
                    if (name.equals(MPI_LOCAL_PATH_TAG)) {
                        ((MPIProcess) targetProcess).setLocalPath((String) activeHandler.getResultObject());
                    } else if (name.equals(MPI_REMOTE_PATH_TAG)) {
                        ((MPIProcess) targetProcess).setRemotePath((String) activeHandler.getResultObject());
                    } else if (name.equals(MPI_PROCESS_NUMBER_TAG)) {
                        ((MPIProcess) targetProcess).setHostsNumber((String) activeHandler.getResultObject());
                    } else if (name.equals(MPI_NOLOCAL)) {
                        ((MPIProcess) targetProcess).setNoLocal((String) activeHandler.getResultObject());
                    } else {
                        super.notifyEndActiveHandler(name, activeHandler);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    } //  END OF MPI PROCESS HANDLER

    // DEPENDENTPROCESSSEQUENCE process Handler
    public class DependentProcessSequenceHandler extends AbstractUnmarshallerDecorator implements
            ProActiveDescriptorConstants {
        protected ProActiveDescriptorInternal proActiveDescriptor;
        protected boolean isRef;

        public DependentProcessSequenceHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
            this.addHandler(PROCESS_REFERENCE_TAG, new ProcessReferenceHandler());
            this.addHandler(SERVICE_REFERENCE_TAG, new ProcessReferenceHandler());
        }

        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String className = attributes.getValue("class");

            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException("Process defined without specifying the class");
            }

            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // -- implements UnmarshallerHandler
        public Object getResultObject() throws org.xml.sax.SAXException {
            return null;
        }

        // -- PROTECTED METHODS
        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
            if (name.equals(PROCESS_REFERENCE_TAG)) {
                DependentListProcess dep = (DependentListProcess) targetProcess;
                Object result = activeHandler.getResultObject();
                proActiveDescriptor.addProcessToSequenceList(dep, (String) result);
            }
            if (name.equals(SERVICE_REFERENCE_TAG)) {
                DependentListProcess dep = (DependentListProcess) targetProcess;
                Object result = activeHandler.getResultObject();
                //System.out.println(" ON service found: " + (String) result);
                proActiveDescriptor.addServiceToSequenceList(dep, (String) result);
            }
        } //  END OF DEPENDENTPROCESSSEQUENCE PROCESS HANDLER
    }

    // SEQUENTIALPROCESS process Handler
    public class SequentialProcessHandler extends AbstractUnmarshallerDecorator implements
            ProActiveDescriptorConstants {
        protected ProActiveDescriptorInternal proActiveDescriptor;
        protected boolean isRef;

        public SequentialProcessHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
            this.addHandler(PROCESS_REFERENCE_TAG, new ProcessReferenceHandler());
        }

        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String className = attributes.getValue("class");

            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException("Process defined without specifying the class");
            }

            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // -- implements UnmarshallerHandler
        public Object getResultObject() throws org.xml.sax.SAXException {
            return null;
        }

        // -- PROTECTED METHODS
        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
            if (name.equals(PROCESS_REFERENCE_TAG)) {
                IndependentListProcess dep = (IndependentListProcess) targetProcess;
                Object result = activeHandler.getResultObject();
                proActiveDescriptor.addProcessToSequenceList(dep, (String) result);
            }
        }
    } //  END OF SEQUENTIALPROCESS PROCESS HANDLER

    //  Begin inner class SingleValueUnmarshaller
    private class SimpleValueHandler extends BasicUnmarshaller {
        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // read from XML
            String value = attributes.getValue("value");

            setResultObject(value);
        }
    }
}
