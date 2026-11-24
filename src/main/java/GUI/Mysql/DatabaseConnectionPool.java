package GUI.Mysql;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConnectionPool {
    private static DatabaseConnectionPool instance;
    private BlockingQueue<Connection> connectionPool;
    private final int POOL_SIZE = 10;
    private String url, username, password;
    
    private DatabaseConnectionPool() {
        loadDatabaseConfig();
        initializePool();
    }
    
    public static synchronized DatabaseConnectionPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPool();
        }
        return instance;
    }
    
    private void loadDatabaseConfig() {
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
                this.url = proper.getProperty("database.url");
                this.username = proper.getProperty("database.login");
                this.password = proper.getProperty("database.pass");
                Logger.logInfo("Конфигурация базы данных загружена успешно");
            }
        } catch (Exception e) {
            Logger.logError("Ошибка при загрузке конфигурации БД", e);
            throw new RuntimeException("Не удалось загрузить конфигурацию БД", e);
        }
    }
    
    private void initializePool() {
        connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection connection = DriverManager.getConnection(url, username, password);
                connectionPool.offer(connection);
            }
            Logger.logInfo("Пул соединений инициализирован с " + POOL_SIZE + " соединениями");
        } catch (SQLException e) {
            Logger.logError("Ошибка при инициализации пула соединений", e);
            throw new RuntimeException("Не удалось создать пул соединений", e);
        }
    }
    
    public Connection getConnection() throws InterruptedException, SQLException {
        Connection connection = connectionPool.take();
        if (connection.isClosed()) {
            // Если соединение закрыто, создаем новое
            connection = DriverManager.getConnection(url, username, password);
            Logger.logInfo("Создано новое соединение взамен закрытого");
        }
        return connection;
    }
    
    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.offer(connection);
                }
            } catch (SQLException e) {
                Logger.logError("Ошибка при возврате соединения в пул", e);
            }
        }
    }
    
    public void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            try {
                Connection connection = connectionPool.take();
                connection.close();
            } catch (Exception e) {
                Logger.logError("Ошибка при закрытии соединения", e);
            }
        }
        Logger.logInfo("Все соединения пула закрыты");
    }
} 