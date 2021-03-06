/*
 * This file is part of qSim.
 *
 * qSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with qSim.  If not, see <http://www.gnu.org/licenses/>.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.netbeans.api.javahelp.Help;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Switch;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification.Available;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification.Available.IP_PRECEDENCE;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.BestEffortClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.DscpClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.FlowBasedClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.IpPrecedenceClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.impl.NoClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.ActiveQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.BestEffortQueueManagement;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.RandomEarlyDetection;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.WeightedRED;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.queuemanagement.impl.WeightedRED.WredDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling.Available.FIFO;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling.Available.PRIORITY_QUEUEING;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling.Available.ROUND_ROBIN;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling.Available.WEIGHTED_ROUND_ROBIN;
import static sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.PacketScheduling.Available.WFQ;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.PriorityQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.RoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedFairQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedRoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.utils.ClassDefinition;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.QosCreationException;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.ConfirmDialogPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.ClassDefinitionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.DscpClassificationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.IpClassificationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.RedQueueManagementDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.WredQueueManagementDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.WrrConfigurationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.BlockingDialog;

/**
 *
 * @author Igor Kvasnicka
 */
public class SwitchConfigurationDialog extends BlockingDialog<Switch> {

    private static Logger logg = Logger.getLogger(SwitchConfigurationDialog.class);
    private DscpClassificationDialog dscpClassificationDialog;
    private IpClassificationDialog ipClassificationDialog;
    private RedQueueManagementDialog redQueueManagementDialog;
    private WredQueueManagementDialog wredQueueManagementDialog;
    private WrrConfigurationDialog wrrConfigurationDialog;
    private OutputQueuesConfigDialog outputQueuesConfigDialog;
    private boolean creatingComboboxes; //all comboboxes are listening for changes, what is not good when creating (populating) comboboxes
    private ComboItem selectedPacketClassification; //to temporary store selected classification mechanism
    private ComboItem selectedQueueManag;
    private ComboItem selectedPacketSched;
    public static final double DEFAULT_TCP_TIMEOUT = 200;//www.6test.edu.cn/~lujx/linux_networking/0131777203_ch24lev1sec5.html
    private String name = null; //workaround for a SwingX bug, when JXTextField does not return getText() value correctly, when set programmatically
    private String originalName = null;//used when editing

    /**
     * Creates new form RouterConfigurationDialog
     */
    public SwitchConfigurationDialog(String switchName) {
        super(WindowManager.getDefault().getMainWindow());
        initComponents();

        ((SpinnerNumberModel) spinProcessingMin.getModel()).setMinimum(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMin.getModel()).setValue(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMax.getModel()).setMinimum(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMax.getModel()).setValue(DelayHelper.MIN_PROCESSING_DELAY);
        txtName.setText(switchName);
        name = switchName;
        originalName = null;
        lblError.setVisible(false);
        this.setSize(489, 423);
        this.setMinimumSize(new Dimension(489, 423));
        initQosComboboxes();
        initQosConfigurationButtons();

        outputQueuesConfigDialog = new OutputQueuesConfigDialog(WindowManager.getDefault().getMainWindow());
        selectedPacketClassification = ((ComboItem) comboQosClassif.getItemAt(0));
        selectedQueueManag = ((ComboItem) comboQosQueue.getItemAt(0));
        selectedPacketSched = ((ComboItem) comboQosScheduling.getItemAt(0));
        spinTcpTimeout.setValue(DEFAULT_TCP_TIMEOUT);
    }

    /**
     * this constructor is used when editing
     *
     * @param existingRouter
     */
    public SwitchConfigurationDialog(Switch swi, String originalName, boolean copied) {
        this(swi.getName());

        if (copied) {
            setTitle(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "SwitchConfigurationDialog.title"));
            txtName.setText("");
            this.originalName = null;
        } else {
            this.originalName = originalName;
        }

        spinProcessingMax.setValue(swi.getMaxProcessingDelay());
        spinProcessingMin.setValue(swi.getMinProcessingDelay());
        spinTcpTimeout.setValue(swi.getTcpDelay());
        spinProcessing.setValue(swi.getMaxProcessingPackets());
        spinInputQueue.setValue(swi.getMaxIntputQueueSize());
        outputQueuesConfigDialog.setOutputQueues(swi.getOutputQueueManager().getQueues());
        spinTx.setValue(swi.getMaxTxBufferSize());
        spinRx.setValue(swi.getMaxRxBufferSize());

        txtName.setText(swi.getName());
        txtDescription.setText(swi.getDescription());



        PacketClassification.Available packetClassifEnum = retirevePacketClassifEnum(swi.getQosMechanism().getPacketClassification());
        switch (packetClassifEnum) {
            case BEST_EFFORT:
                comboQosClassif.setSelectedIndex(0);
                break;
            case DSCP:
                HashMap<String, Object> params = ((DscpClassification) (swi.getQosMechanism()).getPacketClassification()).getParameters();
                dscpClassificationDialog = new DscpClassificationDialog(this, (DscpManager) params.get(DscpClassification.DSCP_DEFINITIONS));
                comboQosClassif.setSelectedIndex(1);
                break;
            case FLOW_BASED:
                comboQosClassif.setSelectedIndex(2);
                break;
            case IP_PRECEDENCE:
                HashMap<String, Object> paramsIp = ((IpPrecedenceClassification) (swi.getQosMechanism()).getPacketClassification()).getParameters();
                IpPrecedenceClassification.IpDefinition[] ipDefinitions = (IpPrecedenceClassification.IpDefinition[]) paramsIp.get(IpPrecedenceClassification.IP_DEFINITIONS);
                IpPrecedenceClassification.IpDefinition undefinedQueue = (IpPrecedenceClassification.IpDefinition) paramsIp.get(IpPrecedenceClassification.NOT_DEFINED_QUEUE);
                ipClassificationDialog = new IpClassificationDialog(this, ipDefinitions, undefinedQueue);
                comboQosClassif.setSelectedIndex(3);
                break;
            case NONE:
                comboQosClassif.setSelectedIndex(4);
                break;
            default:
                throw new IllegalStateException("unknown packet classif enum: " + packetClassifEnum);
        }

        ActiveQueueManagement.Available activeQueueEnum = retireveActiveQueueManagEnum(swi.getQosMechanism().getActiveQueueManagement());
        HashMap<String, Object> paramsQueue;
        switch (activeQueueEnum) {
            case NONE:
                comboQosQueue.setSelectedIndex(0);
                break;
            case RED:
                paramsQueue = ((RandomEarlyDetection) (swi.getQosMechanism()).getActiveQueueManagement()).getParameters();
                redQueueManagementDialog = new RedQueueManagementDialog(this, (Double) paramsQueue.get(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR), (Double) paramsQueue.get(RandomEarlyDetection.MAX_PROBABILITY), (Double) paramsQueue.get(RandomEarlyDetection.MAX_THRESHOLD), (Double) paramsQueue.get(RandomEarlyDetection.MIN_THRESHOLD));
                comboQosQueue.setSelectedIndex(1);
                break;
            case WRED:
                paramsQueue = ((WeightedRED) (swi.getQosMechanism()).getActiveQueueManagement()).getParameters();
                wredQueueManagementDialog = new WredQueueManagementDialog(this, (WredDefinition[]) paramsQueue.get(WeightedRED.WRED_DEFINITION), isDscpClassificationSelected());
                comboQosQueue.setSelectedIndex(2);
                break;
            default:
                throw new IllegalStateException("unknown active queue management enum: " + activeQueueEnum);
        }

        PacketScheduling.Available packetSchedEnum = retirevePacketSchedulingEnum(swi.getQosMechanism().getPacketScheduling());
        switch (packetSchedEnum) {
            case FIFO:
                comboQosScheduling.setSelectedIndex(0);
                break;
            case PRIORITY_QUEUEING:
                comboQosScheduling.setSelectedIndex(1);
                break;
            case ROUND_ROBIN:
                comboQosScheduling.setSelectedIndex(2);
                break;
            case WEIGHTED_ROUND_ROBIN:
                comboQosScheduling.setSelectedIndex(3);
                break;
            case WFQ:
                comboQosScheduling.setSelectedIndex(4);
                break;
            default:
                throw new IllegalStateException("unknown packet scheduling enum: " + packetSchedEnum);
        }

        if (swi.getQosMechanism().getQueueWeights() != null) {
            wrrConfigurationDialog = new WrrConfigurationDialog(this, swi.getQosMechanism().getQueueWeights());
        }

        selectedPacketClassification = (ComboItem) comboQosClassif.getSelectedItem();
        selectedQueueManag = (ComboItem) comboQosQueue.getSelectedItem();
        selectedPacketSched = (ComboItem) comboQosScheduling.getSelectedItem();
    }

    @Override
    protected void showDialogHook() {
        /**
         * if simulation is running, no edits are allowed - so user cannot hit
         * OK button
         */
        bntOk.setEnabled(!NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().isSimulationRunning());
    }

    private PacketScheduling.Available retirevePacketSchedulingEnum(PacketScheduling scheduling) {
        if (scheduling instanceof FifoScheduling) {
            return PacketScheduling.Available.FIFO;
        }
        if (scheduling instanceof PriorityQueuingScheduling) {
            return PacketScheduling.Available.PRIORITY_QUEUEING;
        }
        if (scheduling instanceof RoundRobinScheduling) {
            return PacketScheduling.Available.ROUND_ROBIN;
        }
        if (scheduling instanceof WeightedRoundRobinScheduling) {
            return PacketScheduling.Available.WEIGHTED_ROUND_ROBIN;
        }
        if (scheduling instanceof WeightedFairQueuingScheduling) {
            return PacketScheduling.Available.WFQ;
        }

        throw new IllegalStateException("unable to determine packet scheduling enum");
    }

    private ActiveQueueManagement.Available retireveActiveQueueManagEnum(ActiveQueueManagement manag) {
        if (manag instanceof RandomEarlyDetection) {
            return ActiveQueueManagement.Available.RED;
        }
        if (manag instanceof WeightedRED) {
            return ActiveQueueManagement.Available.WRED;
        }
        if (manag instanceof BestEffortQueueManagement) {
            return ActiveQueueManagement.Available.NONE;
        }

        throw new IllegalStateException("unable to determine queue management enum");
    }

    private PacketClassification.Available retirevePacketClassifEnum(PacketClassification classif) {
        if (classif instanceof BestEffortClassification) {
            return Available.BEST_EFFORT;
        }
        if (classif instanceof DscpClassification) {
            return Available.DSCP;
        }
        if (classif instanceof FlowBasedClassification) {
            return Available.FLOW_BASED;
        }
        if (classif instanceof IpPrecedenceClassification) {
            return Available.IP_PRECEDENCE;
        }
        if (classif instanceof NoClassification) {
            return Available.NONE;
        }

        throw new IllegalStateException("unable to determine packet classif enum");
    }

    private void initQosComboboxes() {
        creatingComboboxes = true;

        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.NONE, "None"));
        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.RED, "Random early detection (RED)"));
        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.WRED, "Weighted RED (WRED)"));


        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.BEST_EFFORT, "Best effort"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.DSCP, "DSCP"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.FLOW_BASED, "Flow based"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.IP_PRECEDENCE, "Type of service"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.NONE, "Use previous"));

        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.FIFO, "FIFO"));//0
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.PRIORITY_QUEUEING, "Priority queueing"));//1
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.ROUND_ROBIN, "Fair queueing"));//2
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.WEIGHTED_ROUND_ROBIN, "Weighted round robin"));//3
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.WFQ, "Weighted fair queueing (WFQ)"));//4

        creatingComboboxes = false;
    }

    /**
     * when dialog is created, it is necessary to enable/disable configuration
     * buttons
     */
    private void initQosConfigurationButtons() {

        //classification configuration
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        btnConfigClassif.setEnabled(classEnum.hasParameters());

        //active queue management configuration
        btnConfigQueue.setEnabled(((ActiveQueueManagement.Available) ((ComboItem) comboQosQueue.getSelectedItem()).getValue()).hasParameters());

        //packet scheudling configuration
        PacketScheduling.Available schedEnum = ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());
        btnConfigScheduling.setEnabled(schedEnum.hasParameters());

    }

    /**
     * show appropriate configuration dialog for desired classification
     */
    private void showConfigClassification() {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());

        if (!classEnum.hasParameters()) {
            logg.warn("button for configuration of Qos classification mechanism with no parameters was pressed - this should not happen...");
            return;
        }
        switch (classEnum) {
            case DSCP:
                if (dscpClassificationDialog == null) {
                    dscpClassificationDialog = new DscpClassificationDialog(this);
                }
                dscpClassificationDialog.setVisible(true);
                break;
            case IP_PRECEDENCE:
                if (ipClassificationDialog == null) {
                    ipClassificationDialog = new IpClassificationDialog(this);
                }
                ipClassificationDialog.setVisible(true);
                break;
            default:
                throw new IllegalStateException("undefined dialog for classification " + classEnum);
        }
    }

    /**
     * show appropriate configuration dialog for desired active queue management
     */
    private void showConfigQueueManagement() {
        ActiveQueueManagement.Available classEnum = ((ActiveQueueManagement.Available) ((ComboItem) comboQosQueue.getSelectedItem()).getValue());

        if (!classEnum.hasParameters()) {
            logg.warn("button for configuration of Qos active queuemanagement mechanism with no parameters was pressed - this should not happen...");
            return;
        }
        switch (classEnum) {
            case RED:
                if (redQueueManagementDialog == null) {
                    redQueueManagementDialog = new RedQueueManagementDialog(this);
                }
                redQueueManagementDialog.setVisible(true);
                break;

            case WRED:
                if (wredQueueManagementDialog == null) {
                    wredQueueManagementDialog = new WredQueueManagementDialog(this, isDscpClassificationSelected());
                }
                wredQueueManagementDialog.setVisible(true);


                break;
            default:
                throw new IllegalStateException("undefined dialog for queue management " + classEnum);
        }
    }

    /**
     * show appropriate configuration dialog for desired packet scheduling
     */
    private void showConfigScheduling() {
        PacketScheduling.Available classEnum = ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());

        if (!classEnum.hasParameters()) {
            logg.warn("button for configuration of Qos packet scheduling mechanism with no parameters was pressed - this should not happen...");
            return;
        }
        switch (classEnum) {
            case WEIGHTED_ROUND_ROBIN:
                showWrrConfiguration();
                break;
            default:
                throw new IllegalStateException("undefined dialog for packet scheduling " + classEnum);
        }
    }

    private void showWrrConfiguration() {
        if (wrrConfigurationDialog == null) {
            wrrConfigurationDialog = new WrrConfigurationDialog(this, getOutputQueueCount());
        }
        wrrConfigurationDialog.setVisible(true);
    }

    private boolean isDscpClassificationSelected() {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        if (PacketClassification.Available.DSCP == classEnum) {
            return true;
        }
        return false;
    }

    private boolean isIpPrecedenceClassificationSelected() {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((SwitchConfigurationDialog.ComboItem) comboQosClassif.getSelectedItem()).getValue());
        if (PacketClassification.Available.IP_PRECEDENCE == classEnum) {
            return true;
        }
        return false;
    }

//    /**
//     * returns queue numbers of all defined queues
//     *
//     * @return null if flow based classification
//     */
//    private Set<Integer> getDefinedQueues() {
//        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
//        Set<Integer> queues = new TreeSet<Integer>();
//
//        switch (classEnum) {
//            case BEST_EFFORT:
//                queues.add(0);
//                break;
//            case DSCP:
//                for (DscpDefinition def : dscpClassificationDialog.getDscpDefinitions()) {
//                    queues.add(def.getQueueNumber());
//                }
//                break;
//            case FLOW_BASED:
//                queues = null;
//                break;
//            case IP_PRECEDENCE:
//                for (int i = 0; i < 8; i++) {
//                    queues.add(i);
//                }
//                break;
//            case NONE:
//                break;
//            default:
//                throw new IllegalStateException("unable to predict numbe rof output queues for classification: " + classEnum);
//        }
//        return queues;
//    }
    public int getOutputQueueCount() {
        if (outputQueuesConfigDialog == null || outputQueuesConfigDialog.getUserInput() == null) {
            return 0;
        }
        return outputQueuesConfigDialog.getUserInput().size();
    }

    /**
     * creates QosMechanismDefinition object according to user input user input
     * should be already validated before calling this method
     *
     * @return non-null string if some QoS mechanism was not configured at all
     */
    private QosMechanismDefinition createQosMechanismDefinition() throws QosCreationException {
        //packet classifiation
        PacketClassification packetClassification = createPacketClassification();

        //active queue management
        ActiveQueueManagement activeQueueManagement = createActiveQueueManagement();

        //packet scheduling
        PacketScheduling packetScheduling = createPacketScheduling();

        //qos classes
        ClassDefinition[] classes = null;
        if (areClassesDefined()) {
            classes = wredQueueManagementDialog.getAllClassDefinitions();
        }
        int[] qeueuWeights = null;
        if (areQueueWeightsDefined()) {
            qeueuWeights = wrrConfigurationDialog.getWeights();
        }

        //finally create QoS mechanism definition        
        QosMechanismDefinition qosMechanismDefinition = new QosMechanismDefinition(qeueuWeights, packetScheduling, packetClassification, activeQueueManagement);
        qosMechanismDefinition.setClassDefinitions(classes);
        return qosMechanismDefinition;
    }

    private PacketScheduling createPacketScheduling() throws QosCreationException {
        PacketScheduling packetScheduling;
        PacketScheduling.Available schedAvailableEnum = ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());
        PacketClassification.Available classifEnum = ((PacketClassification.Available) ((SwitchConfigurationDialog.ComboItem) comboQosClassif.getSelectedItem()).getValue());

        //check for classes to be defined - if class based scheduling is selected      

        switch (schedAvailableEnum) {
            case FIFO:
                packetScheduling = new FifoScheduling();
                break;
            case PRIORITY_QUEUEING:
                packetScheduling = new PriorityQueuingScheduling();
                break;
            case ROUND_ROBIN:
                packetScheduling = new RoundRobinScheduling();
                break;
            case WEIGHTED_ROUND_ROBIN:
                if (wrrConfigurationDialog == null) {
                    throw new IllegalStateException("classDefinitionDialog is NULL - this should be taken care of");
                }
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(WeightedRoundRobinScheduling.QUEUES_WEIGHT, wrrConfigurationDialog.getWeights());
                packetScheduling = new WeightedRoundRobinScheduling(params);
                break;
            case WFQ:
                packetScheduling = new WeightedFairQueuingScheduling();
                break;
            default:
                throw new IllegalStateException("unknown enum for active queue management: " + schedAvailableEnum);
        }

        return packetScheduling;
    }

    private ActiveQueueManagement createActiveQueueManagement() throws QosCreationException {
        ActiveQueueManagement queueManagement;
        ActiveQueueManagement.Available classEnum = ((ActiveQueueManagement.Available) ((ComboItem) comboQosQueue.getSelectedItem()).getValue());
        switch (classEnum) {
            case NONE:
                queueManagement = new BestEffortQueueManagement();
                break;
            case RED:
                if (redQueueManagementDialog == null) {
                    throw new QosCreationException(NbBundle.getMessage(RouterConfigurationDialog.class, "queue_management_not_configured"));
                }
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, redQueueManagementDialog.getExponentialWeightFactor());
                params.put(RandomEarlyDetection.MAX_PROBABILITY, redQueueManagementDialog.getMaxProbability());
                params.put(RandomEarlyDetection.MIN_THRESHOLD, redQueueManagementDialog.getMinThreshlod());
                params.put(RandomEarlyDetection.MAX_THRESHOLD, redQueueManagementDialog.getMaxThreshlod());
                queueManagement = new RandomEarlyDetection(params);
                break;
            case WRED:
                if (wredQueueManagementDialog == null) {
                    throw new QosCreationException(NbBundle.getMessage(RouterConfigurationDialog.class, "queue_management_not_configured"));
                }
                if (!areClassesDefined()) {
                    throw new QosCreationException(NbBundle.getMessage(RouterConfigurationDialog.class, "queue_management_not_configured"));
                }
                Collection<WredDefinition> configuration = wredQueueManagementDialog.getConfiguration();
                final WeightedRED.WredDefinition[] definitions = configuration.toArray(new WredDefinition[configuration.size()]);
                HashMap<String, Object> params2 = new HashMap<String, Object>();
                params2.put(WeightedRED.WRED_DEFINITION, definitions);
                queueManagement = new WeightedRED(params2);
                break;
            default:
                throw new IllegalStateException("unknown enum for active queue management: " + classEnum);
        }

        return queueManagement;
    }

    private PacketClassification createPacketClassification() throws QosCreationException {
        PacketClassification classification;
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        switch (classEnum) {
            case BEST_EFFORT:
                classification = new BestEffortClassification();
                break;
            case DSCP:
                if (dscpClassificationDialog == null) {
                    dscpClassificationDialog = new DscpClassificationDialog(this);
                }
                dscpClassificationDialog.makeDefaultQueueNumber();
                dscpClassificationDialog.makeDefinitions();
                List<DscpDefinition> result = dscpClassificationDialog.getDscpDefinitions();
                final DscpManager dscpManager = new DscpManager(result.toArray(new DscpDefinition[result.size()]), dscpClassificationDialog.getDefaultQueueNumber(), getOutputQueueCount());

                HashMap<String, Object> params2 = new HashMap<String, Object>();
                params2.put(DscpClassification.DSCP_DEFINITIONS, dscpManager);
                classification = new DscpClassification(params2);
                break;
            case FLOW_BASED:
                classification = new FlowBasedClassification();
                break;
            case IP_PRECEDENCE:
                if (ipClassificationDialog == null) {
                    ipClassificationDialog = new IpClassificationDialog(this);
                }
                ipClassificationDialog.makeDefaultQueueNumber();
                ipClassificationDialog.makeIpDefinitions();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(IpPrecedenceClassification.NOT_DEFINED_QUEUE, ipClassificationDialog.getDefaultQueueNumber());
                params.put(IpPrecedenceClassification.OUTPUT_QUEUE_COUNT, getOutputQueueCount());
                params.put(IpPrecedenceClassification.IP_DEFINITIONS, ipClassificationDialog.getIpDefinitions());
                classification = new IpPrecedenceClassification(params);
                break;
            case NONE:
                classification = new NoClassification();
                break;
            default:
                throw new IllegalStateException("unknown enum for classification: " + classEnum);
        }

        return classification;
    }

    private boolean areClassesDefined() {
        if (wredQueueManagementDialog == null) {
            return false;
        }
        if (wredQueueManagementDialog.getAllClassDefinitions() == null) {
            return false;
        }
        return wredQueueManagementDialog.getAllClassDefinitions().length != 0;
    }

    private boolean areQueueWeightsDefined() {
        if (wrrConfigurationDialog == null) {
            return false;
        }
        if (wrrConfigurationDialog.getWeights() == null) {
            return false;
        }
        return true;
    }

    private PacketScheduling.Available getSelectedPacketScheduling() {
        return ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtDescription = new org.jdesktop.swingx.JXTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        spinProcessing = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spinTx = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinRx = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        spinInputQueue = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        spinProcessingMin = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        spinProcessingMax = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        spinTcpTimeout = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        comboQosQueue = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        comboQosClassif = new javax.swing.JComboBox();
        btnConfigQueue = new javax.swing.JButton();
        btnConfigScheduling = new javax.swing.JButton();
        btnConfigClassif = new javax.swing.JButton();
        comboQosScheduling = new sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DisabledItemsComboBox();
        jButton3 = new javax.swing.JButton();
        bntOk = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(502, 423));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel1.text")); // NOI18N

        txtName.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.txtName.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel6.text")); // NOI18N

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setToolTipText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.txtDescription.toolTipText")); // NOI18N
        txtDescription.setPrompt(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.txtDescription.prompt")); // NOI18N
        jScrollPane3.setViewportView(txtDescription);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel6))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel2.text")); // NOI18N

        spinProcessing.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(50), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel3.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel3.text")); // NOI18N

        spinTx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(-1), null, Integer.valueOf(1)));

        jLabel4.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel4.text")); // NOI18N

        spinRx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(-1), null, Integer.valueOf(1)));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel5.text")); // NOI18N

        spinInputQueue.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1)));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jPanel4.border.title"))); // NOI18N
        jPanel4.setToolTipText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jPanel4.toolTipText")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel7.text")); // NOI18N

        spinProcessingMin.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        jLabel8.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel8.text")); // NOI18N

        spinProcessingMax.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spinProcessingMin, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                    .addComponent(spinProcessingMax))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(spinProcessingMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(spinProcessingMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel9.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel9.text")); // NOI18N

        spinTcpTimeout.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        jLabel10.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel10.text")); // NOI18N

        jLabel11.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel11.text")); // NOI18N

        jButton2.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel12.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel12.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(30, 30, 30)
                        .addComponent(spinTcpTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel5))
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spinInputQueue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spinRx, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spinTx, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spinProcessing, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))))))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinProcessing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinTx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spinRx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(spinInputQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(spinTcpTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jLabel13.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel13.text")); // NOI18N

        comboQosQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboQosQueueActionPerformed(evt);
            }
        });

        jLabel14.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel14.text")); // NOI18N

        jLabel15.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jLabel15.text")); // NOI18N

        comboQosClassif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboQosClassifActionPerformed(evt);
            }
        });

        btnConfigQueue.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.btnConfigQueue.text")); // NOI18N
        btnConfigQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigQueueActionPerformed(evt);
            }
        });

        btnConfigScheduling.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.btnConfigScheduling.text")); // NOI18N
        btnConfigScheduling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigSchedulingActionPerformed(evt);
            }
        });

        btnConfigClassif.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.btnConfigClassif.text")); // NOI18N
        btnConfigClassif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigClassifActionPerformed(evt);
            }
        });

        comboQosScheduling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboQosSchedulingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGap(0, 104, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addComponent(comboQosClassif, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnConfigClassif, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(comboQosScheduling, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(comboQosQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(37, 37, 37)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnConfigQueue, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .addComponent(btnConfigScheduling, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(77, 77, 77))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel13)
                            .addComponent(jLabel15))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboQosClassif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConfigClassif))
                .addGap(18, 18, 18)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboQosQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConfigQueue))
                .addGap(18, 18, 18)
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConfigScheduling)
                    .addComponent(comboQosScheduling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/help.png"))); // NOI18N
        jButton3.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jButton3.toolTipText")); // NOI18N
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        bntOk.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.bntOk.text")); // NOI18N
        bntOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntOkActionPerformed(evt);
            }
        });

        jButton8.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.jButton8.text")); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setText(org.openide.util.NbBundle.getMessage(SwitchConfigurationDialog.class, "SwitchConfigurationDialog.lblError.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(58, 58, 58)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))))
            .addGroup(layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(bntOk, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblError)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bntOk)
                    .addComponent(jButton8))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Help help = Lookup.getDefault().lookup(Help.class);
        help.showHelp(new HelpCtx("sk.stuba.fiit.kvasnicka.topologyvisual.switch"));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void bntOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntOkActionPerformed
        //OK button

        if ((txtName.getText() != null)) {
            name = txtName.getText();
        }

        if (!validateInput()) {
            return;
        }

        Double maxProcessingDelay = (Double) spinProcessingMax.getValue();
        Double minProcessingDelay = (Double) spinProcessingMin.getValue();
        Double tcptimeout = (Double) spinTcpTimeout.getValue();
        Integer processingPackets = (Integer) spinProcessing.getValue();
        Integer inputQueue = (Integer) spinInputQueue.getValue();
        List<OutputQueue> outputQueueList = outputQueuesConfigDialog.getUserInput();
        Integer rxSize = (Integer) spinRx.getValue();
        Integer txSize = (Integer) spinTx.getValue();
        try {
            QosMechanismDefinition qosMechanismDefinition = createQosMechanismDefinition();
            if ((qosMechanismDefinition.getPacketScheduling() instanceof FifoScheduling) && outputQueueList.size() != 1) {
                throw new QosCreationException("There can be only one output queue defined for this packet scheduling algorithm.");
            }

            Switch resultObject = new Switch(name, txtDescription.getText(), qosMechanismDefinition, txSize, rxSize, outputQueueList, inputQueue, processingPackets, tcptimeout, minProcessingDelay, maxProcessingDelay);
            setUserInput(resultObject);
            closeDialog();
        } catch (QosCreationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_bntOkActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //cancel button
        cancelDialog();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void comboQosQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboQosQueueActionPerformed
        if (creatingComboboxes) {
            return;
        }



        ActiveQueueManagement.Available newSelected = ((ActiveQueueManagement.Available) ((SwitchConfigurationDialog.ComboItem) comboQosQueue.getSelectedItem()).getValue());

        if (!isDscpClassificationSelected() && !isIpPrecedenceClassificationSelected()) {
            if (newSelected == ActiveQueueManagement.Available.WRED) {//WRED can be selected only for IP or DSCP classification
                comboQosQueue.setSelectedItem(selectedQueueManag);
                JOptionPane.showMessageDialog(this,
                        "WRED can be selected only, when DSCP or Type of service classification is selected as packet classification.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        btnConfigQueue.setEnabled(newSelected.hasParameters());
        selectedQueueManag = ((ComboItem) comboQosQueue.getSelectedItem());
    }//GEN-LAST:event_comboQosQueueActionPerformed

    private void btnConfigQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigQueueActionPerformed
        showConfigQueueManagement();
    }//GEN-LAST:event_btnConfigQueueActionPerformed

    private void comboQosClassifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboQosClassifActionPerformed
        if (creatingComboboxes) {
            return;
        }

        ComboItem classEnum = ((ComboItem) comboQosClassif.getSelectedItem());

        if ((PacketClassification.Available) selectedPacketClassification.getValue() == (PacketClassification.Available) classEnum.getValue()) {//user has selected the same item again
            return;
        }

        selectedPacketClassification = classEnum;

        //flow based classification and class based packet scheduling dont work together
        btnConfigClassif.setEnabled(((PacketClassification.Available) classEnum.getValue()).hasParameters());
    }//GEN-LAST:event_comboQosClassifActionPerformed

    private void btnConfigClassifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigClassifActionPerformed
        showConfigClassification();
    }//GEN-LAST:event_btnConfigClassifActionPerformed

    private void btnConfigSchedulingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigSchedulingActionPerformed
        showConfigScheduling();
    }//GEN-LAST:event_btnConfigSchedulingActionPerformed

    private void comboQosSchedulingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboQosSchedulingActionPerformed
        if (creatingComboboxes) {
            return;
        }

        PacketScheduling.Available schedEnum = ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());


        if ((PacketScheduling.Available.WEIGHTED_ROUND_ROBIN == schedEnum)) {
            if (!areClassesDefined()) {//user selected one of class based scheduling mechanisms, but no classes are defined
                if (!PreferenciesHelper.isNeverShowQosClassConfirmation()) {
                    ConfirmDialogPanel panel = new ConfirmDialogPanel(NbBundle.getMessage(RouterConfigurationDialog.class, "class_definition_warning_scheduling"));
                    NotifyDescriptor.Confirmation descriptor = new NotifyDescriptor.Confirmation(
                            panel, // instance of your panel
                            NbBundle.getMessage(RouterConfigurationDialog.class, "warning_title"), // title of the dialog
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE);

                    //show dialog
                    DialogDisplayer.getDefault().notify(descriptor);


                    if (panel.isNeverShow()) {
                        PreferenciesHelper.setNeverShowQosClassConfirmation(panel.isNeverShow());
                    }
                }

                //user cannot select this item, yet - reverse user selection                                
                comboQosScheduling.setSelectedItem(selectedPacketSched);
                return;

            }
        }

        btnConfigScheduling.setEnabled(schedEnum.hasParameters());
        selectedPacketSched = ((ComboItem) comboQosScheduling.getSelectedItem());

    }//GEN-LAST:event_comboQosSchedulingActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        outputQueuesConfigDialog.showDialog();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntOk;
    private javax.swing.JButton btnConfigClassif;
    private javax.swing.JButton btnConfigQueue;
    private javax.swing.JButton btnConfigScheduling;
    private javax.swing.JComboBox comboQosClassif;
    private javax.swing.JComboBox comboQosQueue;
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DisabledItemsComboBox comboQosScheduling;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblError;
    private javax.swing.JSpinner spinInputQueue;
    private javax.swing.JSpinner spinProcessing;
    private javax.swing.JSpinner spinProcessingMax;
    private javax.swing.JSpinner spinProcessingMin;
    private javax.swing.JSpinner spinRx;
    private javax.swing.JSpinner spinTcpTimeout;
    private javax.swing.JSpinner spinTx;
    private org.jdesktop.swingx.JXTextArea txtDescription;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables

    private boolean validateInput() {
        lblError.setVisible(false);

        if (StringUtils.isEmpty(name)) {//name is required
            showError("Name is required.");
            return false;
        }
        if (!name.equals(originalName)) {
            if (!NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().isVertexNameUnique(name)) {
                showError("Name must be unique.");
                return false;
            }
        }

        if ((Double) spinProcessingMin.getValue() > (Double) spinProcessingMax.getValue()) {
            showError("Minimal processing delay must not be greater than maximal proc. delay.");
            return false;
        }

        //the rest of the validation is done by JSpinner components themself (see their model definitions, if you don't believe me)

        return true;
    }

    private void showError(String text) {
        lblError.setText(text);
        lblError.setVisible(true);
    }

    private static class ComboItem {

        @Getter
        private Object value;
        @Getter
        private String label;

        public ComboItem(Object value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
