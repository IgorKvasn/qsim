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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.SimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;

/**
 *
 * @author Igor Kvasnicka
 */
public class ContainerPanel extends javax.swing.JPanel {

    private static final Logger logg = Logger.getLogger(ContainerPanel.class);
    private SimulationRuleIterator panelIterator;
    private JPanel actualPanel;

    /**
     * Creates new form ContainerPanel
     */
    public ContainerPanel(SimulationRuleIterator panelIterator) {
        initComponents();
        this.panelIterator = panelIterator;
        jButton3.setEnabled(false);
        jButton4.setEnabled(!panelIterator.isPanelLast());
    }

    public void reset() {
        jButton4.setText(NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton4.text"));
    }

    public void setPanel(JPanel panel) {
        if (actualPanel != null) {
            jPanel1.remove(actualPanel);
        }
        actualPanel = panel;
        jPanel1.add(actualPanel, BorderLayout.CENTER);
        jPanel1.validate();
        validate();
    }

    private void cancelAddSimulRule() {
        TopologyVisualisation topolVisual = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation();
        topolVisual.retrieveVertexByClickCancel();

        panelIterator.cancelIterator();

        AddSimulationTopComponent componentAdd = (AddSimulationTopComponent) WindowManager.getDefault().findTopComponent("AddSimulationTopComponent");
        if (componentAdd == null) {
            logg.error("Could not find component AddSimulationTopComponent");
            return;
        }
        componentAdd.close();
    }

    private void reloadSimulationRules() {
        SimulationTopComponent myTC = (SimulationTopComponent) WindowManager.getDefault().findTopComponent("SimulationTopComponent");
        myTC.loadSimulationRules();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/arrow_previous.png"))); // NOI18N
        jButton3.setText(org.openide.util.NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/arrow_next.png"))); // NOI18N
        jButton4.setText(org.openide.util.NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jButton1.setText(org.openide.util.NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 511, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4)
                        .addGap(14, 14, 14)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                        .addGap(56, 56, 56))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(jButton1))
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        logg.debug("next panel");
        if (panelIterator.isPanelLast()) {//finishing wizard            
            if (panelIterator.isCurrentPanelValid()) { //check for last panel validity

                if (NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation() == null) {
                    throw new IllegalStateException("active topology visualisation is NULL");
                }

                // add new rule to storage of all simulation rules
                NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getSimulationData().addSimulationData(panelIterator.getStoredData());

                //reloads all simulation rules displayed
                reloadSimulationRules();

                //delete all used data
                panelIterator.cancelIterator();

                //close this top component
                AddSimulationTopComponent componentAdd = (AddSimulationTopComponent) WindowManager.getDefault().findTopComponent("AddSimulationTopComponent");
                if (componentAdd == null) {
                    logg.error("Could not find component AddSimulationTopComponent");
                    return;
                }
                componentAdd.close();

                jButton4.setText(NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton4.text"));

            } else {
                try {
                    ((PacketSendingPanel) panelIterator.getCurrentPanel()).showErrorLabel(NbBundle.getMessage(ContainerPanel.class, "packetSendingPanel_not_valid"));
                } catch (ClassCastException e) {
                    throw new IllegalStateException("last panel is not PacketSendingPanel - this should not happen");
                }
            }
        }

        panelIterator.nextPanel();
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        jButton4.setText(NbBundle.getMessage(ContainerPanel.class, "ContainerPanel.jButton4.text"));
        if (panelIterator.isPanelLast()) {
            jButton4.setText(NbBundle.getMessage(ContainerPanel.class, "finish"));
        }
        if (panelIterator.isPanelFirst() || panelIterator.isPreviousDisabled()) {
            jButton3.setEnabled(false);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        panelIterator.previousPanel();
        jButton3.setEnabled(true);
        jButton4.setEnabled(true);
        if (panelIterator.isPanelLast()) {
            jButton4.setEnabled(false);
        }
        if (panelIterator.isPanelFirst()) {
            jButton3.setEnabled(false);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cancelAddSimulRule();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
