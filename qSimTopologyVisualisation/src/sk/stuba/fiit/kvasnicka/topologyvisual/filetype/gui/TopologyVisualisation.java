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
package sk.stuba.fiit.kvasnicka.topologyvisual.filetype.gui;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import sk.stuba.fiit.kvasnicka.qsimsimulation.facade.SimulationFacade;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.ConfigureSimulationAction;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.PauseSimulationAction;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.RunSimulationAction;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.StopSimulationAction;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.events.topologystate.TopologyStateChangedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.exceptions.RoutingException;
import sk.stuba.fiit.kvasnicka.topologyvisual.filetype.TopologyFileTypeDataObject;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.edges.TopologyEdge;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.events.vertexcreated.VertexCreatedListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyElementCreatorHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.vertices.TopologyVertex;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.DialogHandler;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.TopologyPaletteTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionEvent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.palette.events.PaletteSelectionListener;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.SimulationTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.simulationdata.SimulRuleReviewTopComponent;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.VerticesSelectionPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;
import sk.stuba.fiit.kvasnicka.topologyvisual.simulation.StatisticalDataManager;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.Topology;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.EdgeUtils;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.SimulationData;
import sk.stuba.fiit.kvasnicka.topologyvisual.utils.VerticesUtil;

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
public final class TopologyVisualisation extends JPanel implements VertexCreatedListener, DocumentListener, MultiViewElement, PaletteSelectionListener {

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
    @Getter
    private SimulationFacade simulationFacade = new SimulationFacade(); //todo serialise - SimulationManager and PingManager
    @Getter
    private transient SimulationData simulationData;
    @Getter
    private transient TopologyStateEnum simulationState;
    private transient StatisticalDataManager statManager;
    private transient SimulRuleReviewTopComponent simulRuleReviewTopComponent;
    private transient SimulationTopComponent simulationTopComponent;

    public TopologyVisualisation(TopologyFileTypeDataObject dataObject) {
        this.dataObject = dataObject;
        if (dialogHandler == null) {
            dialogHandler = new DialogHandler();
        }

        initComponents();

        content = new InstanceContent();
        topology = new Topology(this);
        simulationData = new SimulationData(dataObject, topology);

        setSimulationState(TopologyStateEnum.NOTHING);

        initTopology();
        initPalette();
    }

    

    /**
     * starts the simulation
     */
    public void runSimulation() {
        //todo check if simulation is already running, but beware that simulation may be paused - playing paused simulation means resume
        if (simulationFacade.isTimerRunning()) {
            throw new IllegalStateException("simulation is already running");
        }

        //are there any simulation logs to simulate?
        if (simulationData.getSimulationData().isEmpty()) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(TopologyVisualisation.class, "no_simulation_rules"),
                    NbBundle.getMessage(TopologyVisualisation.class, "no_simulation_rules_title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        try {
            //finalise simulation rules = init routing
            List<SimulationRuleBean> simulationRules = simulationData.getSimulationRulesFinalised(); //very important method !!
            //add simulation rules into simulation facade
            for (SimulationRuleBean rule : simulationRules) {
                simulationFacade.addSimulationRule(rule);
            }

            //change simulation state
            setSimulationState(TopologyStateEnum.RUN);

            //close palette, simulation top component, add simulation rule top component,... 
            //everuthing that is no use for simulation
            closeTopologyCreationWindows();

            statManager = new StatisticalDataManager(simulationRules);
            simulationFacade.addPingRuleListener(statManager);
            simulationFacade.addSimulationRuleListener(statManager);
            simulationFacade.addPingPacketDeliveredListener(statManager);

            //opens all supporting windows for simulation
            openSimulationWindows(statManager, simulationFacade.getSimulationRules());

            if (simulationFacade.isTimerPaused()) {
                simulationFacade.resumeTimer();
            } else {
                simulationFacade.initTimer(EdgeUtils.convertTopologyEdgeListToEdgeList(topology.getG().getEdges()), VerticesUtil.convertTopologyVertexList2NetworkNodeList(topology.getVertexFactory().getAllVertices()));
                simulationFacade.startTimer();
            }

        } catch (RoutingException ex) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(TopologyVisualisation.class, "simulation_rule_error_part1") + "\n" + ex.getMessage() + "\n" + NbBundle.getMessage(TopologyVisualisation.class, "simulation_rule_error_part2"),
                    NbBundle.getMessage(TopologyVisualisation.class, "simulation_rule_error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * stops (cancels) the simulation
     */
    public void stopSimulation() {
        if (simulationState != TopologyStateEnum.RUN && simulationState != TopologyStateEnum.PAUSED) {
            throw new IllegalStateException("simulation is not eligible for stop - only running or paused simulations can be stopped");
        }

        if (statManager == null) {
            throw new IllegalStateException("statManager is NULL - it seems like simulation has not been started");
        }

        //change simulation state
        setSimulationState(TopologyStateEnum.NOTHING);

        simulationFacade.removePingRuleListener(statManager);
        simulationFacade.removeSimulationRuleListener(statManager);
        simulationFacade.removePingPacketDeliveredListener(statManager);

        closeSimulationWindows();

        //opens palette sho user can add new vertices/edges
        TopologyPaletteTopComponent palette = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (palette == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        palette.open();
    }

    /**
     * temporary pauses the simulation
     */
    public void pauseSimulation() {
        //change simulation state
        setSimulationState(TopologyStateEnum.PAUSED);
    }

    /**
     * configure simulation rules
     */
    public void configureSimulation() {
        simulationTopComponent = new SimulationTopComponent(simulationFacade, simulationData);
        Mode outputMode = WindowManager.getDefault().findMode("myoutput");
        outputMode.dockInto(simulationTopComponent);
        simulationTopComponent.open();
        simulationTopComponent.requestActive();
    }

    private void setSimulationState(TopologyStateEnum state) {
        simulationState = state;
        updateToolbarButtons(false);
        fireTopologyStateChangeEvent(new TopologyStateChangedEvent(this, state));
    }

    public void reloadSimulationRuleData() {
        //reload simulation rules shown in SimulationTopComponent
        if (simulationTopComponent != null && simulationTopComponent.isOpened()) {//only if it is opened (note: opened is not the same as visible - it may be opened, but covered by some other TopComponent)
            simulationTopComponent.loadSimulationRules();
        }
    }

    /**
     * updates toolbar buttons according to current simulation state
     *
     * @param disable true if toolbar buttons should be disabled no matter
     * simulation state
     */
    private void updateToolbarButtons(boolean disable) {
        if (active) {//only active TopolVisualisation can update toolbar buttons
            logg.debug("-------" + simulationState);
            if (disable) {//toolbar buttons should be disabled
                RunSimulationAction.getInstance().updateState(null);
                PauseSimulationAction.getInstance().updateState(null);
                StopSimulationAction.getInstance().updateState(null);
                ConfigureSimulationAction.getInstance().updateState(null);

            } else {
                RunSimulationAction.getInstance().updateState(simulationState);
                PauseSimulationAction.getInstance().updateState(simulationState);
                StopSimulationAction.getInstance().updateState(simulationState);
                ConfigureSimulationAction.getInstance().updateState(simulationState);
            }
        }
    }

    /**
     * closes these top components: <ol> <li>SimulationTopCopmponent</li>
     * <li>AddSimulationTopComponent</li> <li>TopologyPaletteTopComponent</li>
     * </ol>
     *
     */
    private void closeTopologyCreationWindows() {
        TopologyPaletteTopComponent palette = (TopologyPaletteTopComponent) WindowManager.getDefault().findTopComponent("TopologyPaletteTopComponent");
        if (palette == null) {
            logg.error("Could not find component TopologyPaletteTopComponent");
            return;
        }
        logg.debug("---- closing palette - can close: " + palette.canClose());
        palette.close();

        if (simulationTopComponent != null) {
            simulationTopComponent.close();
            simulationTopComponent.closeAddSimulationTopComponent();
            simulationTopComponent = null;
        }
    }

    private void openSimulationWindows(StatisticalDataManager statManager, List<SimulationRuleBean> simulRules) {
        simulRuleReviewTopComponent = new SimulRuleReviewTopComponent(simulationFacade);
        simulRuleReviewTopComponent.setSimulationRules(statManager, simulRules);
        simulationFacade.addSimulationRuleActivatedListener(simulRuleReviewTopComponent);
        simulationFacade.addPingRuleListener(simulRuleReviewTopComponent);
        simulationFacade.addSimulationRuleListener(simulRuleReviewTopComponent);

        Mode outputMode = WindowManager.getDefault().findMode("commonpalette");
        outputMode.dockInto(simulRuleReviewTopComponent);
        simulRuleReviewTopComponent.open();
        simulRuleReviewTopComponent.requestActive();
    }

    private void closeSimulationWindows() {
        if (simulRuleReviewTopComponent != null) {//if null, this top component is not opened
            simulationFacade.removeSimulationRuleActivatedListener(simulRuleReviewTopComponent);
            simulationFacade.removePingRuleListener(simulRuleReviewTopComponent);
            simulationFacade.removeSimulationRuleListener(simulRuleReviewTopComponent);
            simulRuleReviewTopComponent.close();
            simulRuleReviewTopComponent.closeSimulationDataTopComponent();
            simulRuleReviewTopComponent = null;
        }
        if (simulationTopComponent != null) {
            simulationTopComponent.close();
            simulationTopComponent = null;
        }
    }

    public AddSimulationTopComponent getAddSimulRuleTopComponent() {
        return simulationTopComponent.getAddSimulationTopComponent();
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
        if (simulationTopComponent != null) {
            simulationTopComponent.close();
            simulationTopComponent.closeAddSimulationTopComponent();
        }
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

    public void addTopologyStateChangedListener(TopologyStateChangedListener listener) {
        listenerList.add(TopologyStateChangedListener.class, listener);
    }

    public void removeTopologyStateChangedListener(TopologyStateChangedListener listener) {
        listenerList.remove(TopologyStateChangedListener.class, listener);
    }

    private void fireTopologyStateChangeEvent(TopologyStateChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i].equals(TopologyStateChangedListener.class)) {
                ((TopologyStateChangedListener) listeners[i + 1]).topologyStateChangeOccured(evt);
            }
        }
    }

    public PaletteActionEnum getSelectedAction() {
        return selectedAction;
    }

    @Override
    public void vertexCreatedOccurred(VertexCreatedEvent evt) {
        content.add(evt.getNewVertex());//todo this has PROBABLY no meaning
        topologyModified();
    }

    public void topologyModified() {
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
        updateToolbarButtons(false);
    }

    @Override
    public void componentClosed() {
        active = false;
        hideSimulationTopComponents();
        topologyElementCreator.cancelAction();
        selectedAction = null;
        updateToolbarButtons(true);
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
        updateToolbarButtons(false);

    }

    @Override
    public void componentDeactivated() {
        updateToolbarButtons(true);
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
