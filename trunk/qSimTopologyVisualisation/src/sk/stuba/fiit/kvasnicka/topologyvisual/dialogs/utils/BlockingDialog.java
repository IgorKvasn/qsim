package sk.stuba.fiit.kvasnicka.topologyvisual.dialogs.utils;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * BlockingDialog blocks flow of the program and waits until user closes dialog
 * It is used to wait for user's input
 *
 * @author Igor Kvasnicka
 */
public abstract class BlockingDialog<R> extends JDialog {
    /**
     * here is stored user input
     */
    private R userInput;

    /**
     * creates new instance of JDialog
     * this dialog cannot be closed by clicking on "close" button in upper bar - so do not forget to create your own Close/Cancel button
     *
     * @param owner owner of this dialog
     */
    protected BlockingDialog(JFrame owner) {
        super(owner, true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    /**
     * shows dialog - blocking operation
     */
    public void showDialog() {
        setLocationRelativeTo(super.getOwner());
        setVisible(true);
    }


    /**
     * method that returns user input data
     *
     * @return any object according to implementation
     */
    public R getUserInput() {
        return userInput;
    }

    /**
     * sets userInput property
     * this property will be returned as a result of this dialog
     *
     * @param userInput user input
     */
    public void setUserInput(R userInput) {
        this.userInput = userInput;
    }

    /**
     * closes dialog after
     * <b>NOTE:</b> this method should be called after the <i>userInput</i> property is being set
     */
    protected void closeDialog() {
        this.setVisible(false);
        this.dispose();
    }
}
