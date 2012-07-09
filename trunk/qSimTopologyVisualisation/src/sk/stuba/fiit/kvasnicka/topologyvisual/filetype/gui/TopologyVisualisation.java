/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.event.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.DialogHandler;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.VertexSelectionManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyElementCreatorHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.TopologyPaletteTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.SimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.VerticesSelectionPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.lookuputils.RouteChanged;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;

/**
 * Top component which displays something.
 */
@MultiViewElement.Registration(displayName = "#LBL_TopologyCreatorMultiview",
iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png",
mimeType = "text/qsim",
persistenceType = TopComponent.PERSISTENCE_NEVER,
preferredID = "TopologyMultiviewElement",
position = 2000)
@NbBundle.Messages("LBL_TopologyCreatorMultiview=Topology")
public final class TopologyVisualisation extends JPanel implements Serializable, VertexCreatedListener, DocumentListener, MultiViewElement, PaletteSelectionListener {

    private static Logger logg = Logger.getLogger(TopologyVisualisation.class);
    private Topology topology;
    private TopologyElementCreatorHelper topologyElementCreator;
    private PaletteActionEnum selectedAction = null;
    private DialogHandler dialogHandler;
    private InstanceContent content;
    @Getter
    private TopologyFileTypeDataObject dataObject;
    private JToolBar toolBar = new JToolBar();
    private MultiViewElementCallback callback;
    @Getter
    @Setter
    private boolean active = false;
    private VerticesSelectionPanel verticesSelectionPanel = null;//used as callback object when user is selecting vertex during simulation rules definition
    private SimulationFacade simulationFacade; //todo serialise - SimulationManager and PingManager

    public TopologyVisualisation(TopologyFileTypeDataObject dataObject) {
        this.dataObject = dataObject;
        if (dialogHandler == null) {
            dialogHandler = new DialogHandler();
        }

        initComponents();

        content = new InstanceContent();

        topology = new Topology(this);
        initTopology();
        initPalette();
        initToolbar();        
    }

    public SimulationFacade getSimulationFacade() {
        if (simulationFacade == null) {
            simulationFacade = new SimulationFacade();
        }
        return simulationFacade;
    }

    /**
     * inits toolbar buttons for this MultiViewElement
     */
    private void initToolbar() {
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS));
        JButton btnSimulRules = new JButton(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/play_simul_16.png"));
        btnSimulRules.setToolTipText(NbBundle.getMessage(TopologyVisualisation.class, "bntDefineSimulation"));
        btnSimulRules.setFocusPainted(false);
        btnSimulRules.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SimulationTopComponent component = (SimulationTopComponent) WindowManager.getDefault().findTopComponent("SimulationTopComponent");
                if (component == null) {
                    logg.error("Could not find component SimulationTopComponent");
                    return;
                }
                component.open();
            }
        });


        JButton btnPlay = new JButton(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/play_16.png"));
        btnPlay.setToolTipText(NbBundle.getMessage(TopologyVisualisation.class, "btnPlay"));
        btnPlay.setFocusPainted(false);
        btnPlay.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        toolBar.add(Box.createHorizontalStrut(30));
        toolBar.add(btnSimulRules);
        toolBar.add(btnPlay);
    }

    /**
     * inits listeners for topology palette
     */
    private void initPalette() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.initListener(this);
    }

    /**
     * inits topology object
     */
    private void initTopology() {
        DeserialisationResult loadSettings = dataObject.getLoadSettings();

        if (!loadSettings.isJungLoaded()) {
            dialogHandler = new DialogHandler();
            topology.createDefaultSettings();
        } else {
            loadSettings.getVFactory().setTopolElementTopComponent(this);
            topology.loadFromSettings(loadSettings);
            dialogHandler = new DialogHandler(topology.getVertexFactory().getVertexRouterList().size(), topology.getVertexFactory().getVertexSwitchList().size(), topology.getVertexFactory().getVertexComputerList().size());
        }
        topology.initTopology();
        setTopologyCreationMode();
        topologyElementCreator = new TopologyElementCreatorHelper(topology, this);
        //listen for vertices to change their position
        topology.getVv().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                topologyModified();
            }
        });
    }

    @Deprecated
    private TopologyVisualisation() {
//        associateLookup(new ProxyLookup(new Lookup[]{
//                    new AbstractLookup(content),//creating new nodes will invoke certain action in RoutingTopComponent
////                    Lookups.fixed(paletteController), //palette is opening together with this window
//                //   Lookups.singleton(this)//used to retieve curently activated TopologyVisualisation window
//                }));
    }

    @Override
    public void paletteSelectedOccurred(PaletteSelectionEvent evt) {
        selectedAction = evt.getSelectedAction();
        setStatusBarText(NbBundle.getMessage(TopologyVisualisation.class, "creating.new") + " " + selectedAction.getDisplayableName());
        topologyElementCreator.setAction(selectedAction);
    }

    /**
     * no action selected in palette
     */
    public void deselectAction() {
        selectedAction = null;
    }

    @Override
    public void paletteDeselectedOccurred(PaletteSelectionEvent evt) {
        topologyElementCreator.cancelAction();
        topology.deselectVertices();

    }

    public void paletteClearSelection() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.clearSelection();
    }

    private void openPaletteWindow() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.open();
    }

    private void hidePaletteWindow() {
        TopologyPaletteTopComponent component = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (component == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        component.close();
    }

    private void hideSimulationTopComponents() {
        SimulationTopComponent component = (SimulationTopComponent) WindowManager.getDefault().findTopComponent("SimulationTopComponent");
        if (component == null) {
            logg.error("Could not find component SimulationTopComponent");
            return;
        }
        component.close();

        AddSimulationTopComponent componentAdd = (AddSimulationTopComponent) WindowManager.getDefault().findTopComponent("AddSimulationTopComponent");
        if (componentAdd == null) {
            logg.error("Could not find component AddSimulationTopComponent");
            return;
        }
        componentAdd.close();
    }

    /**
     * adds Jung component into TopComponent - this happens when topology has
     * been loaded from file (successfully or not)
     *
     * @param vv
     */
    public void addJungIntoFrame(VisualizationViewer<TopologyVertex, TopologyEdge> vv) {
        logg.debug("adding Jung component to TopComponent");
        jPanel1.add(topology.getVv(), BorderLayout.CENTER);
        validate();
        topology.getVv().repaint();
    }

    /**
     * sets text that will be shown in status bar - e.g. information what is
     * user about to do,...
     *
     * @param text text to show
     */
    public void setStatusBarText(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }

    /**
     * returns name of the vertex that user has clicked on
     *
     * @return
     * @see
     * #retrieveVertexByClickCancel(sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.VerticesSelectionPanel)
     */
    public void retrieveVertexByClick(VerticesSelectionPanel verticesSelectionPanel) {
        this.verticesSelectionPanel = verticesSelectionPanel;
        setSimulationRulesMode();
    }

    /**
     * cancels user decision to pick vertex for his simulation rule
     *
     * @return
     * @see
     * #retrieveVertexByClick(sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.VerticesSelectionPanel)
     */
    public void retrieveVertexByClickCancel() {
        setTopologyCreationMode();
        this.verticesSelectionPanel = null;
    }

    /**
     * callback from VertexPickedSimulRulesListener
     *
     * @param v
     */
    public void simulationRulesVertexPicked(TopologyVertex v) {
        if (Topology.TopologyModeEnum.SIMULATION_RULES != topology.getTopologyMode()) {//this method is active only when creating simulation rules
            return;
        }
        if (verticesSelectionPanel == null) {
            throw new IllegalStateException("verticesSelectionPanel is NULL");
        }
        verticesSelectionPanel.setVertexPicked(v);
    }

    public TopologyElementCreatorHelper getTopologyElementCreator() {
        return topologyElementCreator;
    }

    public DialogHandler getDialogHandler() {
        return dialogHandler;
    }

    public Topology getTopology() {
        return topology;
    }

    /**
     * mode for creating topology
     */
    public void setTopologyCreationMode() {
        topology.setMode(Topology.TopologyModeEnum.CREATION);
    }

    /**
     * mode for defining simulation rules
     */
    public void setSimulationRulesMode() {
        topology.setMode(Topology.TopologyModeEnum.SIMULATION_RULES);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
    }//GEN-LAST:event_jPanel1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("active", Boolean.toString(active));
        //  store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        active = Boolean.parseBoolean(p.getProperty("active"));
        if (active) {//make this topolvisual active and all other non-active
            NetbeansWindowHelper.getInstance().activateWindow(this);
        }
        //  read your settings according to their version
    }

    public PaletteActionEnum getSelectedAction() {
        return selectedAction;
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        content.add(evt.getNewVertex());
        topologyModified();
    }

    @Deprecated
    public void routesChanged() {
        logg.debug("routes changed");
        content.add(new RouteChanged());
        topologyModified();
    }

    private void topologyModified() {
        dataObject.modifiedTopology(callback.getTopComponent(), topology.getG(), topology.getLayout(), topology.getVertexFactory());
    }

    //-------savable-------
    @Override
    public void insertUpdate(DocumentEvent e) {
        topologyModified();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        topologyModified();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        topologyModified();
    }

    //-------------
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolBar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return dataObject.getLookup();
    }

    @Override
    public void componentOpened() {
        NetbeansWindowHelper.getInstance().activateWindow(this);
    }

    @Override
    public void componentClosed() {
        hideSimulationTopComponents();
        topologyElementCreator.cancelAction();
        selectedAction = null;
    }

    @Override
    public void componentShowing() {
        NetbeansWindowHelper.getInstance().activateWindow(this);
    }

    @Override
    public void componentHidden() {
        hidePaletteWindow();
        setStatusBarText("");
        topologyElementCreator.cancelAction();
        paletteClearSelection();
    }

    @Override
    public void componentActivated() {
        openPaletteWindow();
        NetbeansWindowHelper.getInstance().activateWindow(this);
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (dataObject == null) {
            return;
        }
        if (dataObject.isDirty()) {
            callback.updateTitle(dataObject.getPrimaryFile().getNameExt() + "*");
        } else {
            callback.updateTitle(dataObject.getPrimaryFile().getNameExt());
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
}
