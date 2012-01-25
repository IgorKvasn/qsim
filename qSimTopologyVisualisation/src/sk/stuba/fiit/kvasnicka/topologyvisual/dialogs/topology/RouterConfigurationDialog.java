package sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.topology;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils.BlockingDialog;
import sk.stuba.fiit.kvasnicka.topologyvisual.graph.utils.TopologyVertexFactory;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.NetbeansWindowHelper;

/**
 * provides dialog for initial configuration of router
 *
 * @author Igor Kvasnicka
 */
public class RouterConfigurationDialog extends BlockingDialog<RouterConfigurationDialog.ResultObject> {

    private static Logger logg = Logger.getLogger(RouterConfigurationDialog.class);
    private Router router;
    private JXTextField txtName;

    /**
     * creates new instance of dialog
     *
     * @param router Router object that is being configured; this object is
     * read-only
     */
    public RouterConfigurationDialog(Router router) {
        super(null);
        setTitle(NbBundle.getMessage(RouterConfigurationDialog.class, "create.new.router"));
        this.router = router;

        initGui();
    }

    private void initGui() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        add(centerPanel, BorderLayout.CENTER);
        txtName = new JXTextField();
        txtName.setColumns(10);
        txtName.setText(router.getName());
        JXLabel lblName = new JXLabel(NbBundle.getMessage(RouterConfigurationDialog.class, "name"));
        lblName.setLabelFor(txtName);
        centerPanel.add(lblName);
        centerPanel.add(txtName);

        JPanel bottomPanel = new JPanel();
        JButton btnOk = new JButton(NbBundle.getMessage(RouterConfigurationDialog.class, "ok"));
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
        JButton btnCancel = new JButton(NbBundle.getMessage(RouterConfigurationDialog.class, "cancel"));
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setUserInput(null);
                closeDialog();
            }
        });
        bottomPanel.add(btnOk);
        bottomPanel.add(btnCancel);


        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
    }

    private boolean validateInput() {
        boolean ok = true;
        if (StringUtils.isEmpty(txtName.getText())) {
            txtName.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (NetbeansWindowHelper.getInstance().getActiveTopology() == null) {
                return false;
            }
            if (!NetbeansWindowHelper.getInstance().getActiveTopology().getVertexFactory().isVertexNameUnique(txtName.getText())) {//router name must be unique
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
