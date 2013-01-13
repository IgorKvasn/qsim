/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import sk.stuba.fiit.kvasnicka.topologyvisual.actions.buttons.ButtonEnum;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.resources.ImageResourceHelper;
import sk.stuba.fiit.kvasnicka.topologyvisual.topology.TopologyStateEnum;

/**
 *
 * @author Igor Kvasnicka
 */
@ActionID(category = "Simulation",
id = "sk.stuba.fiit.kvasnicka.topologyvisual.actions.SimulationSeedAction")
@ActionRegistration( lazy = false, displayName = "#CTL_SimulationSeedAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Simulation", position = 3633)
})
@NbBundle.Messages("CTL_SimulationSeedAction=Speed")
public class SimulationSpeedAction extends AbstractAction implements Presenter.Toolbar {

    private static Logger logg = Logger.getLogger(RunSimulationAction.class);
    private JButton buttonPlus = new JButton();
    private JButton buttonMinus = new JButton();
    private JLabel lblSpeed = new JLabel();
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private static SimulationSpeedAction INSTANCE;

    public SimulationSpeedAction() {
        buttonPlus.setContentAreaFilled(false);
        buttonPlus.setEnabled(false);
        buttonPlus.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/plus.png"));
        buttonPlus.setFocusPainted(false);
        buttonPlus.setBorderPainted(false);
        buttonPlus.setOpaque(true);
        buttonPlus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().increaseSpeedSimulation(SimulationSpeedAction.this);
            }
        });
        
        buttonMinus.setContentAreaFilled(false);
        buttonMinus.setFocusPainted(false);
        buttonMinus.setOpaque(true);
        buttonMinus.setBorderPainted(false);
        buttonMinus.setEnabled(false);
        buttonMinus.setIcon(ImageResourceHelper.loadImage("/sk/stuba/fiit/kvasnicka/topologyvisual/resources/files/minus.png"));
        buttonMinus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetbeansWindowHelper.getInstance().getActiveTopologyVisualisation().decreaseSpeedSimulation(SimulationSpeedAction.this);
            }
        });

        buttonPanel.add(buttonMinus);
        buttonPanel.add(lblSpeed);
        buttonPanel.add(buttonPlus);
        buttonPanel.setSize(100, buttonPanel.getHeight());        

        INSTANCE = this;
    }

    public static SimulationSpeedAction getInstance() {
        return INSTANCE;
    }

    public void updateSpeed(double speed) {
        lblSpeed.setText(speed + "x");
    }

    @Override
    public Component getToolbarPresenter() {
        return buttonPanel;
    }

    public void updateState(TopologyStateEnum state) {
        if (state == null) {
            buttonPlus.setEnabled(false);
            buttonMinus.setEnabled(false);
            lblSpeed.setEnabled(false);
            return;
        }
        buttonPlus.setEnabled(false);
        buttonMinus.setEnabled(false);
        lblSpeed.setEnabled(false);

        if (state.isButtonEnabled(ButtonEnum.SPEED)) {
            buttonPlus.setEnabled(true);
            buttonMinus.setEnabled(true);
            lblSpeed.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //delegated to panel
    }
}
