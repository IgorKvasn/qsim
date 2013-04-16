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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.table.*;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.topologyvisual.PreferenciesHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane.CloseableTabbedPaneListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * Top component which displays something.
 */
//@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs//SimulationLogTopComponent//EN",
//autostore = false)
@TopComponent.Description(preferredID = "SimulationLogTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs.SimulationLogTopComponent")
//@ActionReference(path = "Menu/Window" /*
// * , position = 333
// */)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationLogTopComponentAction" 
//        , preferredID = "SimulationLogTopComponent"
//)
@Messages({
    "CTL_SimulationLogTopComponentAction=SimulationLogTopComponent",
    "CTL_SimulationLogTopComponent=Logs",
    "HINT_SimulationLogTopComponent=Logs"
})
public final class SimulationLogTopComponent extends TopComponent implements SimulationLogListener, DropDownHiddenListener, CloseableTabbedPaneListener {

    private static Logger logg = Logger.getLogger(SimulationLogTopComponent.class);
    /**
     * mapping between TopologyVertex and SimulationLogPanel<br/> each
     * TopologyVertex has got one SimulationLogPanel<br/> note: TopologyVertex
     * object is represented by its name (String)
     */
    private Map<String, JTable> panels = new HashMap<String, JTable>();
    private Map<FilterMapKey, RowFilter<Object, Object>> tableFilterMap = new HashMap<FilterMapKey, RowFilter<Object, Object>>();
    private Map<String, Integer> tableRowIndex = new HashMap<String, Integer>();
    private Map<String, JScrollPane> tablesMap = new HashMap<String, JScrollPane>();

    public SimulationLogTopComponent() {
        initComponents();
        setName(Bundle.CTL_SimulationLogTopComponent());
        setToolTipText(Bundle.HINT_SimulationLogTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        initCategoryDropDown();

        dropCategory.addDropDownHiddenListener(this);
        closeableTabbedPane1.addCloseableTabbedPaneListener(this);

    }

    public void init(Collection<TopologyVertex> vertices) {
        //init tables for all vertices
        for (TopologyVertex v : vertices) {
            createSimulationLogPanel(v.getName());
        }
    }

    public void showVetices(List<TopologyVertex> vertices) {
        if (panels.isEmpty()) {//simulation was not started yet
            return;
        }
        for (TopologyVertex v : vertices) {
            //add table to tabbed pane
            addSimulationLogPanel(v.getName());
        }
        closeableTabbedPane1.setSelectedComponent(tablesMap.get(vertices.get(vertices.size() - 1).getName()));
    }

    private void initCategoryDropDown() {
        for (LogCategory cat : LogCategory.values()) {
            dropCategory.addCheckBoxMenuItem(cat.toString(), true);
        }
        dropCategory.selectAll(true);
    }

    /**
     * category filter has been changed
     *
     * @param selectedCheckBoxes list of selected checkboxes (categories)
     */
    private void updateLogs(List<String> selectedCheckBoxes) {
        final String regex = createRegex(selectedCheckBoxes);

        for (Map.Entry<String, JTable> entry : panels.entrySet()) {
            JTable table = entry.getValue();

            RowFilter<Object, Object> filter = getFilter(selectedCheckBoxes, regex);

            TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
            sorter.setRowFilter(filter);
            table.setRowSorter(sorter);
        }
    }

    /**
     * creates RowFilter for specified selected check boxes <br>all filters are
     * cached
     *
     * @param selectedCheckBoxes
     * @param regex
     * @return
     */
    private RowFilter<Object, Object> getFilter(List<String> selectedCheckBoxes, final String regex) {
        FilterMapKey key = new FilterMapKey(selectedCheckBoxes);
        if (tableFilterMap.containsKey(key)) {
            return tableFilterMap.get(key);
        }

        RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry entry) {
                String severity = (String) entry.getValue(1);
                return severity.matches(regex);
            }
        };
        tableFilterMap.put(key, filter);
        return filter;
    }

    private String createRegex(List<String> categoryList) {
        if (categoryList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String cat : categoryList) {
            sb.append("(.*").append(cat).append(".*)|");
        }
        //remove the last |
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private void addLog(SimulationLog simulationLog) {
        if (simulationLog == null) {
            throw new IllegalArgumentException("simulationLog is NULL");
        }

        String simulTime;
        if (simulationLog.getSimulationTime() == -1) {
            simulTime = "N/A";
        } else {
            simulTime = simulationLog.getFormattedSimulationTime();
        }

        if (simulationLog.getSourceName().equals(SimulationLogUtils.SOURCE_GENERAL)) {//all panels should be notified
            for (Map.Entry<String, JTable> entry : panels.entrySet()) {
                JTable table = entry.getValue();
                addRow(table, entry.getKey(), (DefaultTableModel) table.getModel(), new Object[]{(tableRowIndex.get(entry.getKey()) + 1), simulationLog.getCategory().toString(), simulationLog.getCause(), simulTime});
            }
            return;
        }

        JTable table = addSimulationLogPanel(simulationLog.getSourceName());
//        if (table == null) {//no one is interrested in this simulation log
//            return;
//        }
        addRow(table, simulationLog.getSourceName(), (DefaultTableModel) table.getModel(), new Object[]{(tableRowIndex.get(simulationLog.getSourceName()) + 1), simulationLog.getCategory().toString(), simulationLog.getCause(), simulTime});
    }

    private void addRow(JTable table, String tableName, DefaultTableModel model, Object[] row) {
        int newRowIndex = (PreferenciesHelper.isAddNewSimulationLogsAtBottom() ? model.getRowCount() : 0);

        tableRowIndex.put(tableName, tableRowIndex.get(tableName) + 1);

        if (PreferenciesHelper.isUnlimitedSimulationLogs()) {
            model.insertRow(newRowIndex, row);
        } else {
            int maxLogCount = PreferenciesHelper.getSimulationLogsCount();
            if (model.getRowCount() >= maxLogCount) {
                //there are too many rows - first I have to delete the oldest row
                removeOldestRow(model);
                if (PreferenciesHelper.isAddNewSimulationLogsAtBottom()) {//when adding at bottom, I have removed one line
                    newRowIndex -= 1;
                }
            }
            model.insertRow(newRowIndex, row);
        }

        scrollToNewRow(table, newRowIndex, 0);
    }

    private static void scrollToNewRow(JTable table, int row, int col) {
        JScrollPane sp = getScrollPane(table);
        if (sp == null) {
            return;
        }
        JViewport viewport = sp.getViewport();
        Rectangle r = table.getCellRect(row, col, true);
        Point p = viewport.getViewPosition();
        r.setLocation(r.x - p.x, r.y - p.y + table.getRowHeight());
        viewport.scrollRectToVisible(r);
    }

    private static JScrollPane getScrollPane(Component c) {
        while ((c = c.getParent()) != null) {
            if (c instanceof JScrollPane) {
                return (JScrollPane) c;
            }
        }
        return null;
    }

    private void removeOldestRow(DefaultTableModel model) {
        int lowestRowValue = Integer.MAX_VALUE;
        int lowestRowIndex = Integer.MAX_VALUE;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (lowestRowValue > (Integer) model.getValueAt(i, 0)) {
                lowestRowIndex = i;
                lowestRowValue = (Integer) model.getValueAt(i, 0);
            }
        }

        if (lowestRowIndex == Integer.MAX_VALUE) {
            return;
        }
        model.removeRow(lowestRowIndex);
    }

    /**
     * deletes all logs for currently visible log panel
     */
    private void clearLogs() {
        JXTable table = getSelectedTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }
    }

    /**
     * returns JXTable that is currently selected by user
     *
     * @return
     */
    private JXTable getSelectedTable() {
        JScrollPane scrollPane = (JScrollPane) closeableTabbedPane1.getSelectedComponent();

        return (JXTable) scrollPane.getViewport().getView();
    }

    private JTable createSimulationLogPanel(String vertex) {

        JXTable table = new JXTable();
        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
            "#", "Category", "Message", "Simulation time"
        }) {
            Class[] types = new Class[]{
                Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });


        //add sorter so table can be filtered
        table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));

        //table highlighter
        initTableRowHighlighter(table);

        table.setFillsViewportHeight(true);

        //add table to hashmap
        panels.put(vertex, table);

        tableRowIndex.put(vertex, 0);

        return table;
    }

    private void initTableRowHighlighter(JXTable table) {
        TableColumnModel colModel = table.getColumnModel();
        for (Enumeration<TableColumn> colEnum = colModel.getColumns(); colEnum.hasMoreElements();) {
            TableColumn c = colEnum.nextElement();
            c.setCellRenderer(new RowHighlighterTableCellRender(JLabel.CENTER));
        }
    }

    /**
     *
     * retrieves cached value of a table or creates new table<br> when new table
     * is created it is also created new Tab for it
     *
     * @param vertex
     * @return re
     */
    private JTable addSimulationLogPanel(String vertex) {
        if (!panels.containsKey(vertex)) {
            throw new IllegalStateException("table for simulation logs was not created");
        }

        if (tablesMap.containsKey(vertex)) {
            return panels.get(vertex);
        }
        JTable table = panels.get(vertex);//null check was already performed at the beginning of this method
        //add table to hashmap
        JScrollPane pane = new JScrollPane(table);
        closeableTabbedPane1.addTab(vertex, pane);//add a new tab
        closeableTabbedPane1.setSelectedComponent(pane);//selects tab to give it a focus
        tablesMap.put(vertex, pane);
        return table;
    }

    /**
     * user closed the panel
     *
     * @param vertex name of the vertex assocoated with this simulation log
     * panel
     */
    private void panelClosed(String vertex) {
        tablesMap.remove(vertex);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dropCategory = new sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DropDownButton(false);
        closeableTabbedPane1 = new sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane.CloseableTabbedPane();
        jButton1 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(dropCategory, org.openide.util.NbBundle.getMessage(SimulationLogTopComponent.class, "SimulationLogTopComponent.dropCategory.text")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/clear.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SimulationLogTopComponent.class, "SimulationLogTopComponent.jButton1.text")); // NOI18N
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
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dropCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(41, 41, 41)
                .addComponent(closeableTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(dropCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(jButton1)
                .addContainerGap(219, Short.MAX_VALUE))
            .addComponent(closeableTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        clearLogs();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane.CloseableTabbedPane closeableTabbedPane1;
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DropDownButton dropCategory;
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    @Override
    public void simulationLogOccurred(SimulationLogEvent sle) {
        addLog(sle.getSimulationLog());
    }

    @Override
    public void dropDownHiddenOccurred(DropDownHiddenEvent evt) {
        updateLogs(evt.getSelectedCheckBoxes());
    }

    @Override
    public boolean closeTab(int index) {
        panelClosed(closeableTabbedPane1.getTitleAt(index));
        return true;
    }

    /**
     * simple class that acts as key in hash map
     */
    private static class FilterMapKey {

        private boolean info;
        private boolean warning;
        private boolean error;

        private FilterMapKey(List<String> stringList) {
            if (stringList.contains("INFO")) {
                this.info = true;
            }
            if (stringList.contains("ERROR")) {
                this.error = true;
            }
            if (stringList.contains("WARNING")) {
                this.warning = true;
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.info ? 1 : 0);
            hash = 37 * hash + (this.warning ? 1 : 0);
            hash = 37 * hash + (this.error ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FilterMapKey other = (FilterMapKey) obj;
            if (this.info != other.info) {
                return false;
            }
            if (this.warning != other.warning) {
                return false;
            }
            if (this.error != other.error) {
                return false;
            }
            return true;
        }
    }

    /**
     * highlight particular row according to the cell's value
     */
    class RowHighlighterTableCellRender extends DefaultTableCellRenderer {

        private final Color HIGHLIGHT_COLOR_WARNING = Color.YELLOW;
        private final Color HIGHLIGHT_COLOR_ERROR = Color.RED;
        private final Color HIGHLIGHT_COLOR_INFO = Color.WHITE;

        public RowHighlighterTableCellRender(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int categoryColumn = table.convertColumnIndexToView(1);
            String category = (String) table.getValueAt(row, categoryColumn);
            Color foreground = Color.WHITE;

            if (category.contains(LogCategory.ERROR.toString())) {
                foreground = HIGHLIGHT_COLOR_ERROR;
            }

            if (category.contains(LogCategory.INFO.toString())) {
                foreground = HIGHLIGHT_COLOR_INFO;
            }
            if (category.contains(LogCategory.WARNING.toString())) {
                foreground = HIGHLIGHT_COLOR_WARNING;
            }

            if (!isSelected) {
                comp.setBackground(foreground);
                comp.setForeground(Color.BLACK);
            } else {
                comp.setBackground(Color.WHITE);
                comp.setForeground(Color.BLACK);
            }

            setToolTipText(value.toString());

            return comp;
        }
    }
}
