package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.sql.SQLException;

import javafx.scene.control.DatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import java.time.ZoneId;
import java.util.Random;

public class RegistrationFormMore18 extends JFrame {

    private static String discountYN;
    private JCheckBox checkBox;
    private JTextField fullNameField;
    private JTextField parentNameField;
    private JTextField NumberOfCard;
    private JTextField addres;
    private JComboBox<String> document;
    private JTextField numberOrSeriaOfDocument;
    private JTextField DateOfGiven;
    private JTextField WhoGive;
    private JTextField PlaceOfWork;
    private JTextField telphoneNumber;
    private JTextField WorkTelphoneNumber;
    private JComboBox<String> genderComboBox;
    private JTextField birthdate;
    private JTextField registrationDateField;
    private JTextField allergies;
    private JList<String> doctors;
    private DefaultListModel<String> DoctorList;
    private JList<String> diagnose;
    private DefaultListModel<String> DiagnoseList;
    private JButton submitButton;
    private JButton parentButton;
    private final int WIDTH = 1200;
    private final int HEIGHT = 600;
    private static JPanel panel;
    private JButton addButtonDoctor;
    private JButton deleteDoctorButton;
    private JButton deleteDiagnoseButton;
    private JButton addButtonDiagnose;
    private JLabel DoctorLabel;
    private JLabel DiagnoseLabel;


    public RegistrationFormMore18() {
        setResizable(false);
        setTitle("Форма регистрации (c родителем)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.drawRect(10, 200, 500, 135);
                g.drawRect(700, 0, 1, 600);
                int y = getHeight() / 2; // Вычисляем середину высоты панели

                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(3)); // Устанавливаем толщину линии

                g2d.drawLine(701, y, getWidth(), y); // Рисуем линию по горизонтали
            }
        };
        getContentPane().add(panel);
        placeComponents(panel);
        setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        DiagnoseList = new DefaultListModel<>();
        DoctorList = new DefaultListModel<>();
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подлючение!");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date currentDate = new Date();
        String defaultDate = dateFormat.format(currentDate);
        panel.setLayout(null);
        JLabel hilabel = new JLabel("Заполнение формы");
        hilabel.setBounds(210, 5, 150, 25);
        hilabel.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(hilabel);
        JLabel fullNameLabel = new JLabel("ФИО:");
        fullNameLabel.setBounds(30, 30, 110, 25);
        panel.add(fullNameLabel);
        fullNameField = new JTextField(20);
        fullNameField.setBounds(120, 30, 200, 25);
        panel.add(fullNameField);
        JLabel parentNameLabel = new JLabel("Имя родителя:");
        parentNameLabel.setBounds(330, 30, 110, 25);
        panel.add(parentNameLabel);
        parentNameField = new JTextField(20);
        parentNameField.setBounds(440, 30, 140, 25);
        panel.add(parentNameField);
        JLabel cardNumber = new JLabel("Введите номер карты:");
        cardNumber.setBounds(30, 60, 250, 25);
        panel.add(cardNumber);

        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000;

        NumberOfCard = new JTextField(20);
        NumberOfCard.setText(String.valueOf(randomNumber));
        NumberOfCard.setBounds(200, 60, 140, 25);
        panel.add(NumberOfCard);
        JLabel dobLabel = new JLabel("Дата рождения:");
        dobLabel.setBounds(30, 90, 180, 25);
        panel.add(dobLabel);
        birthdate = new JTextField(20);
        birthdate.setBounds(200, 90, 160, 25);
        panel.add(birthdate);
        UtilDateModel dateModel = new UtilDateModel();
        dateModel.setSelected(true);
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, new Properties());
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.getJFormattedTextField().setText("Выбрать дату");
        datePicker.setBounds(370, 90, 125, 25);
        datePicker.setBackground(Color.WHITE);
        datePicker.setForeground(Color.white);
        datePicker.setBackground(new Color(119, 155, 191));
        datePicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date selectBirthdayValue = (Date) dateModel.getValue();
                LocalDate localDate = selectBirthdayValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String selectedDateStr = localDate.toString();
                birthdate.setText(selectedDateStr);
            }
        });

        panel.add(datePicker);

        JLabel genderLabel = new JLabel("Пол:");
        genderLabel.setBounds(30, 120, 100, 25);
        panel.add(genderLabel);
        String[] genders = {"Мужской", "Женский"};
        genderComboBox = new JComboBox<>(genders);
        panel.add(genderComboBox);
        genderComboBox.setBounds(120, 120, 200, 25);
        JLabel registrationDateLabel = new JLabel("Дата регистрации:");
        registrationDateLabel.setBounds(30, 150, 200, 25);
        panel.add(registrationDateLabel);
        registrationDateField = new JTextField(20);
        registrationDateField.setBounds(210, 147, 200, 25);
        registrationDateField.setText(defaultDate);
        panel.add(registrationDateField);
        JLabel addresLife = new JLabel("Введите адрес:");
        addresLife.setBounds(30, 180, 200, 25);
        panel.add(addresLife);
        addres = new JTextField(20);
        addres.setBounds(210, 175, 200, 25);
        panel.add(addres);
        String[] documentTypes = {"Паспорт", "Удостоверение личности", "СНИЛС", "Водительское удостоверение", "Заграничный паспорт", "Свидетельство о рождении"};
        JLabel documentLabel = new JLabel("Выберите документ:");
        documentLabel.setBounds(30, 210, 200, 25);
        panel.add(documentLabel);
        document = new JComboBox<>(documentTypes);
        document.setBounds(295, 210, 200, 25);
        panel.add(document);
        JLabel seriaOrNumberLabel = new JLabel("Серия документа(номер):");
        seriaOrNumberLabel.setBounds(30, 240, 270, 25);
        panel.add(seriaOrNumberLabel);
        numberOrSeriaOfDocument = new JTextField(20);
        numberOrSeriaOfDocument.setBounds(295, 240, 170, 25);
        panel.add(numberOrSeriaOfDocument);
        JLabel byWhoGivenLabel = new JLabel("Кем выдан документ(ФИО):");
        byWhoGivenLabel.setBounds(30, 270, 270, 25);
        panel.add(byWhoGivenLabel);
        WhoGive = new JTextField(20);
        WhoGive.setBounds(295, 270, 190, 25);
        panel.add(WhoGive);
        JLabel whenGivenLabel = new JLabel("Дата выдачи документа:");
        whenGivenLabel.setBounds(30, 300, 270, 25);
        panel.add(whenGivenLabel);
        DateOfGiven = new JTextField(20);
        DateOfGiven.setBounds(295, 300, 170, 25);
        panel.add(DateOfGiven);
        document.addActionListener(e -> {
            String selectedDocument = (String) document.getSelectedItem();
            switch (selectedDocument) {
                case "Паспорт":
                    seriaOrNumberLabel.setText("Серия и номер паспорта:");
                    byWhoGivenLabel.setText("Кем выдан паспорт (ФИО):");
                    whenGivenLabel.setText("Дата выдачи паспорта:");
                    break;
                case "Удостоверение личности":
                    seriaOrNumberLabel.setText("Серия и номер удостоверения:");
                    byWhoGivenLabel.setText("Кем выдано удостоверение (ФИО):");
                    whenGivenLabel.setText("Дата выдачи удостоверения:");
                    break;
                case "СНИЛС":
                    seriaOrNumberLabel.setText("Номер СНИЛС:");
                    byWhoGivenLabel.setText("Кем выдан СНИЛС (ФИО):");
                    whenGivenLabel.setText("Дата выдачи СНИЛС:");
                    break;
                case "Водительское удостоверение":
                    seriaOrNumberLabel.setText("Номер водительского удостоверения:");
                    byWhoGivenLabel.setText("Кем выдано водительское удостоверение:");
                    whenGivenLabel.setText("Дата выдачи водительского удостоверения:");
                    break;
                case "Заграничный паспорт":
                    seriaOrNumberLabel.setText("Номер заграничного паспорта:");
                    byWhoGivenLabel.setText("Кем выдан заграничный паспорт (ФИО):");
                    whenGivenLabel.setText("Дата выдачи заграничного паспорта:");
                    break;
                case "Свидетельство о рождении":
                    seriaOrNumberLabel.setText("Номер свидетельства о рождении:");
                    byWhoGivenLabel.setText("Кем выдано свидетельство о рождении:");
                    whenGivenLabel.setText("Дата выдачи свидетельства о рождении:");
                    break;
            }
        });
        JLabel WhereWork = new JLabel("Место работы:");
        WhereWork.setBounds(30, 340, 170, 25);
        panel.add(WhereWork);
        PlaceOfWork = new JTextField(20);
        PlaceOfWork.setBounds(125, 340, 170, 25);
        panel.add(PlaceOfWork);
        JLabel HomeNumber = new JLabel("Домашний номер телефона:");
        HomeNumber.setBounds(30, 370, 175, 25);
        panel.add(HomeNumber);
        telphoneNumber = new JTextField(20);
        telphoneNumber.setBounds(210, 370, 175, 25);
        panel.add(telphoneNumber);
        JLabel WorkNumber = new JLabel("Рабочий номер телефона");
        WorkNumber.setBounds(30, 410, 175, 25);
        panel.add(WorkNumber);
        WorkTelphoneNumber = new JTextField(20);
        WorkTelphoneNumber.setBounds(190, 410, 175, 25);
        panel.add(WorkTelphoneNumber);

        JLabel discount = new JLabel("Скидка - ");
        discount.setBounds(380, 408, 100, 30);
        discount.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(discount);

        JLabel allergiesLabel = new JLabel("Перенесённые и");
        allergiesLabel.setBounds(30, 450, 175, 25);
        panel.add(allergiesLabel);

        JLabel allergiesLabel2 = new JLabel("сопутствующие заболевания:");
        allergiesLabel2.setBounds(10, 463, 200, 25);
        panel.add(allergiesLabel2);

        allergies = new JTextField(20);
        allergies.setBounds(195, 455, 170, 25);
        panel.add(allergies);

        checkBox = new JCheckBox();
        checkBox.setBounds(455, 415, 100, 20);
        checkBox.setForeground(Color.black);
        panel.add(checkBox);
        discountYN = "Нет";
        checkBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (checkBox.isSelected()) {
                    discountYN = "Да";
                } else {
                    discountYN = "Нет";
                }
            }
        });
        submitButton = new JButton("Сохранить");
        submitButton.setBounds(250, 500, 160, 45);
        submitButton.setForeground(Color.WHITE);
        submitButton.setBackground(new Color(119, 155, 191));
        panel.add(submitButton);

        parentButton = new JButton("Без родителя");
        parentButton.setBounds(30, 500, 160, 45);
        parentButton.setForeground(Color.white);
        parentButton.setBorder(BorderFactory.createLineBorder(Color.black));
        parentButton.setBackground(new Color(119, 155, 191));
        panel.add(parentButton);

        diagnose = new JList<>(DiagnoseList);
        JScrollPane diagnoseScroll = new JScrollPane(diagnose);
        diagnoseScroll.setBounds(702, 310, 600, 253);
        panel.add(diagnoseScroll);
        diagnose.addMouseListener(new MouseAdapter() {
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
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM diagnose.alldiagnose WHERE diagnoseName='" + diagnose.getSelectedValue() + "'");
                        if (resultSet.next()) {
                            String diagnoseName = diagnose.getSelectedValue();
                            String id = resultSet.getString("id");
                            aboutDiagnose aboutDoctor = new aboutDiagnose(id,diagnoseName, () -> refreshDoctorsAndDiagnose(doctors, diagnose));
                            aboutDoctor.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка,доктор не найден");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        addButtonDiagnose = new JButton("Добавить диагноз");
        addButtonDiagnose.setBounds(702, 281, 200, 29);
        panel.add(addButtonDiagnose);
        addButtonDiagnose.setForeground(Color.white);
        addButtonDiagnose.setBackground(new Color(119, 155, 191));

        deleteDiagnoseButton = new JButton("Удалить диагноз");
        deleteDiagnoseButton.setBounds(1000, 281, 200, 29);
        panel.add(deleteDiagnoseButton);
        deleteDiagnoseButton.setForeground(Color.white);
        deleteDiagnoseButton.setBackground(new Color(119, 155, 191));

        DiagnoseLabel = new JLabel("Диагнозы");
        DiagnoseLabel.setBounds(920, 281, 100, 29);
        DiagnoseLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        DiagnoseLabel.setForeground(Color.black);
        panel.add(DiagnoseLabel);

        doctors = new JList<>(DoctorList);
        JScrollPane doctorScroll = new JScrollPane(doctors);
        doctorScroll.setBounds(701, 30, 601, 250);
        panel.add(doctorScroll);

        addButtonDoctor = new JButton("Добавить врача");
        addButtonDoctor.setBounds(702, 0, 200, 30);
        panel.add(addButtonDoctor);
        addButtonDoctor.setForeground(Color.WHITE);
        addButtonDoctor.setBackground(new Color(119, 155, 191));

        deleteDoctorButton = new JButton("Удалить врача");
        deleteDoctorButton.setBounds(1000, 0, 200, 30);
        panel.add(deleteDoctorButton);
        deleteDoctorButton.setForeground(Color.WHITE);
        deleteDoctorButton.setBackground(new Color(119, 155, 191));

        DoctorLabel = new JLabel("Доктора");
        DoctorLabel.setBounds(922, 0, 100, 29);
        DoctorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        DoctorLabel.setForeground(Color.black);
        panel.add(DoctorLabel);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.setBounds(20, 0, 100, 25);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDoctorsAndDiagnose(doctors, diagnose);
            }
        });

        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(119, 155, 191));
        panel.add(refreshButton);
        allergies.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        deleteDoctorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        deleteDiagnoseButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        doctorScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        diagnoseScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addButtonDiagnose.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addButtonDoctor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        refreshButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        fullNameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        parentNameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        NumberOfCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        document.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        numberOrSeriaOfDocument.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        WhoGive.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        DateOfGiven.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        birthdate.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        registrationDateField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addres.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        PlaceOfWork.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        telphoneNumber.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        WorkTelphoneNumber.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        submitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        allergies.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String card = NumberOfCard.getText();
                String fullName = fullNameField.getText();
                String parentName = parentNameField.getText();
                String gender = (String) genderComboBox.getSelectedItem();
                String documentName = (String) document.getSelectedItem();
                String numberDocument = numberOrSeriaOfDocument.getText();
                String ByWhoGive = WhoGive.getText();
                String WhenGet = DateOfGiven.getText();
                String birthdateYear = birthdate.getText();
                String registrationDate = registrationDateField.getText();
                String addresWhere = addres.getText();
                String PlaceOfwork = PlaceOfWork.getText();
                String TelephoneNum = telphoneNumber.getText();
                String WorkTelephoneNum = WorkTelphoneNumber.getText();
                String discount = discountYN;
                String doctor = doctors.getSelectedValue();
                String diagnosePacient = diagnose.getSelectedValue();
                String allergiesText= allergies.getText();
                if (fullName.isEmpty() || birthdateYear.isEmpty() || registrationDate.isEmpty() || addresWhere.isEmpty() || documentName.isEmpty() || card.isEmpty() || numberDocument.isEmpty() || ByWhoGive.isEmpty()
                        || WhenGet.isEmpty() || PlaceOfwork.isEmpty() || TelephoneNum.isEmpty() || doctors.isSelectionEmpty() || diagnose.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(null, "Вы пропустили поле для ввода");
                } else if (!(fullName.isEmpty() && birthdateYear.isEmpty() && registrationDate.isEmpty() && addresWhere.isEmpty() && documentName.isEmpty() && card.isEmpty() && numberDocument.isEmpty() && ByWhoGive.isEmpty()
                        && WhenGet.isEmpty() && PlaceOfwork.isEmpty() && TelephoneNum.isEmpty() && doctor.isEmpty() && diagnosePacient.isEmpty())) {
                    if (!card.matches("[\\d.-]+")) {
                        JOptionPane.showMessageDialog(null, "Номер карты неверный");
                    } else if (!registrationDate.matches("[\\d.-]+")) {
                        JOptionPane.showMessageDialog(null, "Формат данных для даты неверный");
                    } else {
                        String[] dateParts = birthdateYear.split("[.-]");
                        int year, month, day;
                        try {
                            if (dateParts.length == 3) {
                                if (dateParts[0].length() == 4) {
                                    year = Integer.parseInt(dateParts[0]);
                                    month = Integer.parseInt(dateParts[1]);
                                    day = Integer.parseInt(dateParts[2]);
                                    LocalDate currentDate = LocalDate.now();
                                    LocalDate birthdate = LocalDate.of(year, month, day);
                                    Period age = Period.between(birthdate, currentDate);
                                    String totalAge = String.valueOf(age.getYears());
                                    try {
                                        if (discount.equals("Да")) {
                                            try {
                                                GUI.DiscountInProcent discountInProcent = new GUI.DiscountInProcent();
                                                discountInProcent.addWindowListener(new WindowAdapter() {
                                                    @Override
                                                    public void windowClosed(WindowEvent e) {
                                                        Double discount = discountInProcent.getDiscount();
                                                        String discountWithProcent = discountInProcent.getDiscountWithProcent();
                                                        GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                                        medicineSelect.addWindowListener(new WindowAdapter() {
                                                            @Override
                                                            public void windowClosed(WindowEvent e) {
                                                                String medicine = medicineSelect.getMedicine();
                                                                double sumOfMedicine = medicineSelect.getSum();
                                                                double discountValue = discount / 100.0;
                                                                double discountedSum = sumOfMedicine * (1 - discountValue);
                                                                double roundedSum = Math.round(discountedSum * 100.0) / 100.0;
                                                                String totalSum = String.valueOf(roundedSum);
                                                                try {
                                                                    Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                                            numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discountWithProcent, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                                    dispose();
                                                                } catch (SQLException ex) {
                                                                    throw new RuntimeException(ex);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (Exception E) {
                                                throw new RuntimeException(E);
                                            }
                                        } else {
                                            GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                            medicineSelect.addWindowListener(new WindowAdapter() {
                                                @Override
                                                public void windowClosed(WindowEvent e) {
                                                    String medicine = medicineSelect.getMedicine();
                                                    String totalSum = String.valueOf(medicineSelect.getSum());
                                                    try {
                                                        Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                                numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discount, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                        dispose();
                                                    } catch (SQLException ex) {
                                                        throw new RuntimeException(ex);
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных(404)");
                                    }
                                } else if (dateParts[1].length() == 4) {
                                    year = Integer.parseInt(dateParts[1]);
                                    month = Integer.parseInt(dateParts[0]);
                                    day = Integer.parseInt(dateParts[2]);
                                    LocalDate currentDate = LocalDate.now();
                                    LocalDate birthdate = LocalDate.of(year, month, day);
                                    Period age = Period.between(birthdate, currentDate);
                                    String totalAge = String.valueOf(age.getYears());
                                    try {
                                        if (discount.equals("Да")) {
                                            try {
                                                GUI.DiscountInProcent discountInProcent = new GUI.DiscountInProcent();
                                                discountInProcent.addWindowListener(new WindowAdapter() {
                                                    @Override
                                                    public void windowClosed(WindowEvent e) {
                                                        Double discount = discountInProcent.getDiscount();
                                                        String discountWithProcent = discountInProcent.getDiscountWithProcent();
                                                        GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                                        medicineSelect.addWindowListener(new WindowAdapter() {
                                                            @Override
                                                            public void windowClosed(WindowEvent e) {
                                                                String medicine = medicineSelect.getMedicine();
                                                                double sumOfMedicine = medicineSelect.getSum();
                                                                double discountValue = discount / 100.0;
                                                                double discountedSum = sumOfMedicine * (1 - discountValue);
                                                                double roundedSum = Math.round(discountedSum * 100.0) / 100.0;
                                                                String totalSum = String.valueOf(roundedSum);
                                                                try {
                                                                    Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                                            numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discountWithProcent, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                                    dispose();
                                                                } catch (SQLException ex) {
                                                                    throw new RuntimeException(ex);
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            } catch (Exception E) {
                                                throw new RuntimeException(E);
                                            }
                                        } else {
                                            GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                            medicineSelect.addWindowListener(new WindowAdapter() {
                                                @Override
                                                public void windowClosed(WindowEvent e) {
                                                    String medicine = medicineSelect.getMedicine();
                                                    String totalSum = String.valueOf(medicineSelect.getSum());
                                                    try {
                                                        Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                                numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discount, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                        dispose();
                                                    } catch (SQLException ex) {
                                                        throw new RuntimeException(ex);
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных(404)");
                                    }
                                } else if (dateParts[2].length() == 4) {
                                    year = Integer.parseInt(dateParts[2]);
                                    month = Integer.parseInt(dateParts[1]);
                                    day = Integer.parseInt(dateParts[0]);
                                    LocalDate currentDate = LocalDate.now();
                                    LocalDate birthdate = LocalDate.of(year, month, day);
                                    Period age = Period.between(birthdate, currentDate);
                                    String totalAge = String.valueOf(age.getYears());
                                    if (discount.equals("Да")) {
                                        try {
                                            GUI.DiscountInProcent discountInProcent = new GUI.DiscountInProcent();
                                            discountInProcent.addWindowListener(new WindowAdapter() {
                                                @Override
                                                public void windowClosed(WindowEvent e) {
                                                    Double discount = discountInProcent.getDiscount();
                                                    String discountWithProcent = discountInProcent.getDiscountWithProcent();
                                                    GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                                    medicineSelect.addWindowListener(new WindowAdapter() {
                                                        @Override
                                                        public void windowClosed(WindowEvent e) {
                                                            String medicine = medicineSelect.getMedicine();
                                                            double sumOfMedicine = medicineSelect.getSum();
                                                            double discountValue = discount / 100.0;
                                                            double discountedSum = sumOfMedicine * (1 - discountValue);
                                                            double roundedSum = Math.round(discountedSum * 100.0) / 100.0;
                                                            String totalSum = String.valueOf(roundedSum);
                                                            try {
                                                                Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                                        numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discountWithProcent, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                                dispose();
                                                            } catch (SQLException ex) {
                                                                throw new RuntimeException(ex);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        } catch (Exception E) {
                                            throw new RuntimeException(E);
                                        }
                                    } else {
                                        GUI.medicineSelect medicineSelect = new GUI.medicineSelect();
                                        medicineSelect.addWindowListener(new WindowAdapter() {
                                            @Override
                                            public void windowClosed(WindowEvent e) {
                                                String medicine = medicineSelect.getMedicine();
                                                String totalSum = String.valueOf(medicineSelect.getSum());
                                                try {
                                                    Database(card, fullName, totalAge, TelephoneNum, parentName, WorkTelephoneNum, gender, documentName,
                                                            numberDocument, ByWhoGive, WhenGet, PlaceOfwork, addresWhere, birthdateYear, registrationDate, discount, diagnosePacient, doctor, medicine, totalSum,allergiesText);
                                                    dispose();
                                                } catch (SQLException ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "Формат данных для даты неверный");
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Формат данных для даты неверный");
                            }
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Формат данных для даты неверный");
                        }
                    }
                }
            }
        });
        deleteDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                        refreshDoctorsAndDiagnose(doctors, diagnose);
                        connection.close();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(null, "Произошла ошибка при удаление врача из списка");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Перед удалением вы должны выбрать врача!");
                }
            }
        });
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
                            aboutDoctor aboutDoctor = new aboutDoctor(doctorName, timeWork, telephoneNum, () -> refreshDoctorsAndDiagnose(doctors, diagnose));
                            aboutDoctor.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка,доктор не найден");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        deleteDiagnoseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String selectDiagnose = diagnose.getSelectedValue();
                    Properties proper = new Properties();
                    if (!selectDiagnose.isEmpty()) {
                        try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                            proper.load(input);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение!");
                        }
                        try {
                            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                            Statement statement = connection.createStatement();
                            statement.execute("DELETE FROM diagnose.alldiagnose WHERE diagnoseName='" + selectDiagnose + "'");
                            JOptionPane.showMessageDialog(null, "Диагноз успешно удален из списка");
                            refreshDoctorsAndDiagnose(doctors, diagnose);
                            connection.close();
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при удаление диагноза");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Выберите диагноз перед удалением");
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Возникла ошибка,возможно вы не выбрали элемент перед удалением!");
                }
            }
        });
        addButtonDiagnose.addActionListener(new ActionListener() {
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
                                refreshDoctorsAndDiagnose(doctors, diagnose);
                                connection.close();
                            } catch (Exception exception) {
                                JOptionPane.showMessageDialog(null, "Произошла ошибка при записи данных(1220)");
                            }
                            addDiagnose.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Введите название");
                        }
                    }
                });
            }
        });
        addButtonDoctor.addActionListener(new ActionListener() {
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
                                refreshDoctorsAndDiagnose(doctors,diagnose);
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
        parentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parentButton.getText().equals("С родителем")) {
                    parentButton.setText("Без родителя");
                    setTitle("Форма регистрации (с родителем)");
                    parentNameField.setText("");
                    parentNameField.setVisible(true);
                    parentNameLabel.setVisible(true);
                } else {
                    parentButton.setText("С родителем");
                    setTitle("Форма регистрации (без родителя)");
                    parentNameField.setText("");
                    parentNameField.setVisible(false);
                    parentNameLabel.setVisible(false);
                }
            }
        });
    }

    public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final String datePattern = "yyyy-MM-dd";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }

            return "";
        }
    }

    private void refreshDoctorsAndDiagnose(JList<String> doctorsList, JList<String> diagnoseList) {
        DoctorList.clear();
        DiagnoseList.clear();
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
                connection.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подлючение!");
        }
        diagnoseList.updateUI();
        doctorsList.updateUI();
    }

    public static void Database(String card, String fullName, String totalAge, String TelephoneNum, String parentName, String WorkTelephoneNum, String gender, String documentName,
                                String numberDocument, String ByWhoGive, String WhenGet, String PlaceOfwork, String addresWhere, String birthdateYear, String registrationDate, String discount, String diagnose, String doctorName, String medicine, String sumMoney, String allergies) throws SQLException {
        Properties proper = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
            proper.load(input);
        } catch (Exception exc) {
            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
        }
        try {
            if (parentName.isEmpty()) {
                parentName = "Не записано";
            }
            JFrame RecordChoose = new JFrame();
            RecordChoose.setTitle("Выберите дату для записи");
            RecordChoose.setSize(400, 200);
            RecordChoose.setLocationRelativeTo(null);
            JPanel addRecordPanel = new JPanel();
            addRecordPanel.setLayout(null);

            JLabel dateTime = new JLabel("Дата:");
            dateTime.setBounds(30, 30, 100, 25);
            addRecordPanel.add(dateTime);

            UtilDateModel dateModel = new UtilDateModel();
            JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, new Properties());
            JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
            datePicker.getJFormattedTextField().setText("Выберите дату");
            datePicker.setBounds(130, 30, 200, 25);
            datePicker.setBackground(Color.WHITE);
            addRecordPanel.add(datePicker);

            JSpinner timeHour = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeHour, "HH:mm");
            timeHour.setEditor(timeEditor);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.set(Calendar.MINUTE, 0);

            timeHour.setValue(calendar.getTime());
            timeHour.setBounds(130, 60, 200, 25);
            addRecordPanel.add(timeHour);

            JButton saveButton = new JButton("Отправить");
            saveButton.setBounds(130, 100, 100, 30);
            addRecordPanel.add(saveButton);
            saveButton.setForeground(Color.black);
            saveButton.setBackground(Color.white);

            RecordChoose.add(addRecordPanel);
            RecordChoose.setVisible(true);
            String finalParentName = parentName;
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Date selectedDate = (Date) dateModel.getValue();
                    if (selectedDate != null) {
                        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        String selectedDateStr = localDate.toString();
                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        String selectedTimeStr = timeFormat.format(timeHour.getValue());

                        Properties proper = new Properties();
                        try (InputStream input = Files.newInputStream(Paths.get("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties"))) {
                            proper.load(input);
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Произошла ошибка при подключение");
                        }
                        try {
                            Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                            Statement statement = connection.createStatement();
                            statement.execute("INSERT INTO  addperson.pacient  (cardNumber,name,age,telephoneNumber,parentName,WorkTelephoneNum,gender,documentName,seriaOfDocument,WhoGiveDocument,whenDocumentGet, placeOfWork, addres, birthdate, dateRegistration, diagnose, discount, doctorName, dateOfRecord,timeOfRecord,medicine,sum,allergies) VALUES ('" + card + "','"
                                    + fullName + "','" + totalAge + "','" + TelephoneNum + "','" + finalParentName + "','" + WorkTelephoneNum + "','" + gender + "','" + documentName + "','"
                                    + numberDocument + "','" + ByWhoGive + "','" + WhenGet + "','" + PlaceOfwork + "','" + addresWhere + "','" + birthdateYear + "','" + registrationDate + "','" + diagnose + "','" + discount + "','" + doctorName + "','" + selectedDateStr + "','" + selectedTimeStr + "','" + medicine + "','" + sumMoney + "','" + allergies + "');");
                            statement.execute("INSERT INTO records.allrecords(record)VALUES('" + selectedDateStr + "');");
                            JOptionPane.showMessageDialog(null, "Регистрация успешна! Данные записаны");
                            connection.close();
                            RegistrationFormMore18 form = new RegistrationFormMore18();
                            form.dispose();
                            RecordChoose.dispose();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Выберите дату");
                    }
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Не удалось открыть окно");
            e.printStackTrace();
        }
    }
}