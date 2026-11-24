package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class ToothSelectionDialog extends JDialog {
    private static final int TOOTH_SIZE = 20;
    private static final int TOOTH_GAP = 5;
    private int teethCountHorizontal = 16; // По умолчанию коренные зубы
    private Set<Point> selectedTeeth = new HashSet<>();
    private String selectedToothNumber = "";

    public ToothSelectionDialog(JFrame parent) {
        super(parent, "Выбор зуба", true);
        setLayout(new BorderLayout());
        setSize(450, 200);
        setLocationRelativeTo(parent);

        // Панель для отображения зубов
        ToothPanel toothPanel = new ToothPanel();
        add(toothPanel, BorderLayout.CENTER);

        // Панель управления
        JPanel controlPanel = new JPanel();
        JButton toggleTeethButton = new JButton("Молочные/Коренные");
        toggleTeethButton.setForeground(Color.WHITE);
        toggleTeethButton.setBackground(new Color(119, 155, 191));
        toggleTeethButton.addActionListener(e -> {
            teethCountHorizontal = (teethCountHorizontal == 16) ? 10 : 16; // Переключаем между молочными и коренными
            toothPanel.setTeethCountHorizontal(teethCountHorizontal);
            toothPanel.repaint();
            pack();
        });

        JButton confirmButton = new JButton("Выбрать");
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(new Color(119, 155, 191));
        confirmButton.addActionListener(e -> {
            if (!selectedTeeth.isEmpty()) {
                // Получаем номер зуба
                Point firstTooth = selectedTeeth.iterator().next();
                selectedToothNumber = getToothNumber(firstTooth.x, firstTooth.y);
                dispose(); // Закрываем диалог
            } else {
                JOptionPane.showMessageDialog(this, "Выберите зуб!");
            }
        });

        controlPanel.add(toggleTeethButton);
        controlPanel.add(confirmButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Обработчик кликов по зубам
        toothPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int xIndex = e.getX() / (TOOTH_SIZE + TOOTH_GAP);
                int yIndex = e.getY() / (TOOTH_SIZE + TOOTH_GAP);
                int xRemainder = e.getX() % (TOOTH_SIZE + TOOTH_GAP);
                int yRemainder = e.getY() % (TOOTH_SIZE + TOOTH_GAP);

                // Проверяем, что клик был внутри зуба
                if (xRemainder < TOOTH_SIZE && yRemainder < TOOTH_SIZE) {
                    Point point = new Point(xIndex, yIndex);
                    selectedTeeth.clear(); // Очищаем предыдущий выбор
                    selectedTeeth.add(point); // Добавляем новый выбранный зуб
                    toothPanel.repaint();
                }
            }
        });
    }

    // Метод для получения номера зуба
    private String getToothNumber(int x, int y) {
        boolean isPrimary = teethCountHorizontal == 10; // Молочные зубы
        int quadrant;
        int toothNumber;

        if (y == 0) { // Верхняя челюсть
            quadrant = x < teethCountHorizontal / 2 ? (isPrimary ? 5 : 1) : (isPrimary ? 6 : 2);
            toothNumber = x < teethCountHorizontal / 2 ? (teethCountHorizontal / 2 - x) : (x - teethCountHorizontal / 2 + 1);
        } else { // Нижняя челюсть
            quadrant = x < teethCountHorizontal / 2 ? (isPrimary ? 8 : 4) : (isPrimary ? 7 : 3);
            toothNumber = x < teethCountHorizontal / 2 ? (teethCountHorizontal / 2 - x) : (x - teethCountHorizontal / 2 + 1);
        }

        return quadrant + "" + toothNumber;
    }

    // Метод для получения выбранного номера зуба
    public String getSelectedToothNumber() {
        return selectedToothNumber;
    }

    // Панель для отображения зубов
    private class ToothPanel extends JPanel {
        public ToothPanel() {
            setPreferredSize(new Dimension(teethCountHorizontal * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_GAP, 2 * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_SIZE));
        }

        public void setTeethCountHorizontal(int count) {
            teethCountHorizontal = count;
            setPreferredSize(new Dimension(teethCountHorizontal * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_GAP, 2 * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_SIZE));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < teethCountHorizontal; i++) {
                for (int j = 0; j < 2; j++) {
                    int x = TOOTH_GAP + i * (TOOTH_SIZE + TOOTH_GAP);
                    int y = TOOTH_GAP + j * (TOOTH_SIZE + TOOTH_GAP);
                    Point point = new Point(i, j);

                    // Закрашиваем выбранный зуб
                    if (selectedTeeth.contains(point)) {
                        g.setColor(Color.GREEN);
                        g.fillRect(x, y, TOOTH_SIZE, TOOTH_SIZE);
                    }

                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, TOOTH_SIZE, TOOTH_SIZE);

                    // Отображаем номер зуба
                    String toothNumber = getToothNumber(i, j);
                    g.drawString(toothNumber, x + TOOTH_SIZE / 4, y + TOOTH_SIZE / 2);
                }
            }
        }
    }
}
