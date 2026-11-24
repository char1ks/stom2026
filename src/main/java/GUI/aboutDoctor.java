package GUI;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;


public class aboutDoctor extends JFrame {
    private static final Font labelFont = new Font("Arial", Font.BOLD, 16);
    private static final Font labelFontmini = new Font("Arial", Font.BOLD, 12);
    
    // Конструктор без параметров для использования в HomeScreen
    public aboutDoctor() {
        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Все врачи");
        setLayout(null);
        
        JLabel titleLabel = new JLabel("Список всех врачей");
        titleLabel.setFont(labelFont);
        titleLabel.setBounds(100, 20, 250, 25);
        add(titleLabel);
        
        // Здесь можно добавить логику для отображения всех врачей
        // Например, создать таблицу или список с врачами из базы данных
        
        JButton closeButton = new JButton("Закрыть");
        closeButton.setBounds(150, 200, 100, 30);
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(119, 155, 191));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(closeButton);
        
        setVisible(true);
    }
    
    public aboutDoctor(String doctorName,String timeWork,String telephoneNumber, Runnable onWindowClosed){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onWindowClosed.run();
            }
        });
        JLabel labelAbout = new JLabel("Данные о враче!");
        labelAbout.setFont(labelFont);
        labelAbout.setBounds(100,10,150,25);
        add(labelAbout);
        JLabel labelAboutDrINFO = new JLabel("(Просмотр)");
        labelAboutDrINFO.setFont(labelFontmini);
        labelAboutDrINFO.setBounds(130,25,150,25);
        add(labelAboutDrINFO);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        JLabel labelName = new JLabel("Имя:");
        labelName.setFont(labelFont);
        labelName.setBounds(30, 70, 150, 25);
        add(labelName);

        JTextField textFieldName = new JTextField(doctorName);
        textFieldName.setBounds(125, 70, 150, 25);
        textFieldName.setBorder(blackBorder); // Установка черной границы
        textFieldName.setEditable(false);
        add(textFieldName);

        // Создание и добавление JLabel и JTextField для "Время"
        JLabel labelTime = new JLabel("Время:");
        labelTime.setFont(labelFont);
        labelTime.setBounds(30, 130, 150, 25);
        add(labelTime);

        JTextField textFieldTime = new JTextField(timeWork);
        textFieldTime.setBounds(125, 130, 150, 25);
        textFieldTime.setBorder(blackBorder); // Установка черной границы
        textFieldTime.setEditable(false);
        add(textFieldTime);

        // Создание и добавление JLabel и JTextField для "Номер"
        JLabel labelNumber = new JLabel("Номер:");
        labelNumber.setFont(labelFont);
        labelNumber.setBounds(30, 190, 150, 25);
        add(labelNumber);
        JLabel labelNumber2 = new JLabel("телефона");
        labelNumber2.setFont(labelFontmini);
        labelNumber2.setBounds(30   , 205, 150, 25);
        add(labelNumber2);

        JTextField textFieldNumber = new JTextField(telephoneNumber);
        textFieldNumber.setBounds(125, 190, 150, 25);
        textFieldNumber.setBorder(blackBorder); // Установка черной границы
        textFieldNumber.setEditable(false);
        add(textFieldNumber);

        JButton backButton = new JButton("Назад!");
        backButton.setBounds(105, 250, 120, 30);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(backButton.getText().equals("Назад!")){
                    dispose();
                }else if (backButton.getText().equals("Изменить!")){
                    String nameDoctorNew = textFieldName.getText();
                    String TIMEDoctorNew = textFieldTime.getText();
                    String TelephoneNumDoctorNew = textFieldNumber.getText();
                    Properties proper = new Properties();

                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }

                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        String updateQuery ="UPDATE doctors.alldoctors SET doctorName = '" + nameDoctorNew + "', timeWork = '" + TIMEDoctorNew + "',telephoneNumber='"+TelephoneNumDoctorNew+"' WHERE doctorName = '" + doctorName + "'";
                        statement.executeUpdate(updateQuery);
                        JOptionPane.showMessageDialog(null,"Данные успешно изменены");
                        dispose();
                    } catch (SQLException exc) {
                        exc.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Ошибка при обновлении данных о докторе: " + exc.getMessage());
                    }
                }
            }
        });
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(119, 155, 191));
        backButton.setFocusPainted(false);
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setBorder(BorderFactory.createEmptyBorder());
            }
        });
        add(backButton);

        JButton redactSome = new JButton("\uD83D\uDCDD");
        redactSome.setBounds(230, 250, 25, 30);
        redactSome.setForeground(Color.WHITE);
        redactSome.setBackground(new Color(119, 155, 191));
        redactSome.setBorder(BorderFactory.createLineBorder(Color.black));
        redactSome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (redactSome.getText().equals("\uD83D\uDCDD")){
                    backButton.setText("Изменить!");
                    textFieldNumber.setEditable(true);
                    textFieldName.setEditable(true);
                    textFieldTime.setEditable(true);
                    redactSome.setText("↩️");
                    labelAboutDrINFO.setText("");
                }else {
                    labelAboutDrINFO.setText("(Просмотр)");
                    backButton.setText("Назад!");
                    textFieldNumber.setEditable(false);
                    textFieldName.setEditable(false);
                    textFieldTime.setEditable(false);
                    redactSome.setText("\uD83D\uDCDD");
                }
            }
        });
        add(redactSome);

        setSize(350,350);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Данные о докторе:"+doctorName);
        setLayout(null);
        setVisible(true);
    }
}
