/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 *
 * @author Igor Kvasnicka
 */
@NbBundle.Messages({
    "delete=Delete"
})
public class RoutingPanel extends PanelInterface {

    private JComboBox tableComboBox;
    private DefaultTableModel model;
    private Topology activeTopology;
    private SimulationRuleIterator iterator;

    /**
     * Creates new form RoutingPanel
     */
    public RoutingPanel() {
        activeTopology = NetbeansWindowHelper.getInstance().getActiveTopology();
        activeTopology.setMode(Topology.TopologyModeEnum.ROUTING);
        tableComboBox = new JComboBox(new DefaultComboBoxModel());
        tableComboBox.setEditable(true);
        tableComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<TopologyVertex> vertices = getFixedVertices();
                showRoute(vertices.toArray(new TopologyVertex[vertices.size()]));
            }
        });

        initComponents();
        this.model = (DefaultTableModel) jTable1.getModel();
        jLabel1.setVisible(false);


        Action increase = new AbstractAction("+") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf(e.getActionCommand());
                ((DefaultTableModel) table.getModel()).removeRow(modelRow);
            }
        };
        ButtonColumn inc = new ButtonColumn(jTable1, increase, 1);

    }

    /**
     * retrieves fixed vertices from jTable. all empty rows will be omitted
     *
     * @return
     */
    private List<TopologyVertex> getFixedVertices() {
        List<TopologyVertex> list = new LinkedList<TopologyVertex>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ("".equals(model.getValueAt(i, 0))) {//this is an empty row - omit it
                continue;
            }
            if (!(model.getValueAt(i, 0) instanceof TopologyVertex)) {//just a cast check to be sure
                continue;
            }
            list.add((TopologyVertex) model.getValueAt(i, 0));
        }
        return list;
    }

    private void showRoute(TopologyVertex[] vertices) {
        try {
            jLabel1.setVisible(false);
            activeTopology.highlightEdgesFromTo(iterator.getStoredData().getSourceVertex(), iterator.getStoredData().getDestinationVertex(), vertices);
        } catch (RoutingException ex) {
            jLabel1.setText(ex.getMessage());
            jLabel1.setVisible(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Node", "Delete"
            }
        ));
        jTable1.setRowHeight(24);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(RoutingPanel.class, "RoutingPanel.jTable1.columnModel.title0")); // NOI18N
        jTable1.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(tableComboBox));
        jTable1.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(RoutingPanel.class, "RoutingPanel.jTable1.columnModel.title1")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/add.png"))); // NOI18N
        jButton1.setText(org.openide.util.NbBundle.getMessage(RoutingPanel.class, "RoutingPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(RoutingPanel.class, "RoutingPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(RoutingPanel.class, "RoutingPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jLabel1)))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        addRow(null);
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private void addRow(TopologyVertex v) {
        if (v == null) {
            model.addRow(new Object[]{"", NbBundle.getMessage(RoutingPanel.class, "delete")});
        } else {
            model.addRow(new Object[]{v, NbBundle.getMessage(RoutingPanel.class, "delete")});
        }
    }

    @Override
    public boolean init(SimulationRuleIterator iterator) {
        this.iterator = iterator;
        activeTopology.setMode(Topology.TopologyModeEnum.ROUTING);
        initTableComboBox();
        //creates default routing
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        try {
            jLabel1.setVisible(false);
            activeTopology.highlightEdgesFromTo(iterator.getStoredData().getSourceVertex(), iterator.getStoredData().getDestinationVertex());
        } catch (RoutingException ex) {
            jLabel1.setText(ex.getMessage());
            jLabel1.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean validateData() {
        List<TopologyVertex> fixedVertices = getFixedVertices();

        //check for cycles
        try {

            activeTopology.highlightEdgesFromTo(iterator.getStoredData().getSourceVertex(), iterator.getStoredData().getDestinationVertex(), fixedVertices.toArray(new TopologyVertex[fixedVertices.size()]));
        } catch (RoutingException ex) {
            jLabel1.setText(ex.getMessage());
            jLabel1.setVisible(true);
            return false;
        }
        
        //store data
        fixedVertices.add(0, iterator.getStoredData().getSourceVertex());
        fixedVertices.add(iterator.getStoredData().getDestinationVertex());
        iterator.getStoredData().setRoute(fixedVertices);

        return true;
    }

    private void initTableComboBox() {
        ((DefaultComboBoxModel) tableComboBox.getModel()).removeAllElements();
        Topology topology = NetbeansWindowHelper.getInstance().getActiveTopology();
        List<TopologyVertex> allVertices = topology.getVertexFactory().getAllVertices();
        tableComboBox.addItem("");
        for (TopologyVertex v : allVertices) {
            tableComboBox.addItem(v);
        }
    }

    private void moveRowUp(int row) {
        if (row == 0) {
            return;
        }
        model.moveRow(row, row, row - 1);
    }

    private void moveRowDown(int row) {

        if (row == model.getRowCount()) {
            return;
        }
        model.moveRow(row, row, row + 1);
    }

    /**
     * The ButtonColumn class provides a renderer and an editor that looks like
     * a JButton. The renderer and editor will then be used for a specified
     * column in the table. The TableModel will contain the String to be
     * displayed on the button.
     *
     * The button can be invoked by a mouse click or by pressing the space bar
     * when the cell has focus. Optionaly a mnemonic can be set to invoke the
     * button. When the button is invoked the provided Action is invoked. The
     * source of the Action will be the table. The action command will contain
     * the model row number of the button that was clicked.
     *
     */
    public class ButtonColumn extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

        private JTable table;
        private Action action;
        private int mnemonic;
        private Border originalBorder;
        private Border focusBorder;
        private JButton renderButton;
        private JButton editButton;
        private Object editorValue;
        private boolean isButtonColumnEditor;

        /**
         * Create the ButtonColumn to be used as a renderer and editor. The
         * renderer and editor will automatically be installed on the
         * TableColumn of the specified column.
         *
         * @param table the table containing the button renderer/editor
         * @param action the Action to be invoked when the button is invoked
         * @param column the column to which the button renderer/editor is added
         */
        public ButtonColumn(JTable table, Action action, int column) {
            this.table = table;
            this.action = action;

            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);
            originalBorder = editButton.getBorder();

            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
            table.addMouseListener(this);
        }

        /**
         * Get foreground color of the button when the cell has focus
         *
         * @return the foreground color
         */
        public Border getFocusBorder() {
            return focusBorder;
        }

        /**
         * The foreground color of the button when the cell has focus
         *
         * @param focusBorder the foreground color
         */
        public void setFocusBorder(Border focusBorder) {
            this.focusBorder = focusBorder;
            editButton.setBorder(focusBorder);
        }

        public int getMnemonic() {
            return mnemonic;
        }

        /**
         * The mnemonic to activate the button when the cell has focus
         *
         * @param mnemonic the mnemonic
         */
        public void setMnemonic(int mnemonic) {
            this.mnemonic = mnemonic;
            renderButton.setMnemonic(mnemonic);
            editButton.setMnemonic(mnemonic);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            if (value == null) {
                editButton.setText("");
                editButton.setIcon(null);
            } else if (value instanceof Icon) {
                editButton.setText("");
                editButton.setIcon((Icon) value);
            } else {
                editButton.setText(value.toString());
                editButton.setIcon(null);
            }

            this.editorValue = value;
            return editButton;
        }

        @Override
        public Object getCellEditorValue() {
            return editorValue;
        }

//
//  Implement TableCellRenderer interface
//
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                renderButton.setForeground(table.getSelectionForeground());
                renderButton.setBackground(table.getSelectionBackground());
            } else {
                renderButton.setForeground(table.getForeground());
                renderButton.setBackground(UIManager.getColor("Button.background"));
            }

            if (hasFocus) {
                renderButton.setBorder(focusBorder);
            } else {
                renderButton.setBorder(originalBorder);
            }

//		renderButton.setText( (value == null) ? "" : value.toString() );
            if (value == null) {
                renderButton.setText("");
                renderButton.setIcon(null);
            } else if (value instanceof Icon) {
                renderButton.setText("");
                renderButton.setIcon((Icon) value);
            } else {
                renderButton.setText(value.toString());
                renderButton.setIcon(null);
            }

            return renderButton;
        }

//
//  Implement ActionListener interface
//
	/*
         * The button has been pressed. Stop editing and invoke the custom
         * Action
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.convertRowIndexToModel(table.getEditingRow());
            fireEditingStopped();

            //  Invoke the Action

            ActionEvent event = new ActionEvent(
                    table,
                    ActionEvent.ACTION_PERFORMED,
                    "" + row);
            action.actionPerformed(event);
        }

//
//  Implement MouseListener interface
//
	/*
         * When the mouse is pressed the editor is invoked. If you then then
         * drag the mouse to another cell before releasing it, the editor is
         * still active. Make sure editing is stopped when the mouse is
         * released.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (table.isEditing()
                    && table.getCellEditor() == this) {
                isButtonColumnEditor = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isButtonColumnEditor
                    && table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }

            isButtonColumnEditor = false;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}