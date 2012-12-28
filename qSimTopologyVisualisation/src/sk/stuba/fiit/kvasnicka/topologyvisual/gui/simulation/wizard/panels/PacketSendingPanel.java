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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels;

import java.util.Random;
import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.qsimsimulation.qos.classification.utils.dscp.DscpValuesEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

/**
 *
 * @author Igor Kvasnicka
 */
public class PacketSendingPanel extends PanelInterface {

    private static final int MAX_PORT_VALUE = 65536;
    private SimulationRuleIterator iterator;
    private static final Random random = new Random();

    /**
     * Creates new form PacketSendingPanel
     */
    public PacketSendingPanel() {
        initComponents();
        initComboboxLayer4();

        initDscpCombo();
        initTosCombo();

        errorLabel.setVisible(false);
    }

    private void initTosCombo() {
        comboTos.removeAllItems();

        for (IpPrecedence ipPrecedenceEnum : IpPrecedence.values()) {
            comboTos.addItem(new ComboItem(ipPrecedenceEnum, String.valueOf(ipPrecedenceEnum.getIntRepresentation())));
        }
    }

    private void initDscpCombo() {
        comboDSCP.removeAllItems();
        for (DscpValuesEnum dscpEnum : DscpValuesEnum.values()) {
            comboDSCP.addItem(new ComboItem(dscpEnum, dscpEnum.getTextName()));
        }
    }

    private void initComboboxLayer4() {
        comboLayer4.removeAllItems();

        comboLayer4.addItem(new ComboItem(Layer4TypeEnum.TCP, "TCP"));
        comboLayer4.addItem(new ComboItem(Layer4TypeEnum.UDP, "UDP"));
        comboLayer4.addItem(new ComboItem(Layer4TypeEnum.ICMP, "ICMP"));
    }

    @Override
    public boolean init(SimulationRuleIterator iterator) {
        this.iterator = iterator;
        hideErrorLabel();
        return true;
    }

    @Override
    public boolean validateData() {
        Layer4TypeEnum layer4prot = (Layer4TypeEnum) (((ComboItem) comboLayer4.getSelectedItem()).getValue());
        iterator.getStoredData().setLayer4protocol(layer4prot);

        int count = ((Integer) spinCount.getValue()).intValue();
        if (jCheckBox1.isSelected()) {
            count = -1;
        }
        iterator.getStoredData().setPacketCount(count);
        iterator.getStoredData().setPacketSize((Integer) spinSize.getValue());
        iterator.getStoredData().setActivationDelay(getActivationDelay());
        if (layer4prot == Layer4TypeEnum.ICMP) {
            iterator.getStoredData().setSrcPort(generateRandomPortNumber());
            iterator.getStoredData().setDestPort(generateRandomPortNumber());
        } else {
            iterator.getStoredData().setSrcPort((Integer) spinSrcPort.getValue());
            iterator.getStoredData().setDestPort((Integer) spinDestPort.getValue());
        }

        if (radioTos.isSelected()) {//user has selected IP ToS
            iterator.getStoredData().setIpPrecedence((IpPrecedence) ((ComboItem) comboTos.getSelectedItem()).getValue());
            iterator.getStoredData().setDscpValuesEnum(null);
        } else {//user has selected DSCP
            iterator.getStoredData().setIpPrecedence(null);
            iterator.getStoredData().setDscpValuesEnum((DscpValuesEnum) ((ComboItem) comboDSCP.getSelectedItem()).getValue());
        }

        return true;
    }

    @Override
    public void initValues(Data data) {

        comboLayer4.setSelectedIndex(getSelectedIndexLayer4(data.getLayer4protocol()));

        if (data.getPacketCount() == -1) {
            spinCount.setValue(0);
            jCheckBox1.setSelected(true);
        } else {
            spinCount.setValue(data.getPacketCount() == 0 ? 1 : data.getPacketCount());
            jCheckBox1.setSelected(false);
        }
        spinSize.setValue(data.getPacketSize() == 0 ? 1 : data.getPacketSize());

        spinSrcPort.setValue(data.getSrcPort() == 0 ? 1 : data.getSrcPort());
        spinDestPort.setValue(data.getDestPort() == 0 ? 1 : data.getDestPort());

        //activation delay
        if (data.getActivationDelay() == 0) {
            radioActiveOnStart.setSelected(true);
            jRadioButton1.setSelected(false);
        } else {
            radioActiveOnStart.setSelected(false);
            jRadioButton1.setSelected(true);
            spinActive.setValue(data.getActivationDelay() == 0 ? 1 : data.getActivationDelay());
        }

        if (data.getLayer4protocol() == Layer4TypeEnum.ICMP) {
            spinSrcPort.setEnabled(false);
            spinDestPort.setEnabled(false);
        } else {
            spinSrcPort.setEnabled(true);
            spinDestPort.setEnabled(true);
        }
        if (data.getDscpValuesEnum() == null) {//user has selected IP ToS
            radioTos.setSelected(true);
            comboDSCP.setSelectedIndex(0);
            comboTos.setSelectedIndex(getSelectedIndexTos(data.getIpPrecedence()));
        } else {//user has selected DSCP            
            radioDscp.setSelected(true);
            comboTos.setSelectedIndex(0);
            comboDSCP.setSelectedIndex(getSelectedIndexDscp(data.getDscpValuesEnum()));
        }
    }

    private int generateRandomPortNumber() {
        return random.nextInt(MAX_PORT_VALUE);
    }

    /**
     * returns index in comboDscp component
     *
     * @param dscp
     * @return
     */
    private int getSelectedIndexDscp(DscpValuesEnum dscp) {
        if (dscp == null) {
            return 0;
        }
        for (int i = 0; i < comboDSCP.getItemCount(); i++) {
            if (((ComboItem) comboDSCP.getItemAt(i)).getValue().equals(dscp)) {
                return i;
            }
        }
        throw new IllegalStateException("unknown value " + dscp);
    }

    /**
     * returns index in comboTos component
     *
     * @param ip
     * @return
     */
    private int getSelectedIndexTos(IpPrecedence ip) {
        if (ip == null) {
            return 0;
        }
        for (int i = 0; i < comboTos.getItemCount(); i++) {
            if (((ComboItem) comboTos.getItemAt(i)).getValue().equals(ip)) {
                return i;
            }
        }
        throw new IllegalStateException("unknown value " + ip);
    }

    /**
     * returns index in comboLayer4 component
     *
     * @param l4
     * @return
     */
    private int getSelectedIndexLayer4(Layer4TypeEnum l4) {
        if (l4 == null) {
            return 0;
        }
        for (int i = 0; i < comboLayer4.getItemCount(); i++) {
            if (((ComboItem) comboLayer4.getItemAt(i)).getValue().equals(l4)) {
                return i;
            }
        }
        throw new IllegalStateException("unknown value " + l4);
    }

    private int getActivationDelay() {
        if (radioActiveOnStart.isSelected()) {
            return 0;
        } else {
            return ((Integer) spinActive.getValue()).intValue();
        }
    }

    private void hideErrorLabel() {
        errorLabel.setVisible(false);
    }

    public void showErrorLabel(String txt) {
        errorLabel.setText(txt);
        errorLabel.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        spinActive = new javax.swing.JSpinner();
        jRadioButton1 = new javax.swing.JRadioButton();
        radioActiveOnStart = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        spinCount = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        comboLayer4 = new javax.swing.JComboBox();
        spinSize = new javax.swing.JSpinner();
        errorLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        spinSrcPort = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        spinDestPort = new javax.swing.JSpinner();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        radioTos = new javax.swing.JRadioButton();
        radioDscp = new javax.swing.JRadioButton();
        comboDSCP = new javax.swing.JComboBox();
        comboTos = new javax.swing.JComboBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jPanel1.border.title"))); // NOI18N

        spinActive.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        spinActive.setEditor(new javax.swing.JSpinner.NumberEditor(spinActive, ""));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButton1, org.jdesktop.beansbinding.ELProperty.create("${selected}"), spinActive, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jRadioButton2.text")); // NOI18N

        buttonGroup1.add(radioActiveOnStart);
        radioActiveOnStart.setSelected(true);
        radioActiveOnStart.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.radioActiveOnStart.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addGap(18, 18, 18)
                        .addComponent(spinActive, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(radioActiveOnStart))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(radioActiveOnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinActive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton1))
                .addContainerGap())
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel2.text")); // NOI18N

        spinCount.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jCheckBox1, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), spinCount, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel3.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel5.text")); // NOI18N

        comboLayer4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboLayer4ActionPerformed(evt);
            }
        });

        spinSize.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(500), Integer.valueOf(1), null, Integer.valueOf(1)));

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        errorLabel.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.errorLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel1.text")); // NOI18N

        spinSrcPort.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel6.text")); // NOI18N

        spinDestPort.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jCheckBox1.text_1")); // NOI18N

        buttonGroup2.add(radioTos);
        radioTos.setSelected(true);
        radioTos.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.radioTos.text_1")); // NOI18N

        buttonGroup2.add(radioDscp);
        radioDscp.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.radioDscp.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, radioDscp, org.jdesktop.beansbinding.ELProperty.create("${selected}"), comboDSCP, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, radioTos, org.jdesktop.beansbinding.ELProperty.create("${selected}"), comboTos, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioTos)
                    .addComponent(radioDscp))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(comboTos, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(comboDSCP, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioTos)
                    .addComponent(comboTos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioDscp)
                    .addComponent(comboDSCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(errorLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(comboLayer4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spinCount)
                            .addComponent(spinSize, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                            .addComponent(spinSrcPort)
                            .addComponent(spinDestPort))
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(spinCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(spinSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(comboLayer4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(spinSrcPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(spinDestPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(errorLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void comboLayer4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboLayer4ActionPerformed
        if ((Layer4TypeEnum) ((ComboItem) comboLayer4.getSelectedItem()).getValue() == Layer4TypeEnum.ICMP) {
            spinSrcPort.setEnabled(false);
            spinDestPort.setEnabled(false);
        } else {
            spinSrcPort.setEnabled(true);
            spinDestPort.setEnabled(true);
        }
    }//GEN-LAST:event_comboLayer4ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JComboBox comboDSCP;
    private javax.swing.JComboBox comboLayer4;
    private javax.swing.JComboBox comboTos;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton radioActiveOnStart;
    private javax.swing.JRadioButton radioDscp;
    private javax.swing.JRadioButton radioTos;
    private javax.swing.JSpinner spinActive;
    private javax.swing.JSpinner spinCount;
    private javax.swing.JSpinner spinDestPort;
    private javax.swing.JSpinner spinSize;
    private javax.swing.JSpinner spinSrcPort;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    public class ComboItem {

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
