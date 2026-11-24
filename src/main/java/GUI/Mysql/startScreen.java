package GUI.Mysql;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class startScreen extends JFrame {
    private final JTextField loginTextField;
    private final JPasswordField passwordField;
    private final int WIDTH = 350;
    private final int HEIGHT = 300;


    startScreen() {
        ImageIcon appIcon = new ImageIcon("C:\\IntellijIdeaJava\\Cart\\photo_2023-11-18_23-43-45.jpg");
        Image image = appIcon.getImage();

        // Улучшение качества изображения
        int width = 48; // Желаемая ширина
        int height = 48; // Желаемая высота
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        // Создание кастомной иконки с растянутым изображением
        ImageIcon customIcon = new ImageIcon(scaledImage);

        // Установка кастомной иконки для приложения
        setIconImage(customIcon.getImage());
        setResizable(false);
        setTitle("Главный Экран");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        getContentPane().add(panel);

        JLabel loginLabel = new JLabel("Логин (имя):");
        loginLabel.setBounds(50, 50, 100, 25);
        loginTextField = new JTextField();
        loginTextField.setBounds(160, 50, 150, 25);
        panel.add(loginLabel);
        panel.add(loginTextField);
        panel.setBackground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setBounds(50, 100, 100, 25);
        passwordField = new JPasswordField();
        passwordField.setBounds(160, 100, 150, 25);
        panel.add(passwordLabel);
        panel.add(passwordField);


        JButton entranceButton = new JButton("Вход");
        entranceButton.setFont(new Font("Arial", Font.BOLD, 20));
        entranceButton.setBounds(115, 200, 100, 25);
        panel.add(entranceButton);
        JLabel noteLabel = new JLabel("Введите данные от уже созданной БД");
        noteLabel.setBounds(50, 150, 250, 25);
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(noteLabel);
        entranceButton.setForeground(Color.WHITE);
        entranceButton.setBackground(new Color(119, 155, 191));
        passwordLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        loginLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        entranceButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        loginTextField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        entranceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Properties proper = new Properties();

                    try (InputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                        proper.load(input);
                    } catch (Exception exc) {
                        JOptionPane.showMessageDialog(null, "Ошибка при поиске файла");
                        throw new RuntimeException(exc);
                    }
                    if (!loginTextField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                        String login = loginTextField.getText();
                        String password = passwordField.getText();
                        proper.setProperty("database.login", login);
                        proper.setProperty("database.pass", password);

                        try (FileOutputStream output = new FileOutputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                            proper.store(output, null);
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(null, "Ошибка при сохранении данных в файл");
                            throw new RuntimeException(exc);
                        }

                        JOptionPane.showMessageDialog(null, "Данные записаны!");

                        // Проверяем подключение к БД с новыми данными
                        try {
                            try (InputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                                proper.load(input);
                            }
                            
                            // Пытаемся подключиться к БД
                            try (Connection testConnection = DriverManager.getConnection(
                                proper.getProperty("database.url"), 
                                proper.getProperty("database.login"), 
                                proper.getProperty("database.pass"))) {
                                // Если подключение успешно, продолжаем
                                testConnection.close();
                                
                                // Проверяем и обновляем схему базы данных
                                DataBaseCreateIfNotExist.checkAndCreateDatabaseSchema();
                                // Автоматически создаем базы данных в фоне
                                DataBaseCreateIfNotExist.autoCreateAllDatabases();
                                // Проверяем и сбрасываем нумерацию договоров при необходимости
                                DatabaseUtil.checkAndResetContractNumberIfNeeded();
                                dispose();
                                GUI.HomeScreen homeScreen = new GUI.HomeScreen();
                            }
                        } catch (SQLException exc) {
                            // Если подключение не удалось, показываем одно сообщение и просим ввести данные снова
                            JOptionPane.showMessageDialog(null, 
                                "Не удалось подключиться к базе данных с указанными данными.\n" +
                                "Проверьте логин и пароль и попробуйте снова.", 
                                "Ошибка подключения", 
                                JOptionPane.ERROR_MESSAGE);
                            
                            // Очищаем поля для повторного ввода
                            loginTextField.setText("");
                            passwordField.setText("");
                            loginTextField.requestFocusInWindow();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Не все поля для ввода заполнены");
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Ошибка при поиске файла");
                    throw new RuntimeException(exc);
                }
            }
        });



        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String[] args) {
        startScreen startScreen=new startScreen();
    }
}
