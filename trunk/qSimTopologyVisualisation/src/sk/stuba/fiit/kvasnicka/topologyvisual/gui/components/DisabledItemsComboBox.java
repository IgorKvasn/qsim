package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class DisabledItemsComboBox extends JComboBox {

    public DisabledItemsComboBox() {
        super();
        super.setRenderer(new DisabledItemsComboBox.DisabledItemsRenderer());
    }
    private Set<Integer> disabled_items = new TreeSet<Integer>();

    public void addItem(Object anObject, boolean enabled) {
        super.addItem(anObject);
        if (!enabled) {
            disabled_items.add(getItemCount() - 1);
        }
    }

    @Override
    public void removeAllItems() {
        super.removeAllItems();
        disabled_items = new HashSet();
    }

    @Override
    public void removeItemAt(final int anIndex) {
        super.removeItemAt(anIndex);
        disabled_items.remove(anIndex);
    }

    @Override
    public void removeItem(final Object anObject) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemAt(i) == anObject) {
                disabled_items.remove(i);
            }
        }
        super.removeItem(anObject);
    }

    public boolean isItemEnabled(int index) {
        return !disabled_items.contains(index);
    }

    /**
     * sets desired item as enabled/disabled if item should become disabled and
     * it is currently selected, first enabled item will be selected instead <p>
     * <b>warning:</b> case when all items are disabled is not taken care of, so there must always be at least one item enabled
     *
     * @param index
     * @param enabled
     */
    public void setItemEnabled(int index, boolean enabled) {
        if (index > getItemCount() - 1) {
            throw new IllegalArgumentException("invalid item number: " + index + "; there are only " + getItemCount() + " items");
        }
        disabled_items.add(index);
        if (getSelectedIndex() == index) {//item is selected
            for (int i = 0; i < getItemCount(); i++) {
                if (!disabled_items.contains(i)) {//I have found some enabled item
                    setSelectedIndex(i);
                    break;
                }
            }
        }
        repaint();
    }

    @Override
    public void setSelectedIndex(int index) {
        if (!disabled_items.contains(index)) {
            super.setSelectedIndex(index);
        }
    }

    private class DisabledItemsRenderer extends BasicComboBoxRenderer {

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (disabled_items.contains(index)) {
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
