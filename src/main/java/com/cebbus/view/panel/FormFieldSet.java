package com.cebbus.view.panel;

import com.cebbus.view.chart.ColorPalette;

import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.table.TableColumn;
import java.awt.*;

public class FormFieldSet {

    protected Box createTitleLabelBox(String text, int width, int height) {
        Box box = Box.createHorizontalBox();
        setSize(box, width, height);

        box.add(createTitleLabel(text));
        return box;
    }

    protected JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ColorPalette.BLUE);
        label.setIcon(BasicIconFactory.getMenuArrowIcon());

        return label;
    }

    protected JLabel createThinLabel() {
        JLabel label = new JLabel();

        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        return label;
    }

    protected JLabel createThinLabel(String text) {
        JLabel label = createThinLabel();
        label.setText(text);

        return label;
    }

    protected void setSize(JComponent component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
    }

    protected void addToForm(Box container, JLabel label, JComponent component, int width) {
        setSize(label, 75, 20);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        setSize(component, width - 75, 20);
        component.setAlignmentX(Component.RIGHT_ALIGNMENT);

        Box box = Box.createHorizontalBox();
        setSize(box, width, 20);
        box.add(label);
        box.add(component);

        container.add(box);
        container.add(Box.createVerticalStrut(2));
    }

    protected void setColumnSize(TableColumn column, int width) {
        column.setWidth(width);
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setPreferredWidth(width);
    }
}
