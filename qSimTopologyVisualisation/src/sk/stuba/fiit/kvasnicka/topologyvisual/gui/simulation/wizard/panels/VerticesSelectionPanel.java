/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels;

import java.util.List;
import javax.swing.JComboBox;
import org.apache.log4j.Logger;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.SimulationRuleException;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.JComboBoxAutoCompletator;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * In this panel user defines source and destination vertices
 *
 * @author Igor Kvasnicka
 */
public class VerticesSelectionPanel extends PanelInterface {

    private static final Logger logg = Logger.getLogger(VerticesSelectionPanel.class);
    //flag that inicates if this panel has been already initialised
    private boolean inited = false;
    //this is a combobox that user is selecting by clickin on topology
    private JComboBox pickingNameComboBox;
    private SimulationRuleIterator iterator;

    /**
     * Creates new form VerticesSelectionPanel
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public VerticesSelectionPanel() {
        initComponents();
        new JComboBoxAutoCompletator(combDestination);
        new JComboBoxAutoCompletator(combSource);
    }

    /**
     * initializes panel; do not forget to call this method
     */
    @Override
    public boolean init(SimulationRuleIterator iterator) {
        this.iterator = iterator;
        lblError.setVisible(false);
        if (inited) {
            return true;
        }
        inited = true;
        return fillComboBoxes();
    }

    private boolean fillComboBoxes() {
        Topology topology = NetbeansWindowHelper.getInstance().getActiveTopology();
        if (topology == null) {
            logg.error("topology is null - no selected TopologyMultiviewElement window");
            return false;
        }
        if (topology.getVertexFactory() == null) {
            logg.error("VertexFactory is null");
            return false;
        }

        List<TopologyVertex> vertices = topology.getVertexFactory().getAllVertices();
        if (vertices == null) {
            logg.error("list of all vertices is null");
            return false;
        }
        for (TopologyVertex v : vertices) {
            combDestination.addItem(v);
            combSource.addItem(v);
        }
        combDestination.setSelectedItem("");
        combSource.setSelectedItem("");
        return true;
    }

    @Override
    public boolean validateData() {
        lblError.setVisible(false);
        try {
            validateUserInput();
        } catch (SimulationRuleException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            return false;
        }
           //data is valid - now it is a good time to store these data
        iterator.getStoredData().setDestinationVertex((TopologyVertex) combDestination.getSelectedItem());
        iterator.getStoredData().setSourceVertex((TopologyVertex) combSource.getSelectedItem());
        return true;
    }

    /**
     * validates if all combo boxes are correctly filled - they must not be
     * empty and not identical
     *
     * @throws SimulationRuleException
     */
    private void validateUserInput() throws SimulationRuleException {
        if (combDestination.getSelectedItem() == null || combSource.getSelectedItem() == null) {
            throw new SimulationRuleException(NbBundle.getMessage(VerticesSelectionPanel.class, "vertex_empty_error"));
        }
        if ("".equals(combDestination.getSelectedItem())) {
            throw new SimulationRuleException(NbBundle.getMessage(VerticesSelectionPanel.class, "vertex_empty_error"));
        }
        if ("".equals(combSource.getSelectedItem())) {
            throw new SimulationRuleException(NbBundle.getMessage(VerticesSelectionPanel.class, "vertex_empty_error"));
        }

        TopologyVertex vDest = null;
        TopologyVertex vSource = null;
        try {
            vDest = (TopologyVertex) combDestination.getSelectedItem();
            vSource = (TopologyVertex) combSource.getSelectedItem();
        } catch (ClassCastException e) {
            //this means that one of the vertices if filled incorectly - user enters a string into ComboBox but such a vertex does not exist
            throw new SimulationRuleException(NbBundle.getMessage(VerticesSelectionPanel.class, "vertex_not_found_error"));
        }

        if (vDest.getName().equals(vSource.getName())) {//both vertices are the same
            throw new SimulationRuleException(NbBundle.getMessage(VerticesSelectionPanel.class, "vertices_identical_error"));
        }
    }

    /**
     * user wants to pick vertex from topology
     *
     * @param combo where to write user's choice
     */
    private void pickVertexFromTopology(JComboBox combo) {
        pickingNameComboBox = combo;
        TopologyVisualisation topolVisual = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation();
        topolVisual.retrieveVertexByClick(this);
    }
     /**
     * cancels user decision to pick vertex from topology
     *
     * @param combo where to write user's choice
     */
    private void pickVertexFromTopologyCancel() {
        pickingNameComboBox = null;
        TopologyVisualisation topolVisual = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation();
        topolVisual.retrieveVertexByClickCancel();
    }

    /**
     * callback method when user selects vertex on topology
     */
    public void setVertexPicked(TopologyVertex v) {
        logg.debug("vertex selected: " + v.getName());
        pickingNameComboBox.setSelectedItem(v);
        pickingNameComboBox = null;
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
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
        combSource = new javax.swing.JComboBox();
        combDestination = new javax.swing.JComboBox();
        lblError = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jLabel3.text")); // NOI18N

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.lblError.text")); // NOI18N

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/hand.png"))); // NOI18N
        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/hand.png"))); // NOI18N
        jToggleButton2.setText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jToggleButton2.text")); // NOI18N
        jToggleButton2.setToolTipText(org.openide.util.NbBundle.getMessage(VerticesSelectionPanel.class, "VerticesSelectionPanel.jToggleButton2.toolTipText")); // NOI18N
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(combSource, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(combDestination, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton1)
                            .addComponent(jToggleButton2)))
                    .addComponent(jLabel1)
                    .addComponent(lblError))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(combSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(combDestination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(lblError))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToggleButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (jToggleButton1.isSelected()) {
            pickVertexFromTopology(combSource);
        }else{//cancel action
             pickVertexFromTopologyCancel();
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
         if (jToggleButton2.isSelected()) {
            pickVertexFromTopology(combDestination);
        }else{//cancel action
             pickVertexFromTopologyCancel();
        }
    }//GEN-LAST:event_jToggleButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox combDestination;
    private javax.swing.JComboBox combSource;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JLabel lblError;
    // End of variables declaration//GEN-END:variables
}
