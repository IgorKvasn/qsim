package sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.topology;

import org.jdesktop.swingx.JXLabel;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.text.NumberFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.Edge;
import sk.stuba.fiit.kvasnicka.topologyvisual.gui.dialogs.utils.BlockingDialog;

/**
 * provides dialog for initial configuration of edge
 *
 * @author Igor Kvasnicka
 */
public class EdgeConfigurationDialog extends BlockingDialog<EdgeConfigurationDialog.ResultObject> {

    private Edge edge;
    private JFormattedTextField txtSpeed;
    private JFormattedTextField txtLength;

    /**
     * creates new instance of dialog
     *
     * @param edge      Edge object that is being configured; this object is read-only
     */
    public EdgeConfigurationDialog(Edge edge) {
        super(null);
        setTitle(NbBundle.getMessage(EdgeConfigurationDialog.class,"create.new.edge"));
        this.edge = edge;

        initGui();
    }

    private void initGui() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        add(centerPanel, BorderLayout.CENTER);

        JPanel speedPanel = new JPanel();

        DecimalFormat decimalFormat = new DecimalFormat("#");
        NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
        txtSpeed = new JFormattedTextField(numberFormatter);
        txtSpeed.setColumns(10);
        try {
            txtSpeed.setText(String.valueOf(edge.getSpeed()));
        } catch (IllegalStateException e) {
            txtSpeed.setText("");
        }
        txtSpeed.setToolTipText(NbBundle.getMessage(EdgeConfigurationDialog.class, "bitrate.must.be.decimal.number"));

        JXLabel lblSpeed = new JXLabel(NbBundle.getMessage(EdgeConfigurationDialog.class,"bitrate.bit.s"));
        lblSpeed.setLabelFor(txtSpeed);
        speedPanel.add(lblSpeed);
        speedPanel.add(txtSpeed);
        centerPanel.add(speedPanel);


        JPanel lengthPanel = new JPanel();
        txtLength = new JFormattedTextField(numberFormatter);
        txtLength.setColumns(10);
        try {
            txtLength.setText(String.valueOf(edge.getLength()));
        } catch (IllegalStateException e) {
            txtLength.setText("");
        }
        txtLength.setToolTipText(NbBundle.getMessage(EdgeConfigurationDialog.class,"length.must.be.decimal.number"));

        JXLabel lblLength = new JXLabel(NbBundle.getMessage(EdgeConfigurationDialog.class,"length.m"));
        lblLength.setLabelFor(txtLength);
        lengthPanel.add(lblLength);
        lengthPanel.add(txtLength);
        centerPanel.add(lengthPanel);


        JPanel bottomPanel = new JPanel();
        JButton btnOk = new JButton(NbBundle.getMessage(EdgeConfigurationDialog.class,"ok"));
        btnOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!validateInput()) {
                    return;
                }

                ResultObject resultObject = new ResultObject(Long.valueOf(txtSpeed.getText()), Integer.valueOf(txtLength.getText()));
                //resultObject.setName(txtName.getText());
                setUserInput(resultObject);
                closeDialog();
            }
        });
        JButton btnCancel = new JButton(NbBundle.getMessage(EdgeConfigurationDialog.class,"cancel"));
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
        if (!StringUtils.isNumeric(txtSpeed.getText()) || StringUtils.isEmpty(txtSpeed.getText())) { //speed is numeric and non-empty
            txtSpeed.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (Long.valueOf(txtSpeed.getText()) <= 0) {//speed is not negative nor zero
                txtSpeed.setBackground(new Color(249, 77, 77));
                ok = false;
            } else {
                txtSpeed.setBackground(UIManager.getColor("JFormattedTextField.background"));
            }
        }

        if (!StringUtils.isNumeric(txtLength.getText()) || StringUtils.isEmpty(txtLength.getText())) { //length is numeric and non-empty
            txtLength.setBackground(new Color(249, 77, 77));
            ok = false;
        } else {
            if (Long.valueOf(txtLength.getText()) <= 0) {//length is not negative nor zero
                txtLength.setBackground(new Color(249, 77, 77));
                ok = false;
            } else {
                txtLength.setBackground(UIManager.getColor("JFormattedTextField.background"));
            }
        }


        return ok;
    }

    public class ResultObject {

        private long speed;
        private int length;

        public ResultObject(long speed, int length) {
            this.speed = speed;
            this.length = length;
        }

        public long getSpeed() {
            return speed;
        }

        public int getLength() {
            return length;
        }
    }
}
