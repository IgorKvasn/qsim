/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata.StatisticalDataChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata.StatisticalDataEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.rules.SimulRuleStatisticalData;

/**
 * panel that shows simulation statistical data for one simulation rule
 *
 * @author Igor Kvasnicka
 */
public class SimulationDataPanel extends javax.swing.JPanel implements StatisticalDataChangedListener {

    private final SimulRuleStatisticalData statData;
    private final SimulationDataTopComponent simulationDataTopComponent;
    private ColorPreviewIcon colorPreviewIcon;

    /**
     * Creates new form SimulationDataPanel
     */
    public SimulationDataPanel(SimulRuleStatisticalData data, SimulationDataTopComponent simulationDataTopComponent, Color traceColor) {
        initComponents();
        this.statData = data;
        updateData();
        this.simulationDataTopComponent = simulationDataTopComponent;
        colorPreviewIcon = new ColorPreviewIcon(traceColor);
        lblColorPreview.setIcon(colorPreviewIcon);
    }

    private void updateData() {
        lblMax.setText(String.valueOf(statData.getMaxDelay()));
        lblMin.setText(String.valueOf(statData.getMinDelay()));
        lblAverage.setText(String.valueOf(statData.calculateAverageDelay()));
        lblNumber.setText(String.valueOf(statData.getStatisticalDataCount()));
    }

    private void changeTraceColor() {
        Color newColor = JColorChooser.showDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(SimulationDataPanel.class, "color_in_chart"), colorPreviewIcon.color);
        if (newColor == null) {
            return;
        }
        colorPreviewIcon.color = newColor;
        statData.getChartTrace().setColor(newColor);
        lblColorPreview.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        lblMin = new javax.swing.JLabel();
        lblMax = new javax.swing.JLabel();
        lblAverage = new javax.swing.JLabel();
        lblNumber = new javax.swing.JLabel();
        lblColorPreview = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jLabel4.text")); // NOI18N

        jCheckBox1.setSelected(true);
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton1.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblMin.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.lblMin.text")); // NOI18N

        lblMax.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.lblMax.text")); // NOI18N

        lblAverage.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.lblAverage.text")); // NOI18N

        lblNumber.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.lblNumber.text")); // NOI18N

        lblColorPreview.setText(org.openide.util.NbBundle.getMessage(SimulationDataPanel.class, "SimulationDataPanel.lblColorPreview.text")); // NOI18N
        lblColorPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lblColorPreviewMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblNumber))
                    .addComponent(jButton1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(117, 117, 117)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAverage)
                            .addComponent(lblMax)
                            .addComponent(lblMin)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblColorPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblMin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblMax))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblAverage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblNumber))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblColorPreview, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        simulationDataTopComponent.removeSimulationRule(statData.getRule());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        simulationDataTopComponent.showInChart(statData, jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void lblColorPreviewMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblColorPreviewMouseReleased
        changeTraceColor();
    }//GEN-LAST:event_lblColorPreviewMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblAverage;
    private javax.swing.JLabel lblColorPreview;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblNumber;
    // End of variables declaration//GEN-END:variables

    @Override
    public void statisticalDataChangeOccured(StatisticalDataEvent event) {
        updateData();
    }

    class ColorPreviewIcon implements Icon {

        private Color color;

        ColorPreviewIcon(Color initialColor) {
            this.color = initialColor;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(0, 0, getIconWidth(), getIconHeight());
        }
    }
}
