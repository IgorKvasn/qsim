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
package sk.stuba.fiit.kvasnicka.topologyvisual.gui.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXSearchField;

/**
 *
 * @author Igor Kvasnicka
 */
public class DropDownButton extends JButton implements ActionListener {

  
    private JPopupMenu popup = new JPopupMenu();
    private List<JCheckBox> checkBoxMenuItems = new LinkedList<JCheckBox>();
    private JXSearchField searchField;
    private JLabel emptyLabel = new JLabel("No results");
    private JPanel mainPanel = new JPanel();
    private JPanel checkPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane();
    private boolean firstTime = true;
    private boolean isSelected = false;
    private GeneralPath arrow;
    
    public DropDownButton() {
        init(true);
    }

    public DropDownButton(boolean searchFieldEnabled) {
        init(searchFieldEnabled);
    }

    private void init(boolean searchFieldEnabled) {

        addActionListener(this);
        mainPanel.setLayout(new BorderLayout());


        JPanel northPanel = new JPanel(new BorderLayout());
        if (searchFieldEnabled) {
            addSearchField(northPanel);
        }
        final JCheckBox chSelectAll = new JCheckBox("All");
        chSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(chSelectAll.isSelected());
            }
        });
        chSelectAll.setAlignmentX(LEFT_ALIGNMENT);

        northPanel.add(chSelectAll, BorderLayout.CENTER);
        northPanel.setAlignmentX(LEFT_ALIGNMENT);
        northPanel.setBackground(Color.red);
        mainPanel.add(northPanel, BorderLayout.NORTH);


        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.PAGE_AXIS));
        scrollPane.setViewportView(checkPanel);
        scrollPane.setPreferredSize(new Dimension(200, 100));

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        popup.add(mainPanel);




    }

    private void addSearchField(JPanel northPanel) {
        searchField = new JXSearchField("Filter");
        searchField.setColumns(7);
        searchField.setAlignmentX(LEFT_ALIGNMENT);
        searchField.setSearchMode(JXSearchField.SearchMode.INSTANT);
        searchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                filterCheckBoxList(e.getActionCommand());
            }
        });
        northPanel.add(searchField, BorderLayout.LINE_START);
    }

    private void selectAll(boolean select) {
        for (JCheckBox item : checkBoxMenuItems) {
            item.setSelected(select);
        }
    }

    private void filterCheckBoxList(String text) {
        checkPanel.remove(emptyLabel);

        if (StringUtils.isEmpty(text)) {
            for (JCheckBox item : checkBoxMenuItems) {
                checkPanel.remove(item);//removes item, so no duplicate will occure
                checkPanel.add(item);//add new item
            }

            mainPanel.revalidate();
            mainPanel.repaint();

            popup.revalidate();
            popup.repaint();

            return;
        }
        boolean empty = true;
        for (JCheckBox item : checkBoxMenuItems) {
            checkPanel.remove(item);//removes item, so no duplicate will occure

            if (item.getText().toLowerCase().contains(text.toLowerCase())) {
                empty = false;
                checkPanel.add(item);//add new item
            }
        }
        if (empty) {
            checkPanel.add(emptyLabel);
        }


        mainPanel.revalidate();
        mainPanel.repaint();

        popup.revalidate();
        popup.repaint();
    }

    /**
     * returns labels of all selected JCheckBoxMenuItem
     *
     * @return
     */
    public List<String> getSelectedCheckBoxItems() {
        List<String> list = new LinkedList<String>();
        for (JCheckBox item : checkBoxMenuItems) {
            if (item.isSelected()) {
                list.add(item.getText());
            }
        }
        return list;
    }

    /**
     * adds new JCheckBoxMenuItem to the popup menu
     *
     * @param label
     */
    public void addCheckBoxMenuItem(String label) {
        JCheckBox chbox = new JCheckBox(label);
        checkPanel.add(chbox);
        checkBoxMenuItems.add(chbox);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        popup.show(this, 0, getHeight());
    }

    public class StayOpenCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

        @Override
        protected void doClick(MenuSelectionManager msm) {
            menuItem.doClick(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        Font font = UIManager.getFont("Button.font");
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = font.getStringBounds(super.getText(), frc);
        float sx = 5f;
        float sy = (float) ((h + r.getHeight()) / 2) - font.getLineMetrics(super.getText(), frc).getDescent();
        g2.drawString(super.getText(), sx, sy);
        double x = sx + r.getWidth() + sx;
        if (isSelected) {
            g2.setPaint(Color.gray);
            g2.draw(new Line2D.Double(x, 0, x, h));
            g2.setPaint(Color.white);
            g2.draw(new Line2D.Double(x + 1, 0, x + 1, h));
            g2.setPaint(Color.gray);
            g2.draw(new Rectangle2D.Double(0, 0, getSize().width - 1, h - 1));
        }
        float ax = (float) (x + sx);
        if (firstTime) {
            createArrow(ax, h);
        }
        g2.setPaint(UIManager.getColor("Menu.foreground"));
        g2.fill(arrow);
        ax += 10f + sx;
        if (firstTime) {
            setSize((int) ax, h);         // initial sizing
            setPreferredSize(getSize());
            setMaximumSize(getSize());   // resizing behavior
            firstTime = false;
        }
    }

    private void createArrow(float x, int h) {
        arrow = new GeneralPath();
        arrow.moveTo(x - 4, h / 2.5f);
        arrow.lineTo(x - 4 + 6f, h / 2.5f);
        arrow.lineTo(x - 4 + 3f, h * 2 / 3f);
        arrow.closePath();
    }
}
