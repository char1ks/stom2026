package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class ToothFormulaFrame extends JFrame {


    private static final int TOOTH_SIZE = 20;
    private static final int TOOTH_GAP = 5;
    private int teethCountHorizontal = 16;
    private boolean paintingMode = false;
    private Set<Point> currentPaintedTeeth;
    private int currentKey = 1;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/teeth_formula";
    private static  String USER = "root";
    private static  String PASS = "lesha130709";

    public ToothFormulaFrame() {

        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при установки соединения");
            throw new RuntimeException(e);
        }
            USER=proper.getProperty("database.login");
            PASS=proper.getProperty("database.pass");
        setTitle("Формула постоянных зубов");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setResizable(false);
        ToothPanel toothPanel = new ToothPanel();
        add(toothPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JTextField keyField = new JTextField(5);
        JButton paintButton = new JButton("Закрасить зуб");
        JButton saveButton = new JButton("Сохранить набор");
        JButton loadButton = new JButton("Загрузить набор");

        paintButton.setForeground(Color.white);
        paintButton.setBackground(new Color(119, 155, 191));
        saveButton.setForeground(Color.white);
        saveButton.setBackground(new Color(119, 155, 191));
        loadButton.setForeground(Color.white);
        loadButton.setBackground(new Color(119, 155, 191));

        paintButton.addActionListener(e -> {
            paintingMode = !paintingMode;
            paintButton.setText(paintingMode ? "Выключить режим закрашивания" : "Закрасить зуб");
        });

        saveButton.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText());
                saveTeethSetToDB(key, currentPaintedTeeth);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректный ключ (целое число).");
            }
        });

        loadButton.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText());
                currentKey = key;
                currentPaintedTeeth = loadTeethSetFromDB(currentKey);
                toothPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректный ключ (целое число).");
            }
        });

        controlPanel.add(paintButton);
        controlPanel.add(new JLabel("Ключ набора:"));
        controlPanel.add(keyField);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        add(controlPanel, BorderLayout.NORTH);

        currentPaintedTeeth = new HashSet<>();
        pack();
        setLocationRelativeTo(null);
        
        // Увеличиваем ширину окна, чтобы все зубы помещались
        Dimension currentSize = getSize();
        setSize(currentSize.width + 50, currentSize.height);
        JButton toggleTeethCountButton = new JButton("Молочные");
        toggleTeethCountButton.setForeground(Color.white);
        toggleTeethCountButton.setBackground(new Color(119, 155, 191));

// Set a smaller font for the button
        toggleTeethCountButton.setFont(new Font("Arial", Font.PLAIN, 10));

// Set a preferred size for the button to make it narrower
        toggleTeethCountButton.setPreferredSize(new Dimension(145, toggleTeethCountButton.getPreferredSize().height));

        toggleTeethCountButton.addActionListener(e -> {
            if (teethCountHorizontal == 16) {
                teethCountHorizontal = 10;
                setTitle("Формула молочных зубов");
                toggleTeethCountButton.setText("Коренные");
            } else {
                teethCountHorizontal = 16;
                setTitle("Формула постоянных зубов");
                toggleTeethCountButton.setText("Молочные");
            }
            toothPanel.setTeethCountHorizontal(teethCountHorizontal);
            toothPanel.repaint();
            pack();
        });
        controlPanel.add(toggleTeethCountButton);
        add(controlPanel, BorderLayout.NORTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(toggleTeethCountButton, BorderLayout.NORTH); // Add the button to the top of the right panel

// Add the right panel to the frame
        add(rightPanel, BorderLayout.EAST);
    }


    private void saveTeethSetToDB(int key, Set<Point> teethSet) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement("REPLACE INTO teeth_sets (`key`, `teeth_data`) VALUES (?, ?)")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(teethSet);
            byte[] teethSetBytes = baos.toByteArray();

            pstmt.setInt(1, key);
            pstmt.setBytes(2, teethSetBytes);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null,"Набор успешно сохранен!");
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Point> loadTeethSetFromDB(int key) {
        Set<Point> teethSet = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement("SELECT `teeth_data` FROM teeth_sets WHERE `key` = ?")) {
            pstmt.setInt(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                byte[] teethSetBytes = rs.getBytes("teeth_data");
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(teethSetBytes));
                teethSet = (Set<Point>) ois.readObject();
            }
        } catch (SQLException | IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return teethSet;
    }
    private String getToothNumber(int x, int y) {
        boolean isPrimary = getTitle().equals("Формула молочных зубов");
        int quadrant;
        int toothNumber;
        if (y == 0) { // Верхняя челюсть
            quadrant = x < teethCountHorizontal / 2 ? (isPrimary ? 5 : 1) : (isPrimary ? 6 : 2);
            toothNumber = x < teethCountHorizontal / 2 ? (teethCountHorizontal / 2 - x) : (x - teethCountHorizontal / 2 + 1);
        } else { // Нижняя челюсть
            quadrant = x < teethCountHorizontal / 2 ? (isPrimary ? 8 : 4) : (isPrimary ? 7 : 3);
            if (isPrimary) {
                toothNumber = x < teethCountHorizontal / 2 ? (5 - x) : (x - 5 + 1);
            } else {
                toothNumber = x < teethCountHorizontal / 2 ? (8 - x) : (x - 8 + 1);
            }
        }
        // Для молочных зубов уменьшаем номер зуба, так как их всего 5 в квадранте
        if (isPrimary && toothNumber > 5) {
            return ""; // Возвращаем пустую строку, если зуба нет (например, 6-й зуб в молочной челюсти)
        }
        // Преобразуем квадрант и номер зуба в двузначный номер зуба
        int toothCode = quadrant * 10 + toothNumber;
        return String.valueOf(toothCode);
    }

    private class ToothPanel extends JPanel {
        public ToothPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (paintingMode) {
                        int xIndex = e.getX() / (TOOTH_SIZE + TOOTH_GAP);
                        int yIndex = e.getY() / (TOOTH_SIZE + TOOTH_GAP);
                        int xRemainder = e.getX() % (TOOTH_SIZE + TOOTH_GAP);
                        int yRemainder = e.getY() % (TOOTH_SIZE + TOOTH_GAP);

                        // Проверяем, что клик был внутри зуба, а не на промежутке
                        if (xRemainder < TOOTH_SIZE && yRemainder < TOOTH_SIZE) {
                            Point point = new Point(xIndex, yIndex);
                            if (currentPaintedTeeth.contains(point)) {
                                currentPaintedTeeth.remove(point);
                            } else {
                                currentPaintedTeeth.add(point);
                            }
                            repaint();
                        }
                    }
                }
            });
        }
        public void setTeethCountHorizontal(int count) {
            teethCountHorizontal = count;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < teethCountHorizontal; i++) {
                for (int j = 0; j < 2; j++) {
                    int x = TOOTH_GAP + i * (TOOTH_SIZE + TOOTH_GAP);
                    int y = TOOTH_GAP + j * (TOOTH_SIZE + TOOTH_GAP);
                    Point point = new Point(i, j);
                    if (currentPaintedTeeth.contains(point)) {
                        g.setColor(Color.GREEN);
                        g.fillRect(x, y, TOOTH_SIZE, TOOTH_SIZE);
                    }
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, TOOTH_SIZE, TOOTH_SIZE);

                    // Отображение номера зуба
                    String toothNumber = getToothNumber(i, j);
                    g.drawString(toothNumber, x + TOOTH_SIZE / 4, y + TOOTH_SIZE / 2);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int width = teethCountHorizontal * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_GAP;
            int height = 2 * (TOOTH_SIZE + TOOTH_GAP) + TOOTH_SIZE;
            return new Dimension(width, height);
        }
    }

}
