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

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.IpPrecedence;
import sk.stuba.fiit.kvasnicka.qsimsimulation.enums.Layer4TypeEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData.Data;

/**
 *
 * @author Igor Kvasnicka
 */
public class PacketSendingPanel extends PanelInterface {

    private SimulationRuleIterator iterator;

    /**
     * Creates new form PacketSendingPanel
     */
    public PacketSendingPanel() {
        initComponents();
        initComboboxLayer4();
        initIpPrecedence();
        errorLabel.setVisible(false);
    }

    private void initIpPrecedence() {
        jComboBox1.removeAllItems();

        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_0, "0 - lowest"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_1, "1"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_2, "2"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_3, "3"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_4, "4"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_5, "5"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_6, "6"));
        jComboBox1.addItem(new ComboItem(IpPrecedence.IP_PRECEDENCE_7, "7 - highest"));
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
        iterator.getStoredData().setLayer4protocol((Layer4TypeEnum) (((ComboItem) comboLayer4.getSelectedItem()).getValue()));

        int count = ((Integer) spinCount.getValue()).intValue();
        if (jCheckBox1.isSelected()) {
            count = -1;
        }
        iterator.getStoredData().setPacketCount(count);
        iterator.getStoredData().setPacketSize((Integer) spinSize.getValue());
        iterator.getStoredData().setActivationDelay(getActivationDelay());
        iterator.getStoredData().setSrcPort((Integer) spinSrcPort.getValue());
        iterator.getStoredData().setDestPort((Integer) spinDestPort.getValue());
        iterator.getStoredData().setIpPrecedence((IpPrecedence) ((ComboItem) jComboBox1.getSelectedItem()).getValue());
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
        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();

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
                .addContainerGap(149, Short.MAX_VALUE))
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

        spinSize.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(500), Integer.valueOf(1), null, Integer.valueOf(1)));

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        errorLabel.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.errorLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel1.text")); // NOI18N

        spinSrcPort.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel6.text")); // NOI18N

        spinDestPort.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel4.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jLabel4.text")); // NOI18N

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(PacketSendingPanel.class, "PacketSendingPanel.jCheckBox1.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(spinDestPort)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(errorLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(87, 87, 87))
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
                            .addComponent(spinSrcPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(spinDestPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(errorLabel))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(44, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comboLayer4;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton radioActiveOnStart;
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
