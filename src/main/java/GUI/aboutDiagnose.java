package GUI;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;


public class aboutDiagnose extends JFrame {
    private static final Font labelFont = new Font("Arial", Font.BOLD, 16);
    private static final Font labelFontmini = new Font("Arial", Font.BOLD, 12);
    
    // Конструктор без параметров для использования в HomeScreen
    public aboutDiagnose() {
        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Все диагнозы");
        setLayout(null);
        
        JLabel titleLabel = new JLabel("Список всех диагнозов");
        titleLabel.setFont(labelFont);
        titleLabel.setBounds(100, 20, 250, 25);
        add(titleLabel);
        
        // Здесь можно добавить логику для отображения всех диагнозов
        // Например, создать таблицу или список с диагнозами из базы данных
        
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
    
    public aboutDiagnose(String id,String diagnoseName, Runnable onWindowClosed){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onWindowClosed.run();
            }
        });
        JLabel labelAbout = new JLabel("Данные о Диагнозе!");
        labelAbout.setFont(labelFont);
        labelAbout.setBounds(100,10,250,25);
        add(labelAbout);
        JLabel labelAboutDrINFO = new JLabel("(Просмотр)");
        labelAboutDrINFO.setFont(labelFontmini);
        labelAboutDrINFO.setBounds(130,25,150,25);
        add(labelAboutDrINFO);

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
        JLabel labelName = new JLabel("Наим.:");
        labelName.setFont(labelFont);
        labelName.setBounds(30, 70, 150, 25);
        add(labelName);

        JTextField textFieldName = new JTextField(diagnoseName);
        textFieldName.setBounds(125, 70, 150, 25);
        textFieldName.setBorder(blackBorder); // Установка черной границы
        textFieldName.setEditable(false);
        add(textFieldName);


        JButton backButton = new JButton("Назад!");
        backButton.setBounds(105, 125, 120, 30);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(backButton.getText().equals("Назад!")){
                    dispose();
                }else if (backButton.getText().equals("Изменить!")){
                    String nameDiagnoseNew = textFieldName.getText();
                    Properties proper = new Properties();

                    try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc);
                    }

                    try {
                        Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"));
                        Statement statement = connection.createStatement();
                        String updateQuery ="UPDATE diagnose.alldiagnose SET diagnoseName='"+nameDiagnoseNew+"' WHERE id = '" + id + "'";
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
        redactSome.setBounds(230, 125, 25, 30);
        redactSome.setForeground(Color.WHITE);
        redactSome.setBackground(new Color(119, 155, 191));
        redactSome.setBorder(BorderFactory.createLineBorder(Color.black));
        redactSome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (redactSome.getText().equals("\uD83D\uDCDD")){
                    backButton.setText("Изменить!");
                    textFieldName.setEditable(true);
                    redactSome.setText("↩️");
                    labelAboutDrINFO.setText("");
                }else {
                    labelAboutDrINFO.setText("(Просмотр)");
                    backButton.setText("Назад!");
                    textFieldName.setEditable(false);
                    redactSome.setText("\uD83D\uDCDD");
                }
            }
        });
        add(redactSome);

        setSize(350,240);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Данные о диагнозе:"+diagnoseName);
        setLayout(null);
        setVisible(true);
    }
}
