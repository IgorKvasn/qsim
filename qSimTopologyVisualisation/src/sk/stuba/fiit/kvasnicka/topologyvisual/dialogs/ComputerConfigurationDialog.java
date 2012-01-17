/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Computer;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.panels.ComputerPanel;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils.BlockingDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 *
 * @author Igor Kvasnicka
 */
public class ComputerConfigurationDialog extends BlockingDialog<ComputerConfigurationDialog.ResultObject> {

    private ComputerPanel compPanel;
    private JButton btnOk, btnCancel;
    private JTextField txtName;

    public ComputerConfigurationDialog(Computer computer) {
        super(null);
        setTitle(NbBundle.getMessage(ComputerConfigurationDialog.class, "create.new.computer"));
        compPanel = new ComputerPanel();
        btnOk = compPanel.btnOK;
        txtName = compPanel.txtName;
        btnCancel = compPanel.btnCancel;

        initGui(computer);
    }

    private void initGui(Computer computer) {
        add(compPanel);

        txtName.setText(computer.getName());

        btnOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!validateInput()) {
                    return;
                }

                ResultObject resultObject = new ResultObject();
                resultObject.setName(txtName.getText());
                setUserInput(resultObject);
                closeDialog();
            }
        });

        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setUserInput(null);
                closeDialog();
            }
        });
        pack();
    }

    private boolean validateInput() {
        boolean ok = true;
        if (StringUtils.isEmpty(txtName.getText())) {
            txtName.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (NetbeansWindowHelper.getInstance().getActiveTopComponentTopology() == null) {
                return false;
            }
            if (!NetbeansWindowHelper.getInstance().getActiveTopComponentTopology().getVertexFactory().isVertexNameUnique(txtName.getText())) {//computer name must be unique
                JOptionPane.showMessageDialog(this, NbBundle.getMessage(RouterConfigurationDialog.class, "duplicity.name.text"), NbBundle.getMessage(RouterConfigurationDialog.class, "duplicity.name.title"), JOptionPane.ERROR_MESSAGE);
                txtName.setBackground(new Color(249, 77, 77));
                ok = false;
            } else {
                txtName.setBackground(UIManager.getColor("JXTextField.background"));
            }
        }
        return ok;
    }

    /**
     * object that stores users input
     */
    public static class ResultObject {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
