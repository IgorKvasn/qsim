/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.palette.gui;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.BorderLayout;
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Getter;
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
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils.DialogHandler;
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
import sk.stuba.fiit.kvasnicka.topologyvisual.lookuputils.RouteChanged;
import sk.stuba.fiit.kvasnicka.topologyvisual.palette.PaletteActionEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.serialisation.DeserialisationResult;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyCreation;

/**
 * Top component which displays something.
 */
@MultiViewElement.Registration(displayName = "#LBL_TopologyCreatorMultiview",
iconBase = "sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/qsimFileType.png",
mimeType = "text/qsim",
persistenceType = TopComponent.PERSISTENCE_NEVER,
preferredID = "TopologyCreatorMultiview",
position = 2000)
@NbBundle.Messages("LBL_TopologyCreatorMultiview=Creator")
public final class TopolElementTopComponent extends JPanel implements Serializable, VertexCreatedListener, DocumentListener, MultiViewElement, PaletteSelectionListener {

    private static Logger logg = Logger.getLogger(TopolElementTopComponent.class);
    private TopologyCreation topology;
    private TopologyElementCreatorHelper topologyElementCreator;
    private PaletteActionEnum selectedPaletteAction = null;
    private DialogHandler dialogHandler;
    private InstanceContent content;
    private TopologyFileTypeDataObject dataObject;
    private JToolBar toolBar = new JToolBar();
    private MultiViewElementCallback callback;
    @Getter
    private VertexSelectionManager vertexSelectionManager = new VertexSelectionManager();

    public TopolElementTopComponent(Lookup lkp) {
        dataObject = lkp.lookup(TopologyFileTypeDataObject.class);
        assert dataObject != null;

        if (dialogHandler == null) {
            dialogHandler = new DialogHandler();
        }

        initComponents();

        content = new InstanceContent();

        topology = new TopologyCreation(this);
        initTopology();
        initPalette();
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
    private TopolElementTopComponent() {
//        associateLookup(new ProxyLookup(new Lookup[]{
//                    new AbstractLookup(content),//creating new nodes will invoke certain action in RoutingTopComponent
////                    Lookups.fixed(paletteController), //palette is opening together with this window
//                //   Lookups.singleton(this)//used to retieve curently activated TopolElementTopComponent window
//                }));
    }

    /**
     * Some Netbeans tutorial said that this ought to be here. I read that
     * tutorial some time ago and a I vividly recall that I was a bit frustrated
     * that nothing worked... Nevertheless this method has something to do with
     * MultiViews. Don't ask me what does this method or why is it here - I also
     * don't understand, why (if it is soooo important) is not part of some
     * interface/TomComponent class
     *
     * @return
     */
    public Object getDefault() {
        return null;
    }

    @Override
    public void paletteSelectedOccurred(PaletteSelectionEvent evt) {
        selectedPaletteAction = evt.getSelectedAction();
        setStatusBarText(NbBundle.getMessage(TopolElementTopComponent.class, "creating.new") + " " + selectedPaletteAction.getDisplayableName());
        if (!PaletteActionEnum.isEdgeAction(selectedPaletteAction)) {//when creating new Vertex, editing mode is required
            topology.setEditingMode();
        }
        topologyElementCreator.setAction(selectedPaletteAction);
    }

    @Override
    public void paletteDeselectedOccurred(PaletteSelectionEvent evt) {
        topologyElementCreator.cancelAction();
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

    public TopologyElementCreatorHelper getTopologyElementCreator() {
        return topologyElementCreator;
    }

    public DialogHandler getDialogHandler() {
        return dialogHandler;
    }

    public TopologyCreation getTopology() {
        return topology;
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
        //  store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        //  read your settings according to their version
    }

    public PaletteActionEnum getSelectedPaletteAction() {
        return selectedPaletteAction;
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
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
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
        NetbeansWindowHelper.getInstance().setActiveTopologyTopComponent(this);
        openPaletteWindow();
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
