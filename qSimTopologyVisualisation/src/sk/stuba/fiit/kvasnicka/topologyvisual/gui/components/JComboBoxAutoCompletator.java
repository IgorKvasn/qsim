/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components;

/**
 *
 * @author Igor Kvasnicka
 */
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/*
 * Tomado de http://tech.chitgoks.com/2009/11/06/autocomplete-jcombobox/
 */
public final class JComboBoxAutoCompletator extends PlainDocument implements FocusListener, KeyListener, PropertyChangeListener {

    private JComboBox comboBox;
    private ComboBoxModel model;
    private JTextComponent editor;
    private boolean hidePopupOnFocusLoss;

    public JComboBoxAutoCompletator() {
// Bug 5100422 on Java 1.5: Editable JComboBox won’t hide popup when tabbing out
        hidePopupOnFocusLoss = System.getProperty(
                "java.version").startsWith("1.5");
    }

    public JComboBoxAutoCompletator(JComboBox jcb) {
        this();
        registerComboBox(jcb);
    }

    public void registerComboBox(JComboBox jcb) {
        desregisterComboBox();
        this.comboBox = jcb;
        comboBox.setEditable(true);
        model = comboBox.getModel();
        editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        editor.setDocument(this);
// Highlight whole text when focus gets lost
        editor.addFocusListener(this);
// Highlight whole text when user hits enter
        editor.addKeyListener(this);
        comboBox.addPropertyChangeListener(this);
// Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected != null) {
            editor.setText(selected.toString());
        } else {
            editor.setText("");
        }
    }

    public void desregisterComboBox() {
        if (comboBox != null) {
            comboBox.getEditor().getEditorComponent().removeFocusListener(this);
            comboBox.getEditor().getEditorComponent().removeKeyListener(this);
            comboBox.removePropertyChangeListener(this);
            comboBox.setSelectedItem(null);
            comboBox = null;
        }
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
// construct the resulting string
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(offs, currentText.length());
        String futureText = beforeOffset + str + afterOffset;
// lookup and select a matching item
        Object item = lookupItem(futureText);
        if (item != null) {
            comboBox.setSelectedItem(item);
        } else {
// keep old item selected if there is no match
            item = comboBox.getSelectedItem();
// imitate no insert (later on offs will be incremented by str.length(): selection won’t move forward)
            offs = offs - str.length();
// provide feedback to the user that his input has been received but can not be accepted
            comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
        }

// remove all text and insert the completed string
        super.remove(0, getLength());
        super.insertString(0, item.toString(), a);

// if the user selects an item via mouse the the whole string will be inserted.
// highlight the entire text if this happens.
        if (item.toString().equals(str) && offs == 0) {
            highlightCompletedText(0);
        } else {
            highlightCompletedText(offs + str.length());
// show popup when the user types
            if (comboBox.isShowing() && comboBox.isFocusOwner()) {
                comboBox.setPopupVisible(true);
            }
        }
    }

    private void highlightCompletedText(int start) {
        editor.setCaretPosition(getLength());
        editor.moveCaretPosition(start);
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
// only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        } else {
// iterate over all items
            for (int i = 0, n = model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
// current item starts with the pattern?
                if (startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
// no item starts with the pattern => return null
        return null;
    }

    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        highlightCompletedText(0);
// Workaround for Bug 5100422 – Hide Popup on focus loss
        if (hidePopupOnFocusLoss) {
            comboBox.setPopupVisible(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            highlightCompletedText(0);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            comboBox.setSelectedIndex(0);
            editor.setText(comboBox.getSelectedItem().toString());
            highlightCompletedText(0);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("model")) {
            registerComboBox(comboBox);
        }
    }
}