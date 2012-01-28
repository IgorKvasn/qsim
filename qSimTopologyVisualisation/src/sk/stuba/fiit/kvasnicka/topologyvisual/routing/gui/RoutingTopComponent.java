/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.routing.gui;

import java.awt.event.ItemEvent;
import java.util.*;
import javax.swing.Action;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.japura.gui.CheckComboBox;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.gui.model.ListCheckModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Computer;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.NetworkNode;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Switch;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.VertexSelectionManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexSelectionChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexSelectionChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.RouterVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.components.JComboBoxAutoCompletator;
import sk.stuba.fiit.kvasnicka.topologyvisual.lookuputils.RouteChanged;
import sk.stuba.fiit.kvasnicka.topologyvisual.routing.RoutingReviewCategoryFactory;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.routing.gui//Routing//EN",
autostore = false)
@TopComponent.Description(preferredID = "RoutingTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.routing.RoutingTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_RoutingActionSidebar",
preferredID = "RoutingTopComponent")
public final class RoutingTopComponent extends TopComponent implements ExplorerManager.Provider, VertexSelectionChangedListener, LookupListener {

    private final ExplorerManager mgr = new ExplorerManager();
    private static Logger logg = Logger.getLogger(RoutingTopComponent.class);
    private RoutingReviewCategoryFactory routingReviewCategoryFactory;
    private DefaultTableModel tableModel = new DefaultTableModel();
    private Map<String, Class> convert = new HashMap<String, Class>(); //a simple converter between item in ComboCheckBox and Networknode object :)
    private Result<RouteChanged> resultRoute;

    public RoutingTopComponent() {
        initComponents();
        logg.debug("RoutingTopComponent - constructor");

        setName(NbBundle.getMessage(RoutingTopComponent.class, "CTL_RoutingTopComponent"));
        setToolTipText(NbBundle.getMessage(RoutingTopComponent.class, "HINT_RoutingTopComponent"));

        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
        routingReviewCategoryFactory = new RoutingReviewCategoryFactory(jComboBox1);
        AbstractNode rootNode = new AbstractNode(Children.create(routingReviewCategoryFactory, true)) {

            @Override
            public Action[] getActions(boolean context) {
                return new Action[]{};//here define action for BeanTreeView
            }
        };

        mgr.setRootContext(rootNode);

        ((BeanTreeView) jScrollPane1).setRootVisible(false);

        initCheckComboBox();

        tableModel = (DefaultTableModel) jTable1.getModel();
        new JComboBoxAutoCompletator(comboRoutingTable); //I do not need to handle newly created instance

        NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getVertexSelectionManager().addVertexSelectionChangedListener(this);

        resultRoute = Utilities.actionsGlobalContext().lookupResult(RouteChanged.class);
        resultRoute.addLookupListener(this);
    }

    @Override
    public void vertexSelectionChangedOccurred(VertexSelectionChangedEvent evt) {
        TopologyVertex firstSelectedVertex = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getVertexSelectionManager().getFirstSelectedRouterVertex();
        logg.debug("showing routing table for: " + firstSelectedVertex);
        comboRoutingTable.setSelectedItem(firstSelectedVertex);
        if (firstSelectedVertex == null) {
            clearTable();
        } else {
            updateTableByComboRouter();
        }
    }

    private void initCheckComboBox() {
        checkComboBox1.setTextFor(CheckComboBox.NONE, "* none item selected *");
        checkComboBox1.setTextFor(CheckComboBox.MULTIPLE, "* multiple items *");
        checkComboBox1.setTextFor(CheckComboBox.ALL, "* all selected *");

        ListCheckModel model = checkComboBox1.getModel();
        addCheckComboItem(model, NbBundle.getMessage(RoutingTopComponent.class, "routers"), Router.class);
        addCheckComboItem(model, NbBundle.getMessage(RoutingTopComponent.class, "switches"), Switch.class);
        addCheckComboItem(model, NbBundle.getMessage(RoutingTopComponent.class, "computers"), Computer.class);


        model.checkAll();

        model.addListCheckListener(new ListCheckListener() {

            @Override
            public void removeCheck(ListEvent event) {
                routingReviewCategoryFactory.removeAllowedNode(convertCombocheck(event.getValues()));
                routingReviewCategoryFactory.resultChanged(null);
            }

            @Override
            public void addCheck(ListEvent event) {
                routingReviewCategoryFactory.addAllowedNode(convertCombocheck(event.getValues()));
                routingReviewCategoryFactory.resultChanged(null);
            }
        });
    }

    private List<Class> convertCombocheck(List<Object> list) {
        List<Class> result = new LinkedList<Class>();
        for (Object o : list) {
            if (!(o instanceof String)) {
                logg.warn("Object " + o + "is not a String - this is rather unexpected...");
                continue;
            }
            result.add(convert.get((String) o));
        }
        return result;
    }

    private void addCheckComboItem(ListCheckModel model, String label, Class networkNode) {
        model.addElement(label);
        convert.put(label, networkNode);
        routingReviewCategoryFactory.addAllowedNode(Arrays.asList(networkNode));
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        updateTableByComboRouter();
    }

    /**
     * updates routing table according to user's selection in jCombobox2
     *
     * @return
     * @throws IllegalStateException
     */
    private void updateTableByComboRouter() throws IllegalStateException {
        if (isRoutingTableShowing()) {
            if (comboRoutingTable.getSelectedItem() == null) {
                clearTable();
                return;
            }
            if (!(comboRoutingTable.getSelectedItem() instanceof RouterVertex)) {
                throw new IllegalStateException("only Router object can be items of jCombobox2 - something went awfully wrong!");
            }
            fillTable(((RouterVertex) comboRoutingTable.getSelectedItem()).getDataModel());
        }
    }

    public void refresh() {
        if (isRoutingTableShowing()) {
            fillComboBox();
        } else {
            logg.debug("refreshing routing tree");
            routingReviewCategoryFactory.resultChanged(null);
        }

    }

    private boolean isRoutingTableShowing() {
        return jTabbedPane1.getSelectedIndex() == 0;
    }

    private void fillComboBox() {
        logg.debug("filling combobox");
        if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
            return;
        }
        List<RouterVertex> vertexRouterList = NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().getVertexRouterList();
        comboRoutingTable.removeAllItems();
        for (RouterVertex vertex : vertexRouterList) {
            comboRoutingTable.addItem(vertex);
        }
        TopologyVertex firstSelectedVertex = NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().getVertexSelectionManager().getFirstSelectedRouterVertex();
        comboRoutingTable.setSelectedItem(firstSelectedVertex);
    }

    private void fillTable(NetworkNode router) {
        logg.debug("filling routing table for " + router.getName());
        clearTable();
        Map<String, String> routes = router.getRoutes();
        for (String destination : routes.keySet()) {
            addTableRow(destination, routes.get(destination));
        }
    }

    private void addTableRow(String destination, String nextHop) {
        tableModel.addRow(new Object[]{destination, nextHop});
    }

    private void clearTable() {
        while (tableModel.getRowCount() != 0) {
            tableModel.removeRow(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelRoutingTable = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        comboRoutingTable = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        panelRoutingReview = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        checkComboBox1 = new org.japura.gui.CheckComboBox();
        jScrollPane1 = new BeanTreeView();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RoutingTopComponent.class, "RoutingTopComponent.jLabel3.text")); // NOI18N
        panelRoutingTable.add(jLabel3);

        comboRoutingTable.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboRoutingTableItemStateChanged(evt);
            }
        });
        panelRoutingTable.add(comboRoutingTable);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Destination", "Next hop"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        panelRoutingTable.add(jScrollPane2);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RoutingTopComponent.class, "RoutingTopComponent.panelRoutingTable.TabConstraints.tabTitle"), panelRoutingTable); // NOI18N

        panelRoutingReview.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridLayout(2, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RoutingTopComponent.class, "RoutingTopComponent.jLabel1.text")); // NOI18N
        jPanel2.add(jLabel1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "next hop only", "destination" }));
        jPanel2.add(jComboBox1);

        jPanel1.add(jPanel2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RoutingTopComponent.class, "RoutingTopComponent.jLabel2.text")); // NOI18N
        jPanel3.add(jLabel2);
        jPanel3.add(checkComboBox1);

        jPanel1.add(jPanel3);

        panelRoutingReview.add(jPanel1, java.awt.BorderLayout.PAGE_START);
        panelRoutingReview.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RoutingTopComponent.class, "RoutingTopComponent.panelRoutingReview.TabConstraints.tabTitle"), panelRoutingReview); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboRoutingTableItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboRoutingTableItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateTableByComboRouter();
        }
    }//GEN-LAST:event_comboRoutingTableItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.japura.gui.CheckComboBox checkComboBox1;
    private javax.swing.JComboBox comboRoutingTable;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panelRoutingReview;
    private javax.swing.JPanel panelRoutingTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        refresh();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        refresh();
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("selectedTab", jTabbedPane1.getSelectedIndex() + "");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        String selTabString = p.getProperty("selectedTab");
        if (selTabString != null) {
            Integer selectedTab = Integer.valueOf(selTabString);
            jTabbedPane1.setSelectedIndex(selectedTab);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
