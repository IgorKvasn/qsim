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
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.helpers.DelayHelper;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.QosMechanismDefinition;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.PacketClassification.Available;
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
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.ClassBasedWFQScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.FifoScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.PriorityQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.RoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedFairQueuingScheduling;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.scheduling.impl.WeightedRoundRobinScheduling;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.QosCreationException;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.ClassDefinitionDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.DscpClassificationDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.RedQueueManagementDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology.qos.WredQueueManagementDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.BlockingDialog;

/**
 *
 * @author Igor Kvasnicka
 */
public class RouterConfigurationDialog extends BlockingDialog<RouterConfigurationDialog.ResultObject> {

    private static Logger logg = Logger.getLogger(RouterConfigurationDialog.class);
    private String routerName;
    private DscpClassificationDialog dscpClassificationDialog;
    private RedQueueManagementDialog redQueueManagementDialog;
    private WredQueueManagementDialog wredQueueManagementDialog;
    private ClassDefinitionDialog classDefinitionDialog;
    private boolean creatingComboboxes; //all comboboxes are listening for changes, what is not good when creating (populating) comboboxes
    private PacketClassification.Available selectedPacketClassification; //to temporary store selected classification mechanism

    /**
     * Creates new form RouterConfigurationDialog
     */
    public RouterConfigurationDialog(String routerName) {
        super(WindowManager.getDefault().getMainWindow());
        this.routerName = routerName;
        initComponents();

        ((SpinnerNumberModel) spinProcessingMin.getModel()).setMinimum(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMin.getModel()).setValue(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMax.getModel()).setMinimum(DelayHelper.MIN_PROCESSING_DELAY);
        ((SpinnerNumberModel) spinProcessingMax.getModel()).setValue(DelayHelper.MIN_PROCESSING_DELAY);
        txtName.setText(routerName);
        lblError.setVisible(false);
        this.setSize(489, 423);
        this.setMinimumSize(new Dimension(489, 423));
        initQosComboboxes();
        initQosConfigurationButtons();

        selectedPacketClassification = (Available) ((ComboItem) comboQosClassif.getItemAt(0)).getValue();
    }

    private void initQosComboboxes() {
        creatingComboboxes = true;

        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.NONE, "None"));
        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.RED, "Random early detection (RED)"));
        comboQosQueue.addItem(new ComboItem(ActiveQueueManagement.Available.WRED, "Weighted RED (WRED)"));


        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.BEST_EFFORT, "Best effort"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.DSCP, "DSCP"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.FLOW_BASED, "Flow based"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.IP_PRECEDENCE, "IP precedence"));
        comboQosClassif.addItem(new ComboItem(PacketClassification.Available.NONE, "Use previous"));

        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.FIFO, "FIFO"));//0
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.PRIORITY_QUEUEING, "Priority queueing"));//1
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.ROUND_ROBIN, "Round robin"));//2
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.WEIGHTED_ROUND_ROBIN, "Weighted round robin"));//3
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.WFQ, "Weighted fair queueing (WFQ)"));//4
        comboQosScheduling.addItem(new ComboItem(PacketScheduling.Available.CB_WFQ, "Class based WFQ"));//5

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
                    if (classDefinitionDialog == null) {
                        classDefinitionDialog = new ClassDefinitionDialog();
                    }
                    dscpClassificationDialog = new DscpClassificationDialog();
                }
                dscpClassificationDialog.setVisible(true);
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
                    redQueueManagementDialog = new RedQueueManagementDialog();
                }
                redQueueManagementDialog.setVisible(true);
                break;

            case WRED:
                int queueCount;
                try {
                    queueCount = calculateQueueCountByClassification();
                    if (wredQueueManagementDialog == null) {
                        wredQueueManagementDialog = new WredQueueManagementDialog(queueCount);
                    } else {
                        wredQueueManagementDialog.setQueueCountLabel(queueCount);
                    }
                    wredQueueManagementDialog.setVisible(true);
                } catch (QosCreationException ex) {
                    return;
                }

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
            logg.warn("button for configuration of Qos active queuemanagement mechanism with no parameters was pressed - this should not happen...");
            return;
        }
        switch (classEnum) {
            case CB_WFQ:
            case WEIGHTED_ROUND_ROBIN:
                if (classDefinitionDialog == null) {
                    classDefinitionDialog = new ClassDefinitionDialog();
                }
                classDefinitionDialog.showDialog(getDefinedQueues(), isDscpClassificationSelected());
                break;
            default:
                throw new IllegalStateException("undefined dialog for packet scheduling " + classEnum);
        }
    }

    private boolean isDscpClassificationSelected() {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        if (PacketClassification.Available.DSCP == classEnum) {
            return true;
        }
        return false;
    }

    /**
     * determines, how many queues will be created according to selected
     * classification
     *
     * @return -1 if unable to determine (e.g. flow based classification)
     */
    private int calculateQueueCountByClassification() throws QosCreationException {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());

        switch (classEnum) {
            case BEST_EFFORT:
                return 1;
            case DSCP:
                Set<Integer> queues = new TreeSet<Integer>();
                if (dscpClassificationDialog == null) { //dscp is not defined yet
                    JOptionPane.showMessageDialog(this,
                            "DSCP is selected, but not configured, yet.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    throw new QosCreationException("dscp not configured");
                }

                for (DscpDefinition def : dscpClassificationDialog.getDscpDefinitions()) {
                    queues.add(def.getQueueNumber());
                }
                return queues.size() + 1;
            case FLOW_BASED:
                return -1;
            case IP_PRECEDENCE:
                return 8;
            case NONE:
                return -1;
            default:
                throw new IllegalStateException("unable to predict number of output queues for classification: " + classEnum);
        }
    }

    /**
     * returns queues defined by selected classification
     *
     * @return null if unable to retrieve queue numbers (e.g. flow based
     * classification)
     */
    private Set<Integer> getQueueNumbersByClassification() throws QosCreationException {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        Set<Integer> result = new TreeSet<Integer>();

        switch (classEnum) {
            case BEST_EFFORT:
                result.add(0);
                return result;
            case DSCP:
                if (dscpClassificationDialog == null) { //dscp is not defined yet
                    JOptionPane.showMessageDialog(this,
                            "DSCP is selected, but not configured, yet.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    throw new QosCreationException("dscp not configured");
                }

                for (DscpDefinition def : dscpClassificationDialog.getDscpDefinitions()) {
                    result.add(def.getQueueNumber());
                }

                result.add(dscpClassificationDialog.getDefaultQueueNumber());

                return result;
            case FLOW_BASED:
                return null;
            case IP_PRECEDENCE:
                for (int i = 0; i < 8; i++) {
                    result.add(i);
                }
                return result;
            case NONE:
                return result;
            default:
                throw new IllegalStateException("unable to retrieve output queues for classification: " + classEnum);
        }
    }

    /**
     * returns queue numbers of all defined queues
     *
     * @return null if flow based classification
     */
    private Set<Integer> getDefinedQueues() {
        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());
        Set<Integer> queues = new TreeSet<Integer>();

        switch (classEnum) {
            case BEST_EFFORT:
                queues.add(0);
                break;
            case DSCP:
                for (DscpDefinition def : dscpClassificationDialog.getDscpDefinitions()) {
                    queues.add(def.getQueueNumber());
                }
                break;
            case FLOW_BASED:
                queues = null;
                break;
            case IP_PRECEDENCE:
                for (int i = 0; i < 8; i++) {
                    queues.add(i);
                }
                break;
            case NONE:
                break;
            default:
                throw new IllegalStateException("unable to predict numbe rof output queues for classification: " + classEnum);
        }
        return queues;
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

        //finally create QoS mechanism definition
        QosMechanismDefinition qosMechanismDefinition = new QosMechanismDefinition(packetScheduling, packetClassification, activeQueueManagement);
        return qosMechanismDefinition;
    }

    private PacketScheduling createPacketScheduling() throws QosCreationException {
        PacketScheduling packetScheduling;
        PacketScheduling.Available schedAvailableEnum = ((PacketScheduling.Available) ((ComboItem) comboQosScheduling.getSelectedItem()).getValue());

        //check for classes to be defined - if class based scheduling is selected
        if ((PacketScheduling.Available.CB_WFQ == schedAvailableEnum) || (PacketScheduling.Available.WEIGHTED_ROUND_ROBIN == schedAvailableEnum)) {
            if (!areClassesDefined()) {
                if (classDefinitionDialog == null) {
                    classDefinitionDialog = new ClassDefinitionDialog();
                }
                classDefinitionDialog.showDialog(getDefinedQueues(), isDscpClassificationSelected());
            }
        }

        if (!areClassesDefined()) {//user refused to define classes
            throw new QosCreationException("No QoS classes defined");
        }


        switch (schedAvailableEnum) {
            case FIFO:
                packetScheduling = new FifoScheduling();
                break;
            case CB_WFQ:
                if (classDefinitionDialog == null) {
                    throw new IllegalStateException("classDefinitionDialog is NULL - this should be taken care of");
                }
                packetScheduling = new ClassBasedWFQScheduling(new HashMap<String, Object>() {
                    {
                        put(ClassBasedWFQScheduling.CLASS_DEFINITIONS, classDefinitionDialog.getClasses());
                    }
                });

                break;
            case PRIORITY_QUEUEING:
                packetScheduling = new PriorityQueuingScheduling();
                break;
            case ROUND_ROBIN:
                packetScheduling = new RoundRobinScheduling();
                break;
            case WEIGHTED_ROUND_ROBIN:
                if (classDefinitionDialog == null) {
                    throw new IllegalStateException("classDefinitionDialog is NULL - this should be taken care of");
                }
                packetScheduling = new WeightedRoundRobinScheduling(new HashMap<String, Object>() {
                    {
                        put(WeightedRoundRobinScheduling.CLASS_DEFINITIONS, classDefinitionDialog.getClasses());
                    }
                });
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

                queueManagement = new RandomEarlyDetection(new HashMap<String, Object>() {
                    {
                        put(RandomEarlyDetection.EXPONENTIAL_WEIGHT_FACTOR, redQueueManagementDialog.getExponentialWeightFactor());
                        put(RandomEarlyDetection.MAX_PROBABILITY, redQueueManagementDialog.getMaxProbability());
                        put(RandomEarlyDetection.MIN_THRESHOLD, redQueueManagementDialog.getMinThreshlod());
                        put(RandomEarlyDetection.MAX_THRESHOLD, redQueueManagementDialog.getMaxThreshlod());
                    }
                });
                break;
            case WRED:
                if (wredQueueManagementDialog == null) {
                    throw new QosCreationException(NbBundle.getMessage(RouterConfigurationDialog.class, "queue_management_not_configured"));
                }
                Collection<WredDefinition> configuration = wredQueueManagementDialog.getConfiguration();

                final WeightedRED.WredDefinition[] definitions = configuration.toArray(new WredDefinition[configuration.size()]);

                queueManagement = new WeightedRED(new HashMap<String, Object>() {
                    {
                        put(WeightedRED.WRED_DEFINITION, definitions);
                    }
                });
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
                    throw new QosCreationException(NbBundle.getMessage(RouterConfigurationDialog.class, "classfication_not_configured"));
                }
                List<DscpDefinition> result = dscpClassificationDialog.getDscpDefinitions();
                final DscpManager dscpManager = new DscpManager(result.toArray(new DscpDefinition[result.size()]), dscpClassificationDialog.getDefaultQueueNumber());

                classification = new DscpClassification(new HashMap<String, Object>() {
                    {
                        put(DscpClassification.DSCP_DEFINITIONS, dscpManager);
                    }
                });
                break;
            case FLOW_BASED:
                classification = new FlowBasedClassification();
                break;
            case IP_PRECEDENCE:
                classification = new IpPrecedenceClassification();
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
        if (classDefinitionDialog == null) {
            return false;
        }
        if (classDefinitionDialog.getClasses() == null) {
            return false;
        }
        return true;
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
        jLabel12 = new javax.swing.JLabel();
        spinOutputQueue = new javax.swing.JSpinner();
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
        jButton4 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(502, 423));
        setPreferredSize(new java.awt.Dimension(502, 423));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel1.text")); // NOI18N

        txtName.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.txtName.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel6.text")); // NOI18N

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setToolTipText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.txtDescription.toolTipText")); // NOI18N
        txtDescription.setPrompt(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.txtDescription.prompt")); // NOI18N
        jScrollPane3.setViewportView(txtDescription);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
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

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel2.text")); // NOI18N

        spinProcessing.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel3.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel3.text")); // NOI18N

        spinTx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(-1), null, Integer.valueOf(1)));

        jLabel4.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel4.text")); // NOI18N

        spinRx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(-1), null, Integer.valueOf(1)));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel5.text")); // NOI18N

        spinInputQueue.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jPanel4.border.title"))); // NOI18N
        jPanel4.setToolTipText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jPanel4.toolTipText")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel7.text")); // NOI18N

        spinProcessingMin.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        jLabel8.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel8.text")); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
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

        jLabel9.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel9.text")); // NOI18N

        spinTcpTimeout.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        jLabel10.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel10.text")); // NOI18N

        jLabel11.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel11.text")); // NOI18N

        jLabel12.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel12.text")); // NOI18N

        spinOutputQueue.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(spinInputQueue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinRx, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinTx, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinProcessing, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinOutputQueue, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(153, 153, 153)
                        .addComponent(spinTcpTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinOutputQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(spinTcpTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jLabel13.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel13.text")); // NOI18N

        comboQosQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboQosQueueActionPerformed(evt);
            }
        });

        jLabel14.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel14.text")); // NOI18N

        jLabel15.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jLabel15.text")); // NOI18N

        comboQosClassif.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboQosClassifActionPerformed(evt);
            }
        });

        btnConfigQueue.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.btnConfigQueue.text")); // NOI18N
        btnConfigQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigQueueActionPerformed(evt);
            }
        });

        btnConfigScheduling.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.btnConfigScheduling.text")); // NOI18N
        btnConfigScheduling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigSchedulingActionPerformed(evt);
            }
        });

        btnConfigClassif.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.btnConfigClassif.text")); // NOI18N
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
                        .addGap(0, 63, Short.MAX_VALUE)
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
                        .addContainerGap())))
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

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/help.png"))); // NOI18N
        jButton3.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton3.toolTipText")); // NOI18N
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/load_configuration.png"))); // NOI18N
        jButton4.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton4.text")); // NOI18N
        jButton4.setToolTipText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton4.toolTipText")); // NOI18N
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setFocusPainted(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton7.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton7.text")); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.jButton8.text")); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setText(org.openide.util.NbBundle.getMessage(RouterConfigurationDialog.class, "RouterConfigurationDialog.lblError.text")); // NOI18N

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))))
            .addGroup(layout.createSequentialGroup()
                .addGap(131, 131, 131)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblError)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Help help = Lookup.getDefault().lookup(Help.class);
        help.showHelp(new HelpCtx("ahoj"));//todo router configuration - help
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO load configuration
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        //OK button

        if (!validateInput()) {
            return;
        }
        Double maxProcessingDelay = (Double) spinProcessingMax.getValue();
        Double minProcessingDelay = (Double) spinProcessingMin.getValue();
        Double tcptimeout = (Double) spinTcpTimeout.getValue();
        Integer processingPackets = (Integer) spinProcessing.getValue();
        Integer inputQueue = (Integer) spinInputQueue.getValue();
        Integer outputQueue = (Integer) spinOutputQueue.getValue();
        Integer rxSize = (Integer) spinRx.getValue();
        Integer txSize = (Integer) spinTx.getValue();
        try {
            QosMechanismDefinition qosMechanismDefinition = createQosMechanismDefinition();
            RouterConfigurationDialog.ResultObject resultObject = new RouterConfigurationDialog.ResultObject(txtName.getText(), txtDescription.getText(), qosMechanismDefinition, txSize, rxSize, inputQueue, outputQueue, processingPackets, tcptimeout, minProcessingDelay, maxProcessingDelay);
            setUserInput(resultObject);
            closeDialog();
        } catch (QosCreationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //cancel button
        cancelDialog();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void comboQosQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboQosQueueActionPerformed
        if (creatingComboboxes) {
            return;
        }

        btnConfigQueue.setEnabled(((ActiveQueueManagement.Available) ((ComboItem) comboQosQueue.getSelectedItem()).getValue()).hasParameters());
    }//GEN-LAST:event_comboQosQueueActionPerformed

    private void btnConfigQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigQueueActionPerformed
        showConfigQueueManagement();
    }//GEN-LAST:event_btnConfigQueueActionPerformed

    private void comboQosClassifActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboQosClassifActionPerformed
        if (creatingComboboxes) {
            return;
        }

        PacketClassification.Available classEnum = ((PacketClassification.Available) ((ComboItem) comboQosClassif.getSelectedItem()).getValue());

        if (selectedPacketClassification == classEnum) {//user has selected the same item again
            return;
        }
        if (classDefinitionDialog != null) {
            //erase QoS class configuration
            classDefinitionDialog.dispose();
            classDefinitionDialog = null;
        }

        //flow based classification and class based packet scheduling dont work together
        btnConfigClassif.setEnabled(classEnum.hasParameters());
        if (PacketClassification.Available.FLOW_BASED == classEnum) {//it is a flow based classification
            comboQosScheduling.setItemEnabled(3, false);//WEIGHTED_ROUND_ROBIN
            comboQosScheduling.setItemEnabled(5, false);//CB_WFQ
        } else {
            comboQosScheduling.setItemEnabled(3, true);
            comboQosScheduling.setItemEnabled(5, true);
        }

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
        btnConfigScheduling.setEnabled(schedEnum.hasParameters());

        if ((PacketScheduling.Available.WEIGHTED_ROUND_ROBIN == schedEnum) || (PacketScheduling.Available.CB_WFQ == schedEnum)) {
            if (!areClassesDefined()) {//user selected one of class based scheduling mechanisms, but no classes are defined
                JOptionPane.showMessageDialog(this,
                        "To use class-based packet scheduling, you have to define classes first",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);

            }
        }

    }//GEN-LAST:event_comboQosSchedulingActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConfigClassif;
    private javax.swing.JButton btnConfigQueue;
    private javax.swing.JButton btnConfigScheduling;
    private javax.swing.JComboBox comboQosClassif;
    private javax.swing.JComboBox comboQosQueue;
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DisabledItemsComboBox comboQosScheduling;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
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
    private javax.swing.JSpinner spinOutputQueue;
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

        //todo validate input - router configuration dialog
        if (StringUtils.isEmpty(txtName.getText())) {//name is required
            showError("Name is required.");
            return false;
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

    /**
     * object that stores users input
     */
    @Getter
    public static class ResultObject {

        private String name;
        private String description;
        private QosMechanismDefinition qosMechanismDefinition;
        private int maxTxBufferSize;
        private int maxIntputQueueSize;
        private int maxOutputQueueSize;
        private int maxRxBufferSize;
        private int maxProcessingPackets;
        private double tcpDelay;
        private double minProcessingDelay;
        private double maxProcessingDelay;

        public ResultObject(String name, String description, QosMechanismDefinition qosMechanismDefinition, int maxTxBufferSize, int maxRxBufferSize, int maxIntputQueueSize, int maxOutputQueueSize, int maxProcessingPackets, double tcpDelay, double minProcessingDelay, double maxProcessingDelay) {
            this.name = name;
            this.description = description;
            this.qosMechanismDefinition = qosMechanismDefinition;
            this.maxTxBufferSize = maxTxBufferSize;
            this.maxIntputQueueSize = maxIntputQueueSize;
            this.maxOutputQueueSize = maxOutputQueueSize;
            this.maxRxBufferSize = maxRxBufferSize;
            this.maxProcessingPackets = maxProcessingPackets;
            this.tcpDelay = tcpDelay;
            this.minProcessingDelay = minProcessingDelay;
            this.maxProcessingDelay = maxProcessingDelay;
        }
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
