package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SelectedListCellRenderer extends JLabel implements ListCellRenderer<String> {

    private static final Color SELECTED_COLOR = Color.BLACK;
    private static final Color UNSELECTED_COLOR = Color.WHITE;

    public SelectedListCellRenderer() {
        setOpaque(true);
        setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value);

        if (isSelected) {
            setBackground(SELECTED_COLOR);
            setForeground(Color.WHITE);
        } else {
            setBackground(UNSELECTED_COLOR);
            setForeground(Color.BLACK);
        }

        return this;
    }
}