package GUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class medicineSelect2 extends JFrame {
    private final int WIDTH = 450;
    private final int HEIGHT = 550;
    private final JPanel panel;
    private JList<String> areaMedicine;
    private DefaultListModel<String> medicineList;
    private int sumOfmoney;
    private DefaultListModel<Integer> priceList;
    private List<String> selectedValues;
    private static JLabel sumValueLabel;
    private static JTextField searchField;
    private static String resultMedicine = "";
    private static final StringBuilder build = new StringBuilder();

    medicineSelect2() {
        setResizable(false);
        setTitle("Выбор услуг");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        panel = new JPanel();
        getContentPane().add(panel);
        placeComponents(panel);
        setVisible(true);
        setResizable(false);
    }


    private void placeComponents(JPanel panel) {
        panel.setLayout(null);
        medicineList = new DefaultListModel<>();
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при установки соединения");
                throw new RuntimeException(e);
            }
            try {
                Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM resources.medicine");
                while (resultSet.next()) {
                    medicineList.addElement(resultSet.getString("medicine"));
                }
                connection.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных");
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при установки соединения");
            throw new RuntimeException(e);
        }
        JLabel sumLabel = new JLabel("Сумма:");
        sumLabel.setBounds(200, 10, 120, 25);
        panel.add(sumLabel);

        sumValueLabel = new JLabel("0");
        sumValueLabel.setBounds(214, 20, 120, 25);
        panel.add(sumValueLabel);

        areaMedicine = new JList<>(medicineList);
        areaMedicine.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Обработчик двойного нажатия
        areaMedicine.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Двойное нажатие
                    String selectedMedicine = areaMedicine.getSelectedValue();
                    if (selectedMedicine != null) {
                        try {
                            Properties proper = new Properties();
                            try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                                proper.load(input);
                            } catch (Exception exc) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                            }
                            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                                Statement statement = connection.createStatement();
                                ResultSet resultSet = statement.executeQuery("SELECT price FROM resources.medicine WHERE medicine='" + selectedMedicine + "'");
                                if (resultSet.next()) {
                                    int price = resultSet.getInt("price");
                                    sumOfmoney += price;
                                    sumValueLabel.setText(String.valueOf(sumOfmoney));
                                }
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при получении данных");
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(areaMedicine);
        priceList = new DefaultListModel<>();
        scroll.setBounds(0, 90, WIDTH, HEIGHT - 90);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(scroll);

        JButton redactSome = new JButton("\uD83D\uDCDD");
        redactSome.setBounds(275, 15, 20, 25);
        redactSome.setForeground(Color.WHITE);
        redactSome.setBackground(new Color(119, 155, 191));
        redactSome.setBorder(BorderFactory.createLineBorder(Color.black));
        redactSome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (areaMedicine.getSelectedValue() != null) {
                    Properties proper = new Properties();
                    try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                    }
                    try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));) {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM resources.medicine WHERE medicine='" + areaMedicine.getSelectedValue() + "'");
                        if (resultSet.next()) {
                            String code = resultSet.getString("code");
                            String nameMedicine = resultSet.getString("medicine");
                            String price = resultSet.getString("price");
                            showDetailsAboutService(code, nameMedicine, price);
                        } else {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка,услуга не найдена");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Вы не выбрали значение");
                }
            }
        });
        panel.add(redactSome);

        JButton addCons = new JButton("Добавить расходник");
        addCons.setBounds(10, 10, 130, 25);
        addCons.setForeground(Color.WHITE);
        addCons.setBackground(new Color(119, 155, 191));
        addCons.setBorder(BorderFactory.createLineBorder(Color.black));
        addCons.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame addMedicine = new JFrame();
                addMedicine.setSize(290, 300);
                addMedicine.setResizable(false);
                addMedicine.setVisible(true);
                addMedicine.setLocationRelativeTo(null);

                JPanel writeMedicine = new JPanel();
                addMedicine.getContentPane().add(writeMedicine);
                writeMedicine.setLayout(null);

                JLabel codeLabel = new JLabel("Код:");
                codeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                codeLabel.setBounds(20, 30, 120, 25);
                writeMedicine.add(codeLabel);

                JTextField codeField = new JTextField();
                codeField.setBounds(100, 30, 125, 25);
                codeField.setBorder(BorderFactory.createLineBorder(Color.black));
                writeMedicine.add(codeField);

                JLabel nameOFmedicine = new JLabel("Услуга:");
                nameOFmedicine.setFont(new Font("Arial", Font.PLAIN, 16));
                nameOFmedicine.setBounds(20, 70, 120, 25);
                writeMedicine.add(nameOFmedicine);

                JTextField nameOFservice = new JTextField();
                nameOFservice.setBounds(100, 70, 125, 25);
                nameOFservice.setBorder(BorderFactory.createLineBorder(Color.black));
                writeMedicine.add(nameOFservice);

                JLabel PriceOfMedicine = new JLabel("Цена:");
                PriceOfMedicine.setFont(new Font("Arial", Font.PLAIN, 16));
                PriceOfMedicine.setBounds(20, 110, 120, 25);
                writeMedicine.add(PriceOfMedicine);

                JTextField priceField = new JTextField();
                priceField.setBounds(100, 110, 125, 25);
                priceField.setBorder(BorderFactory.createLineBorder(Color.black));
                writeMedicine.add(priceField);

                JButton sendData = new JButton("Создать");
                sendData.setBounds(90, 180, 125, 25);
                sendData.setForeground(Color.WHITE);
                sendData.setBackground(new Color(119, 155, 191));
                sendData.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                sendData.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Properties proper = new Properties();
                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                            proper.load(input);
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                            throw new RuntimeException(exc);
                        }
                        if (!(priceField.getText().isEmpty() || nameOFservice.getText().isEmpty() || codeField.getText().isEmpty())) {
                            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                                String query = "INSERT INTO resources.medicine (code, medicine, price) VALUES (?, ?, ?)";
                                PreparedStatement statement = connection.prepareStatement(query);
                                statement.setString(1, codeField.getText());
                                statement.setString(2, nameOFservice.getText());
                                statement.setString(3, priceField.getText());
                                statement.executeUpdate();
                                JOptionPane.showMessageDialog(null, "Данные успешно записаны!");
                                refreshCons(areaMedicine);
                                addMedicine.dispose();
                            } catch (Exception exception) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных");
                                throw new RuntimeException(exception);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Все поля должны быть заполнены");
                        }
                    }
                });
                writeMedicine.add(sendData);
            }
        });
        panel.add(addCons);
        JButton deleteCons = new JButton("Удалить расходник");
        deleteCons.setBounds(10, 45, 130, 25);
        deleteCons.setForeground(Color.WHITE);
        deleteCons.setBackground(new Color(119, 155, 191));
        deleteCons.setBorder(BorderFactory.createLineBorder(Color.black));
        deleteCons.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] selectedValues = areaMedicine.getSelectedValuesList().toArray(new String[0]);
                Properties proper = new Properties();
                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                    proper.load(input);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                    throw new RuntimeException(exc);
                }
                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                    StringBuilder queryBuilder = new StringBuilder("DELETE FROM resources.medicine WHERE medicine IN (");
                    for (int i = 0; i < selectedValues.length; i++) {
                        queryBuilder.append("'").append(selectedValues[i]).append("'");
                        if (i < selectedValues.length - 1) {
                            queryBuilder.append(", ");
                        }
                    }
                    queryBuilder.append(")");
                    Statement statement = connection.createStatement();
                    statement.execute(queryBuilder.toString());
                    JOptionPane.showMessageDialog(null, "Данные успешно удалены!");
                    refreshCons(areaMedicine);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
        panel.add(deleteCons);
        JLabel labelFind = new JLabel("Поиск\uD83D\uDD0E:");
        labelFind.setBounds(340, -10, 120, 30);
        panel.add(labelFind);

        searchField = new JTextField();
        searchField.setBounds(300, 15, 140, 25);
        searchField.setBorder(BorderFactory.createLineBorder(Color.black));
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(Color.WHITE);
        if (searchField.isEnabled()) {
            searchField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String searchText = searchField.getText();
                    if (searchField.getText().isEmpty()) {
                        refreshCons(areaMedicine);
                    } else {
                        Properties proper = new Properties();
                        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                            proper.load(input);
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                        }
                        try {
                            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT medicine FROM resources.medicine WHERE medicine LIKE '%" + searchText + "%' OR price LIKE '%" + searchText + "%' OR code LIKE '%" + searchText + "%'");
                            medicineList.clear();
                            while (resultSet.next()) {
                                String medicineName = resultSet.getString("medicine");
                                medicineList.addElement(medicineName);
                            }
                            connection.close();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    resultMedicine = "";
                }
            });
        }
        panel.add(searchField);
        JButton resetButton = new JButton("\uD83D\uDDD1");
        resetButton.setBounds(150, 45, 25, 25);
        resetButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(119, 155, 191));
        resetButton.setBorder(BorderFactory.createLineBorder(Color.black));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sumOfmoney = 0;
                sumValueLabel.setText(String.valueOf(sumOfmoney));
                areaMedicine.clearSelection();
                build.setLength(0);
            }
        });
        panel.add(resetButton);
    }
    private void refreshCons(JList<String> cons) {
        int previousSum = sumOfmoney;
        medicineList.clear();
        priceList.clear();
        sumValueLabel.setText(String.valueOf(previousSum));
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
            throw new RuntimeException(exc);
        }
        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM resources.medicine");
            while (resultSet.next()) {
                medicineList.addElement(resultSet.getString("medicine"));
                int price = resultSet.getInt("price");
                priceList.addElement(price);
            }
            cons.setModel(medicineList);
            cons.repaint();
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
        sumValueLabel.setText(String.valueOf(previousSum));
        sumOfmoney = previousSum;
    }

    private void showDetailsAboutService(String code, String name, String price) {
        JFrame aboutFrame = new JFrame();
        JLabel labelHello = new JLabel("Изменение данных!");
        labelHello.setBounds(120, 10, 150, 25);
        aboutFrame.add(labelHello);

        JLabel codeLabel = new JLabel("Код:");
        codeLabel.setBounds(10, 45, 110, 25);
        codeLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(codeLabel);
        JTextField textFileWithCode = new JTextField(code);
        textFileWithCode.setBounds(135, 45, 120, 25);
        textFileWithCode.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(textFileWithCode);

        JLabel nameOfService = new JLabel("Услуга:");
        nameOfService.setBounds(10, 85, 110, 25);
        nameOfService.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(nameOfService);
        JTextField textFileWithName = new JTextField(name);
        textFileWithName.setBounds(135, 85, 120, 25);
        textFileWithName.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(textFileWithName);


        JLabel priceOfService = new JLabel("Цена:");
        priceOfService.setBounds(10, 125, 110, 25);
        priceOfService.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(priceOfService);
        JTextField textFileWithPrice = new JTextField(price);
        textFileWithPrice.setBounds(135, 125, 120, 25);
        textFileWithPrice.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutFrame.add(textFileWithPrice);

        JButton saveButton = new JButton("Сохранить");
        saveButton.setBounds(120, 160, 100, 30);
        saveButton.setForeground(Color.black);
        saveButton.setBackground(Color.white);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCode = textFileWithCode.getText();
                String newName = textFileWithName.getText();
                String newPrice = textFileWithPrice.getText();
                Properties proper = new Properties();
                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                    proper.load(input);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                    throw new RuntimeException(exc);
                }
                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                    Statement statement = connection.createStatement();
                    String updateQuery = "UPDATE resources.medicine SET code = '" + newCode + "', medicine = '" + newName + "', price = '" + newPrice + "' WHERE code = '" + code + "'";
                    statement.executeUpdate(updateQuery);
                    JOptionPane.showMessageDialog(null, "Данные успешно изменены");
                    refreshCons(areaMedicine);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при выполнении запроса");
                }

                aboutFrame.dispose();
            }
        });
        aboutFrame.add(saveButton);

        aboutFrame.setSize(350, 240);
        aboutFrame.setTitle("Изменение услуги");
        aboutFrame.setLocationRelativeTo(null);
        aboutFrame.setResizable(false);
        aboutFrame.setLayout(null);
        aboutFrame.setVisible(true);
    }


    public String getMedicine() {
        return resultMedicine;
    }

    public int getSum() {
        return sumOfmoney;
    }
}