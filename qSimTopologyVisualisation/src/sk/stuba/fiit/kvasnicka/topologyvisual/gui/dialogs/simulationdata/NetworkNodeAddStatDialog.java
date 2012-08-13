/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.simulationdata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.Getter;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXSearchField;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui.TopologyVisualisation;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.NetworkNodeStatisticsTopComponent.NetworkNodePropertyEnum;

/**
 *
 * @author Igor Kvasnicka
 */
public class NetworkNodeAddStatDialog extends javax.swing.JDialog {

    private RowFilter<ListModel, Object> listFilter;
    private NetworkNodeStatisticsTopComponent statisticsTopComponent;
    private CheckListManager<TopologyVertex> checkListManager;
    private TopologyVisualisation topologyVisualisation;
    private DefaultListModel listModel = new DefaultListModel();

    /**
     * Creates new form NetworkNodeAddStatDialog
     */
    public NetworkNodeAddStatDialog(NetworkNodeStatisticsTopComponent statisticsTopComponent, TopologyVisualisation topologyVisualisation) {
        super(WindowManager.getDefault().getMainWindow(), true);
        initComponents();

        jXSearchField1.setSearchMode(JXSearchField.SearchMode.INSTANT);
        jXSearchField1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterList(e.getActionCommand());
            }
        });
        this.statisticsTopComponent = statisticsTopComponent;
        checkListManager = new CheckListManager<TopologyVertex>(jXList1);
        this.topologyVisualisation = topologyVisualisation;

        jXList1.setModel(listModel);

    }

    private void filterList(String text) {
        listFilter = RowFilter.regexFilter(text);
        jXList1.setRowFilter(listFilter);
    }

    private void resetData() {
        jXSearchField1.setText("");
        jXList1.setRowFilter(null);
        lblSelectedCount.setText("0/" + topologyVisualisation.getTopology().getVertexFactory().getAllVertices().size());

        listModel.clear();
        for (TopologyVertex v : topologyVisualisation.getTopology().getVertexFactory().getAllVertices()) {
            listModel.addElement(new ListItem(v.getName(), v));
        }

        checkListManager.clearSelection();
    }

    private Set<NetworkNodePropertyEnum> getSelectedProperties() {
        Set<NetworkNodePropertyEnum> result = new TreeSet<NetworkNodePropertyEnum>();
        if (chckInput.isSelected()) {
            result.add(NetworkNodePropertyEnum.INPUT_BUFFER);
        }
        if (chckOutput.isSelected()) {
            result.add(NetworkNodePropertyEnum.OUTPUT_BUFFER);
        }
        if (chckProcessing.isSelected()) {
            result.add(NetworkNodePropertyEnum.PROCESSING);
        }
        if (chckRX.isSelected()) {
            result.add(NetworkNodePropertyEnum.RX);
        }
        if (chckTX.isSelected()) {
            result.add(NetworkNodePropertyEnum.TX);
        }
        return result;
    }

    private List<TopologyVertex> getSelectedNetworkNodes() {
        return checkListManager.getSelectedList();
    }

    private void updateNumberOfSelectedItems() {
        lblSelectedCount.setText(checkListManager.getSelectedList().size() + "/" + topologyVisualisation.getTopology().getVertexFactory().getAllVertices().size());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chckRX = new javax.swing.JCheckBox();
        chckTX = new javax.swing.JCheckBox();
        chckInput = new javax.swing.JCheckBox();
        chckOutput = new javax.swing.JCheckBox();
        chckProcessing = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jXSearchField1 = new org.jdesktop.swingx.JXSearchField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXList1 = new org.jdesktop.swingx.JXList();
        jLabel1 = new javax.swing.JLabel();
        lblSelectedCount = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckRX, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.chckRX.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckTX, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.chckTX.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckInput, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.chckInput.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckOutput, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.chckOutput.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chckProcessing, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.chckProcessing.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chckProcessing)
                    .addComponent(chckOutput)
                    .addComponent(chckInput)
                    .addComponent(chckTX)
                    .addComponent(chckRX))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chckRX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckTX)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckInput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chckProcessing)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jPanel2.border.title"))); // NOI18N

        jXSearchField1.setText(org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jXSearchField1.text")); // NOI18N
        jXSearchField1.setToolTipText(org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jXSearchField1.toolTipText")); // NOI18N
        jXSearchField1.setLayoutStyle(org.jdesktop.swingx.JXSearchField.LayoutStyle.VISTA);

        jXList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jXList1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblSelectedCount, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.lblSelectedCount.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblSelectedCount)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jXSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblSelectedCount))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(NetworkNodeAddStatDialog.class, "NetworkNodeAddStatDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(138, 138, 138)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        statisticsTopComponent.addNetworkNodes(getSelectedNetworkNodes(), getSelectedProperties());
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chckInput;
    private javax.swing.JCheckBox chckOutput;
    private javax.swing.JCheckBox chckProcessing;
    private javax.swing.JCheckBox chckRX;
    private javax.swing.JCheckBox chckTX;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXList jXList1;
    private org.jdesktop.swingx.JXSearchField jXSearchField1;
    private javax.swing.JLabel lblSelectedCount;
    // End of variables declaration//GEN-END:variables

    public void showDialog() {
        resetData();
        setVisible(true);
    }

    private class CheckListCellRenderer extends JPanel implements ListCellRenderer {

        private ListCellRenderer delegate;
        private ListSelectionModel selectionModel;
        private JCheckBox checkBox = new JCheckBox();

        private CheckListCellRenderer(ListCellRenderer renderer, ListSelectionModel selectionModel) {
            this.delegate = renderer;
            this.selectionModel = selectionModel;
            setLayout(new BorderLayout());
            setOpaque(false);
            checkBox.setOpaque(false);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component renderer = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            checkBox.setSelected(selectionModel.isSelectedIndex(index));
            removeAll();
            add(checkBox, BorderLayout.WEST);
            add(renderer, BorderLayout.CENTER);
            return this;
        }
    }

    private class CheckListManager<T> extends MouseAdapter implements ListSelectionListener, ActionListener {

        private ListSelectionModel selectionModel = new DefaultListSelectionModel();
        private JXList list;
        private int hotspot = new JCheckBox().getPreferredSize().width;
        @Getter
        private List<T> selectedList = new LinkedList<T>();

        private CheckListManager(JXList list) {
            this.list = list;
            list.setCellRenderer(new CheckListCellRenderer(list.getCellRenderer(), selectionModel));
            list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
            list.addMouseListener(this);
            selectionModel.addListSelectionListener(this);
        }

        public ListSelectionModel getSelectionModel() {
            return selectionModel;
        }

        private void toggleSelection(int index, int viewIndex) {
            if (index < 0) {
                return;
            }

            T item = (T) list.getElementAt(viewIndex);

            if (selectionModel.isSelectedIndex(index)) {
                selectionModel.removeSelectionInterval(index, index);
                selectedList.add(item);
            } else {
                selectionModel.addSelectionInterval(index, index);
                selectedList.remove(item);
            }
            updateNumberOfSelectedItems();
        }

        /*------------------------------[ MouseListener ]-------------------------------------*/
        @Override
        public void mouseClicked(MouseEvent me) {
            int viewIndex = list.locationToIndex(me.getPoint());
            int index = list.convertIndexToModel(viewIndex);

            if (index < 0) {
                return;
            }
            if (me.getX() > list.getCellBounds(index, index).x + hotspot) {
                return;
            }
            toggleSelection(index, viewIndex);
        }

        /*-----------------------------[ ListSelectionListener ]---------------------------------*/
        @Override
        public void valueChanged(ListSelectionEvent e) {
            list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
        }

        /*-----------------------------[ ActionListener ]------------------------------*/
        @Override
        public void actionPerformed(ActionEvent e) {
            int viewIndex = list.getSelectedIndex();
            int index = list.convertIndexToModel(viewIndex);
            toggleSelection(index, viewIndex);
        }

        private void clearSelection() {
            selectedList.clear();
            selectionModel.clearSelection();
        }
    }

    @Getter
    private class ListItem {

        private String label;
        private Object value;

        public ListItem(String label, Object value) {
            this.label = label;
            this.value = value;
        }
    }
}
