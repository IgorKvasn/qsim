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
import java.util.*;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.*;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.log.SimulationLogListener;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.LogCategory;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLog;
import sk.stuba.fiit.kvasnicka.qsimsimulation.logs.SimulationLogUtils;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane.CloseableTabbedPaneListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.dropdownevent.DropDownHiddenListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.SimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs//SimulationLogTopComponent//EN",
autostore = false)
@TopComponent.Description(preferredID = "SimulationLogTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.logs.SimulationLogTopComponent")
//@ActionReference(path = "Menu/Window" /*
// * , position = 333
// */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationLogTopComponentAction" //        , preferredID = "SimulationLogTopComponent"
)
@Messages({
    "CTL_SimulationLogTopComponentAction=SimulationLogTopComponent",
    "CTL_SimulationLogTopComponent=SimulationLogTopComponent Window",
    "HINT_SimulationLogTopComponent=This is a SimulationLogTopComponent window"
})
public final class SimulationLogTopComponent extends TopComponent implements SimulationLogListener, DropDownHiddenListener, CloseableTabbedPaneListener {

    private static Logger logg = Logger.getLogger(SimulationLogTopComponent.class);
    /**
     * mapping between TopologyVertex and SimulationLogPanel<br/> each
     * TopologyVertex has got one SimulationLogPanel<br/> note: TopologyVertex
     * object is represented by its name (String)
     */
    private Map<String, JXTable> panels = new HashMap<String, JXTable>();
    private Topology topology;

    public SimulationLogTopComponent() {
        initComponents();
        setName(Bundle.CTL_SimulationLogTopComponent());
        setToolTipText(Bundle.HINT_SimulationLogTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        initCategoryDropDown();

        dropCategory.addDropDownHiddenListener(this);
        closeableTabbedPane1.addCloseableTabbedPaneListener(this);
    }

    public void showVetices(Collection<TopologyVertex> vertices) {
        for (TopologyVertex v : vertices) {
            createSimulationLogPanel(v.getName());
        }
    }

    /**
     * sets Topology object that will be bind to this simulation log
     *
     * @param topology
     */
    public void setTopology(Topology topology) {
        this.topology = topology;
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
        String regex = createRegex(selectedCheckBoxes);

        for (Map.Entry<String, JXTable> entry : panels.entrySet()) {
            JXTable table = entry.getValue();

            table.setRowFilter(RowFilter.regexFilter(regex, table.convertColumnIndexToView(0)));
        }
    }

    private String createRegex(List<String> categoryList) {
        if (categoryList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String cat : categoryList) {
            sb.append("(").append(cat).append(")|");
        }
        //remove the last |
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private void addLog(SimulationLog simulationLog) {
        if (simulationLog == null) {
            throw new IllegalArgumentException("simulationLog is NULL");
        }
        if (simulationLog.getSourceName().equals(SimulationLogUtils.SOURCE_GENERAL)) {//all panels should be notified
            for (Map.Entry<String, JXTable> entry : panels.entrySet()) {
                JXTable table = entry.getValue();
                ((DefaultTableModel) table.getModel()).addRow(new String[]{simulationLog.getCategory().toString(), simulationLog.getCause(), simulationLog.getFormattedSimulationTime()});
            }
            return;
        }

        JXTable table = getSimulationLogPanel(simulationLog.getSourceName());
        if (table == null) {//no one is interrested in this simulation log
            return;
        }
        ((DefaultTableModel) table.getModel()).addRow(new String[]{simulationLog.getCategory().toString(), simulationLog.getCause(), simulationLog.getFormattedSimulationTime()});
    }

    private void createSimulationLogPanel(String vertex) {
        JXTable table = new JXTable();
        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Category", "Message", "Simulation time"
                }) {

            Class[] types = new Class[]{
                java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false
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

        //add table to hashmap
        panels.put(vertex, table);

        //add table to tabbed pane
        closeableTabbedPane1.addTab(vertex, table);
    }

    private void initTableRowHighlighter(JXTable table) {
        TableColumnModel colModel = table.getColumnModel();
        for (Enumeration<TableColumn> colEnum = colModel.getColumns(); colEnum.hasMoreElements();) {
            TableColumn c = colEnum.nextElement();
            c.setCellRenderer(new RowHighlighterTableCellRender(JLabel.CENTER));
        }
    }

    private JXTable getSimulationLogPanel(String vertex) {
        if (panels.containsKey(vertex)) {
            return panels.get(vertex);
        }
        return null;
    }

    /**
     * user closed the panel
     *
     * @param vertex name of the vertex assocoated with this simulation log
     * panel
     */
    private void panelClosed(String vertex) {
        panels.remove(vertex);
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

        org.openide.awt.Mnemonics.setLocalizedText(dropCategory, org.openide.util.NbBundle.getMessage(SimulationLogTopComponent.class, "SimulationLogTopComponent.dropCategory.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(dropCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(closeableTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(dropCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(200, Short.MAX_VALUE))
            .addComponent(closeableTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.closeabletabbedpane.CloseableTabbedPane closeableTabbedPane1;
    private sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.DropDownButton dropCategory;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (topology == null) {
            throw new IllegalStateException("topology is NULL; setTopology() method must be called");
        }
        topology.getTopolElementTopComponent().getSimulationFacade().addSimulationLogListener(this);
    }

    @Override
    public void componentClosed() {
        if (topology == null) {
            throw new IllegalStateException("topology is NULL; setTopology() method must be called");
        }
        topology.getTopolElementTopComponent().getSimulationFacade().removeSimulationLogListener(this);
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

            int categoryColumn = table.convertColumnIndexToView(0);
            String category = (String) table.getValueAt(row, categoryColumn);
            Color foreground = Color.WHITE;

            if (category.equals(LogCategory.ERROR.toString())) {
                foreground = HIGHLIGHT_COLOR_ERROR;
            }

            if (category.equals(LogCategory.INFO.toString())) {
                foreground = HIGHLIGHT_COLOR_INFO;
            }
            if (category.equals(LogCategory.WARNING.toString())) {
                foreground = HIGHLIGHT_COLOR_WARNING;
            }

            if (!isSelected) {
                comp.setBackground(foreground);
                comp.setForeground(Color.BLACK);
            } else {
                comp.setBackground(Color.WHITE);
                comp.setForeground(Color.BLACK);
            }

            return comp;
        }
    }
}
