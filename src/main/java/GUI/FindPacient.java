package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

public class FindPacient {
    private JFrame searchFrame;
    private JTextField searchField;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private Connection connection;
    
    public FindPacient() {
        createSearchWindow();
    }
    
    private void createSearchWindow() {
        // Создаем соединение с БД
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            }
            connection = DriverManager.getConnection(proper.getProperty("database.url"), 
                    proper.getProperty("database.login"), proper.getProperty("database.pass"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка подключения к базе данных");
            e.printStackTrace();
            return;
        }
        
        searchFrame = new JFrame("Поиск пациентов");
        searchFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        searchFrame.setSize(900, 600);
        searchFrame.setLocationRelativeTo(null);
        searchFrame.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        JLabel searchLabel = new JLabel("Поиск по имени, телефону, номеру карты или адресу:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(30, 35));
        
        // Добавляем слушатель для автоматического поиска при вводе
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }
        });
        
        // Слушатель для клавиши Enter - переход на пациента на главной странице
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String searchText = searchField.getText().trim();
                    if (!searchText.isEmpty()) {
                        // Ищем пациента по имени в таблице результатов
                        if (tableModel.getRowCount() > 0) {
                            // Выбираем первого пациента из результатов
                            String patientId = (String) tableModel.getValueAt(0, 0);
                            String patientName = (String) tableModel.getValueAt(0, 1);
                            
                            // Закрываем окно поиска
                            try {
                                if (connection != null && !connection.isClosed()) {
                                    connection.close();
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            searchFrame.dispose();
                            
                            // Здесь можно добавить логику для перехода на пациента на главной странице
                            // Например, можно вызвать метод из HomeScreen для поиска и выделения пациента
                            JOptionPane.showMessageDialog(null, "Переход на пациента: " + patientName + " (ID: " + patientId + ")");
                        }
                    }
                }
            }
        });
        
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Создаем таблицу результатов
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableModel.addColumn("ID");
        tableModel.addColumn("ФИО");
        tableModel.addColumn("Номер карты");
        tableModel.addColumn("Телефон");
        tableModel.addColumn("Возраст");
        tableModel.addColumn("Адрес");
        
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        resultsTable.setRowHeight(25);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Добавляем сортировку
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(880, 500));
        
        // Двойной клик для просмотра деталей
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = resultsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        // Конвертируем индекс с учетом сортировки
                        int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
                        String patientId = (String) tableModel.getValueAt(modelRow, 0);
                        showPatientDetails(patientId);
                    }
                }
            }
        });
        
        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(119, 155, 191));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        closeButton.addActionListener(e -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            searchFrame.dispose();
        });
        
        // Инструкция
        JLabel instructionLabel = new JLabel("<html><i>Начните вводить текст для поиска. Результаты будут обновляться автоматически.<br>Двойной клик на строке - просмотр подробной информации.</i></html>");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Компоновка
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(instructionLabel, BorderLayout.SOUTH);
        
        searchFrame.add(mainPanel);
        searchFrame.setVisible(true);
        
        // Фокус на поле поиска
        searchField.requestFocusInWindow();
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            // Если поле пустое, загружаем всех пациентов (с ограничением)
            loadAllPatients();
            return;
        }
        
        try {
            // Оптимизированный поиск с использованием индексов
            String query = "SELECT id, name, cardNumber, telephoneNumber, age, addres " +
                          "FROM addperson.pacient " +
                          "WHERE name LIKE ? OR " +
                          "cardNumber LIKE ? OR " +
                          "telephoneNumber LIKE ? OR " +
                          "addres LIKE ? " +
                          "ORDER BY name ASC " +
                          "LIMIT 100";
            
            PreparedStatement statement = connection.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            
            ResultSet resultSet = statement.executeQuery();
            
            // Очищаем таблицу
            tableModel.setRowCount(0);
            
            // Заполняем таблицу результатами
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getString("id"));
                row.add(resultSet.getString("name"));
                row.add(resultSet.getString("cardNumber"));
                row.add(resultSet.getString("telephoneNumber"));
                row.add(resultSet.getString("age"));
                row.add(resultSet.getString("addres"));
                tableModel.addRow(row);
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(searchFrame, "Ошибка при поиске: " + e.getMessage());
        }
    }
    
    private void loadAllPatients() {
        try {
            // Загружаем первых 100 пациентов для быстрого отображения
            String query = "SELECT id, name, cardNumber, telephoneNumber, age, addres " +
                          "FROM addperson.pacient " +
                          "ORDER BY name ASC " +
                          "LIMIT 100";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            
            // Очищаем таблицу
            tableModel.setRowCount(0);
            
            // Заполняем таблицу результатами
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getString("id"));
                row.add(resultSet.getString("name"));
                row.add(resultSet.getString("cardNumber"));
                row.add(resultSet.getString("telephoneNumber"));
                row.add(resultSet.getString("age"));
                row.add(resultSet.getString("addres"));
                tableModel.addRow(row);
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showPatientDetails(String patientId) {
        try {
            String query = "SELECT * FROM addperson.pacient WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, patientId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                // Используем тот же метод что и на главной странице
                String id = resultSet.getString("id");
                String cardNumber = resultSet.getString("cardNumber");
                String name = resultSet.getString("name");
                String age = resultSet.getString("age");
                String telephoneNumber = resultSet.getString("telephoneNumber");
                String parentName = resultSet.getString("parentName");
                String workTelephoneNumber = resultSet.getString("WorkTelephoneNum");
                String gender = resultSet.getString("gender");
                String documentName = resultSet.getString("documentName");
                String seriaOfDocument = resultSet.getString("seriaOfDocument");
                String whoGiveDocument = resultSet.getString("WhoGiveDocument");
                String whenDocumentGet = resultSet.getString("whenDocumentGet");
                String placeOfWork = resultSet.getString("placeOfWork");
                String addres = resultSet.getString("addres");
                String birthdate = resultSet.getString("birthdate");
                String dateRegistration = resultSet.getString("dateRegistration");
                String diagnose = resultSet.getString("diagnose");
                String discount = resultSet.getString("discount");
                String doctorName = resultSet.getString("doctorName");
                String recordTime = resultSet.getString("timeOfRecord");
                String record = resultSet.getString("dateOfRecord");
                String medicina = resultSet.getString("medicine");
                String sum = resultSet.getString("sum");
                String allergies = resultSet.getString("allergies");
                
                // Вызываем статический метод из HomeScreen
                HomeScreen.showPatientDetails(id, cardNumber, name, age, telephoneNumber, parentName, 
                        workTelephoneNumber, gender, documentName, seriaOfDocument, whoGiveDocument, 
                        whenDocumentGet, placeOfWork, addres, birthdate, dateRegistration, discount, 
                        diagnose, doctorName, record, medicina, sum, recordTime, allergies);
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(searchFrame, "Ошибка при загрузке деталей: " + e.getMessage());
        }
    }
}