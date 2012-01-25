/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation;

import java.awt.BorderLayout;
import org.apache.log4j.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.SimulationRuleIterator;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.wizard.panels.ContainerPanel;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation//AddSimulation//EN",
autostore = false)
@TopComponent.Description(preferredID = "AddSimulationTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "sk.stuba.fiit.kvasnicka.topologyvisual.gui.simulation.AddSimulationTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AddSimulationAction",
preferredID = "AddSimulationTopComponent")
@Messages({
    "CTL_AddSimulationAction=AddSimulation",
    "CTL_AddSimulationTopComponent=AddSimulation Window",
    "HINT_AddSimulationTopComponent=This is a AddSimulation window"
})
public final class AddSimulationTopComponent extends TopComponent {

    private final static Logger logg = Logger.getLogger(AddSimulationTopComponent.class);
    private ContainerPanel containerPanel;
    private SimulationRuleIterator panelIterator;

    public AddSimulationTopComponent() {
        initComponents();
        setName(Bundle.CTL_AddSimulationTopComponent());
        setToolTipText(Bundle.HINT_AddSimulationTopComponent());
        panelIterator = new SimulationRuleIterator();
        containerPanel = new ContainerPanel(panelIterator);
        panelIterator.setContainerPanel(containerPanel);
        setLayout(new BorderLayout());
        add(containerPanel, BorderLayout.CENTER);
        validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        panelIterator.initDefaultPanel();
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
}
