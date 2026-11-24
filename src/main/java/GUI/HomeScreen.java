package GUI;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.toedter.calendar.JDateChooser;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xslf.util.PDFFontMapper;
import org.xhtmlrenderer.pdf.ITextRenderer;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;


public class HomeScreen extends JFrame {

    public static String pacientName;
    public static String namePacient;
    private static JFrame detailsFrame;
    private JList<String> diagnose;
    private DefaultListModel<String> DiagnoseList;
    public static DefaultListModel<String> DoctorList;
    private JList<String> doctors;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static JPanel calendarPanel;
    public static JLabel recordText;
    public static JLabel timeRegDate;
    private static JLabel monthLabel;
    private static JLabel yearLabel;
    private static boolean isDaySelected;
    private static int year;
    private static int month;
    private static JTable table;
    private DefaultTableModel model;
    private JTable table2;
    private static JLabel countLabel;
    private DefaultTableModel model2;
    private JButton pacientAddButton;
    private JLabel hilabel;
    private static JButton addRecord;
    private DefaultListModel<String> RecordListModel;
    private DefaultListModel<String> RecordListAll;
    private JList AllRecords;
    private JLabel nearestRecord;
    private int checker;
    private int checker2;
    private static int idForInformation;
    private static boolean checkerForADDrecord = false;
    
    // Переменные для постраничной загрузки
    private static int currentOffset = 0;
    private static final int PAGE_SIZE = 1000;
    private static boolean isLoading = false;
    private static boolean hasMoreData = true;
    private static JScrollPane mainScrollPane; // Ссылка на scrollPane для настройки скролла

    private static class ArrowIcon implements Icon {
        private final int direction;
        private final int size;

        public ArrowIcon(int direction, int size) {
            this.direction = direction;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);

            int[] xPoints = new int[3];
            int[] yPoints = new int[3];

            if (direction == SwingConstants.WEST) {
                xPoints[0] = x + size;
                yPoints[0] = y;
                xPoints[1] = x;
                yPoints[1] = y + size / 2;
                xPoints[2] = x + size;
                yPoints[2] = y + size;
            } else if (direction == SwingConstants.EAST) {
                xPoints[0] = x;
                yPoints[0] = y;
                xPoints[1] = x + size;
                yPoints[1] = y + size / 2;
                xPoints[2] = x;
                yPoints[2] = y + size;
            }

            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }


    public HomeScreen() {
        setResizable(false);
        setTitle("Главный Экран");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawRect(0, 229, 1200, 1);
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(3)); // Установите желаемую толщину линии
                g2.drawLine(735, 0, 735, 230);
                g2.setStroke(oldStroke); // Восстановите предыдущую толщину линии
            }
        };
        getContentPane().add(panel);
        placeCompanent(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void placeCompanent(JPanel panel) {
        panel.setLayout(null);
        hilabel = new JLabel("Добро пожаловать!");
        hilabel.setBounds(250, 0, 200, 25);
        hilabel.setFont(new Font("Arial", Font.PLAIN, 20));
        panel.add(hilabel);

        nearestRecord = new JLabel("Ближайшие записи");
        nearestRecord.setBounds(900, 0, 148, 20);
        nearestRecord.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(nearestRecord);
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"Уникальный номер", "ФИО", "Номер карточки", "Номер телефона", "Пол", "Адрес"});
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        mainScrollPane = new JScrollPane(table);
        mainScrollPane.setBounds(0, 230, WIDTH, 540);
        JScrollBar verticalScrollBar = mainScrollPane.getVerticalScrollBar();
        // Устанавливаем предпочтительный размер полосы прокрутки (ширина, высота)
        verticalScrollBar.setPreferredSize(new Dimension(35, verticalScrollBar.getPreferredSize().height));

        panel.add(mainScrollPane);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                checker++;
                if (checker == 1) {
                    sortTableByColumn(column);
                    checker = 1;
                } else if (checker == 2) {
                    sortTableByColumn2(column);
                    checker = 2;
                } else if (checker == 3) {
                    resetTableSorting();
                    checker = 0;
                }
            }
        });
        showDB();

        JButton refreshButton = new JButton("Обновить");
        refreshButton.setBounds(275, 70, 100, 30);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(119, 155, 191));

        panel.add(refreshButton);
        JButton printTodayReportButton = new JButton("Распечатать отчет за сегодня");
        printTodayReportButton.setBounds(225, 175, 200, 25);
        printTodayReportButton.setForeground(Color.WHITE);
        printTodayReportButton.setBackground(new Color(119, 155, 191));

        printTodayReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDateChooser dateChooser = new JDateChooser();
                int result = JOptionPane.showConfirmDialog(null, dateChooser, "Выберите дату", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    LocalDate selectedDate = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    // Запрос пути сохранения
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Сохранить отчет как PDF");
                    fileChooser.setSelectedFile(new File("report_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf"));
                    int userSelection = fileChooser.showSaveDialog(null);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        saveReportAsPDF(selectedDate, fileToSave.getAbsolutePath());
                    }
                }
            }
        });
        panel.add(printTodayReportButton);

        JButton aboutPacientButton = new JButton("Получить инфо.");
        aboutPacientButton.setBounds(275, 130, 100, 30);
        aboutPacientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    pacientName = (String) table.getValueAt(selectedRow, 1);
                    JFrame newFrame = new JFrame("Информация о пациенте:" + pacientName);
                    newFrame.setSize(900, 700);
                    newFrame.setResizable(false);
                    newFrame.setLocationRelativeTo(null);
                    newFrame.setLayout(null);

                    JButton deleteButton = new JButton("Удалить");
                    deleteButton.setBounds(60, 20, 100, 25);
                    deleteButton.setForeground(Color.white);
                    deleteButton.setBackground(new Color(119, 155, 191));
                    deleteButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int selectedRow = table2.getSelectedRow();
                            if (selectedRow != -1) {
                                String uniqueNumber = (String) table2.getValueAt(selectedRow, 0);
                                String name = (String) table2.getValueAt(selectedRow, 1);
                                String sum = (String) table2.getValueAt(selectedRow, 7);
                                String discount = (String) table2.getValueAt(selectedRow, 6);
                                String deleteSql = "DELETE FROM addperson.pacient WHERE id = ? AND name = ? AND sum = ? AND discount = ?";
                                Properties proper = new Properties();
                                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                    proper.load(input);
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, "Произошла ошибка при установки соединения");
                                    throw new RuntimeException(exc);
                                }
                                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                     PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                                    statement.setString(1, uniqueNumber);
                                    statement.setString(2, name);
                                    statement.setString(3, sum);
                                    statement.setString(4, discount);
                                    statement.executeUpdate();
                                    JOptionPane.showMessageDialog(null, "Карточка успешно удалена!");
                                    showDB2();
                                    refreshTable();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                    JOptionPane.showMessageDialog(null, "Произошла ошибка при удалении записи.");
                                }

                            } else {
                                JOptionPane.showMessageDialog(null, "Выберите запись для удаления.");
                            }
                        }
                    });
                    newFrame.add(deleteButton);

                    JButton editButton = new JButton("Редактировать");
                    editButton.setBounds(685, 20, 125, 25);
                    editButton.setForeground(Color.white);
                    editButton.setBackground(new Color(119, 155, 191));
                    editButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int selectedRow = table2.getSelectedRow();
                            if (selectedRow != -1) {
                                String uniqueNumber = (String) table2.getValueAt(selectedRow, 0);
                                Properties proper = new Properties();
                                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                    proper.load(input);
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                                    throw new RuntimeException(exc);
                                }
                                try {
                                    Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet;
                                    resultSet = statement.executeQuery("SELECT * FROM addperson.pacient WHERE id='" + uniqueNumber + "'");
                                    if (resultSet.next()) {
                                        String id = resultSet.getString("id");
                                        String cardNumber = resultSet.getString("cardNumber");
                                        String fullname = resultSet.getString("name");
                                        String age = resultSet.getString("age");
                                        String telephoneNumber = resultSet.getString("telephoneNumber");
                                        String parentName = resultSet.getString("parentName");
                                        String workTelephoneNumber = resultSet.getString("WorkTelephoneNum");
                                        String gender = resultSet.getString("gender");
                                        String documentName = resultSet.getString("documentName");
                                        String documentSeria = resultSet.getString("seriaOfDocument");
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
                                        HomeScreen.showPatientDetails(id, cardNumber, fullname, age, telephoneNumber, parentName, workTelephoneNumber, gender, documentName
                                                , documentSeria, whoGiveDocument, whenDocumentGet, placeOfWork, addres, birthdate, dateRegistration, discount, diagnose, doctorName, record, medicina, sum, recordTime, allergies);
                                    }
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
                                    throw new RuntimeException(exc);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Выберите запись для редактирования.");
                            }
                        }
                    });
                    newFrame.add(editButton);

                    JLabel abouttotalAmountField = new JLabel("Общая сумма:");
                    abouttotalAmountField.setBounds(445, 5, 100, 30);
                    abouttotalAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
                    newFrame.add(abouttotalAmountField);

                    JLabel totalAmountField = new JLabel();
                    totalAmountField.setBounds(465, 20, 100, 25);
                    totalAmountField.setFont(new Font("Arial", Font.PLAIN, 14));
                    totalAmountField.setForeground(Color.red);
                    newFrame.add(totalAmountField);
                    model2 = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    model2.setColumnIdentifiers(new String[]{"Уникальный номер", "ФИО", "Номер телефона", "Дата регистрации", "Врач", "Диагноз", "Скидка", "Сумма"});
                    table2 = new JTable(model2);
                    table2.setFont(new Font("Arial", Font.PLAIN, 12));
                    JScrollPane scrollPane = new JScrollPane(table2);
                    scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
                    scrollPane.setBounds(60, 60, 750, 550);
                    newFrame.add(scrollPane);
                    countLabel = new JLabel("Количество записей: ");
                    countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                    countLabel.setBounds(255, 10, 150, 25);
                    newFrame.add(countLabel);
                    JLabel countLabel2 = new JLabel();
                    countLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
                    countLabel2.setBounds(307, 20, 150, 25);
                    countLabel2.setForeground(Color.red);
                    newFrame.add(countLabel2);
                    table2.getTableHeader().addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int column = table2.columnAtPoint(e.getPoint());
                            checker2++;
                            if (checker2 == 1) {
                                sortTableByColumn4(column);
                                checker2 = 1;
                            } else if (checker2 == 2) {
                                sortTableByColumn3(column);
                                checker2 = 2;
                            } else if (checker2 == 3) {
                                resetTableSorting2();
                                checker2 = 0;
                            }
                        }
                    });
                    showDB2();
                    double totalAmount = calculateTotalAmount();
                    totalAmountField.setText(String.valueOf(totalAmount));
                    countLabel2.setText(String.valueOf(table2.getRowCount()));
                    newFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите пациента из списка.");
                }
            }
        });


        aboutPacientButton.setBorder(BorderFactory.createLineBorder(Color.black));
        aboutPacientButton.setForeground(Color.WHITE);
        aboutPacientButton.setBackground(new Color(119, 155, 191));
        panel.add(aboutPacientButton);


        JButton cardButton = new JButton("Карты записи");
        cardButton.setBounds(425, 105, 150, 30);
        cardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewFrame();
            }
        });
        cardButton.setForeground(Color.WHITE);
        cardButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cardButton.setBackground(new Color(119, 155, 191));
        panel.add(cardButton);

        JButton buttonAbout = new JButton("Справка");
        buttonAbout.setBounds(0, 0, 120, 25);
        buttonAbout.setForeground(Color.white);
        buttonAbout.setBackground(new Color(119, 155, 191));
        buttonAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DoctorList = new DefaultListModel<>();
                DiagnoseList = new DefaultListModel<>();
                try {
                    Properties proper = new Properties();
                    try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                    }
                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        ResultSet doctors = statement.executeQuery("SELECT DoctorName FROM doctors.alldoctors");
                        while (doctors.next()) {
                            DoctorList.addElement(doctors.getString("DoctorName"));
                        }
                        connection.close();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных(1220)");
                    }
                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM diagnose.alldiagnose");
                        while (resultSet.next()) {
                            DiagnoseList.addElement(resultSet.getString("diagnoseName"));
                        }
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при подлючение!");
                }
                JFrame newFrame = new JFrame();
                newFrame.setTitle("Справка");
                newFrame.setSize(350, 550);
                newFrame.setLayout(null);
                JButton backButton = new JButton();
                backButton.setBounds(0, 0, 120, 25);
                backButton.setForeground(Color.WHITE);
                backButton.setBackground(new Color(119, 155, 191));
                backButton.setBorder(BorderFactory.createLineBorder(Color.black, 3));
                backButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(true);
                        newFrame.dispose();
                    }
                });

                Icon arrowIcon = new ArrowIcon(SwingConstants.WEST, 12);
                backButton.setIcon(arrowIcon);
                JButton addDoctorButton = new JButton("Добавить врача");
                addDoctorButton.setFont(new Font("Arial", Font.PLAIN, 10));
                addDoctorButton.setBounds(20, 145, 120, 30);
                addDoctorButton.setForeground(Color.white);
                addDoctorButton.setBackground(new Color(119, 155, 191));
                addDoctorButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame addDoctorFrame = new JFrame();
                        addDoctorFrame.setTitle("Добавить врача");
                        addDoctorFrame.setSize(500, 300); // Увеличен размер окна
                        addDoctorFrame.setLocationRelativeTo(null);
                        addDoctorFrame.setResizable(false);
                        addDoctorFrame.getContentPane().setBackground(new Color(235, 235, 235)); // Цвет фона окна

                        JPanel addDoctorPanel = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                g.setColor(new Color(215, 215, 215)); // Цвет полос
                                g.fillRect(0, 0, getWidth(), 10); // Верхняя полоса
                                g.fillRect(0, getHeight() - 10, getWidth(), 10); // Нижняя полоса
                            }
                        };
                        addDoctorPanel.setLayout(null);
                        addDoctorPanel.setOpaque(false); // Панель прозрачная, чтобы видеть фон окна

                        JLabel nameLabel = new JLabel("Имя врача:");
                        nameLabel.setBounds(30, 30, 100, 25);
                        addDoctorPanel.add(nameLabel);

                        JTextField nameField = new JTextField();
                        nameField.setBounds(140, 30, 300, 25);
                        addDoctorPanel.add(nameField);

                        JLabel timeWorkLabel = new JLabel("Время работы:");
                        timeWorkLabel.setBounds(30, 70, 100, 25);
                        addDoctorPanel.add(timeWorkLabel);

                        JTextField timeWorkField = new JTextField();
                        timeWorkField.setBounds(140, 70, 300, 25);
                        addDoctorPanel.add(timeWorkField);

// Добавляем новые элементы для номера телефона
                        JLabel phoneLabel = new JLabel("Телефон:");
                        phoneLabel.setBounds(30, 110, 100, 25);
                        addDoctorPanel.add(phoneLabel);

                        JTextField phoneField = new JTextField();
                        phoneField.setBounds(140, 110, 300, 25);
                        addDoctorPanel.add(phoneField);

                        JButton saveButton = new JButton("Отправить");
                        saveButton.setBounds(170, 160, 160, 30);
                        addDoctorPanel.add(saveButton);
                        saveButton.setForeground(Color.WHITE);
                        saveButton.setBackground(new Color(119, 155, 191));

                        addDoctorFrame.add(addDoctorPanel);
                        addDoctorFrame.setVisible(true);

                        saveButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String doctorName = nameField.getText();
                                String timeWork = timeWorkField.getText();
                                String telephoneNumber = phoneField.getText();
                                if (!doctorName.isEmpty() && !timeWork.isEmpty() && !telephoneNumber.isEmpty()) {
                                    Properties proper = new Properties();
                                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                        proper.load(input);
                                    } catch (Exception exc) {
                                        throw new RuntimeException(exc);
                                    }
                                    try {
                                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                        Statement statement = connection.createStatement();
                                        statement.execute("INSERT INTO doctors.alldoctors(doctorName, timeWork, telephoneNumber) VALUES('" + doctorName + "', '" + timeWork + "', '" + telephoneNumber + "');");
                                        JOptionPane.showMessageDialog(null, "Доктор успешно записан");
                                        updateDoctorList(DoctorList);
                                    } catch (Exception exception) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных(1220)");
                                    }
                                    addDoctorFrame.dispose();
                                } else {
                                    if (doctorName.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Введите имя врача");
                                    } else if (timeWork.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Введите время работы");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Введите номер телефона");
                                    }
                                }
                            }
                        });
                    }
                });

                JButton DoctorButton = new JButton("Врачи");
                DoctorButton.setFont(new Font("Arial", Font.PLAIN, 12));
                DoctorButton.setBounds(20, 100, 120, 30);
                DoctorButton.setForeground(Color.white);
                DoctorButton.setBackground(new Color(119, 155, 191));

                DoctorButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame DoctorFrame = new JFrame();
                        DoctorFrame.setTitle("Врачи");
                        DoctorFrame.setSize(400, 400);
                        DoctorFrame.setLocationRelativeTo(null);
                        DoctorFrame.setResizable(false);
                        DoctorFrame.setVisible(true);
                        DoctorFrame.setLayout(null);

                        JButton deleteDoctorButton = new JButton("Удалить врача");
                        deleteDoctorButton.setFont(new Font("Arial", Font.PLAIN, 12));
                        deleteDoctorButton.setBounds(120, 20, 145, 25);
                        deleteDoctorButton.setForeground(Color.white);
                        deleteDoctorButton.setBackground(new Color(119, 155, 191));
                        deleteDoctorButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    String selectDoctor = doctors.getSelectedValue();
                                    if (!selectDoctor.isEmpty()) {
                                        Properties proper = new Properties();
                                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                            proper.load(input);
                                        } catch (Exception exc) {
                                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение!");
                                        }
                                        try {
                                            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                            Statement statement = connection.createStatement();
                                            statement.execute("DELETE FROM doctors.alldoctors WHERE DoctorName='" + selectDoctor + "';");
                                            JOptionPane.showMessageDialog(null, "Врач успешно удален из списка");
                                            updateDoctorList(DoctorList);
                                            connection.close();
                                        } catch (Exception exception) {
                                            JOptionPane.showMessageDialog(null, "Произошла ошибка при удаление врача из списка");
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Перед удалением вы должны выбрать врача!");
                                    }
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, "Возникла ошибка,возможно вы не выбрали элемент перед удалением!");
                                }
                            }
                        });
                        DoctorFrame.add(deleteDoctorButton);

                        doctors = new JList<>(DoctorList);
                        doctors.setBorder(BorderFactory.createLineBorder(Color.black));
                        JScrollPane doctorScroll = new JScrollPane(doctors);
                        DoctorFrame.add(doctorScroll);
                        doctorScroll.setBounds(0, 75, getWidth(), getHeight());
                        doctors.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if (e.getClickCount() == 2) {
                                    Properties proper = new Properties();
                                    try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                                        proper.load(input);
                                    } catch (Exception exc) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                                    }
                                    try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                                        Statement statement = connection.createStatement();
                                        ResultSet resultSet = statement.executeQuery("SELECT * FROM doctors.alldoctors WHERE doctorname='" + doctors.getSelectedValue() + "'");
                                        if (resultSet.next()) {
                                            String timeWork = resultSet.getString("timeWork");
                                            String doctorName = doctors.getSelectedValue();
                                            String telephoneNum = resultSet.getString("telephoneNumber");
                                            aboutDoctor aboutDoctor = new aboutDoctor(doctorName, timeWork, telephoneNum, () -> updateDoctorList(DoctorList));
                                        } else {
                                            JOptionPane.showMessageDialog(null, "Произошла ошибка,доктор не найден");
                                        }
                                    } catch (SQLException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        });
                    }
                });
                JButton allDiagnosees = new JButton("Диагнозы");
                allDiagnosees.setFont(new Font("Arial", Font.PLAIN, 12));
                allDiagnosees.setBounds(200, 100, 120, 30);
                allDiagnosees.setForeground(Color.white);
                allDiagnosees.setBackground(new Color(119, 155, 191));
                newFrame.add(allDiagnosees);
                allDiagnosees.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame diagnoseFrame = new JFrame();
                        diagnoseFrame.setTitle("Диагнозы");
                        diagnoseFrame.setSize(400, 400);
                        diagnoseFrame.setLocationRelativeTo(null);
                        diagnoseFrame.setResizable(false);
                        diagnoseFrame.setVisible(true);
                        diagnoseFrame.setLayout(null);

                        JButton deleteDiagnoseButton = new JButton("Удалить Диагноз");
                        deleteDiagnoseButton.setFont(new Font("Arial", Font.PLAIN, 10));
                        deleteDiagnoseButton.setBounds(120, 20, 145, 30);
                        deleteDiagnoseButton.setForeground(Color.white);
                        deleteDiagnoseButton.setBackground(new Color(119, 155, 191));
                        deleteDiagnoseButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    String selectDiagnose = diagnose.getSelectedValue();
                                    if (!selectDiagnose.isEmpty()) {
                                        Properties proper = new Properties();
                                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                            proper.load(input);
                                        } catch (Exception exc) {
                                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение!");
                                        }
                                        try {
                                            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                            Statement statement = connection.createStatement();
                                            statement.execute("DELETE FROM diagnose.alldiagnose WHERE diagnoseName='" + selectDiagnose + "';");
                                            JOptionPane.showMessageDialog(null, "Диагноз успешно удален из списка");
                                            updateDiagnoseList(DiagnoseList);
                                            connection.close();
                                        } catch (Exception exception) {
                                            JOptionPane.showMessageDialog(null, "Произошла ошибка при удаление диагноза из списка");
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Перед удалением вы должны выбрать диагноз!");
                                    }
                                } catch (Exception exc) {
                                    JOptionPane.showMessageDialog(null, "Возникла ошибка,возможно вы не выбрали элемент перед удалением!");
                                }
                            }
                        });
                        diagnose = new JList<>(DiagnoseList);
                        JScrollPane diagnoseScroll = new JScrollPane(diagnose);
                        diagnoseScroll.setBounds(0, 75, getWidth(), getHeight());
                        diagnoseFrame.add(diagnoseScroll);
                        diagnoseFrame.add(deleteDiagnoseButton);
                    }
                });
                JButton addDiagnosees = new JButton("Добавить диагноз");
                addDiagnosees.setFont(new Font("Arial", Font.PLAIN, 10));
                addDiagnosees.setBounds(200, 145, 120, 30);
                addDiagnosees.setForeground(Color.white);
                addDiagnosees.setBackground(new Color(119, 155, 191));
                newFrame.add(addDiagnosees);
                addDiagnosees.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFrame addDiagnose = new JFrame();
                        addDiagnose.setTitle("Добавить диагноз");
                        addDiagnose.setSize(400, 200);
                        addDiagnose.setLocationRelativeTo(null);
                        addDiagnose.setResizable(false);

                        JPanel addDiagnosePanel = new JPanel();
                        addDiagnosePanel.setLayout(null);

                        JLabel diagnoseName = new JLabel("Диагноз(название):");
                        diagnoseName.setBounds(30, 30, 100, 25);
                        addDiagnosePanel.add(diagnoseName);

                        JTextField diagnoseNameField = new JTextField();
                        diagnoseNameField.setBounds(130, 30, 200, 25);
                        addDiagnosePanel.add(diagnoseNameField);

                        JButton saveButton = new JButton("Сохранить");
                        saveButton.setBounds(130, 70, 100, 30);
                        addDiagnosePanel.add(saveButton);
                        saveButton.setForeground(Color.black);
                        saveButton.setBackground(Color.white);

                        addDiagnose.add(addDiagnosePanel);
                        addDiagnose.setVisible(true);
                        saveButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {

                                String diagnoseName = diagnoseNameField.getText();
                                if (!diagnoseName.isEmpty()) {
                                    Properties proper = new Properties();
                                    try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                                        proper.load(input);
                                    } catch (Exception exc) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                                    }
                                    try {
                                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                        Statement statement = connection.createStatement();
                                        statement.execute("INSERT INTO diagnose.alldiagnose(diagnoseName) VALUES('" + diagnoseName + "');");
                                        JOptionPane.showMessageDialog(null, "Диагноз успешно добавлен!");
                                        updateDiagnoseList(DiagnoseList);
                                        connection.close();
                                    } catch (Exception exception) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных(1220)");
                                        throw new RuntimeException(exception);
                                    }
                                    addDiagnose.dispose();
                                } else {
                                    JOptionPane.showMessageDialog(null, "Введите название");
                                }
                            }
                        });
                    }
                });
                JButton addResourse = new JButton("Расходники");
                addResourse.setFont(new Font("Arial", Font.PLAIN, 14));
                addResourse.setBounds(20, 195, 300, 30);
                addResourse.setForeground(Color.white);
                addResourse.setBackground(new Color(119, 155, 191));
                addResourse.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        medicineSelect2 medicineSelect = new medicineSelect2();
                    }
                });

                addDiagnosees.setBorder(BorderFactory.createLineBorder(Color.black));
                backButton.setBorder(BorderFactory.createLineBorder(Color.black));
                DoctorButton.setBorder(BorderFactory.createLineBorder(Color.black));
                addDoctorButton.setBorder(BorderFactory.createLineBorder(Color.black));
                allDiagnosees.setBorder(BorderFactory.createLineBorder(Color.black));
                addResourse.setBorder(BorderFactory.createLineBorder(Color.black));
                newFrame.add(addResourse);
                newFrame.add(DoctorButton);
                newFrame.add(addDoctorButton);
                newFrame.add(backButton);
                setVisible(false);
                newFrame.setVisible(true);
                newFrame.setLocationRelativeTo(null);
            }
        });


        panel.add(buttonAbout);

        pacientAddButton = new JButton("Добавить пациента");
        pacientAddButton.setBounds(75, 50, 150, 30);
        pacientAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrationFormMore18 FORM = new RegistrationFormMore18();
                refreshTable();
            }
        });
        pacientAddButton.setForeground(Color.WHITE);
        pacientAddButton.setBackground(new Color(119, 155, 191));
        panel.add(pacientAddButton);


        JButton findPacient = new JButton("Поиск пациента");
        findPacient.setBounds(425, 50, 150, 30);
        findPacient.setForeground(Color.WHITE);
        findPacient.setBackground(new Color(119, 155, 191));
        findPacient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindPacient Fp = new FindPacient();
            }
        });
        panel.add(findPacient);
        RecordListAll = new DefaultListModel<>();
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при поиске файла с данными");
                throw new RuntimeException(e);
            }
            try {
                Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT record FROM records.allrecords");
                while (resultSet.next()) {
                    RecordListAll.addElement(resultSet.getString("record"));
                }
                ArrayList<String> sortedList = new ArrayList<>();
                for (int i = 0; i < RecordListAll.size(); i++) {
                    sortedList.add(RecordListAll.getElementAt(i));
                }
                Collections.sort(sortedList);
                RecordListAll.clear();
                for (String element : sortedList) {
                    RecordListAll.addElement(element);
                }
                connection.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при выгрузке данных");
            }
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
            throw new RuntimeException(exc);
        }

        RecordListModel = new DefaultListModel<>();
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при поиске файла с данными");
                throw new RuntimeException(e);
            }
            try {
                Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT dateOfRecord FROM addperson.pacient");
                ArrayList<String> sortedList = new ArrayList<>();
                LocalDate currentDate = LocalDate.now();
                while (resultSet.next()) {
                    String dateOfRecord = resultSet.getString("dateOfRecord");
                    if (resultSet.wasNull() || dateOfRecord == null || dateOfRecord.trim().isEmpty()) {
                        continue;
                    } else {
                        try {
                            LocalDate recordDate = LocalDate.parse(dateOfRecord);
                            if (recordDate.isAfter(currentDate)) {
                                sortedList.add(dateOfRecord);
                            } else {
                                continue;
                            }
                        } catch (Exception e) {
                            // Игнорируем записи с некорректными датами
                            System.err.println("Некорректная дата записи: " + dateOfRecord);
                            continue;
                        }
                    }
                }
                for (String element : sortedList) {
                    RecordListModel.addElement(element);
                }
                connection.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение к БД с выгрузкой данных для записей");
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выгрузке данных в записи");
            throw new RuntimeException(e);
        }
        JButton deleteButton = new JButton("Удалить пациента");
        deleteButton.setBounds(75, 100, 150, 30);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int row = table.getSelectedRow();
                    String id = (String) table.getValueAt(row, 0);
                    Properties proper = new Properties();
                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                        throw new RuntimeException(exc);
                    }
                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        statement.execute("DELETE FROM addperson.pacient WHERE id='" + id + "'");
                        JOptionPane.showMessageDialog(null, "Клиент успешно удален из БД");
                        refreshTable();
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, "Похоже вы не выбрали пациента");
                    throw new RuntimeException(exception);
                }

            }
        });


        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(119, 155, 191));
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(deleteButton);
        AllRecords = new JList<>(RecordListModel);
        AllRecords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String data = (String) AllRecords.getSelectedValue();
                    Properties proper = new Properties();
                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                        throw new RuntimeException(exc);
                    }
                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM addperson.pacient WHERE dateOfRecord='" + data + "'");
                        if (resultSet.next()) {
                            String id = resultSet.getString("id");
                            String cardNumber = resultSet.getString("cardNumber");
                            String fullname = resultSet.getString("name");
                            String age = resultSet.getString("age");
                            String telephoneNumber = resultSet.getString("telephoneNumber");
                            String parentName = resultSet.getString("parentName");
                            String workTelephoneNumber = resultSet.getString("WorkTelephoneNum");
                            String gender = resultSet.getString("gender");
                            String documentName = resultSet.getString("documentName");
                            String documentSeria = resultSet.getString("seriaOfDocument");
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
                            showPatientDetails(id, cardNumber, fullname, age, telephoneNumber, parentName, workTelephoneNumber, gender, documentName
                                    , documentSeria, whoGiveDocument, whenDocumentGet, placeOfWork, addres, birthdate, dateRegistration, discount, diagnose, doctorName, record, medicina, sum, recordTime, allergies);
                        }
                        connection.close();
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Пациент не найден");
                        throw new RuntimeException(exc);
                    }
                }
            }
        });
        JScrollPane scrollForRecord = new JScrollPane(AllRecords);
        scrollForRecord.setBounds(736, 20, 451, 208);
        panel.add(scrollForRecord);

        nearestRecord.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        printTodayReportButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        findPacient.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        refreshButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pacientAddButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    String id = (String) table.getValueAt(row, 0);
                    Properties proper = new Properties();
                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                        throw new RuntimeException(exc);
                    }
                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM addperson.pacient WHERE id='" + id + "'");
                        if (resultSet.next()) {
                            String cardNumber = resultSet.getString("cardNumber");
                            String fullname = resultSet.getString("name");
                            String age = resultSet.getString("age");
                            String telephoneNumber = resultSet.getString("telephoneNumber");
                            String parentName = resultSet.getString("parentName");
                            String workTelephoneNumber = resultSet.getString("WorkTelephoneNum");
                            String gender = resultSet.getString("gender");
                            String documentName = resultSet.getString("documentName");
                            String documentSeria = resultSet.getString("seriaOfDocument");
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
                            showPatientDetails(id, cardNumber, fullname, age, telephoneNumber, parentName, workTelephoneNumber, gender, documentName
                                    , documentSeria, whoGiveDocument, whenDocumentGet, placeOfWork, addres, birthdate, dateRegistration, discount, diagnose, doctorName, record, medicina, sum, recordTime, allergies);
                            updateCalendar();
                        }
                        connection.close();
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }
                }
            }
        });
    }

    private double calculateTotalAmount() {
        double totalAmount = 0;
        for (int row = 0; row < table2.getRowCount(); row++) {
            Object value = table2.getValueAt(row, 7);
            if (value instanceof Double) {
                totalAmount += (Double) value;
            } else if (value instanceof String) {
                try {
                    double amount = Double.parseDouble((String) value);
                    totalAmount += amount;
                } catch (NumberFormatException e) {
                    // Обработка ошибки преобразования строки в число
                    e.printStackTrace();
                }
            }
        }
        return totalAmount;
    }

    public static void updateDoctorList(DefaultListModel<String> doctorListModel) {
        doctorListModel.clear();
        Properties proper = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
            return;
        }
        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
             Statement statement = connection.createStatement();
             ResultSet doctors = statement.executeQuery("SELECT DoctorName FROM doctors.alldoctors")) {
            while (doctors.next()) {
                doctorListModel.addElement(doctors.getString("DoctorName"));
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных");
        }
    }

    private static void updateDiagnoseList(DefaultListModel<String> diagnoseListModel) {
        diagnoseListModel.clear();
        Properties proper = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
            return;
        }
        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
             Statement statement = connection.createStatement();
             ResultSet diagnose = statement.executeQuery("SELECT diagnoseName FROM diagnose.alldiagnose")) {
            while (diagnose.next()) {
                diagnoseListModel.addElement(diagnose.getString("diagnoseName"));
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных");
        }
    }


    public void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        RecordListModel.clear();
        model.setRowCount(0);
        
        // Сбрасываем состояние пагинации
        currentOffset = 0;
        hasMoreData = true;
        isLoading = false;
        
        // Загружаем первую страницу
        loadPatients(0, PAGE_SIZE);
        
        // Загружаем записи для календаря отдельно
        loadCalendarRecords();
    }
    
    private void loadCalendarRecords() {
        try {
            Properties proper = new Properties();
            try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                proper.load(input);
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                return;
            }
            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
            // Загружаем только даты записей для календаря
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT dateOfRecord FROM addperson.pacient WHERE dateOfRecord IS NOT NULL AND dateOfRecord != ''");

            // Получаем текущую дату
            LocalDate today = LocalDate.now();

            while (resultSet.next()) {
                // Получаем дату записи из базы данных
                String dateOfRecordStr = resultSet.getString("dateOfRecord");
                
                // Проверяем, что дата не пустая и не null
                if (dateOfRecordStr != null && !dateOfRecordStr.trim().isEmpty()) {
                    try {
                        LocalDate dateOfRecord = LocalDate.parse(dateOfRecordStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        // Сравниваем дату записи с текущей датой
                        if (!dateOfRecord.isBefore(today)) { // Если дата записи >= сегодняшней
                            RecordListModel.addElement(dateOfRecordStr);
                        }
                    } catch (Exception e) {
                        // Игнорируем записи с некорректными датами
                        System.err.println("Некорректная дата записи: " + dateOfRecordStr);
                    }
                }
            }
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных");
            throw new RuntimeException(e);
        }
    }

    private void showDB() {
        loadPatients(0, PAGE_SIZE);
        setupScrollListener();
    }
    
    private void loadPatients(int offset, int limit) {
        if (isLoading || !hasMoreData) {
            return;
        }
        
        isLoading = true;
        
        Properties proper = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
            isLoading = false;
            return;
        }

        try {
            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
            // Загружаем данные постранично, отсортированные по уникальному номеру (от большего к меньшему)
            PreparedStatement statement = connection.prepareStatement(
                "SELECT id, cardNumber, name, telephoneNumber, gender, addres FROM addperson.pacient " +
                "ORDER BY id DESC LIMIT ? OFFSET ?");
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            
            ResultSet resultSet = statement.executeQuery();
            int loadedCount = 0;
            
            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("cardNumber"),
                        resultSet.getString("telephoneNumber"),
                        resultSet.getString("gender"),
                        resultSet.getString("addres")
                });
                loadedCount++;
            }
            
            connection.close();
            
            // Если загрузили меньше чем лимит, значит данных больше нет
            if (loadedCount < limit) {
                hasMoreData = false;
            }
            
            currentOffset += loadedCount;
            isLoading = false;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных о пациентах");
            isLoading = false;
            throw new RuntimeException(e);
        }
    }
    
    private void setupScrollListener() {
        JScrollBar verticalScrollBar = mainScrollPane.getVerticalScrollBar();
        
        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting() && !isLoading && hasMoreData) {
                    int maxValue = verticalScrollBar.getMaximum();
                    int currentValue = verticalScrollBar.getValue();
                    int visibleAmount = verticalScrollBar.getVisibleAmount();
                    
                    // Если прокрутили почти до конца (остаток менее 100 пикселей)
                    if (maxValue - currentValue - visibleAmount < 100) {
                        loadPatients(currentOffset, PAGE_SIZE);
                    }
                }
            }
        });
    }

    private void showDB2() {
        Properties proper = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
        }

        try {
            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
            // Оптимизированный запрос с PreparedStatement и LIMIT
            PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, telephoneNumber, dateRegistration, doctorName, diagnose, discount, sum FROM addperson.pacient WHERE name = ? ORDER BY dateRegistration DESC LIMIT 100");
            statement.setString(1, pacientName);
            ResultSet resultSet = statement.executeQuery();

            model2.setRowCount(0);

            while (resultSet.next()) {
                model2.addRow(new Object[]{
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("telephoneNumber"),
                        resultSet.getString("dateRegistration"),
                        resultSet.getString("doctorName"),
                        resultSet.getString("diagnose"),
                        resultSet.getString("discount"),
                        resultSet.getString("sum")
                });
            }
            connection.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных о пациентах");
            throw new RuntimeException(e);
        }
    }


    private void resetTableSorting2() {
        table2.getRowSorter().setSortKeys(null);
        table2.setAutoCreateRowSorter(false);
        model2.fireTableDataChanged();
    }

    private void sortTableByColumn4(int column) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model2);
        table2.setRowSorter(sorter);
        sorter.toggleSortOrder(column);
    }

    private void sortTableByColumn3(int column) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model2);
        table2.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(column, SortOrder.DESCENDING)); // сортировка по убыванию
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    private void resetTableSorting() {
        table.getRowSorter().setSortKeys(null);
        table.setAutoCreateRowSorter(false);
        model.fireTableDataChanged();
    }

    private void sortTableByColumn(int column) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.toggleSortOrder(column);
    }

    private void sortTableByColumn2(int column) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(column, SortOrder.DESCENDING)); // сортировка по убыванию
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    public static void showPatientDetails(String id, String card, String fullName, String totalAge, String TelephoneNum, String parentName, String WorkTelephoneNum, String gender, String documentName,
                                          String numberDocument, String ByWhoGive, String WhenGet, String PlaceOfwork, String addresWhere, String birthdateYear, String registrationDate, String discount, String diagnose, String doctorName, String record, String medicina, String sum, String timeOfRecord, String allergies) {
        namePacient = fullName;
        idForInformation = Integer.parseInt(id);
        detailsFrame = new JFrame("Детали о пациенте с номером-" + id);
        detailsFrame.setLayout(null);
        detailsFrame.setResizable(false);
        JLabel pacientNameLabel = new JLabel("Имя пациента:");
        pacientNameLabel.setBounds(20, 5, 100, 25);
        pacientNameLabel.setForeground(Color.black);
        pacientNameLabel.setBackground(Color.WHITE);
        detailsFrame.add(pacientNameLabel);
        JLabel pacientName = new JLabel(fullName);
        pacientName.setForeground(Color.black);
        pacientName.setBackground(Color.WHITE);
        pacientName.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension preferredSize = pacientName.getPreferredSize();
        int widthName = preferredSize.width + 10;
        pacientName.setBounds(130, 5, widthName, 25);
        detailsFrame.add(pacientName);

        JLabel cardNumberLabel = new JLabel("Номер карточки:");
        cardNumberLabel.setBounds(20, 35, 120, 25);
        cardNumberLabel.setForeground(Color.black);
        cardNumberLabel.setBackground(Color.white);
        detailsFrame.add(cardNumberLabel);
        JLabel cardNum = new JLabel(card);
        cardNum.setForeground(Color.black);
        cardNum.setBackground(Color.white);
        cardNum.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension preferredSizeOfCard = cardNum.getPreferredSize();
        int widthCard = preferredSizeOfCard.width + 10;
        cardNum.setBounds(130, 35, widthCard, 25);
        detailsFrame.add(cardNum);

        JLabel ageLabel = new JLabel("Возраст:");
        ageLabel.setBounds(20, 65, 100, 25);
        ageLabel.setForeground(Color.black);
        ageLabel.setBackground(Color.white);
        detailsFrame.add(ageLabel);
        JLabel age = new JLabel(totalAge);
        age.setForeground(Color.black);
        age.setBackground(Color.white);
        age.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension preferredSizeOfAge = age.getPreferredSize();
        int widthAge = preferredSizeOfAge.width + 10;
        age.setBounds(130, 65, widthAge, 25);
        detailsFrame.add(age);

        JLabel telephoneLabel = new JLabel("Номер телефона:");
        telephoneLabel.setBounds(20, 95, 120, 25);
        telephoneLabel.setForeground(Color.black);
        telephoneLabel.setBackground(Color.white);
        detailsFrame.add(telephoneLabel);
        JLabel telephoneNumber = new JLabel(TelephoneNum);
        telephoneNumber.setForeground(Color.black);
        telephoneNumber.setBackground(Color.white);
        telephoneNumber.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension preferredSizeOfTelephone = telephoneNumber.getPreferredSize();
        int widthTelephone = preferredSizeOfTelephone.width + 10;
        telephoneNumber.setBounds(130, 95, widthTelephone, 25);
        detailsFrame.add(telephoneNumber);


        JLabel parentLabel = new JLabel("Имя родителя:");
        parentLabel.setBounds(20, 125, 120, 25);
        parentLabel.setForeground(Color.black);
        parentLabel.setBackground(Color.white);
        detailsFrame.add(parentLabel);
        if (!parentName.isEmpty()) {
            JLabel NameOfParent = new JLabel(parentName);
            NameOfParent.setForeground(Color.black);
            NameOfParent.setBackground(Color.white);
            NameOfParent.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
            Dimension preferredSizeOfParentName = telephoneNumber.getPreferredSize();
            int widthParent = preferredSizeOfParentName.width + 10;
            NameOfParent.setBounds(130, 125, widthParent, 25);
            detailsFrame.add(NameOfParent);
        } else {
            JLabel NameOfParent = new JLabel("Не записано");
            NameOfParent.setForeground(Color.black);
            NameOfParent.setBackground(Color.white);
            NameOfParent.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
            Dimension preferredSizeOfParentName = telephoneNumber.getPreferredSize();
            int widthParent = preferredSizeOfParentName.width + 10;
            NameOfParent.setBounds(130, 125, widthParent, 25);
            detailsFrame.add(NameOfParent);
        }
        JLabel WorktelephoneLabel = new JLabel("Номер телефона:");
        JLabel workTelephoneLabelUnder = new JLabel("(рабочий)");
        workTelephoneLabelUnder.setBounds(35, 165, 120, 25);
        WorktelephoneLabel.setBounds(20, 155, 120, 25);
        workTelephoneLabelUnder.setForeground(Color.black);
        workTelephoneLabelUnder.setBackground(Color.WHITE);
        WorktelephoneLabel.setForeground(Color.black);
        WorktelephoneLabel.setBackground(Color.white);
        detailsFrame.add(WorktelephoneLabel);
        detailsFrame.add(workTelephoneLabelUnder);
        if (!WorkTelephoneNum.isEmpty()) {
            JLabel workTelephoneNumber = new JLabel(WorkTelephoneNum);
            workTelephoneNumber.setForeground(Color.black);
            workTelephoneNumber.setBackground(Color.white);
            workTelephoneNumber.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
            Dimension preferredSizeOfWorkTelephone = workTelephoneNumber.getPreferredSize();
            int widthWorkTelephone = preferredSizeOfWorkTelephone.width + 10;
            workTelephoneNumber.setBounds(130, 155, widthWorkTelephone, 25);
            detailsFrame.add(workTelephoneNumber);
        } else {
            JLabel workTelephoneNumber = new JLabel("Тут пусто");
            workTelephoneNumber.setForeground(Color.black);
            workTelephoneNumber.setBackground(Color.white);
            workTelephoneNumber.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
            Dimension preferredSizeOfWorkTelephone = workTelephoneNumber.getPreferredSize();
            int widthWorkTelephone = preferredSizeOfWorkTelephone.width + 10;
            workTelephoneNumber.setBounds(130, 155, widthWorkTelephone, 25);
            detailsFrame.add(workTelephoneNumber);
        }
        JLabel genderLabel = new JLabel("Пол:");
        genderLabel.setBounds(20, 185, 100, 25);
        genderLabel.setForeground(Color.black);
        genderLabel.setBackground(Color.white);
        detailsFrame.add(genderLabel);
        JLabel genderCurrent = new JLabel(gender);
        genderCurrent.setForeground(Color.black);
        genderCurrent.setBackground(Color.white);
        genderCurrent.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeOfGender = genderCurrent.getPreferredSize();
        int widthGender = prefferedSizeOfGender.width + 10;
        genderCurrent.setBounds(130, 185, widthGender, 25);
        detailsFrame.add(genderCurrent);

        JLabel documentLabel = new JLabel("Документ:");
        documentLabel.setBounds(20, 215, 100, 25);
        documentLabel.setForeground(Color.black);
        documentLabel.setBackground(Color.white);
        detailsFrame.add(documentLabel);
        JLabel documents = new JLabel(documentName);
        documents.setForeground(Color.black);
        documents.setBackground(Color.white);
        documents.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeOfDocument = documents.getPreferredSize();
        int widthDocument = prefferedSizeOfDocument.width + 10;
        documents.setBounds(130, 215, widthDocument, 25);
        detailsFrame.add(documents);

        JLabel documentNumberLabel = new JLabel("Серия документа:");
        documentNumberLabel.setBounds(20, 245, 120, 25);
        documentNumberLabel.setForeground(Color.black);
        documentNumberLabel.setBackground(Color.white);
        detailsFrame.add(documentNumberLabel);
        JLabel documentNumber = new JLabel(numberDocument);
        documentNumber.setForeground(Color.black);
        documentNumber.setBackground(Color.white);
        documentNumber.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeOfDocumentNum = documentNumber.getPreferredSize();
        int widthDocumentNum = prefferedSizeOfDocumentNum.width + 10;
        documentNumber.setBounds(130, 245, widthDocumentNum, 25);
        detailsFrame.add(documentNumber);

        JLabel whoGiveDocument = new JLabel("Документ выдан:");
        whoGiveDocument.setBounds(20, 275, 120, 25);
        whoGiveDocument.setForeground(Color.black);
        whoGiveDocument.setBackground(Color.white);
        detailsFrame.add(whoGiveDocument);
        JLabel BywhoDocumentWasGiven = new JLabel(ByWhoGive);
        BywhoDocumentWasGiven.setForeground(Color.black);
        BywhoDocumentWasGiven.setBackground(Color.white);
        BywhoDocumentWasGiven.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeOfDocumentGive = BywhoDocumentWasGiven.getPreferredSize();
        int widthDocumentGive = prefferedSizeOfDocumentGive.width + 10;
        BywhoDocumentWasGiven.setBounds(130, 275, widthDocumentGive, 25);
        detailsFrame.add(BywhoDocumentWasGiven);

        JLabel DateofGivenLabel = new JLabel("Дата выдачи:");
        DateofGivenLabel.setBounds(20, 305, 120, 25);
        DateofGivenLabel.setForeground(Color.black);
        DateofGivenLabel.setBackground(Color.white);
        detailsFrame.add(DateofGivenLabel);
        JLabel dateOfGiven = new JLabel(WhenGet);
        dateOfGiven.setForeground(Color.black);
        dateOfGiven.setBackground(Color.white);
        dateOfGiven.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeOfDocumentDateGive = dateOfGiven.getPreferredSize();
        int widthDocumentGiveDate = prefferedSizeOfDocumentDateGive.width + 10;
        dateOfGiven.setBounds(130, 305, widthDocumentGiveDate, 25);
        detailsFrame.add(dateOfGiven);

        JLabel whereWorkLabel = new JLabel("Место работы:");
        whereWorkLabel.setBounds(20, 335, 120, 25);
        whereWorkLabel.setForeground(Color.black);
        whereWorkLabel.setBackground(Color.white);
        detailsFrame.add(whereWorkLabel);
        JLabel whereWork = new JLabel(PlaceOfwork);
        whereWork.setForeground(Color.black);
        whereWork.setBackground(Color.white);
        whereWork.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeWhereWork = whereWork.getPreferredSize();
        int widthWork = prefferedSizeWhereWork.width + 10;
        whereWork.setBounds(130, 335, widthWork, 25);
        detailsFrame.add(whereWork);

        JLabel whereLiveLabel = new JLabel("Адрес:");
        whereLiveLabel.setBounds(20, 365, 120, 25);
        whereLiveLabel.setForeground(Color.black);
        whereLiveLabel.setBackground(Color.white);
        detailsFrame.add(whereLiveLabel);
        JLabel whereLive = new JLabel(addresWhere);
        whereLive.setForeground(Color.black);
        whereLive.setBackground(Color.white);
        whereLive.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeWhereLive = whereLive.getPreferredSize();
        int widthLive = prefferedSizeWhereLive.width + 10;
        whereLive.setBounds(130, 365, widthLive, 25);
        detailsFrame.add(whereLive);

        JLabel yearbirthLabel = new JLabel("Дата рождения:");
        yearbirthLabel.setBounds(20, 395, 120, 25);
        yearbirthLabel.setForeground(Color.black);
        yearbirthLabel.setBackground(Color.white);
        detailsFrame.add(yearbirthLabel);
        JLabel yearBirth = new JLabel(birthdateYear);
        yearBirth.setForeground(Color.black);
        yearBirth.setBackground(Color.white);
        yearBirth.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeWhenBirth = yearBirth.getPreferredSize();
        int widthbirth = prefferedSizeWhenBirth.width + 10;
        yearBirth.setBounds(130, 395, widthbirth, 25);
        detailsFrame.add(yearBirth);

        JLabel regDateLabel = new JLabel("Дата регистрации:");
        regDateLabel.setBounds(20, 425, 120, 25);
        regDateLabel.setForeground(Color.black);
        regDateLabel.setBackground(Color.white);
        detailsFrame.add(regDateLabel);
        JLabel regDate = new JLabel(registrationDate);
        regDate.setForeground(Color.black);
        regDate.setBackground(Color.white);
        regDate.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeWhenReg = regDate.getPreferredSize();
        int widthReg = prefferedSizeWhenReg.width + 10;
        regDate.setBounds(130, 425, widthReg, 25);
        detailsFrame.add(regDate);


        JLabel discountLabel = new JLabel("Скидка:");
        discountLabel.setBounds(20, 455, 120, 25);
        discountLabel.setForeground(Color.black);
        discountLabel.setBackground(Color.white);
        detailsFrame.add(discountLabel);
        
        // Создаем редактируемое поле для скидки
        String[] discountOptions = {"Нет", "5%", "10%", "15%", "20%", "25%", "30%"};
        JComboBox<String> discountComboBox = new JComboBox<>(discountOptions);
        discountComboBox.setSelectedItem(discount.isEmpty() ? "Нет" : discount);
        discountComboBox.setBounds(130, 455, 100, 25);
        discountComboBox.setForeground(Color.black);
        discountComboBox.setBackground(Color.white);
        detailsFrame.add(discountComboBox);
        
        // Добавляем слушатель для автоматического пересчета суммы при изменении скидки
        discountComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedDiscount = (String) discountComboBox.getSelectedItem();
                try {
                    Properties proper = new Properties();
                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    }
                    try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                        // Получаем текущую сумму и текущую скидку из базы
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT sum, discount FROM addperson.pacient WHERE id = '" + id + "'");
                        if (resultSet.next()) {
                            String currentSum = resultSet.getString("sum");
                            String currentDiscount = resultSet.getString("discount");
                            if (currentSum != null && !currentSum.isEmpty()) {
                                // Заменяем запятую на точку для корректного парсинга
                                String normalizedSum = currentSum.replace(",", ".");
                                double workingSum = Double.parseDouble(normalizedSum);
                                double newSum;
                                
                                // Если текущая скидка не "Нет", то сначала возвращаем к оригинальной сумме
                                if (currentDiscount != null && !currentDiscount.equals("Нет") && currentDiscount.endsWith("%")) {
                                    double discountValue = Double.parseDouble(currentDiscount.replace("%", "")) / 100;
                                    workingSum = workingSum / (1 - discountValue);
                                }
                                
                                if (selectedDiscount.equals("Нет")) {
                                    newSum = workingSum;
                                } else if (selectedDiscount.endsWith("%")) {
                                    double discountValue = Double.parseDouble(selectedDiscount.replace("%", "")) / 100;
                                    newSum = workingSum * (1 - discountValue);
                                } else {
                                    newSum = workingSum;
                                }
                                
                                // Обновляем сумму в базе данных
                                String newSumStr = String.format("%.2f", newSum);
                                statement.executeUpdate("UPDATE addperson.pacient SET sum = '" + newSumStr + "', discount = '" + selectedDiscount + "' WHERE id = '" + id + "'");
                                
                                // Перезагружаем окно деталей пациента
                                detailsFrame.dispose();
                                showPatientDetails(id, card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                        numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, selectedDiscount, diagnose, doctorName, record, medicina, newSumStr, timeOfRecord, allergies);
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Ошибка при обновлении скидки: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        JLabel diagnoseLabel = new JLabel("Диагноз:");
        diagnoseLabel.setBounds(20, 485, 120, 25);
        diagnoseLabel.setForeground(Color.black);
        diagnoseLabel.setBackground(Color.white);
        detailsFrame.add(diagnoseLabel);
        JLabel diagnoseStay = new JLabel(diagnose);
        diagnoseStay.setForeground(Color.black);
        diagnoseStay.setBackground(Color.white);
        diagnoseStay.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedDiagnose = diagnoseStay.getPreferredSize();
        int widthDiagnose = prefferedDiagnose.width + 10;
        diagnoseStay.setBounds(130, 485, widthDiagnose, 25);
        detailsFrame.add(diagnoseStay);

        JLabel doctorNameLabel = new JLabel("Доктор:");
        doctorNameLabel.setBounds(20, 515, 120, 25);
        doctorNameLabel.setForeground(Color.black);
        doctorNameLabel.setBackground(Color.white);
        detailsFrame.add(doctorNameLabel);
        JLabel doctor = new JLabel(doctorName);
        doctor.setForeground(Color.black);
        doctor.setBackground(Color.white);
        doctor.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedDoctor = doctor.getPreferredSize();
        int widthDoctor = prefferedDoctor.width + 10;
        doctor.setBounds(130, 515, widthDoctor, 25);
        detailsFrame.add(doctor);

        JLabel recordLabel = new JLabel("Запись:");
        recordLabel.setBounds(20, 545, 120, 25);
        recordLabel.setForeground(Color.black);
        recordLabel.setBackground(Color.white);
        detailsFrame.add(recordLabel);
        recordText = new JLabel(record);
        recordText.setForeground(Color.black);
        recordText.setBackground(Color.white);
        recordText.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedRecord = recordText.getPreferredSize();
        int widthRecord = prefferedRecord.width + 10;
        recordText.setBounds(130, 545, widthRecord, 25);
        detailsFrame.add(recordText);

        timeRegDate = new JLabel(timeOfRecord);
        timeRegDate.setForeground(Color.black);
        timeRegDate.setBackground(Color.WHITE);
        timeRegDate.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSizeWhenRegtime = timeRegDate.getPreferredSize();
        int widthRegTime = prefferedSizeWhenRegtime.width + 10;
        if (recordText.getText() == null) {
            timeRegDate.setBounds(recordText.getX() + 20, 545, widthRegTime, 25);
        } else {
            timeRegDate.setBounds(recordText.getX() + 100, 545, widthRegTime, 25);
        }
        detailsFrame.add(timeRegDate);

        JLabel cons = new JLabel("Услуги:");
        cons.setBounds(20, 575, 120, 25);
        cons.setForeground(Color.black);
        cons.setBackground(Color.white);
        detailsFrame.add(cons);
        JButton servicesButton = new JButton("Смотреть все!");
        servicesButton.setForeground(Color.BLACK);
        servicesButton.setBackground(Color.WHITE);
        servicesButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        servicesButton.setBounds(130, 575, 130, 25);
        servicesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showServicesWindow(medicina, id);
            }
        });
        detailsFrame.add(servicesButton);

        JLabel price = new JLabel("Сумма:");
        price.setBounds(20, 605, 120, 25);
        price.setForeground(Color.black);
        price.setBackground(Color.white);
        detailsFrame.add(price);

        JLabel textOfSum = new JLabel(sum);
        textOfSum.setForeground(Color.black);
        textOfSum.setBackground(Color.white);
        textOfSum.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        Dimension prefferedSum = textOfSum.getPreferredSize();
        int widthSum = prefferedSum.width + 10;
        textOfSum.setBounds(130, 605, widthSum, 25);
        detailsFrame.add(textOfSum);

        JLabel allergiesLabel = new JLabel("Перенесённые");
        allergiesLabel.setBounds(20, 635, 120, 25);
        allergiesLabel.setForeground(Color.black);
        allergiesLabel.setBackground(Color.white);
        detailsFrame.add(allergiesLabel);
        JLabel allergiesLabel2 = new JLabel("и сопутствующие");
        allergiesLabel2.setBounds(20, 645, 185, 25);
        allergiesLabel2.setForeground(Color.black);
        allergiesLabel2.setBackground(Color.white);
        detailsFrame.add(allergiesLabel2);
        JLabel allergiesLabel3 = new JLabel("заболевания:");
        allergiesLabel3.setBounds(20, 655, 185, 25);
        allergiesLabel3.setForeground(Color.black);
        allergiesLabel3.setBackground(Color.white);
        detailsFrame.add(allergiesLabel3);

        String allergiesText = "<html>" + allergies + "</html>";
        JLabel textOfAllergies = new JLabel(allergiesText);
        textOfAllergies.setForeground(Color.black);
        textOfAllergies.setBackground(Color.white);
        textOfAllergies.setHorizontalAlignment(JLabel.LEFT);
        textOfAllergies.setVerticalAlignment(JLabel.TOP);

        JScrollPane scrollPane = new JScrollPane(textOfAllergies);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(130, 635, 290, 40);
        detailsFrame.add(scrollPane);
        calendarPanel = new JPanel();
        calendarPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        calendarPanel.setLayout(new GridLayout(6, 7, 5, 5));
        calendarPanel.setBounds(450, 20, 300, 300);
        detailsFrame.add(calendarPanel);

        LocalDate currentDate = LocalDate.now();
        year = currentDate.getYear();
        month = currentDate.getMonthValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL", new Locale("ru"));
        String monthName = currentDate.format(formatter);
        monthLabel = new JLabel(monthName);
        monthLabel.setHorizontalAlignment(JLabel.CENTER);
        monthLabel.setVerticalAlignment(JLabel.CENTER);
        monthLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        monthLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        monthLabel.setBounds(527, 0, 150, 20);
        detailsFrame.add(monthLabel);
        yearLabel = new JLabel(String.valueOf(year));
        yearLabel.setHorizontalAlignment(JLabel.CENTER);
        yearLabel.setVerticalAlignment(JLabel.CENTER);
        yearLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        yearLabel.setBounds(530, 325, 150, 25);
        detailsFrame.add(yearLabel);

        JButton previousButton = new JButton("<");
        previousButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        previousButton.setBackground(new Color(119, 155, 191));
        previousButton.setForeground(Color.WHITE);
        previousButton.setBounds(450, 0, 50, 20);
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                month--;
                if (month < 1) {
                    month = 12;
                    year--;
                }
                updateCalendar();
            }
        });
        detailsFrame.add(previousButton);

        JButton nextButton = new JButton(">");
        nextButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        nextButton.setBackground(new Color(119, 155, 191));
        nextButton.setForeground(Color.WHITE);
        nextButton.setBounds(700, 0, 50, 20);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
                updateCalendar();
            }
        });
        detailsFrame.add(nextButton);

        updateCalendar();

        JPanel panelForButtons = new JPanel();
        panelForButtons.setBounds(450, 500, 500, 500);
        panelForButtons.setBorder(BorderFactory.createLineBorder(Color.black, 2));

        JButton WriteAnContract = new JButton("Распечатать документы");
        WriteAnContract.setBorder(BorderFactory.createLineBorder(Color.black));
        WriteAnContract.setBounds(650, 615, 170, 25);
        WriteAnContract.setForeground(Color.WHITE);
        WriteAnContract.setBackground(new Color(119, 155, 191));
        detailsFrame.add(WriteAnContract);

        JButton anyOneReg = new JButton("Повторная запись");
        anyOneReg.setBorder(BorderFactory.createLineBorder(Color.black));
        anyOneReg.setBounds(555, 505, 170, 25);
        anyOneReg.setForeground(Color.WHITE);
        anyOneReg.setBackground(new Color(119, 155, 191));
        anyOneReg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detailsFrame.dispose();
                RegistrationFormMore18_2 registrationFormMore182 = new RegistrationFormMore18_2(fullName, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName, numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, allergies);
                registrationFormMore182.setVisible(true);
            }
        });
        detailsFrame.add(anyOneReg);
        JButton getInfo = new JButton("\uD83D\uDCC3");
        getInfo.setBorder(BorderFactory.createLineBorder(Color.black));
        getInfo.setBounds(755, 505, 25, 25);
        getInfo.setForeground(Color.WHITE);
        getInfo.setBackground(new Color(119, 155, 191));
        getInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getInfo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int rowIndex = -1; // Индекс строки
                        for (int i = 0; i < table.getRowCount(); i++) {
                            String idValue = table.getValueAt(i, 0).toString(); // Значение в столбце id
                            String fullNameValue = table.getValueAt(i, 1).toString(); // Значение в столбце fullName
                            if (idValue.equals(id) && fullNameValue.equals(fullName)) {
                                rowIndex = i; // Запоминаем индекс строки
                                break;
                            }
                        }

                        if (rowIndex != -1) {
                            table.setRowSelectionInterval(rowIndex, rowIndex);
                        }

                        detailsFrame.dispose();
                    }
                });
            }
        });
        detailsFrame.add(getInfo);
        JButton toothFormula = new JButton("Формула зубов");
        toothFormula.setBorder(BorderFactory.createLineBorder(Color.black));
        toothFormula.setBounds(555, 575, 170, 25);
        toothFormula.setForeground(Color.WHITE);
        toothFormula.setBackground(new Color(119, 155, 191));
        toothFormula.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ToothFormulaFrame toothFormulaFrame = new ToothFormulaFrame();
                toothFormulaFrame.setVisible(true);
            }
        });
        detailsFrame.add(toothFormula);

        JButton editData = new JButton("Изменить данные");
        editData.setBounds(465, 615, 150, 25);
        editData.setBorder(BorderFactory.createLineBorder(Color.black));
        editData.setForeground(Color.WHITE);
        editData.setBackground(new Color(119, 155, 191));

        addRecord = new JButton("Добавить запись");
        addRecord.setBorder(BorderFactory.createLineBorder(Color.black));
        addRecord.setBounds(650, 535, 170, 25);
        addRecord.setForeground(Color.WHITE);
        addRecord.setBackground(new Color(119, 155, 191));
        addRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkerForADDrecord = true;
                updateCalendar();
            }
        });
        detailsFrame.add(addRecord);

        JButton deleteRecord = new JButton("Удалить запись");
        deleteRecord.setBorder(BorderFactory.createLineBorder(Color.black));
        deleteRecord.setBounds(465, 535, 150, 25);
        deleteRecord.setForeground(Color.WHITE);
        deleteRecord.setBackground(new Color(119, 155, 191));
        detailsFrame.add(deleteRecord);

        detailsFrame.add(editData);
        detailsFrame.add(panelForButtons);

        editData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame editData = new JFrame("Изменить детали о пациенте с номером-" + id);
                editData.setLayout(null);
                editData.setResizable(false);
                JLabel pacientNameLabel = new JLabel("Имя пациента:");
                pacientNameLabel.setBounds(10, 0, 100, 25);
                pacientNameLabel.setForeground(Color.black);
                pacientNameLabel.setBackground(Color.WHITE);
                editData.add(pacientNameLabel);
                JTextField pacientName = new JTextField(fullName);
                pacientName.setForeground(Color.black);
                pacientName.setBackground(Color.WHITE);
                pacientName.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                Dimension preferredSize = pacientName.getPreferredSize();
                int widthName = preferredSize.width + 10;
                pacientName.setBounds(120, 0, widthName, 25);
                editData.add(pacientName);

                JLabel cardNumberLabel = new JLabel("Номер карточки:");
                cardNumberLabel.setBounds(10, 30, 120, 25);
                cardNumberLabel.setForeground(Color.black);
                cardNumberLabel.setBackground(Color.white);
                editData.add(cardNumberLabel);
                JTextField cardNum = new JTextField(card);
                cardNum.setForeground(Color.black);
                cardNum.setBackground(Color.white);
                cardNum.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension preferredSizeOfCard = cardNum.getPreferredSize();
                int widthCard = preferredSizeOfCard.width + 10;
                cardNum.setBounds(120, 30, widthCard, 25);
                editData.add(cardNum);


                JLabel ageLabel = new JLabel("Возраст:");
                ageLabel.setBounds(10, 60, 100, 25);
                ageLabel.setForeground(Color.black);
                ageLabel.setBackground(Color.white);
                editData.add(ageLabel);
                JTextField age = new JTextField(totalAge);
                age.setForeground(Color.black);
                age.setBackground(Color.white);
                age.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension preferredSizeOfAge = age.getPreferredSize();
                int widthAge = preferredSizeOfAge.width + 10;
                age.setBounds(120, 60, widthAge, 25);
                editData.add(age);

                JLabel telephoneLabel = new JLabel("Номер телефона:");
                telephoneLabel.setBounds(10, 90, 120, 25);
                telephoneLabel.setForeground(Color.black);
                telephoneLabel.setBackground(Color.white);
                editData.add(telephoneLabel);
                JTextField telephoneNumber = new JTextField(TelephoneNum);
                telephoneNumber.setForeground(Color.black);
                telephoneNumber.setBackground(Color.white);
                telephoneNumber.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension preferredSizeOfTelephone = telephoneNumber.getPreferredSize();
                int widthTelephone = preferredSizeOfTelephone.width + 10;
                telephoneNumber.setBounds(120, 90, widthTelephone, 25);
                editData.add(telephoneNumber);


                JLabel parentLabel = new JLabel("Имя родителя:");
                parentLabel.setBounds(10, 120, 120, 25);
                parentLabel.setForeground(Color.black);
                parentLabel.setBackground(Color.white);
                editData.add(parentLabel);
                JTextField NameOfParent = new JTextField(parentName);
                NameOfParent.setForeground(Color.black);
                NameOfParent.setBackground(Color.white);
                NameOfParent.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension preferredSizeOfParentName = telephoneNumber.getPreferredSize();
                int widthParent = preferredSizeOfParentName.width + 10;
                NameOfParent.setBounds(120, 120, widthParent, 25);
                editData.add(NameOfParent);

                JLabel WorktelephoneLabel = new JLabel("Номер телефона:");
                JLabel workTelephoneLabelUnder = new JLabel("(рабочий)");
                workTelephoneLabelUnder.setBounds(25, 160, 120, 25);
                WorktelephoneLabel.setBounds(10, 150, 120, 25);
                workTelephoneLabelUnder.setForeground(Color.black);
                workTelephoneLabelUnder.setBackground(Color.WHITE);
                WorktelephoneLabel.setForeground(Color.black);
                WorktelephoneLabel.setBackground(Color.white);
                editData.add(WorktelephoneLabel);
                editData.add(workTelephoneLabelUnder);
                JTextField workTelephoneNumber = new JTextField(WorkTelephoneNum);
                workTelephoneNumber.setForeground(Color.black);
                workTelephoneNumber.setBackground(Color.white);
                workTelephoneNumber.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension preferredSizeOfWorkTelephone = workTelephoneNumber.getPreferredSize();
                int widthWorkTelephone = preferredSizeOfWorkTelephone.width + 10;
                workTelephoneNumber.setBounds(120, 150, widthWorkTelephone, 25);
                editData.add(workTelephoneNumber);

                JLabel genderLabel = new JLabel("Пол:");
                genderLabel.setBounds(10, 180, 100, 25);
                genderLabel.setForeground(Color.black);
                genderLabel.setBackground(Color.white);
                editData.add(genderLabel);
                JLabel genderCurrent = new JLabel(gender);
                genderCurrent.setForeground(Color.black);
                genderCurrent.setBackground(Color.white);
                genderCurrent.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeOfGender = genderCurrent.getPreferredSize();
                int widthGender = prefferedSizeOfGender.width + 10;
                genderCurrent.setBounds(120, 180, widthGender, 25);
                editData.add(genderCurrent);

                JLabel documentLabel = new JLabel("Документ:");
                documentLabel.setBounds(10, 210, 100, 25);
                documentLabel.setForeground(Color.black);
                documentLabel.setBackground(Color.white);
                editData.add(documentLabel);
                JTextField documents = new JTextField(documentName);
                documents.setForeground(Color.black);
                documents.setBackground(Color.white);
                documents.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeOfDocument = documents.getPreferredSize();
                int widthDocument = prefferedSizeOfDocument.width + 10;
                documents.setBounds(120, 210, widthDocument, 25);
                editData.add(documents);

                JLabel documentNumberLabel = new JLabel("Серия документа:");
                documentNumberLabel.setBounds(10, 240, 120, 25);
                documentNumberLabel.setForeground(Color.black);
                documentNumberLabel.setBackground(Color.white);
                editData.add(documentNumberLabel);
                JTextField documentNumber = new JTextField(numberDocument);
                documentNumber.setForeground(Color.black);
                documentNumber.setBackground(Color.white);
                documentNumber.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeOfDocumentNum = documentNumber.getPreferredSize();
                int widthDocumentNum = prefferedSizeOfDocumentNum.width + 10;
                documentNumber.setBounds(120, 240, widthDocumentNum, 25);
                editData.add(documentNumber);

                JLabel whoGiveDocument = new JLabel("Документ выдан:");
                whoGiveDocument.setBounds(10, 270, 120, 25);
                whoGiveDocument.setForeground(Color.black);
                whoGiveDocument.setBackground(Color.white);
                editData.add(whoGiveDocument);
                JTextField BywhoDocumentWasGiven = new JTextField(ByWhoGive);
                BywhoDocumentWasGiven.setForeground(Color.black);
                BywhoDocumentWasGiven.setBackground(Color.white);
                BywhoDocumentWasGiven.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeOfDocumentGive = BywhoDocumentWasGiven.getPreferredSize();
                int widthDocumentGive = prefferedSizeOfDocumentGive.width + 10;
                BywhoDocumentWasGiven.setBounds(120, 270, widthDocumentGive, 25);
                editData.add(BywhoDocumentWasGiven);

                JLabel DateofGivenLabel = new JLabel("Дата выдачи:");
                DateofGivenLabel.setBounds(10, 300, 120, 25);
                DateofGivenLabel.setForeground(Color.black);
                DateofGivenLabel.setBackground(Color.white);
                editData.add(DateofGivenLabel);
                JTextField dateOfGiven = new JTextField(WhenGet);
                dateOfGiven.setForeground(Color.black);
                dateOfGiven.setBackground(Color.white);
                dateOfGiven.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeOfDocumentDateGive = dateOfGiven.getPreferredSize();
                int widthDocumentGiveDate = prefferedSizeOfDocumentDateGive.width + 10;
                dateOfGiven.setBounds(120, 300, widthDocumentGiveDate, 25);
                editData.add(dateOfGiven);

                JLabel whereWorkLabel = new JLabel("Место работы:");
                whereWorkLabel.setBounds(10, 330, 120, 25);
                whereWorkLabel.setForeground(Color.black);
                whereWorkLabel.setBackground(Color.white);
                editData.add(whereWorkLabel);
                JTextField whereWork = new JTextField(PlaceOfwork);
                whereWork.setForeground(Color.black);
                whereWork.setBackground(Color.white);
                whereWork.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeWhereWork = whereWork.getPreferredSize();
                int widthWork = prefferedSizeWhereWork.width + 10;
                whereWork.setBounds(120, 330, widthWork, 25);
                editData.add(whereWork);

                JLabel whereLiveLabel = new JLabel("Адрес:");
                whereLiveLabel.setBounds(10, 360, 120, 25);
                whereLiveLabel.setForeground(Color.black);
                whereLiveLabel.setBackground(Color.white);
                editData.add(whereLiveLabel);
                JTextField whereLive = new JTextField(addresWhere);
                whereLive.setForeground(Color.black);
                whereLive.setBackground(Color.white);
                whereLive.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeWhereLive = whereLive.getPreferredSize();
                int widthLive = prefferedSizeWhereLive.width + 10;
                whereLive.setBounds(120, 360, widthLive, 25);
                editData.add(whereLive);

                JLabel yearbirthLabel = new JLabel("Дата рождения:");
                yearbirthLabel.setBounds(10, 390, 120, 25);
                yearbirthLabel.setForeground(Color.black);
                yearbirthLabel.setBackground(Color.white);
                editData.add(yearbirthLabel);
                JTextField yearBirth = new JTextField(birthdateYear);
                yearBirth.setForeground(Color.black);
                yearBirth.setBackground(Color.white);
                yearBirth.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeWhenBirth = yearBirth.getPreferredSize();
                int widthbirth = prefferedSizeWhenBirth.width + 10;
                yearBirth.setBounds(120, 390, widthbirth, 25);
                editData.add(yearBirth);

                JLabel regDateLabel = new JLabel("Дата регистрации:");
                regDateLabel.setBounds(10, 420, 120, 25);
                regDateLabel.setForeground(Color.black);
                regDateLabel.setBackground(Color.white);
                editData.add(regDateLabel);
                JTextField regDate = new JTextField(registrationDate);
                regDate.setForeground(Color.black);
                regDate.setBackground(Color.white);
                regDate.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSizeWhenReg = regDate.getPreferredSize();
                int widthReg = prefferedSizeWhenReg.width + 10;
                regDate.setBounds(120, 420, widthReg, 25);
                editData.add(regDate);

                JLabel discountLabel = new JLabel("Скидка:");
                discountLabel.setBounds(10, 450, 120, 25);
                discountLabel.setForeground(Color.black);
                discountLabel.setBackground(Color.white);
                editData.add(discountLabel);
                
                // Создаем редактируемое поле для скидки с предустановленными значениями
                String[] discountOptions = {"Нет", "5%", "10%", "15%", "20%", "25%", "30%"};
                JComboBox<String> discountComboBox = new JComboBox<>(discountOptions);
                discountComboBox.setSelectedItem(discount.isEmpty() ? "Нет" : discount);
                discountComboBox.setBounds(120, 450, 100, 25);
                discountComboBox.setForeground(Color.black);
                discountComboBox.setBackground(Color.white);
                editData.add(discountComboBox);

                JLabel diagnoseLabel = new JLabel("Диагноз:");
                diagnoseLabel.setBounds(10, 480, 120, 25);
                diagnoseLabel.setForeground(Color.black);
                diagnoseLabel.setBackground(Color.white);
                editData.add(diagnoseLabel);
                JTextField diagnoseStay = new JTextField(diagnose);
                diagnoseStay.setForeground(Color.black);
                diagnoseStay.setBackground(Color.white);
                diagnoseStay.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedDiagnose = diagnoseStay.getPreferredSize();
                int widthDiagnose = prefferedDiagnose.width + 10;
                diagnoseStay.setBounds(120, 480, widthDiagnose, 25);
                editData.add(diagnoseStay);

                JLabel doctorNameLabel = new JLabel("Доктор:");
                doctorNameLabel.setBounds(10, 510, 120, 25);
                doctorNameLabel.setForeground(Color.black);
                doctorNameLabel.setBackground(Color.white);
                editData.add(doctorNameLabel);
                JTextField doctor = new JTextField(doctorName);
                doctor.setForeground(Color.black);
                doctor.setBackground(Color.white);
                doctor.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedDoctor = doctor.getPreferredSize();
                int widthDoctor = prefferedDoctor.width + 10;
                doctor.setBounds(120, 510, widthDoctor, 25);
                editData.add(doctor);

                JLabel recordLabel = new JLabel("Запись:");
                recordLabel.setBounds(10, 540, 120, 25);
                recordLabel.setForeground(Color.black);
                recordLabel.setBackground(Color.white);
                editData.add(recordLabel);
                JTextField recordText = new JTextField(record);
                recordText.setForeground(Color.black);
                recordText.setBackground(Color.white);
                recordText.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedRecord = recordText.getPreferredSize();
                int widthRecord = prefferedRecord.width + 10;
                recordText.setBounds(120, 540, widthRecord, 25);
                editData.add(recordText);

                JLabel cons = new JLabel("Услуги:");
                cons.setBounds(10, 570, 120, 25);
                cons.setForeground(Color.black);
                cons.setBackground(Color.white);
                editData.add(cons);
                JTextField consText = new JTextField(medicina);
                consText.setForeground(Color.black);
                consText.setBackground(Color.white);
                consText.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedCons = consText.getPreferredSize();
                int widthCons = prefferedCons.width + 10;
                consText.setBounds(120, 570, widthCons, 25);
                editData.add(consText);

                JLabel sumLabel = new JLabel("Сумма:");
                sumLabel.setBounds(10, 600, 120, 25);
                sumLabel.setForeground(Color.black);
                sumLabel.setBackground(Color.white);
                editData.add(sumLabel);
                JTextField sumText = new JTextField(sum);
                sumText.setForeground(Color.black);
                sumText.setBackground(Color.white);
                sumText.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedSum = sumText.getPreferredSize();
                int widthSum = prefferedSum.width + 10;
                sumText.setBounds(120, 600, widthSum, 25);
                editData.add(sumText);
                
                // Добавляем слушатель для автоматического пересчета суммы при изменении скидки
                discountComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String selectedDiscount = (String) discountComboBox.getSelectedItem();
                        String currentSum = sumText.getText();
                        if (!currentSum.isEmpty()) {
                            try {
                                double originalSum = Double.parseDouble(currentSum);
                                double newSum;
                                if (selectedDiscount.equals("Нет")) {
                                    newSum = originalSum;
                                } else if (selectedDiscount.endsWith("%")) {
                                    double discountValue = Double.parseDouble(selectedDiscount.replace("%", "")) / 100;
                                    newSum = originalSum * (1 - discountValue);
                                } else {
                                    newSum = originalSum;
                                }
                                sumText.setText(String.format("%.2f", newSum));
                                System.out.println("Сумма изменена с " + originalSum + " на " + newSum + " со скидкой " + selectedDiscount);
                            } catch (NumberFormatException ex) {
                                // Если сумма не число, не меняем её
                                System.out.println("Ошибка при парсинге суммы: " + currentSum);
                            }
                        } else {
                            System.out.println("Сумма пуста");
                        }
                    }
                });

                JLabel allergiesLabel = new JLabel("Перенесённые и");
                allergiesLabel.setBounds(10, 625, 120, 25);
                allergiesLabel.setForeground(Color.black);
                allergiesLabel.setBackground(Color.white);
                editData.add(allergiesLabel);
                JLabel allergiesLabel2 = new JLabel("сопутствующие");
                allergiesLabel2.setBounds(5, 637, 185, 25);
                allergiesLabel2.setForeground(Color.black);
                allergiesLabel2.setBackground(Color.white);
                editData.add(allergiesLabel2);
                JLabel allergiesLabel3 = new JLabel("заболевания:");
                allergiesLabel3.setBounds(10, 647, 185, 25);
                allergiesLabel3.setForeground(Color.black);
                allergiesLabel3.setBackground(Color.white);
                editData.add(allergiesLabel3);

                JTextField alergiesText = new JTextField(allergies);
                alergiesText.setForeground(Color.black);
                alergiesText.setBackground(Color.white);
                alergiesText.setBorder(BorderFactory.createLineBorder(Color.black));
                Dimension prefferedAllergies = alergiesText.getPreferredSize();
                int widthAllergies = prefferedAllergies.width + 10;
                alergiesText.setBounds(120, 630, widthAllergies, 25);
                editData.add(alergiesText);

                JButton submitButton = new JButton("Отправить");
                submitButton.setBounds(120, 660, 150, 25);
                submitButton.setForeground(Color.black);
                submitButton.setBackground(Color.WHITE);
                editData.add(submitButton);
                submitButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Properties proper = new Properties();
                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                            proper.load(input);
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка,Указан не верный путь к данным");
                            throw new RuntimeException(exc);
                        }
                        {
                            try {
                                Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                                Statement statement = connection.createStatement();
                                String selectedDiscount = (String) discountComboBox.getSelectedItem();
                                statement.execute("UPDATE addperson.pacient SET name = '" + pacientName.getText() + "',cardNumber='" + cardNum.getText() + "',age = '" + age.getText() + "'" +
                                        ",telephoneNumber='" + telephoneNumber.getText() + "',parentName='" + NameOfParent.getText() + "',WorkTelephoneNum='" + workTelephoneNumber.getText() + "',documentName='" + documents.getText() + "'" +
                                        ",seriaOfDocument='" + documentNumber.getText() + "',WhoGiveDocument='" + BywhoDocumentWasGiven.getText() + "',whenDocumentGet='" + dateOfGiven.getText() + "',placeOfWork='" + whereWork.getText() + "',addres='" + whereLive.getText() + "'" +
                                        ",birthdate='" + yearBirth.getText() + "',dateRegistration='" + regDate.getText() + "',diagnose='" + diagnoseStay.getText() + "',discount='" + selectedDiscount + "',dateOfRecord='" + recordText.getText() + "'" +
                                        ",doctorName='" + doctor.getText() + "',medicine='" + consText.getText() + "',sum='" + sumText.getText() + "',allergies='" + alergiesText.getText() + "' WHERE id = '" + id + "'");
                                JOptionPane.showMessageDialog(null, "Данные успешно изменены!");
                                editData.dispose();
                                detailsFrame.dispose();
                                connection.close();
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(null, "Ошибка при изменение данных!");
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                });
                editData.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                editData.setSize(450, 730);
                editData.setLocationRelativeTo(null);
                editData.setVisible(true);
            }
        });


        deleteRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Properties proper = new Properties();
                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                    proper.load(input);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка, Указан не верный путь к данным");
                    throw new RuntimeException(exc);
                }
                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                    Statement statement = connection.createStatement();
                    statement.execute("UPDATE addperson.pacient SET dateOfRecord = NULL, timeOfRecord = NULL WHERE id='" + idForInformation + "'");
                    JOptionPane.showMessageDialog(null, "Запись успешно удалена!");
                    recordText.setText(null);
                    timeRegDate.setText(null);
                    updateCalendar();
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при выполнении запроса");
                    throw new RuntimeException(exc);
                }
            }
        });
        WriteAnContract.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Предупреждающее окно");
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setVisible(false);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JFrame frameWrite = new JFrame();
                frame.setBackground(Color.white);
                frame.dispose();
                UIManager.put("OptionPane.buttonBorder", BorderFactory.createLineBorder(Color.BLACK));
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageBackground", Color.WHITE);
                Object[] options = {"Да", "Нет"};
                int result = JOptionPane.showOptionDialog(null, "Вы уверены, что хотите продолжить? Перепроверьте данные пользователя!",
                        "Предупреждение", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (result == JOptionPane.YES_OPTION) {
                    frameWrite.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frameWrite.setSize(420, 200);
                    frameWrite.setResizable(false);
                    frameWrite.setLocationRelativeTo(null);
                    frameWrite.setVisible(true);
                    frameWrite.setLayout(null);
                    frameWrite.setTitle("Печать контракта");
                    frameWrite.setBackground(Color.BLUE);

                    JButton buttonTRUE = new JButton("Печать персональных данных!");
                    buttonTRUE.setForeground(Color.black);
                    buttonTRUE.setBackground(Color.WHITE);
                    buttonTRUE.setBorder(BorderFactory.createLineBorder(Color.black));
                    buttonTRUE.setBounds(10, 10, 205, 30);
                    buttonTRUE.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frameWrite.dispose();
                            PrinterJob printerJob = PrinterJob.getPrinterJob();
                            PageFormat pageFormat = printerJob.defaultPage();
                            Paper paper = new Paper();
                            double paperWidth = 595.22;
                            double paperHeight = 841.98;
                            double leftMargin = 50;
                            double rightMargin = 15;
                            double topMargin = 5;
                            double bottomMargin = 15;
                            paper.setSize(paperWidth, paperHeight);
                            paper.setImageableArea(leftMargin, topMargin, paperWidth - leftMargin - rightMargin, paperHeight - topMargin - bottomMargin);
                            pageFormat.setPaper(paper);
                            Date currentDate = new Date();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'г.'");
                            String formattedDate = dateFormat.format(currentDate);
                            printerJob.setPrintable(new MyPrintable("Согласие пациента на обработку персональных данных\n" +
                                    "\n" +
                                    "Я," + fullName + " ,\n" +
                                    "\n" +
                                    "(Ф.И.О. полностью)\n" +
                                    "\n" +
                                    "Проживающий по адресу: " + addresWhere + ",\n" +
                                    "\n" +
                                    "(по месту регистрации)\n" +
                                    "\n" +
                                    "Документ:" + documentName + ": " + numberDocument + "," + WhenGet + "\n" +
                                    "\n" +
                                    "(серия, дата выдачи\n" +
                                    "\n" +
                                    "Выдано:" + ByWhoGive + ",\n" +
                                    "\n" +
                                    "наименование выдавшего органа)\n" +
                                    "\n" +
                                    "В соответствии с требованиями статьи 9 федерального закона от 27.07.2006 г. «О защите " +
                                    "персональных данных» № 152-ФЗ, подтверждаю свое согласие на обработку ООО " +
                                    "«Стоматология «Доктор Дент», адрес: 352330, Краснодарский край, г.Усть - Лабинск, " +
                                    "ул.Красная, 176А (далее - Оператор) моих персональных данных, включающих: фамилию, имя, " +
                                    "отчество, пол, дату рождения, адрес проживания, контактный телефон , данные о состоянии моего " +
                                    "здоровья, заболеваниях, случаях обращения за медицинской помощью, в медико-профилактических " +
                                    "целях, в целях установления медицинского диагноза и оказания медицинских услуг, при условии, " +
                                    "что их обработка осуществляется лицом, профессионально занимающимся медицинской " +
                                    "деятельностью и обязанным сохранять врачебную тайну. \n" +
                                    "\n" +
                                    "      В процессе оказания Оператором мне медицинской помощи я предоставляю право " +
                                    "медицинским работникам, передавать мои персональные данные, содержащие сведения, " +
                                    "составляющие врачебную тайну, другим должностным лицам Оператора, в интересах моего " +
                                    "обследования и лечения.\n " +
                                    "Предоставляю Оператору право осуществлять все действия (операции) с моими персональными " +
                                    "данными, включая сбор, систематизацию, накопление, хранение, обновление, изменение, " +
                                    "использование, обезличивание, блокирование, уничтожение. Оператор вправе обрабатывать мои " +
                                    "персональные данные посредством внесения их в электронную базу данных, включения в списки " +
                                    "(реестры) и отчетные формы, предусмотренные документами, регламентирующими " +
                                    "предоставление отчетных данных.\n " +
                                    "\n" +
                                    "     Срок хранения моих персональных данных соответствует сроку хранения первичных " +
                                    "медицинских документов (медицинской карты) и составляет двадцать пять лет. " +
                                    "Передача моих персональных данных иным лицам или иное их разглашение может " +
                                    "осуществляться только с моего письменного согласия. \n" +
                                    "Настоящее согласие дано мной " + formattedDate + ". и действует бессрочно. \n" +
                                    "Я оставляю за собой право отозвать свое согласие посредством составления соответствующего " +
                                    "письменного документа, который может быть направлен мной в адрес Оператора по почте " +
                                    "заказным письмом с уведомлением о вручении либо вручен лично под расписку представителю " +
                                    "Оператора.\n " +
                                    "\n" +
                                    "В случае получения моего письменного заявления об отзыве настоящего согласия на обработку " +
                                    "персональных данных, Оператор обязан прекратить их обработку в течение периода времени, " +
                                    "необходимого для завершения взаиморасчетов по оплате оказанной мне до этого медицинской " +
                                    "помощи. " +
                                    "\n" +
                                    "Подпись субъекта персональных данных _______________________"), pageFormat);
                            if (printerJob.printDialog()) {
                                try {
                                    detailsFrame.dispose();
                                    printerJob.print();
                                } catch (PrinterException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    JButton buttonTRUE2 = new JButton("Печать согласия на лечение!");
                    buttonTRUE2.setForeground(Color.black);
                    buttonTRUE2.setBackground(Color.WHITE);
                    buttonTRUE2.setBorder(BorderFactory.createLineBorder(Color.black));
                    buttonTRUE2.setBounds(10, 70, 205, 30);
                    buttonTRUE2.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frameWrite.dispose();
                            PrinterJob printerJob = PrinterJob.getPrinterJob();
                            PageFormat pageFormat = printerJob.defaultPage();
                            Paper paper = new Paper();
                            double paperWidth = 595.22;
                            double paperHeight = 841.98;
                            double leftMargin = 10;
                            double rightMargin = 10;
                            double topMargin = 36;
                            double bottomMargin = 36;
                            paper.setSize(paperWidth, paperHeight);
                            paper.setImageableArea(leftMargin, topMargin, paperWidth - leftMargin - rightMargin, paperHeight - topMargin - bottomMargin);
                            pageFormat.setPaper(paper);
                            Date currentDate = new Date();

                            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'г.'");
                            String formattedDate = dateFormat.format(currentDate);
                            printerJob.setPrintable(new MyPrintable1("Общество с ограниченной ответственностью «Стоматология «Доктор Дент»\nИНФОРМИРОВАННОЕ СОГЛАСИЕ ПАЦИЕНТА\n" +
                                    "на стоматологическое лечение\n" +
                                    "Настоящее информированное добровольное согласие составлено в соответствии со ст. 20 Федерального\n" +
                                    "    закона от 21.11.2011 No 323-ФЗ «Об основах охраны здоровья граждан в Российской Федерации».\n" +
                                    "\n" +
                                    "\"" + formattedDate + "\"\n" +
                                    "   Я,  " + fullName + ", подтверждаю ,  что  получил(а) детальные  объяснения в устной форме о " +
                                    " необхо-\nдимости стоматологического лечения, план которого изложен" +
                                    "в медицинской карте. Мне составлена полная пись-\nменная смета на стоматологическое лечение, которая мне " +
                                    "разъяснена и понятна.\n" +
                                    "   Мне объяснен в понятной форме план стоматологического лечения, включая ожидаемые результаты, риск, пути\n" +
                                    "альтернативного  лечения, возможные   при существующей  ситуации и  в  данных обстоятельствах,  также\n" +
                                    "необходимые исследования, врачебные процедуры и манипуляции, связанные с этим. Альтернативные пути\n" +
                                    "лечения обдуманы мною до принятия решения о виде лечения.\n" +
                                    "   Мне объяснены возможные сопутствующие явления планируемого лечения: длительность, боль, неудобство,\n" +
                                    "припухлость лица, чувствительность к холоду и теплу, синяки на лице, под глазами, шее, долго не проходящее\n" +
                                    "онемение губ, щек, подбородка. Мне объяснено, и я понял(а), что существует вероятность того, что во время\n" +
                                    "осуществления плана лечения, выяснится необходимость в частичном или полном изменении плана лечения.\n" +
                                    "Могут потребоваться дополнительные врачебные процедуры, которые невозможно достоверно и в полной мере\n" +
                                    "предвидеть заранее. Возможно, потребуется направление для консультации и лечения в другие медицинские\n" +
                                    "учреждения, мне это понятно, и я с этим согласен. В связи с этим, точная продолжительность лечения, в том\n" +
                                    "числе и этапов, зависящих от лечения, например, протезирования, может изменяться.\n" +
                                    "   Я проинформирован, что в случае изменения плана лечения, возникает необходимость изменения сметы на\n" +
                                    "лечение. Я согласен полностью оплатить все дополнительные расходы на лечение и диагностику.\n" +
                                    "   Мне ясна вся важность передачи точной достоверной информации о состоянии моего здоровья, а также" +
                                    " выпол-\nнение всех полученных от врача  и персонала  рекомендаций,  касающихся  соблюдения  гигиены полости рта,\n" +
                                    "проведения консервативного лечения, в котором я буду нуждаться, визитов в указанные сроки, приема " +
                                    "лекар-\nственных препаратов, назначенных врачом, пользования аппаратами и несоблюдение указаний" +
                                    "(рекомендаций)\nисполнителя  (  медицинского  работника,   предоставляющего  платную  медицинскую услугу  ) , в том " +
                                    " числе\nназначенного режима лечения, могут снизить качество предоставляемой платной медицинской услуги," +
                                    "повлечь\nза собой невозможность ее завершения в срок или отрицательно сказаться на состоянии здоровья потре" +
                                    "бителя.\n" +
                                    "\n   Я понимаю, что практическая стоматология не является точной наукой. Гарантировать 100% положительный\n" +
                                    "результат проведенного лечения (включая имплантацию," +
                                    "лечение каналов, исправление прикуса ит.д.) не предс-\nтавляется возможным. Речь может идти о прогнозах и " +
                                    "вероятностях, которые разнятся от пациента к пациенту.\n" +
                                    "   Я понимаю, что мое состояние может измениться во время или после стоматологического лечения. Даю свое\n" +
                                    "согласие на изменения плана лечебных мероприятий и" +
                                    "применение альтернативных методов лечения. Я раз-\nрешаю любые изменения дизайна, материалов и ухода, если " +
                                    "они будут сделаны для моего блага.\n" +
                                    "  Я проинформирован(а) о состоянии своего здоровья, о всех возможных вариантах медицинского вмешательства и\n" +
                                    "методах лечения, о возможных последствиях и осложнениях, связанных с ними риском, включая независящие от\n" +
                                    "соблюдения применяемой технологии, получив полные и всесторонние разъяснения, добровольно даю" +
                                    " свое сог-\nласие на проведение обследования(лечения) в предложенном объеме." +
                                    "\n" +
                                    "\n" +
                                    "_______________/" + fullName + "/"), pageFormat);
                            if (printerJob.printDialog()) {
                                try {
                                    detailsFrame.dispose();
                                    printerJob.print();
                                } catch (PrinterException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    JButton buttonTRUE3 = new JButton("Договор");
                    buttonTRUE3.setForeground(Color.black);
                    buttonTRUE3.setBackground(Color.WHITE);
                    buttonTRUE3.setBorder(BorderFactory.createLineBorder(Color.black));
                    buttonTRUE3.setBounds(260, 10, 105, 30);
                    buttonTRUE3.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Показываем диалог выбора: сохранить или напечатать
                            Object[] options = {"Сохранить файл", "Напечатать", "Отмена"};
                            int choice = JOptionPane.showOptionDialog(null, 
                                "Выберите действие с договором:", 
                                "Договор", 
                                JOptionPane.YES_NO_CANCEL_OPTION, 
                                JOptionPane.QUESTION_MESSAGE, 
                                null, options, options[0]);
                            
                            if (choice == JOptionPane.YES_OPTION) {
                                // Сохранить файл
                                generateAndSaveContract();
                            } else if (choice == JOptionPane.NO_OPTION) {
                                // Напечатать
                                generateAndPrintContract();
                            }
                            // Если выбрано "Отмена", ничего не делаем
                        }
                        
                        private void generateAndSaveContract() {
                            try {
                                // Получаем следующий номер договора для этого пациента
                                int patientId = Integer.parseInt(id);
                                int contractNumber = GUI.Mysql.DatabaseUtil.getNextContractNumber(patientId);
                                
                                // Подготавливаем данные пациента
                                WordDocumentHelper.PatientData patientData = new WordDocumentHelper.PatientData(
                                    id, // уникальный ключ
                                    fullName,
                                    birthdateYear,
                                    addresWhere,
                                    documentName, // серия документа
                                    numberDocument, // номер документа
                                    ByWhoGive, // кем выдан
                                    WhenGet, // дата выдачи
                                    TelephoneNum, // телефон
                                    doctorName, // врач
                                    String.valueOf(contractNumber) // номер договора
                                );
                                
                                // Подготавливаем данные услуг
                                List<WordDocumentHelper.ServiceData> services = parseServices(medicina);
                                
                                // Группируем одинаковые услуги (без учета зуба для договора)
                                services = WordDocumentHelper.groupServices(services, false);
                                
                                // Генерируем договор во временный файл (без приложения)
                                String templatePath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\dogovor.docx";
                                String tempPath = WordDocumentHelper.generateContract(templatePath, patientData, services, false, false);
                                
                                // Показываем диалог выбора пути сохранения
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setDialogTitle("Сохранить договор как Word документ");
                                
                                SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                                String timestamp = fileNameFormat.format(new Date());
                                String defaultFileName = "Договор_" + fullName.replaceAll(" ", "_") + "_" + timestamp + ".docx";
                                fileChooser.setSelectedFile(new File(defaultFileName));
                                
                                int userSelection = fileChooser.showSaveDialog(null);
                                
                                if (userSelection == JFileChooser.APPROVE_OPTION) {
                                    File fileToSave = fileChooser.getSelectedFile();
                                    
                                    // Копируем временный файл в выбранное место
                                    Files.copy(Paths.get(tempPath), Paths.get(fileToSave.getAbsolutePath()));
                                    
                                    JOptionPane.showMessageDialog(null, "Договор успешно сохранен: " + fileToSave.getAbsolutePath());
                                    frameWrite.dispose();
                                }
                                
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Ошибка при создании договора: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                        
                        private void generateAndPrintContract() {
                            try {
                                // Получаем следующий номер договора для этого пациента
                                int patientId = Integer.parseInt(id);
                                int contractNumber = GUI.Mysql.DatabaseUtil.getNextContractNumber(patientId);
                                
                                // Подготавливаем данные пациента
                                WordDocumentHelper.PatientData patientData = new WordDocumentHelper.PatientData(
                                    id, // уникальный ключ
                                    fullName,
                                    birthdateYear,
                                    addresWhere,
                                    documentName, // серия документа
                                    numberDocument, // номер документа
                                    ByWhoGive, // кем выдан
                                    WhenGet, // дата выдачи
                                    TelephoneNum, // телефон
                                    doctorName, // врач
                                    String.valueOf(contractNumber) // номер договора
                                );
                                
                                // Подготавливаем данные услуг
                                List<WordDocumentHelper.ServiceData> services = parseServices(medicina);
                                
                                // Группируем одинаковые услуги (без учета зуба для договора)
                                services = WordDocumentHelper.groupServices(services, false);
                                
                                // Генерируем договор во временный файл (без приложения для печати)
                                String templatePath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\dogovor.docx";
                                String tempPath = WordDocumentHelper.generateContract(templatePath, patientData, services, false, false);
                                
                                // Открываем документ для печати
                                java.awt.Desktop.getDesktop().open(new java.io.File(tempPath));
                                
                                frameWrite.dispose();
                                
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Ошибка при создании договора: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                        

                    });

                    JButton buttonTRUE4 = new JButton("Акт исправления");
                    buttonTRUE4.setForeground(Color.black);
                    buttonTRUE4.setBackground(Color.WHITE);
                    buttonTRUE4.setBorder(BorderFactory.createLineBorder(Color.black));
                    buttonTRUE4.setBounds(260, 70, 105, 30);
                    buttonTRUE4.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                // Подготавливаем данные пациента
                                WordDocumentHelper.PatientData patientData = new WordDocumentHelper.PatientData(
                                    id, // уникальный ключ
                                    fullName,
                                    birthdateYear,
                                    addresWhere,
                                    documentName, // серия документа
                                    numberDocument, // номер документа
                                    ByWhoGive, // кем выдан
                                    WhenGet, // дата выдачи
                                    TelephoneNum, // телефон
                                    doctorName // врач
                                );
                                
                                // Подготавливаем данные услуг
                                List<WordDocumentHelper.ServiceData> services = parseServices(medicina);
                                
                                // Для акта НЕ группируем услуги, чтобы каждый зуб показывался отдельно
                                // services = WordDocumentHelper.groupServices(services);
                                
                                // Получаем скидку пациента
                                double discountValue = 0;
                                try {
                                    Properties proper = new Properties();
                                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                        proper.load(input);
                                    }
                                    
                                    try (Connection connection = DriverManager.getConnection(
                                        proper.getProperty("database.url"), 
                                        proper.getProperty("database.login"), 
                                        proper.getProperty("database.pass"))) {
                                        
                                        String query = "SELECT discount FROM addperson.pacient WHERE id = ?";
                                        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                                            stmt.setString(1, id);
                                            ResultSet rs = stmt.executeQuery();
                                            
                                            if (rs.next()) {
                                                String discountStr = rs.getString("discount");
                                                if (discountStr != null && !discountStr.equals("Нет") && discountStr.endsWith("%")) {
                                                    discountValue = Double.parseDouble(discountStr.replace("%", ""));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Ошибка при получении скидки: " + ex.getMessage());
                                }
                                
                                // Генерируем акт во временный файл
                                String templatePath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\act.docx";
                                String tempPath = WordDocumentHelper.generateAct(templatePath, patientData, services, false, discountValue);
                                
                                // Показываем диалог выбора действия
                                String[] options = {"Сохранить", "Печатать", "Отмена"};
                                int choice = JOptionPane.showOptionDialog(
                                    null,
                                    "Что вы хотите сделать с актом?",
                                    "Акт",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                                );
                                
                                if (choice == 0) { // Сохранить
                                    // Показываем диалог выбора пути сохранения
                                    JFileChooser fileChooser = new JFileChooser();
                                    fileChooser.setDialogTitle("Сохранить акт как Word документ");
                                    
                                    SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                                    String timestamp = fileNameFormat.format(new Date());
                                    String defaultFileName = "Акт_" + fullName.replaceAll(" ", "_") + "_" + timestamp + ".docx";
                                    fileChooser.setSelectedFile(new File(defaultFileName));
                                    
                                    int userSelection = fileChooser.showSaveDialog(null);
                                    
                                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                                        File fileToSave = fileChooser.getSelectedFile();
                                        
                                        // Копируем временный файл в выбранное место
                                        Files.copy(Paths.get(tempPath), Paths.get(fileToSave.getAbsolutePath()));
                                        
                                        JOptionPane.showMessageDialog(null, "Акт успешно сохранен: " + fileToSave.getAbsolutePath());
                                        frameWrite.dispose();
                                    }
                                } else if (choice == 1) { // Печатать
                                    // Печатаем документ напрямую через диалог принтера
                                    PrinterJob printerJob = PrinterJob.getPrinterJob();
                                    if (printerJob.printDialog()) {
                                        try {
                                            // Для Word документов нужно использовать специальный подход
                                            // Открываем документ в Word и отправляем на печать
                                            java.awt.Desktop.getDesktop().print(new java.io.File(tempPath));
                                            frameWrite.dispose();
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(null, "Ошибка при печати: " + ex.getMessage());
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                                // Если выбрано "Отмена" (choice == 2), ничего не делаем
                                
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Ошибка при создании акта: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    });
                    frameWrite.add(buttonTRUE);
                    frameWrite.add(buttonTRUE2);
                    frameWrite.add(buttonTRUE3);
                    frameWrite.add(buttonTRUE4);
                    JButton buttonFALSE = new JButton("Отмена");
                    buttonFALSE.setForeground(Color.black);
                    buttonFALSE.setBackground(Color.WHITE);
                    buttonFALSE.setBorder(BorderFactory.createLineBorder(Color.black));
                    buttonFALSE.setBounds(130, 130, 125, 30);
                    buttonFALSE.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frameWrite.dispose();
                        }
                    });
                    frameWrite.add(buttonFALSE);
                } else if (result == JOptionPane.NO_OPTION) {
                    JOptionPane.showMessageDialog(null, "Отлично, повторите попытку позже!!");
                }
            }
        });
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailsFrame.setSize(850, 700);
        detailsFrame.setLocationRelativeTo(null);
        detailsFrame.setVisible(true);
    }

    /**
     * Показывает окно с услугами пациента в красивом табличном виде
     * с возможностью добавления и удаления услуг
     */
    public static void showServicesWindow(String medicina, String patientId) {
        JFrame servicesFrame = new JFrame("Услуги пациента");
        servicesFrame.setLayout(new BorderLayout(10, 10));
        servicesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        servicesFrame.setSize(900, 600);
        servicesFrame.setLocationRelativeTo(null);

        // Создаем модель таблицы
        DefaultTableModel servicesTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        servicesTableModel.addColumn("№");
        servicesTableModel.addColumn("Код");
        servicesTableModel.addColumn("Наименование услуги");
        servicesTableModel.addColumn("Цена");

        // Загружаем услуги из БД и парсим их
        loadPatientServicesFromDB(patientId, servicesTableModel);

        // Создаем таблицу
        JTable servicesTable = new JTable(servicesTableModel);
        servicesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        servicesTable.setRowHeight(30);
        servicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        servicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Настраиваем ширину колонок
        servicesTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // №
        servicesTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Код
        servicesTable.getColumnModel().getColumn(2).setPreferredWidth(400); // Наименование
        servicesTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Цена

        JScrollPane scrollPane = new JScrollPane(servicesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель кнопок с красивым стилем
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton addButton = new JButton("Добавить услугу");
        addButton.setFont(new Font("Arial", Font.PLAIN, 12));
        addButton.setPreferredSize(new Dimension(150, 35));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(new Color(119, 155, 191));
        addButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addButton.addActionListener(e -> {
            showServiceSelectionDialog(servicesTableModel, patientId, servicesFrame);
        });

        JButton deleteButton = new JButton("Удалить услугу");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 12));
        deleteButton.setPreferredSize(new Dimension(150, 35));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(119, 155, 191));
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        deleteButton.addActionListener(e -> {
            deleteServiceFromPatient(servicesTable, servicesTableModel, patientId, servicesFrame);
        });

        JButton closeButton = new JButton("Закрыть");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 12));
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(119, 155, 191));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        closeButton.addActionListener(e -> {
            servicesFrame.dispose();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        // Информационная панель
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><i>Выберите услугу из базы данных для добавления. Удаление услуг сохраняется в базе данных.</i></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoPanel.add(infoLabel);

        // Компоновка
        servicesFrame.add(scrollPane, BorderLayout.CENTER);
        servicesFrame.add(buttonPanel, BorderLayout.SOUTH);
        servicesFrame.add(infoPanel, BorderLayout.NORTH);

        servicesFrame.setVisible(true);
    }

    /**
     * Загружает услуги пациента из базы данных
     */
    private static void loadPatientServicesFromDB(String patientId, DefaultTableModel model) {
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении к базе данных");
            return;
        }

        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            String query = "SELECT medicine FROM addperson.pacient WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, patientId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String medicina = resultSet.getString("medicine");
                if (medicina != null && !medicina.isEmpty()) {
                    parseServicesToTable(medicina, model, connection);
                }
            }

            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке услуг: " + ex.getMessage());
        }
    }

    /**
     * Парсит строку с услугами и возвращает список ServiceData
     * Формат: "Зуб X. Название услуги//" или "Название услуги//"
     */
    private static List<WordDocumentHelper.ServiceData> parseServices(String medicinaString) {
        List<WordDocumentHelper.ServiceData> services = new ArrayList<>();
        
        System.out.println("=== ПАРСИНГ УСЛУГ ===");
        System.out.println("Входная строка: " + medicinaString);
        
        if (medicinaString != null && !medicinaString.isEmpty()) {
            // Парсим строку с услугами
            // Формат: "Зуб X. Услуга1//Зуб Y. Услуга2//"
            String[] serviceParts = medicinaString.split("//");
            
            System.out.println("Количество частей: " + serviceParts.length);
            
            for (String part : serviceParts) {
                if (!part.trim().isEmpty()) {
                    // Извлекаем номер зуба и название услуги
                    String tooth = null;
                    String serviceName = part.trim();
                    
                    // Проверяем, есть ли формат "Зуб X. Услуга"
                    if (part.matches(".*Зуб \\d+\\..*")) {
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Зуб (\\d+)\\.");
                        java.util.regex.Matcher matcher = pattern.matcher(part);
                        if (matcher.find()) {
                            tooth = matcher.group(1);
                            serviceName = part.replaceFirst("Зуб \\d+\\.\\s*", "").trim();
                        }
                    }
                    
                    System.out.println("Обработка услуги: " + serviceName + ", зуб: " + (tooth != null ? tooth : "нет"));
                    
                    // Получаем код и цену услуги из базы данных
                    try {
                        Properties proper = new Properties();
                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                            proper.load(input);
                        }
                        
                        try (Connection connection = DriverManager.getConnection(
                            proper.getProperty("database.url"), 
                            proper.getProperty("database.login"), 
                            proper.getProperty("database.pass"))) {
                            
                            String query = "SELECT code, price FROM resources.medicine WHERE medicine = ?";
                            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                                stmt.setString(1, serviceName);
                                ResultSet rs = stmt.executeQuery();
                                
                                if (rs.next()) {
                                    String code = rs.getString("code");
                                    double price = rs.getDouble("price");
                                    
                                    System.out.println("Найдена в базе: " + serviceName + ", код: " + code + ", цена: " + price);
                                    services.add(new WordDocumentHelper.ServiceData(code, serviceName, 1, price, tooth));
                                } else {
                                    System.out.println("НЕ найдена в базе: " + serviceName + ", добавляем с дефолтными значениями");
                                    // Если не найдена в базе, добавляем с дефолтными значениями
                                    services.add(new WordDocumentHelper.ServiceData("UNKNOWN", serviceName, 1, 0.0, tooth));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Ошибка при получении данных услуги: " + serviceName + ", добавляем с дефолтными значениями");
                        ex.printStackTrace();
                        // При ошибке тоже добавляем услугу с дефолтными значениями
                        services.add(new WordDocumentHelper.ServiceData("ERROR", serviceName, 1, 0.0));
                    }
                }
            }
        }
        
        System.out.println("Итого услуг добавлено: " + services.size());
        System.out.println("=== ПАРСИНГ УСЛУГ ЗАВЕРШЕН ===");
        
        return services;
    }

    /**
     * Парсит строку с услугами и добавляет их в таблицу
     * Формат: "Зуб X. Название услуги//" или "Название услуги//"
     */
    private static void parseServicesToTable(String medicina, DefaultTableModel model, Connection connection) {
        if (medicina == null || medicina.isEmpty()) {
            return;
        }

        // Парсим услуги из строки (формат: Название услуги// или Зуб X. Название услуги//)
        String[] services = medicina.split("//");
        int counter = 1;

        for (String service : services) {
            if (service.isEmpty()) continue;

            try {
                String tooth = "";
                String serviceName = "";
                
                // Проверяем, есть ли информация о зубе
                if (service.startsWith("Зуб ")) {
                    int dotIndex = service.indexOf(". ");
                    if (dotIndex != -1) {
                        tooth = service.substring(4, dotIndex); // Получаем номер зуба
                        serviceName = service.substring(dotIndex + 2); // Получаем название услуги
                    } else {
                        serviceName = service; // Если формат неправильный, берем всю строку
                    }
                } else {
                    serviceName = service.trim(); // Просто название услуги
                }

                if (!serviceName.isEmpty()) {
                    // Получаем код и цену из базы данных
                    String code = "";
                    String price = "";
                    
                    try {
                        String query = "SELECT code, price FROM resources.medicine WHERE medicine = ?";
                        PreparedStatement statement = connection.prepareStatement(query);
                        statement.setString(1, serviceName);
                        ResultSet resultSet = statement.executeQuery();
                        
                        if (resultSet.next()) {
                            code = resultSet.getString("code");
                            price = resultSet.getString("price");
                        } else {
                            code = "";
                            price = "0";
                        }
                        
                        resultSet.close();
                        statement.close();
                    } catch (SQLException ex) {
                        code = "";
                        price = "0";
                    }

                    // Формируем отображаемое название с учетом зуба
                    String displayName = tooth.isEmpty() ? serviceName : "Зуб " + tooth + ". " + serviceName;
                    
                    model.addRow(new Object[]{counter++, code, displayName, price});
                }
            } catch (Exception e) {
                System.out.println("Ошибка при парсинге услуги: " + service);
            }
        }
    }

    /**
     * Показывает диалог выбора услуг из базы данных
     */
    private static void showServiceSelectionDialog(DefaultTableModel model, String patientId, JFrame parentFrame) {
        JDialog selectionDialog = new JDialog(parentFrame, "Выбор услуг из базы данных", true);
        selectionDialog.setSize(500, 600);
        selectionDialog.setLocationRelativeTo(parentFrame);
        selectionDialog.setLayout(new BorderLayout(10, 10));

        // Создаем модель для списка услуг
        DefaultListModel<String> availableServicesModel = new DefaultListModel<>();
        JList<String> availableServicesList = new JList<>(availableServicesModel);
        availableServicesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableServicesList.setFont(new Font("Arial", Font.PLAIN, 12));

        // Панель поиска
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        JLabel searchLabel = new JLabel("Поиск:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Добавляем поиск
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText().trim();
                if (searchText.isEmpty()) {
                    // Если поле поиска пустое, загружаем все услуги
                    loadAllServices(availableServicesModel, selectionDialog);
                } else {
                    // Иначе ищем услуги
                    searchServices(availableServicesModel, searchText, selectionDialog);
                }
            }
        });

        // Загружаем услуги из базы данных
        loadAllServices(availableServicesModel, selectionDialog);

        JScrollPane scrollPane = new JScrollPane(availableServicesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton confirmButton = new JButton("Добавить выбранные");
        confirmButton.setFont(new Font("Arial", Font.PLAIN, 12));
        confirmButton.setPreferredSize(new Dimension(150, 35));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(new Color(119, 155, 191));
        confirmButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        confirmButton.addActionListener(e -> {
            List<String> selectedServices = availableServicesList.getSelectedValuesList();
            if (selectedServices.isEmpty()) {
                JOptionPane.showMessageDialog(selectionDialog, "Выберите хотя бы одну услугу!");
                return;
            }

            // Добавляем выбранные услуги в таблицу и сохраняем в БД
            addSelectedServicesToPatient(selectedServices, model, patientId, parentFrame);
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(119, 155, 191));
        cancelButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cancelButton.addActionListener(e -> {
            selectionDialog.dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // Информационная панель
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><i>Удерживайте Ctrl для выбора нескольких услуг. При добавлении можно выбрать зуб.</i></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoPanel.add(infoLabel);

        // Компонуем панели
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        selectionDialog.add(topPanel, BorderLayout.NORTH);
        selectionDialog.add(scrollPane, BorderLayout.CENTER);
        selectionDialog.add(buttonPanel, BorderLayout.SOUTH);

        selectionDialog.setVisible(true);
    }
    
    /**
     * Загружает все услуги из базы данных
     */
    private static void loadAllServices(DefaultListModel<String> model, JDialog parentDialog) {
        model.clear();
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(parentDialog, "Произошла ошибка при подключении к базе данных");
            return;
        }

        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            String query = "SELECT DISTINCT code, medicine, price FROM resources.medicine ORDER BY medicine";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String code = resultSet.getString("code");
                String name = resultSet.getString("medicine");
                String price = resultSet.getString("price");
                model.addElement(name + " (" + price + " руб.)");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parentDialog, "Ошибка при загрузке услуг: " + ex.getMessage());
        }
    }
    
    /**
     * Ищет услуги по тексту
     */
    private static void searchServices(DefaultListModel<String> model, String searchText, JDialog parentDialog) {
        model.clear();
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(parentDialog, "Произошла ошибка при подключении к базе данных");
            return;
        }

        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            String query = "SELECT DISTINCT code, medicine, price FROM resources.medicine WHERE medicine LIKE ? OR code LIKE ? OR price LIKE ? ORDER BY medicine";
            PreparedStatement statement = connection.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String code = resultSet.getString("code");
                String name = resultSet.getString("medicine");
                String price = resultSet.getString("price");
                model.addElement(name + " (" + price + " руб.)");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parentDialog, "Ошибка при поиске услуг: " + ex.getMessage());
        }
    }

    /**
     * Добавляет выбранные услуги пациенту и сохраняет в БД
     */
    private static void addSelectedServicesToPatient(List<String> selectedServices, DefaultTableModel model, String patientId, JFrame parentFrame) {
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении к базе данных");
            return;
        }

        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            // Сначала получаем текущие услуги пациента
            String currentMedicina = "";
            String query = "SELECT medicine FROM addperson.pacient WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, patientId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                currentMedicina = resultSet.getString("medicine");
                if (currentMedicina == null) {
                    currentMedicina = "";
                }
            }

            resultSet.close();
            statement.close();

            // Добавляем новые услуги
            StringBuilder newServices = new StringBuilder(currentMedicina);
            for (String service : selectedServices) {
                // Парсим формат: название (цена руб.)
                String[] parts = service.split(" \\(");
                if (parts.length >= 2) {
                    String name = parts[0];
                    String price = parts[1].replace(" руб.)", "");

                    // Спрашиваем, нужно ли выбрать зуб
                    int toothChoice = JOptionPane.showConfirmDialog(parentFrame, 
                        "Выбрать зуб для услуги: " + name + "?", 
                        "Выбор зуба", 
                        JOptionPane.YES_NO_OPTION);

                    String serviceString = "";
                    if (toothChoice == JOptionPane.YES_OPTION) {
                        // Открываем диалог выбора зуба
                        ToothSelectionDialog toothDialog = new ToothSelectionDialog(parentFrame);
                        toothDialog.setVisible(true);
                        String selectedTooth = toothDialog.getSelectedToothNumber();
                        
                        if (selectedTooth != null && !selectedTooth.isEmpty()) {
                            serviceString = "Зуб " + selectedTooth + ". " + name + "//";
                        } else {
                            serviceString = name + "//";
                        }
                    } else {
                        serviceString = name + "//";
                    }

                    // Добавляем к текущим услугам с правильным разделителем (без проверки на дубликаты)
                    if (!currentMedicina.isEmpty()) {
                        newServices.append(serviceString);
                    } else {
                        newServices.append(serviceString);
                    }

                    // Получаем код услуги
                    String code = "";
                    try {
                        String codeQuery = "SELECT code FROM resources.medicine WHERE medicine = ?";
                        PreparedStatement codeStatement = connection.prepareStatement(codeQuery);
                        codeStatement.setString(1, name);
                        ResultSet codeResultSet = codeStatement.executeQuery();
                        
                        if (codeResultSet.next()) {
                            code = codeResultSet.getString("code");
                        } else {
                            code = "";
                        }
                        
                        codeResultSet.close();
                        codeStatement.close();
                    } catch (SQLException ex) {
                        code = "";
                    }

                    // Добавляем в таблицу
                    int newRow = model.getRowCount() + 1;
                    model.addRow(new Object[]{newRow, code, serviceString.replace("//", ""), price});
                }
            }

            // Обновляем запись в БД
            String updateQuery = "UPDATE addperson.pacient SET medicine = ? WHERE id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
            updateStatement.setString(1, newServices.toString());
            updateStatement.setString(2, patientId);
            updateStatement.executeUpdate();
            updateStatement.close();

            // Пересчитываем и обновляем общую сумму
            updatePatientTotalSum(patientId, connection);

            JOptionPane.showMessageDialog(null, "Услуги успешно добавлены!");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при добавлении услуг: " + ex.getMessage());
        }
    }

    /**
     * Удаляет выбранную услугу из таблицы и базы данных
     */
    private static void deleteServiceFromPatient(JTable table, DefaultTableModel model, String patientId, JFrame parentFrame) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentFrame, "Выберите услугу для удаления!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parentFrame, 
            "Вы уверены, что хотите удалить эту услугу?", 
            "Подтверждение удаления", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String displayName = (String) model.getValueAt(selectedRow, 2);
            
            // Удаляем из таблицы
            model.removeRow(selectedRow);
            
            // Обновляем нумерацию
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }

            // Удаляем из базы данных
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении к базе данных");
                return;
            }

            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                // Получаем текущие услуги
                String currentMedicina = "";
                String query = "SELECT medicine FROM addperson.pacient WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, patientId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    currentMedicina = resultSet.getString("medicine");
                    if (currentMedicina == null) {
                        currentMedicina = "";
                    }
                }

                resultSet.close();
                statement.close();

                // Удаляем услугу из строки (формат: "Зуб X. Название//" или "Название//")
                // Сначала пробуем точное совпадение
                String serviceToRemove = displayName + "//";
                String newMedicina = currentMedicina.replace(serviceToRemove, "");
                
                // Если точное совпадение не сработало, пробуем с пробелами
                if (newMedicina.equals(currentMedicina)) {
                    serviceToRemove = displayName.trim() + "//";
                    newMedicina = currentMedicina.replace(serviceToRemove, "");
                }
                
                // Если всё ещё не сработало, пробуем удалить по частичному совпадению названия
                if (newMedicina.equals(currentMedicina)) {
                    // Извлекаем только название услуги (без зуба если есть)
                    String serviceName = displayName;
                    if (displayName.startsWith("Зуб ")) {
                        int dotIndex = displayName.indexOf(". ");
                        if (dotIndex != -1) {
                            serviceName = displayName.substring(dotIndex + 2);
                        }
                    }
                    
                    // Удаляем по названию с учетом возможного зуба
                    String[] services = currentMedicina.split("//");
                    StringBuilder result = new StringBuilder();
                    
                    for (String service : services) {
                        if (service.isEmpty()) continue;
                        
                        // Проверяем, содержит ли услуга наше название
                        boolean shouldRemove = false;
                        if (service.contains(serviceName)) {
                            shouldRemove = true;
                        }
                        
                        if (!shouldRemove) {
                            if (result.length() > 0) {
                                result.append("//");
                            }
                            result.append(service);
                        }
                    }
                    
                    newMedicina = result.toString();
                }
                
                // Убираем лишние разделители
                newMedicina = newMedicina.replace("////", "//");
                if (newMedicina.startsWith("//")) {
                    newMedicina = newMedicina.substring(2);
                }
                if (newMedicina.endsWith("//")) {
                    newMedicina = newMedicina.substring(0, newMedicina.length() - 2);
                }
                
                // Если строка пустая или содержит только пробелы, устанавливаем в пустую строку
                if (newMedicina.trim().isEmpty()) {
                    newMedicina = "";
                }

                // Обновляем запись в БД
                String updateQuery = "UPDATE addperson.pacient SET medicine = ? WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setString(1, newMedicina);
                updateStatement.setString(2, patientId);
                updateStatement.executeUpdate();
                updateStatement.close();

                // Пересчитываем и обновляем общую сумму
                updatePatientTotalSum(patientId, connection);

                JOptionPane.showMessageDialog(null, "Услуга успешно удалена!");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Ошибка при удалении услуги: " + ex.getMessage());
            }
        }
    }

    /**
     * Пересчитывает и обновляет общую сумму пациента
     */
    private static void updatePatientTotalSum(String patientId, Connection connection) {
        try {
            // Получаем услуги пациента
            String query = "SELECT medicine FROM addperson.pacient WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, patientId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String medicina = resultSet.getString("medicine");
                if (medicina != null && !medicina.trim().isEmpty()) {
                    // Парсим услуги и суммируем цены
                    double totalSum = 0.0;
                    String[] services = medicina.split("//");
                    
                    for (String service : services) {
                        if (service.isEmpty()) continue;
                        
                        // Извлекаем название услуги (без зуба если есть)
                        String serviceName = service;
                        if (service.startsWith("Зуб ")) {
                            int dotIndex = service.indexOf(". ");
                            if (dotIndex != -1) {
                                serviceName = service.substring(dotIndex + 2);
                            }
                        }
                        
                        // Получаем цену из базы данных
                        try {
                            String priceQuery = "SELECT price FROM resources.medicine WHERE medicine = ?";
                            PreparedStatement priceStatement = connection.prepareStatement(priceQuery);
                            priceStatement.setString(1, serviceName);
                            ResultSet priceResultSet = priceStatement.executeQuery();
                            
                            if (priceResultSet.next()) {
                                double price = priceResultSet.getDouble("price");
                                totalSum += price;
                            }
                            
                            priceResultSet.close();
                            priceStatement.close();
                        } catch (SQLException ex) {
                            System.out.println("Ошибка при получении цены для услуги: " + serviceName);
                        }
                    }
                    
                    // Получаем скидку пациента
                    String discountQuery = "SELECT discount FROM addperson.pacient WHERE id = ?";
                    PreparedStatement discountStatement = connection.prepareStatement(discountQuery);
                    discountStatement.setString(1, patientId);
                    ResultSet discountResultSet = discountStatement.executeQuery();
                    
                    String discount = "Нет";
                    if (discountResultSet.next()) {
                        discount = discountResultSet.getString("discount");
                    }
                    
                    discountResultSet.close();
                    discountStatement.close();
                    
                    // Применяем скидку если есть
                    double finalSum = totalSum;
                    if (discount != null && !discount.equals("Нет")) {
                        if (discount.endsWith("%")) {
                            double discountValue = Double.parseDouble(discount.replace("%", "")) / 100;
                            finalSum = totalSum * (1 - discountValue);
                        }
                    }
                    
                    // Обновляем сумму в БД
                    String updateSumQuery = "UPDATE addperson.pacient SET sum = ? WHERE id = ?";
                    PreparedStatement updateSumStatement = connection.prepareStatement(updateSumQuery);
                    updateSumStatement.setString(1, String.format("%.2f", finalSum));
                    updateSumStatement.setString(2, patientId);
                    updateSumStatement.executeUpdate();
                    updateSumStatement.close();
                } else {
                    // Если услуг нет, устанавливаем сумму в 0
                    String updateSumQuery = "UPDATE addperson.pacient SET sum = ? WHERE id = ?";
                    PreparedStatement updateSumStatement = connection.prepareStatement(updateSumQuery);
                    updateSumStatement.setString(1, "0.00");
                    updateSumStatement.setString(2, patientId);
                    updateSumStatement.executeUpdate();
                    updateSumStatement.close();
                }
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при обновлении общей суммы: " + ex.getMessage());
        }
    }

    private void saveReportAsPDF(LocalDate date, String filePath) {
        Properties proper = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
            throw new RuntimeException(exc);
        }

        try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
            Statement statement = connection.createStatement();
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ResultSet resultSet = statement.executeQuery("SELECT id, name, doctorName, dateOfRecord, sum, discount, medicine FROM addperson.pacient WHERE dateOfRecord='" + dateStr + "'");

            // Создание HTML-строки
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head>")
                    .append("<meta charset=\"UTF-8\"/>") // Установка кодировки UTF-8
                    .append("<style>")
                    .append("body { font-family: 'Arial Unicode MS', Arial, sans-serif; }") // Используем шрифт, поддерживающий кириллицу
                    .append("</style></head><body>")
                    .append("<h1>Отчет за ").append(dateStr).append("</h1>")
                    .append("<table border='1'><tr><th>ID</th><th>ФИО</th><th>Доктор</th><th>Дата записи</th><th>Стоимость</th><th>Скидка</th><th>К оплате</th></tr>");

            double totalSum = 0;

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String doctorName = resultSet.getString("doctorName");
                String dateOfRecord = resultSet.getString("dateOfRecord");
                String sum = resultSet.getString("sum"); // Это стоимость без скидки
                String discount = resultSet.getString("discount");
                String medicines = resultSet.getString("medicine");

                double discountValue = discount.equals("Нет") ? 0 : Double.parseDouble(discount.replace("%", "")) / 100;
                double totalToPay = Double.parseDouble(sum); // это итоговая сумма к оплате (после скидки)
                
                // Вычисляем оригинальную цену до скидки
                double originalPrice;
                if (discountValue > 0 && discountValue < 1) {
                    originalPrice = totalToPay / (1 - discountValue); // восстанавливаем цену до скидки
                } else {
                    originalPrice = totalToPay; // если скидки нет
                }
                double sumValue = originalPrice;

                htmlBuilder.append("<tr>")
                        .append("<td>").append(id).append("</td>")
                        .append("<td>").append(name).append("</td>")
                        .append("<td>").append(doctorName).append("</td>")
                        .append("<td>").append(dateOfRecord).append("</td>")
                        .append("<td>").append(String.format("%.2f", sumValue)).append("</td>")
                        .append("<td>").append(discount).append("</td>")
                        .append("<td>").append(String.format("%.2f", totalToPay)).append("</td>")
                        .append("</tr>");
                totalSum += totalToPay; // Обновляем общую сумму
            }

            htmlBuilder.append("</table>")
                    .append("<h2>Итого: ").append(String.format("%.2f", totalSum)).append(" руб.</h2>")
                    .append("</body></html>");

            // Генерация PDF
            try (FileOutputStream os = new FileOutputStream(filePath)) {
                ITextRenderer renderer = new ITextRenderer();
                // Загрузка шрифта, поддерживающего кириллицу
                String fontPath = "src/main/resources/arialmt.ttf"; // Укажите путь к файлу шрифта
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                renderer.setDocumentFromString(htmlBuilder.toString());
                renderer.layout();
                renderer.createPDF(os);
            } catch (DocumentException | IOException e) {
                throw new RuntimeException(e);
            }
            JOptionPane.showMessageDialog(null, "Отчет сохранен как PDF!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для получения цены лекарства
    private double getMedicinePrice(Connection connection, String medicine) {
        double price = 0;
        String query = "SELECT price FROM resources.medicine WHERE medicine = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, medicine);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                price = Double.parseDouble(rs.getString("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }



    private static int getDaysInMonth(int year, int month) {
        if (month == 2) {
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                return 29;
            } else {
                return 28;
            }
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else {
            return 31;
        }
    }

    private static String getMonthName(int month) {
        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };
        return monthNames[month - 1];
    }

    private static void updateCalendar() {
        Dimension prefferedRecord = recordText.getPreferredSize();
        int widthRecord = prefferedRecord.width + 10;
        Dimension preferredSizeWhenRegtime = timeRegDate.getPreferredSize();
        int widthRegTime = preferredSizeWhenRegtime.width + 10;
        calendarPanel.removeAll();
        int daysInMonth = getDaysInMonth(year, month);
        for (int i = 1; i <= daysInMonth; i++) {
            String dayNumber = String.format("%02d", i);
            JLabel dayLabel = new JLabel(dayNumber);
            dayLabel.setBackground(Color.WHITE);
            dayLabel.setHorizontalAlignment(JLabel.CENTER);
            dayLabel.setVerticalAlignment(JLabel.CENTER);
            dayLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            dayLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            calendarPanel.add(dayLabel);
            isDaySelected = true;
            if (checkerForADDrecord) {
                isDaySelected = true;
                dayLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (isDaySelected) {
                            StringBuilder stringForRecord = new StringBuilder();
                            stringForRecord.append(yearLabel.getText());
                            stringForRecord.append("-");
                            stringForRecord.append(getMonthNum(monthLabel.getText()));
                            stringForRecord.append("-");
                            String day = dayLabel.getText();
                            stringForRecord.append(day);
                            Properties proper = new Properties();
                            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                proper.load(input);
                            } catch (Exception exc) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                                throw new RuntimeException(exc);
                            }
                            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                                Statement statement = connection.createStatement();
                                ResultSet resultSet = statement.executeQuery("SELECT * FROM addperson.pacient WHERE id='" + idForInformation + "'");
                                if (resultSet.next()) {
                                    String record = resultSet.getString("dateOfRecord");
                                    if (record == null) {
                                        dayLabel.setOpaque(true);
                                        dayLabel.setForeground(Color.WHITE);
                                        dayLabel.setBackground(new Color(119, 155, 191));
                                        isDaySelected = false;
                                        statement.executeUpdate("UPDATE addperson.pacient SET dateOfRecord = '" + stringForRecord + "' WHERE id='" + idForInformation + "'");
                                        recordText.setText(String.valueOf(stringForRecord));
                                        recordText.setBounds(130, 545, widthRecord, 25);
                                        timeRegDate.setBounds(recordText.getX() + 20, 545, widthRegTime, 25);
                                        JOptionPane.showMessageDialog(null, "Запись осуществлена!");

                                        JFrame RecordChoose = new JFrame();
                                        RecordChoose.setTitle("Выберите время для записи");
                                        RecordChoose.setSize(400, 200);
                                        RecordChoose.setLocationRelativeTo(null);
                                        JPanel addRecordPanel = new JPanel();
                                        addRecordPanel.setLayout(null);

                                        JLabel dateTime = new JLabel("Дата:");
                                        dateTime.setBounds(30, 40, 100, 25);
                                        addRecordPanel.add(dateTime);

                                        JSpinner timeHour = new JSpinner(new SpinnerDateModel());
                                        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeHour, "HH:mm");
                                        timeHour.setEditor(timeEditor);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(Calendar.HOUR_OF_DAY, 15);
                                        calendar.set(Calendar.MINUTE, 0);

                                        timeHour.setValue(calendar.getTime());
                                        timeHour.setBounds(130, 40, 200, 25);
                                        addRecordPanel.add(timeHour);

                                        JButton saveButton = new JButton("Отправить");
                                        saveButton.setBounds(130, 70, 100, 30);
                                        addRecordPanel.add(saveButton);
                                        saveButton.setForeground(Color.WHITE);
                                        saveButton.setBackground(new Color(119, 155, 191));
                                        saveButton.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                                String selectedTimeStr = timeFormat.format(timeHour.getValue());
                                                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                                                    Statement statement = connection.createStatement();
                                                    statement.executeUpdate("UPDATE addperson.pacient SET timeOfRecord='" + selectedTimeStr + "'WHERE id='" + idForInformation + "'");
                                                    JOptionPane.showMessageDialog(null, "Запись успешно добавлена!");
                                                    detailsFrame.dispose();
                                                    RecordChoose.dispose();
                                                } catch (Exception exception) {
                                                    throw new RuntimeException(exception);
                                                }
                                            }
                                        });
                                        RecordChoose.add(addRecordPanel);
                                        RecordChoose.setVisible(true);
                                        updateCalendar();
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Запись уже существует! ");
                                    }
                                }
                            } catch (Exception exc) {
                                JOptionPane.showMessageDialog(null, "Видимо у вас уже есть запись,перед добавлением новой-удалите предыдущию");
                                throw new RuntimeException(exc);
                            }
                        }
                    }

                });
            }
            String monthNumber = String.format("%02d", month);
            String daynumber = String.format("%02d", i);
            String date = year + "-" + monthNumber + "-" + daynumber;
            try {
                Properties proper = new Properties();
                try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                    proper.load(input);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Произошла ошибка при подключении");
                    throw new RuntimeException(exc);
                }
                try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM addperson.pacient WHERE id='" + idForInformation + "'");
                    if (resultSet.next()) {
                        String record = resultSet.getString("dateOfRecord");
                        if (!(record == null)) {
                            if (record.equals(date) && !record.isEmpty()) {
                                dayLabel.setOpaque(true);
                                dayLabel.setForeground(Color.WHITE);
                                dayLabel.setBackground(new Color(119, 155, 191));
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Ошибка при выводе записи");
                    throw new RuntimeException(e);
                }
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "ПРОИЗОШЛА НЕИЗВЕСТНАЯ ОШИБКА");
                throw new RuntimeException(exc);
            }
        }
        recordText.setBounds(130, 545, widthRecord, 25);
        timeRegDate.setBorder(BorderFactory.createLineBorder(new Color(119, 155, 191), 2));
        if (recordText.getText() == null) {
            timeRegDate.setBounds(recordText.getX() + 20, 545, widthRegTime, 25);
        } else {
            timeRegDate.setBounds(recordText.getX() + 100, 545, widthRegTime, 25);
        }
        calendarPanel.revalidate();
        calendarPanel.repaint();
        yearLabel.setText(String.valueOf(year));
        monthLabel.setText(getMonthName(month));
    }

    private static String getMonthNum(String month) {
        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };
        for (int i = 0; i < monthNames.length; i++) {
            if (monthNames[i].equals(month)) {
                return String.format("%02d", i + 1);
            }
        }
        return null;
    }

    private void createNewFrame() {
        JFrame newFrame = new JFrame();
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setSize(600, 400);
        newFrame.setLayout(new BorderLayout());
        newFrame.setTitle("Распечатка карты");
        JTextArea textArea = new JTextArea("«____»______________ 202_г.\n" +
                "Жалобы: постоянная боль на верхнейнижней челюсти\n" +
                "справаслева, боль при накусывании, чувство выросшего зуба, в\n" +
                "течение 1-2 дней.\n" +
                "Объективно: зуб _____ - глубокая кариозная полость,\n" +
                "зондирование дна и стенок безболезненно, реакция на холод\n" +
                "отрицательная, перкуссия болезненная. Переходная складка в\n" +
                "области проекции корнейня _______ зуба болезненна при\n" +
                "пальпации, отечна.\n" +
                "Диагноз: обострение хронического периодонтита _____ зуба.\n" +
                "Лечение: препарирование кариозной полости_____ зуба, вскрыта и\n" +
                "раскрыта пульпарная полость, инструментальная и\n" +
                "медикаментозная обработка _____ каналов, зуб оставлен открытым\n" +
                "для оттока гнойного экссудата.\n" +
                "К 04.5 А16.07.030.001\n" +
                "А 16.07.002.009\n" +
                "\n" +
                "______________");
        JComboBox<String> comboBox = new JComboBox<>();
        textArea.setEditable(true);
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
            properties.load(input);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при загрузке файла настроек");
            throw new RuntimeException(e);
        }

        try (Connection connection = DriverManager.getConnection(properties.getProperty("database.url"), properties.getProperty("database.login"), properties.getProperty("database.pass"))) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT name, text FROM schablonCard.cards");
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String text = resultSet.getString("text");
                    comboBox.addItem(name);
                    comboBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (name.equals(comboBox.getSelectedItem())) {
                                textArea.setText(text);
                            }
                        }
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при установлении соединения с базой данных");
            throw new RuntimeException(e);
        }

        JButton printButton = new JButton("Печать");
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(new MyPrintable4(textArea.getText()));
                boolean doPrint = job.printDialog();
                if (doPrint) {
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton createButton = new JButton("Создать свою карточку");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame inputFrame = new JFrame();
                inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                inputFrame.setSize(400, 450);
                inputFrame.setLayout(new BorderLayout());
                inputFrame.setTitle("Создание новой карточки");

                JPanel inputPanel = new JPanel();
                inputPanel.setLayout(null);

                JLabel nameLabel = new JLabel("Имя лечения:");
                nameLabel.setBounds(155, 3, 150, 25);
                JTextField nameField = new JTextField();
                nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                nameField.setBounds(120, 20, 150, 25);
                JLabel textField = new JLabel("Текст:");
                textField.setBounds(175, 50, 165, 25);
                JTextArea textArea = new JTextArea();
                textArea.setBounds(5, 75, 375, 300);
                textArea.setBorder(BorderFactory.createLineBorder(Color.black));

                inputPanel.add(nameLabel);
                inputPanel.add(nameField);
                inputPanel.add(textField);
                inputPanel.add(textArea);

                JButton saveButton = new JButton("Сохранить");
                saveButton.setBounds(150, 350, 125, 25);
                saveButton.setBorder(BorderFactory.createLineBorder(Color.black));
                saveButton.setBackground(new Color(119, 155, 191));
                saveButton.setForeground(Color.white);
                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String name = nameField.getText();
                        String text = textArea.getText();
                        if (name != null && text != null) {
                            Properties properties = new Properties();
                            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                properties.load(input);
                            } catch (Exception exc) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при загрузке файла настроек");
                                throw new RuntimeException(exc);
                            }

                            try (Connection connection = DriverManager.getConnection(properties.getProperty("database.url"), properties.getProperty("database.login"), properties.getProperty("database.pass"))) {
                                try (Statement statement = connection.createStatement()) {
                                    statement.execute("INSERT INTO schablonCard.cards(name,text) VALUES ( '" + name + "','" + text + "');");
                                    JOptionPane.showMessageDialog(null, "Карточка успешно добавлена");
                                    newFrame.dispose();
                                    createNewFrame();
                                }
                            } catch (SQLException exc) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при установлении соединения с базой данных");
                                throw new RuntimeException(exc);
                            }
                        }
                        inputFrame.dispose();
                    }
                });

                inputFrame.add(inputPanel, BorderLayout.CENTER);
                inputFrame.add(saveButton, BorderLayout.SOUTH);

                inputFrame.setLocationRelativeTo(null);
                inputFrame.setVisible(true);
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(comboBox);
        topPanel.add(printButton);
        topPanel.add(createButton);

        newFrame.add(topPanel, BorderLayout.NORTH);
        newFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        newFrame.setLocationRelativeTo(null);
        newFrame.setVisible(true);

    }


    static class MyPrintable4 implements Printable {
        private final String textToPrint;

        public MyPrintable4(String text) {
            this.textToPrint = text;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.setFont(new Font("Serif", Font.PLAIN, 10));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw each line of text on the page
            int y = 0;
            int lineHeight = g2d.getFontMetrics().getHeight();
            for (String line : textToPrint.split("\n")) {
                y += lineHeight;
                g2d.drawString(line, 0, y);
            }

            // Draw a rectangle around the text
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(0, 0, 440, y + lineHeight);

            return PAGE_EXISTS;
        }
    }
}

class MyPrintable implements Printable {
    private final String textToPrint;
    private final Font titleFont;
    private final Font contentFont;
    private final Font contentFont2;
    private final Font contentFontBOLD;
    private final Font contentFontItalic;

    public MyPrintable(String textToPrint) {
        this.textToPrint = textToPrint;
        this.titleFont = new Font("Arial", Font.BOLD, 14);
        this.contentFont = new Font("Arial", Font.PLAIN, 10);
        this.contentFont2 = new Font("Arial", Font.PLAIN, 10);
        this.contentFontBOLD = new Font("Arial", Font.BOLD, 10);
        this.contentFontItalic = new Font("Arial", Font.ITALIC, 8);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        g2d.setFont(titleFont);
        FontMetrics titleFontMetrics = g2d.getFontMetrics(titleFont);
        int titleLineHeight = titleFontMetrics.getHeight();

        g2d.setFont(contentFont);
        FontMetrics contentFontMetrics = g2d.getFontMetrics(contentFont);
        int contentLineHeight = contentFontMetrics.getHeight();
        FontMetrics contentFontMetrics2 = g2d.getFontMetrics(contentFont2);
        int contentLineHeight2 = contentFontMetrics2.getHeight();

        int x = 1;
        int y = 20;

        String[] lines = textToPrint.split("\n");
        int pageWidth = (int) pageFormat.getImageableWidth();
        for (String line : lines) {
            if (line.startsWith("Согласие пациента на обработку персональных данных")) {
                g2d.setFont(titleFont);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
                g2d.drawString(line, x1, y);
                y += titleLineHeight;
            } else if (line.startsWith("Подпись субъекта персональных данных")) {
                g2d.setFont(contentFontBOLD);
                g2d.drawString(line, x + 10, y + 55);
                y += contentLineHeight2;
            } else if (line.startsWith("Настоящее согласие дано мной")) {
                g2d.setFont(contentFontBOLD);
                g2d.drawString(line, x, y);
                y += contentLineHeight2;
            } else if (line.startsWith("Я,") || line.startsWith("Проживающий по адресу") || line.startsWith("Документ:") || line.startsWith("Выдано:")) {
                g2d.setFont(contentFont2);
                g2d.drawString(line, x + 80, y);
                // Добавляем подчеркивание
                int lineStartX = 25;
                int lineEndX = 475;
                int lineY = y + 2; // Высота подчеркивания от базовой линии текста
                g2d.drawLine(lineStartX, lineY, lineEndX, lineY);
                y += contentLineHeight2;
            } else if (line.startsWith("(Ф.И.О. полностью)") || line.startsWith("(по месту регистрации)") || line.startsWith("(серия, дата выдачи") || line.startsWith("наименование выдавшего органа)")) {
                g2d.setFont(contentFontItalic);
                g2d.drawString(line, x + 135, y);
                y += contentLineHeight;
            } else {
                if (g2d.getFontMetrics().stringWidth(line) > pageWidth - x) {
                    String[] words = line.split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String word : words) {
                        if (word.contains("ООО")) {
                            g2d.setFont(contentFontBOLD); // Set the font to bold for "ООО"
                        } else {
                            g2d.setFont(contentFont); // Set back to the regular content font for other words
                        }
                        if (g2d.getFontMetrics().stringWidth(sb.toString() + " " + word) > pageWidth - x) {
                            g2d.drawString(sb.toString(), x, y);
                            y += contentLineHeight;
                            sb = new StringBuilder();
                        }
                        sb.append(word).append(" ");
                    }
                    g2d.drawString(sb.toString(), x, y);
                    y += contentLineHeight;
                } else {
                    String[] words = line.split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String word : words) {
                        if (word.contains("требованиями")) {
                            g2d.setFont(contentFontBOLD); // Set the font to bold for "ООО"
                        } else {
                            g2d.setFont(contentFont); // Set back to the regular content font for other words
                        }
                        if (g2d.getFontMetrics().stringWidth(sb.toString() + " " + word) > pageWidth - x) {
                            g2d.drawString(sb.toString(), x, y);
                            y += contentLineHeight;
                            sb = new StringBuilder();
                        }
                        sb.append(word).append(" ");
                    }
                    g2d.drawString(sb.toString(), x, y);
                    y += contentLineHeight;
                }
            }
        }
        g2d.setFont(contentFont);
        return PAGE_EXISTS;
    }
}

class MyPrintable1 implements Printable {
    private final String textToPrint;
    private final Font titleFont;
    private final Font titleFont2;
    private final Font titleFont3;
    private final Font contentFont;

    public MyPrintable1(String textToPrint) {
        this.textToPrint = textToPrint;
        this.titleFont = new Font("Arial", Font.BOLD, 14);
        this.titleFont2 = new Font("Arial", Font.BOLD, 10);
        this.titleFont3 = new Font("Arial", Font.ITALIC, 8);
        this.contentFont = new Font("Arial", Font.PLAIN, 10);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(titleFont);
        FontMetrics titleFontMetrics = g2d.getFontMetrics(titleFont);
        int titleLineHeight = titleFontMetrics.getHeight();
        g2d.setFont(contentFont);
        FontMetrics contentFontMetrics = g2d.getFontMetrics(contentFont);
        int contentLineHeight = contentFontMetrics.getHeight();
        int x = 5;
        int y = 20;
        String[] lines = textToPrint.split("\n");
        for (String line : lines) {
            if (line.endsWith("и понятна.") || line.endsWith("о виде лечения.") || line.endsWith("может изменяться.") ||
                    line.endsWith("лечение и диагностику.") || line.endsWith("на состоянии здоровья") ||
                    line.endsWith("от пациента к пациенту.") || line.endsWith("сделаны для моего блага.") ||
                    line.endsWith("обследования(лечения) в предложенном объеме.")) {
                g2d.setFont(contentFont);
                g2d.drawString(line, x + 20, y);
                y += contentLineHeight;
                y += contentLineHeight;
            } else if (line.startsWith("ИНФОРМИРОВАННОЕ СОГЛАСИЕ ПАЦИЕНТА") || line.startsWith("на стоматологическое лечение")) {
                g2d.setFont(titleFont);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
                g2d.drawString(line, x1, y);
                y += titleLineHeight;
            } else if (line.startsWith("Общество с ограниченной ответственностью «Стоматология «Доктор Дент")) {
                g2d.setFont(titleFont2);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
                g2d.drawString(line, x1 + 75, y);
                y += titleLineHeight;
            } else if (line.startsWith("Настоящее информированное")) {
                g2d.setFont(titleFont3);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
                g2d.drawString(line, x1 + 155, y);
                y += titleLineHeight;
            } else if (line.startsWith("    закона от 21.11.2011 No 323-ФЗ ")) {
                g2d.setFont(titleFont3);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
                g2d.drawString(line, x1 + 140, y);
                y += titleLineHeight;
            } else {
                g2d.setFont(contentFont);
                g2d.drawString(line, x + 20, y);
                y += contentLineHeight;
            }
        }
        return PAGE_EXISTS;
    }
}

class MyPrintable2 implements Printable {
    private final Font titleFont;
    private final Font titleFont2;
    private final Font contentFont;
    private final Font contentFontBold;
    private final String textToPrint;

    public MyPrintable2(String textToPrint) {
        this.textToPrint = textToPrint;
        this.titleFont = new Font("Arial", Font.BOLD, 12);
        this.titleFont2 = new Font("Arial", Font.BOLD, 10);
        this.contentFont = new Font("Arial", Font.PLAIN, 8);
        this.contentFontBold = new Font("Arial", Font.BOLD, 9);
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy 'г.'");
        Date currentDate = new Date();
        String formattedDate = dateFormat.format(currentDate);
        Graphics2D g2d = (Graphics2D) graphics;

        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        int pageWidth = (int) pageFormat.getImageableWidth();
        int pageHeight = (int) pageFormat.getImageableHeight();
        FontMetrics titleFontMetrics = g2d.getFontMetrics(titleFont);
        FontMetrics titleFont2Metrics = g2d.getFontMetrics(titleFont2);
        FontMetrics contentFontMetrics = g2d.getFontMetrics(contentFont);
        int titleLineHeight = titleFontMetrics.getHeight();
        int title2LineHeight = titleFont2Metrics.getHeight();
        int contentLineHeight = contentFontMetrics.getHeight();
        int x = 20; // Horizontal offset for left-aligned text
        int rightX = pageWidth - 20; // Horizontal offset for right-aligned text
        int y = titleLineHeight + 15; // Vertical offset for text, starts below the title
        String[] lines = textToPrint.split("\n");
        int bottomMargin = 20; // Bottom margin to determine the maximum number of lines per page
        int maxLinesPerPage = (pageHeight - y - bottomMargin) / contentLineHeight;
        int startLine = pageIndex * maxLinesPerPage;
        if (startLine >= lines.length) {
            return NO_SUCH_PAGE;
        }
        int endLine = Math.min(startLine + maxLinesPerPage, lines.length);
        for (int i = startLine; i < endLine; i++) {
            String line = lines[i];
            if (line.startsWith("ДОГОВОР") || line.startsWith("на оказание платных медицинских услуг с физическим лицом")) {
                g2d.setFont(titleFont);
                int textWidth = titleFontMetrics.stringWidth(line);
                int x1 = (pageWidth - textWidth) / 2;
                g2d.drawString(line, x1, y);
                y += titleLineHeight;
            } else if (line.contains("г. Усть-Лабинск") && line.contains(formattedDate)) {
                int splitIndex = line.indexOf(formattedDate);
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), 15 + rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.matches("^(Адреса и реквизиты сторон)$") || line.matches("^(6\\. Срок действия договора|5\\. Качество услуги|4\\. Ответственность сторон|3\\. Стоимость услуг и порядок платежей|2\\. Права и обязанности сторон|1\\. Предмет договора)$")) {
                g2d.setFont(titleFont2);
                int textWidth = titleFont2Metrics.stringWidth(line);
                int x1 = (pageWidth - textWidth) / 2;
                g2d.drawString(line, x1, y);
                y += title2LineHeight;
            } else if (line.startsWith("Права Исполнителя:") || line.startsWith("Обязанности Исполнителя:") || line.startsWith("Права Заказчика:") || line.startsWith("Обязанности Заказчика:")) {
                g2d.setFont(contentFontBold);
                g2d.drawString(line, x, y);
                y += titleLineHeight;
            } else if (line.contains("Исполнитель:") && line.contains("Заказчик:")) {
                int splitIndex = line.indexOf("Заказчик:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("ИНН/КПП 2373024174 / 237301001") && line.contains("Документ удостоверяющий личность:")) {
                int splitIndex = line.indexOf("Документ удостоверяющий личность:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("Юр. адрес: 352330, г. Усть-Лабинск, ул.Красная, 176А") && line.contains("Серия, номер:")) {
                int splitIndex = line.indexOf("Серия, номер:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("Тел.+7 918 373 47 10") && line.contains("Дата выдачи:")) {
                int splitIndex = line.indexOf("Дата выдачи:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("Р/с: 40702810400940010515 К/с: 30101810600000000966") && line.contains("Кем выдан:")) {
                int splitIndex = line.indexOf("Кем выдан:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("БИК 040349966, ОКПО 95080042") && line.contains("Проживает по адресу:")) {
                int splitIndex = line.indexOf("Проживает по адресу:");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else if (line.contains("Главный врач _______________ /") && line.contains("Заказчик _______________/")) {
                int splitIndex = line.indexOf("Заказчик _______________/");
                String leftPart = line.substring(0, splitIndex);
                String rightPart = line.substring(splitIndex);
                g2d.setFont(contentFont);
                g2d.drawString(leftPart.trim(), x, y);
                g2d.drawString(rightPart.trim(), rightX - contentFontMetrics.stringWidth(rightPart.trim()), y);
                y += contentLineHeight;
            } else {
                if (g2d.getFontMetrics().stringWidth(line) > pageWidth - x) {
                    g2d.setFont(contentFont);
                    String[] words = line.split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String word : words) {
                        if (g2d.getFontMetrics().stringWidth(sb.toString() + " " + word) > pageWidth - x) {
                            g2d.drawString(sb.toString(), x, y);
                            y += contentLineHeight;
                            sb = new StringBuilder();
                        }
                        sb.append(word).append(" ");
                    }
                    g2d.drawString(sb.toString(), x, y);
                    y += contentLineHeight;
                } else {
                    g2d.setFont(contentFont);
                    g2d.drawString(line, x, y);
                    y += contentLineHeight;
                }
            }
        }
        return PAGE_EXISTS;
    }
}
class MyPrintable5 implements Printable {
    private final String textToPrint;
    private final String moneyNeed;
    private final String doctorName;
    private final String pacientName;
    private final String[] services;
    private final String discount;

    MyPrintable5(String textToPrint, String sum, String services, String PACIENTName, String doctorName, String discount) {
        this.textToPrint = textToPrint;
        this.doctorName = doctorName;
        this.pacientName = PACIENTName;
        moneyNeed = sum;
        this.services = services.split("//");
        this.discount = discount;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(new Font("Serif", Font.BOLD, 12)); // Увеличить размер шрифта и сделать его жирным

        // Рисуем заголовок
        g2d.drawString("«___» __________ 20__г.", 30, 50);
        g2d.drawString("Исполнитель: ООО «Стоматология «Доктор Дент»", 30, 70);
        g2d.drawString("Врач: " + doctorName, 30, 90);
        g2d.drawString("Заказчик: " + pacientName, 30, 110);

        // Получаем коды услуг для вычисления ширины столбца кода
        String[] serviceCodes = new String[services.length];
        int maxCodeWidth = 0;
        Font headerFont = new Font("Serif", Font.BOLD, 12);
        g2d.setFont(headerFont);
        int codeHeaderWidth = g2d.getFontMetrics().stringWidth("Код мед. услуги");

        for (int i = 0; i < services.length; i++) {
            String service = services[i];
            String code = getCodeFromDatabase(service);
            serviceCodes[i] = code;
            int codeWidth = g2d.getFontMetrics().stringWidth(code);
            if (codeWidth > maxCodeWidth) {
                maxCodeWidth = codeWidth;
            }
        }

        // Ширина столбца кода - максимум между заголовком и самым длинным кодом
        int colCodeWidth = Math.max(codeHeaderWidth, maxCodeWidth) + 10;

        // Рисуем заголовок таблицы
        g2d.setFont(headerFont);
        int tableY = 140;
        int x = 30;
        int pageWidth = (int) pageFormat.getImageableWidth();

        // Базовые ширины столбцов
        int colNoWidth = 30;
        int colQuantityWidth = 50;
        int colPriceWidth = 60;
        int colDiscountedPriceWidth = 80;

        // Вычисляем ширину столбца услуги (остается доступное пространство)
        int usedWidth = colNoWidth + colCodeWidth + colQuantityWidth + colPriceWidth + colDiscountedPriceWidth;
        int colServiceWidth = pageWidth - usedWidth - 120; // 120 - отступы справа и слева

        // Позиции столбцов
        int colNoX = x;
        int colCodeX = colNoX + colNoWidth;
        int colServiceX = colCodeX + colCodeWidth;
        int colQuantityX = colServiceX + colServiceWidth;
        int colPriceX = colQuantityX + colQuantityWidth;
        int colDiscountedPriceX = colPriceX + colPriceWidth;

        g2d.drawString("No", colNoX, tableY);
        g2d.drawString("Код мед. услуги", colCodeX, tableY);
        g2d.drawString("Услуга", colServiceX, tableY);
        g2d.drawString("Кол-во", colQuantityX, tableY);
        g2d.drawString("Цена", colPriceX, tableY);
        g2d.drawString("Цена (со скидкой)", colDiscountedPriceX, tableY);

        // Вычисляем позицию y для секции услуг
        int servicesY = 160;
        int currentRowY = servicesY;

        // Рисуем услуги в таблице
        for (int i = 0; i < services.length; i++) {
            String service = services[i];
            String code = serviceCodes[i];
            int rowY = currentRowY;
            int cellWidth = 120;

            // Отрисовка индекса услуги
            g2d.drawString(Integer.toString(i + 1), colNoX, rowY);

            // Отрисовка кода услуги
            g2d.drawString(code, colCodeX, rowY);

            // Настройка шрифта
            int fontSize = 6; // Желаемый размер шрифта
            Font originalFont = g2d.getFont(); // Сохранение текущего шрифта
            Font newFont = originalFont.deriveFont(Font.BOLD, fontSize); // Создание нового шрифта с заданным размером
            g2d.setFont(newFont); // Установка нового шрифта

            int serviceX = colServiceX;
            int maxTextWidth = colServiceWidth - 10;
            int lineHeight = g2d.getFontMetrics().getHeight();
            String[] words = service.split(" ");
            StringBuilder currentLine = new StringBuilder();
            int yOffset = 0;

            // Отрисовка строки с переносом текста
            for (String word : words) {
                String testLine = currentLine + word + " ";
                if (g2d.getFontMetrics().stringWidth(testLine) > maxTextWidth) {
                    // Если текущая строка превышает максимальную ширину, отрисовываем её
                    g2d.drawString(currentLine.toString(), serviceX, rowY + yOffset);
                    yOffset += lineHeight; // Переходим на следующую строку
                    currentLine = new StringBuilder(word + " "); // Начинаем новую строку
                } else {
                    currentLine.append(word).append(" "); // Добавляем слово в текущую строку
                }
            }

            // Отрисовываем оставшуюся часть строки, если есть
            if (currentLine.length() > 0) {
                g2d.drawString(currentLine.toString(), serviceX, rowY + yOffset);
            }

            // Восстановление оригинального шрифта
            g2d.setFont(originalFont);

            // Вычисляем высоту строки с учетом переноса текста
            int rowHeight = 20 + yOffset;
            if (yOffset > 0) {
                rowHeight = Math.max(20, yOffset + 10);
            }

            // Отрисовка прямоугольника для каждой услуги
            int tableWidth = colDiscountedPriceX + colDiscountedPriceWidth - x;
            g2d.drawRect(x, rowY - 10, tableWidth, rowHeight);
            g2d.drawLine(colCodeX, rowY - 10, colCodeX, rowY + rowHeight - 10); // Разделитель между "No" и "Код"
            g2d.drawLine(colServiceX, rowY - 10, colServiceX, rowY + rowHeight - 10); // Разделитель между "Код" и "Услуга"
            g2d.drawLine(colQuantityX, rowY - 10, colQuantityX, rowY + rowHeight - 10); // Разделитель между "Услуга" и "Кол-во"
            g2d.drawLine(colPriceX, rowY - 10, colPriceX, rowY + rowHeight - 10); // Разделитель между "Кол-во" и "Цена"
            g2d.drawLine(colDiscountedPriceX, rowY - 10, colDiscountedPriceX, rowY + rowHeight - 10); // Разделитель между "Цена" и "Цена (со скидкой)"

            // Отрисовка количества
            g2d.drawString("1", colQuantityX, rowY);

            // Получение цены из базы данных
            String price = getPriceFromDatabase(service);
            g2d.drawString(price, colPriceX, rowY);

            // Вычисление цены со скидкой
            String discountedPrice = calculateDiscountedPrice(price);
            g2d.drawString(discountedPrice, colDiscountedPriceX, rowY); // Отрисовка цены со скидкой в новом столбце

            // Переходим к следующей строке с учетом высоты текущей
            currentRowY += rowHeight;
        }

        // Рисуем итоги
        int totalY = currentRowY + 10;
        g2d.drawString("Итого: " + moneyNeed + " руб.", 400, totalY);
        g2d.drawString("К оплате: " + moneyNeed + " руб.", 400, totalY += 20);
        g2d.drawString("Оплачено:", 400, totalY += 20);

        // Рисуем подвал
        int footerY = totalY + 40;
        g2d.drawString("Всего оказано услуг на сумму: " + moneyNeed + " рублей", 30, footerY);
        g2d.drawString("Вышеперечисленные услуги выполнены полностью и в срок. Заказчик претензий по", 30, footerY += 20);
        g2d.drawString("объему, качеству и срокам оказания услуг не имеет.", 30, footerY += 20);

        // Линии для подписей
        int signatureLineY = footerY + 30;
        g2d.drawString("Исполнитель_________________", 30, signatureLineY);
        g2d.drawString("подпись", 180, signatureLineY + 15);
        g2d.drawString("Заказчик____________________", 30, signatureLineY + 40);
        g2d.drawString("подпись", 180, signatureLineY + 55);

        return PAGE_EXISTS;
    }

    private String getCodeFromDatabase(String service) {
        if (service.contains("Зуб"))
            service = service.substring(8);
        if (service.contains("Зуб"))
            service = service.substring(8);
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            }
            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT code FROM resources.medicine WHERE medicine='" + service + "'");
                if (resultSet.next()) {
                    String code = resultSet.getString("code");
                    return code != null && !code.isEmpty() ? code : "";
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private String getPriceFromDatabase(String service) {
        if (service.contains("Зуб"))
            service = service.substring(8);
        if (service.contains("Зуб"))
            service = service.substring(8);
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            }
            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT price FROM resources.medicine WHERE medicine='" + service + "'");
                if (resultSet.next()) {
                    String price = resultSet.getString("price");
                    return price != null && !price.isEmpty() ? price : "0";
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при выводе данных");
            throw new RuntimeException(e);
        }
        return "0";
    }

    private String calculateDiscountedPrice(String price) {
        double originalPrice = Double.parseDouble(price);
        if (discount.equals("Нет")) {
            return price; // Если скидки нет, возвращаем оригинальную цену
        } else if (discount.endsWith("%")) {
            double discountValue = Double.parseDouble(discount.replace("%", "")) / 100;
            double discountedPrice = originalPrice * (1 - discountValue);
            return String.format("%.2f", discountedPrice); // Форматируем до двух знаков после запятой
        } else {
            return price; // Если формат скидки некорректен, возвращаем оригинальную цену
        }
    }




}


