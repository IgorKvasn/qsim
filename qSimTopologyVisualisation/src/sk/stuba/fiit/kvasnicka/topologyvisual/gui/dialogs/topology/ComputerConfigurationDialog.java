/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.components.queues.OutputQueue;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.BlockingDialog;

/**
 *
 * @author Igor Kvasnicka
 */
public class ComputerConfigurationDialog extends BlockingDialog<Computer> {

    private static Logger logg = Logger.getLogger(ComputerConfigurationDialog.class);
    private OutputQueuesConfigDialog outputQueuesConfigDialog;
    private String name = null; //workaround for a SwingX bug, when JXTextField does not return getText() value correctly, when set programmatically
    private String originalName = null;//used when editing

    /**
     * Creates new form ComputerConfigurationDialog
     */
    public ComputerConfigurationDialog(String computerName) {
        super(WindowManager.getDefault().getMainWindow());
        initComponents();
        outputQueuesConfigDialog = new OutputQueuesConfigDialog(WindowManager.getDefault().getMainWindow());
        spinTcpTimeout.setValue(RouterConfigurationDialog.DEFAULT_TCP_TIMEOUT);

        txtName.setText(computerName);
        lblError.setVisible(false);
    }

    public ComputerConfigurationDialog(Computer computer, String originalName, boolean copied) {
        this(computer.getName());

        if (copied) {
            setTitle(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.title"));
            txtName.setText("");
            this.originalName = null;
        } else {
            this.originalName = originalName;
        }

        spinProcessingMax.setValue(computer.getMaxProcessingDelay());
        spinProcessingMin.setValue(computer.getMinProcessingDelay());
        spinTcpTimeout.setValue(computer.getTcpDelay());
        spinProcessing.setValue(computer.getMaxProcessingPackets());
        spinInputQueue.setValue(computer.getMaxIntputQueueSize());
        outputQueuesConfigDialog.setOutputQueues(computer.getOutputQueueManager().getQueues());
        spinTx.setValue(computer.getMaxTxBufferSize());
        spinRx.setValue(computer.getMaxRxBufferSize());

        txtName.setText(computer.getName());
        txtDescription.setText(computer.getDescription());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnOk = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
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
        jButton3 = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOk, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.btnOk.text")); // NOI18N
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton8, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jButton8.text")); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel1.text")); // NOI18N

        txtName.setText(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.txtName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel6.text")); // NOI18N

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setToolTipText(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.txtDescription.toolTipText")); // NOI18N
        txtDescription.setPrompt(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.txtDescription.prompt")); // NOI18N
        jScrollPane3.setViewportView(txtDescription);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel2.text")); // NOI18N

        spinProcessing.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel3.text")); // NOI18N

        spinTx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(-1), null, Integer.valueOf(1)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel4.text")); // NOI18N

        spinRx.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(-1), null, Integer.valueOf(1)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel5.text")); // NOI18N

        spinInputQueue.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jPanel4.border.title"))); // NOI18N
        jPanel4.setToolTipText(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jPanel4.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel7.text")); // NOI18N

        spinProcessingMin.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel8.text")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel9.text")); // NOI18N

        spinTcpTimeout.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jLabel12.text")); // NOI18N

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/help.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jButton3.text")); // NOI18N
        jButton3.setToolTipText(org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.jButton3.toolTipText")); // NOI18N
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(lblError, org.openide.util.NbBundle.getMessage(ComputerConfigurationDialog.class, "ComputerConfigurationDialog.lblError.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(128, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(150, 150, 150))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jTabbedPane1)
                    .addContainerGap()))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addGap(41, 41, 41)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 330, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(jButton8))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(45, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(393, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed

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

        Computer resultObject = new Computer(name, txtDescription.getText(), txSize, rxSize, outputQueueList, inputQueue, processingPackets, tcptimeout, minProcessingDelay, maxProcessingDelay);

        setUserInput(resultObject);
        closeDialog();
    }//GEN-LAST:event_btnOkActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        //cancel button
        cancelDialog();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        outputQueuesConfigDialog.showDialog();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Help help = Lookup.getDefault().lookup(Help.class);
        help.showHelp(new HelpCtx("sk.stuba.fiit.kvasnicka.topologyvisual.computer"));
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOk;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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

    @Override
    protected void showDialogHook() {
        /**
         * if simulation is running, no edits are allowed - so user cannot hit
         * OK button
         */
        btnOk.setEnabled(!NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().isSimulationRunning());
    }
}
