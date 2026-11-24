package GUI.Mysql;

import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtil {
    private static final DatabaseConnectionPool pool = DatabaseConnectionPool.getInstance();
    
    // Кеш для часто используемых данных
    private static Map<String, List<String>> doctorsCache = new HashMap<>();
    private static Map<String, List<String>> diagnosesCache = new HashMap<>();
    private static long lastCacheUpdate = 0;
    private static final long CACHE_TIMEOUT = 300000; // 5 минут
    
    // Получение всех пациентов с использованием PreparedStatement
    public static List<Map<String, String>> getAllPatients() {
        List<Map<String, String>> patients = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = pool.getConnection();
            String query = "SELECT id, cardNumber, name, telephoneNumber, gender, addres, dateOfRecord FROM addperson.pacient";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, String> patient = new HashMap<>();
                    patient.put("id", rs.getString("id"));
                    patient.put("cardNumber", rs.getString("cardNumber"));
                    patient.put("name", rs.getString("name"));
                    patient.put("telephoneNumber", rs.getString("telephoneNumber"));
                    patient.put("gender", rs.getString("gender"));
                    patient.put("addres", rs.getString("addres"));
                    patient.put("dateOfRecord", rs.getString("dateOfRecord"));
                    patients.add(patient);
                }
            }
            Logger.logInfo("Загружено " + patients.size() + " пациентов из БД");
        } catch (Exception e) {
            Logger.logError("Ошибка при загрузке пациентов", e);
        } finally {
            pool.returnConnection(connection);
        }
        
        return patients;
    }
    
    // Получение пациента по имени
    public static List<Map<String, String>> getPatientByName(String patientName) {
        List<Map<String, String>> patients = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = pool.getConnection();
            String query = "SELECT * FROM addperson.pacient WHERE name = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, patientName);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> patient = new HashMap<>();
                        patient.put("id", rs.getString("id"));
                        patient.put("name", rs.getString("name"));
                        patient.put("telephoneNumber", rs.getString("telephoneNumber"));
                        patient.put("dateRegistration", rs.getString("dateRegistration"));
                        patient.put("doctorName", rs.getString("doctorName"));
                        patient.put("diagnose", rs.getString("diagnose"));
                        patient.put("discount", rs.getString("discount"));
                        patient.put("sum", rs.getString("sum"));
                        patients.add(patient);
                    }
                }
            }
            Logger.logInfo("Найдено " + patients.size() + " записей для пациента: " + patientName);
        } catch (Exception e) {
            Logger.logError("Ошибка при поиске пациента по имени: " + patientName, e);
        } finally {
            pool.returnConnection(connection);
        }
        
        return patients;
    }
    
    // Кешированное получение списка врачей
    public static List<String> getDoctors() {
        long currentTime = System.currentTimeMillis();
        
        // Проверяем кеш
        if (doctorsCache.containsKey("doctors") && 
            (currentTime - lastCacheUpdate) < CACHE_TIMEOUT) {
            Logger.logInfo("Возвращаем врачей из кеша");
            return doctorsCache.get("doctors");
        }
        
        List<String> doctors = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = pool.getConnection();
            String query = "SELECT DoctorName FROM doctors.alldoctors ORDER BY DoctorName";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String doctorName = rs.getString("DoctorName");
                    if (doctorName != null && !doctorName.trim().isEmpty()) {
                        doctors.add(doctorName);
                    }
                }
            }
            
            // Обновляем кеш
            doctorsCache.put("doctors", doctors);
            lastCacheUpdate = currentTime;
            Logger.logInfo("Загружено " + doctors.size() + " врачей и обновлен кеш");
            
        } catch (Exception e) {
            Logger.logError("Ошибка при загрузке врачей", e);
        } finally {
            pool.returnConnection(connection);
        }
        
        return doctors;
    }
    
    // Кешированное получение списка диагнозов
    public static List<String> getDiagnoses() {
        long currentTime = System.currentTimeMillis();
        
        // Проверяем кеш
        if (diagnosesCache.containsKey("diagnoses") && 
            (currentTime - lastCacheUpdate) < CACHE_TIMEOUT) {
            Logger.logInfo("Возвращаем диагнозы из кеша");
            return diagnosesCache.get("diagnoses");
        }
        
        List<String> diagnoses = new ArrayList<>();
        Connection connection = null;
        
        try {
            connection = pool.getConnection();
            String query = "SELECT diagnoseName FROM diagnose.alldiagnose ORDER BY diagnoseName";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String diagnoseName = rs.getString("diagnoseName");
                    if (diagnoseName != null && !diagnoseName.trim().isEmpty()) {
                        diagnoses.add(diagnoseName);
                    }
                }
            }
            
            // Обновляем кеш
            diagnosesCache.put("diagnoses", diagnoses);
            lastCacheUpdate = currentTime;
            Logger.logInfo("Загружено " + diagnoses.size() + " диагнозов и обновлен кеш");
            
        } catch (Exception e) {
            Logger.logError("Ошибка при загрузке диагнозов", e);
        } finally {
            pool.returnConnection(connection);
        }
        
        return diagnoses;
    }
    
    // Очистка кеша (для принудительного обновления)
    public static void clearCache() {
        doctorsCache.clear();
        diagnosesCache.clear();
        lastCacheUpdate = 0;
        Logger.logInfo("Кеш очищен");
    }
    
    // Закрытие пула соединений при завершении приложения
    public static void shutdown() {
        pool.closeAllConnections();
        Logger.logInfo("DatabaseUtil завершил работу");
    }
    
    // Получение следующего номера договора с ежегодным сбросом
    public static int getNextContractNumber() {
        Connection connection = null;
        int currentYear = Year.now().getValue();
        
        try {
            connection = pool.getConnection();
            
            // Проверяем, есть ли запись для текущего года
            String checkQuery = "SELECT contract_number FROM dogovor_numbers.contract_numbers WHERE year = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentYear);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Запись для текущего года существует, инкрементируем номер
                    int currentNumber = rs.getInt("contract_number");
                    int newNumber = currentNumber + 1;
                    
                    String updateQuery = "UPDATE dogovor_numbers.contract_numbers SET contract_number = ? WHERE year = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, newNumber);
                        updateStmt.setInt(2, currentYear);
                        updateStmt.executeUpdate();
                    }
                    
                    Logger.logInfo("Получен номер договора: " + newNumber + " для года " + currentYear);
                    return newNumber;
                } else {
                    // Записи для текущего года нет, создаем новую с номером 1
                    String insertQuery = "INSERT INTO dogovor_numbers.contract_numbers (contract_number, year) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, 1);
                        insertStmt.setInt(2, currentYear);
                        insertStmt.executeUpdate();
                    }
                    
                    Logger.logInfo("Создана новая запись для года " + currentYear + " с номером 1");
                    return 1;
                }
            }
        } catch (Exception e) {
            Logger.logError("Ошибка при получении номера договора", e);
            // В случае ошибки возвращаем 0
            return 0;
        } finally {
            pool.returnConnection(connection);
        }
    }
    
    // Проверка и сброс номера договора при необходимости (вызывается при запуске приложения)
    public static void checkAndResetContractNumberIfNeeded() {
        Connection connection = null;
        int currentYear = Year.now().getValue();
        
        try {
            connection = pool.getConnection();
            
            // Проверяем, есть ли запись для текущего года
            String checkQuery = "SELECT contract_number FROM dogovor_numbers.contract_numbers WHERE year = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentYear);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    // Записи для текущего года нет, создаем новую с номером 1
                    String insertQuery = "INSERT INTO dogovor_numbers.contract_numbers (contract_number, year) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, 1);
                        insertStmt.setInt(2, currentYear);
                        insertStmt.executeUpdate();
                    }
                    
                    Logger.logInfo("Инициализирована нумерация договоров для года " + currentYear + " с номера 1");
                }
            }
        } catch (Exception e) {
            Logger.logError("Ошибка при проверке нумерации договоров", e);
        } finally {
            pool.returnConnection(connection);
        }
    }
    
    // Получение номера договора для пациента (без создания нового)
    public static int getContractNumberForPatient(int patientId) {
        Connection connection = null;
        int currentYear = Year.now().getValue();
        
        try {
            connection = pool.getConnection();
            
            String checkPatientQuery = "SELECT contract_number FROM dogovor_numbers.patient_contract_numbers WHERE patient_id = ? AND year = ?";
            try (PreparedStatement checkPatientStmt = connection.prepareStatement(checkPatientQuery)) {
                checkPatientStmt.setInt(1, patientId);
                checkPatientStmt.setInt(2, currentYear);
                ResultSet rs = checkPatientStmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("contract_number");
                }
            }
        } catch (Exception e) {
            Logger.logError("Ошибка при получении номера договора для пациента", e);
        } finally {
            pool.returnConnection(connection);
        }
        
        return 0; // Если номер не найден
    }
    
    // Получение следующего номера договора (общий счетчик для всех договоров)
    public static int getNextContractNumber(int patientId) {
        Connection connection = null;
        
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false); // Отключаем автокоммит для контроля транзакции
            
            // Получаем текущее значение счетчика
            String selectQuery = "SELECT current_number FROM dogovor_numbers.contract_counter WHERE id = 1";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                ResultSet rs = selectStmt.executeQuery();
                
                if (rs.next()) {
                    int currentNumber = rs.getInt("current_number");
                    int newNumber = currentNumber + 1;
                    
                    // Обновляем счетчик
                    String updateQuery = "UPDATE dogovor_numbers.contract_counter SET current_number = ? WHERE id = 1";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, newNumber);
                        updateStmt.executeUpdate();
                    }
                    
                    connection.commit(); // Фиксируем изменения
                    Logger.logInfo("Получен номер договора: " + newNumber);
                    return newNumber;
                } else {
                    // Если счетчик не существует, создаем его со значением 1
                    String insertQuery = "INSERT INTO dogovor_numbers.contract_counter (id, current_number) VALUES (1, 1)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.executeUpdate();
                    }
                    
                    connection.commit(); // Фиксируем изменения
                    Logger.logInfo("Создан счетчик договоров с начальным значением 1");
                    return 1;
                }
            }
        } catch (Exception e) {
            Logger.logError("Ошибка при получении номера договора", e);
            try {
                if (connection != null) {
                    connection.rollback(); // Откатываем транзакцию при ошибке
                }
            } catch (SQLException ex) {
                Logger.logError("Ошибка при откате транзакции", ex);
            }
            // В случае ошибки возвращаем 0
            return 0;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Возвращаем автокоммит
                }
            } catch (SQLException e) {
                Logger.logError("Ошибка при восстановлении автокоммита", e);
            }
            pool.returnConnection(connection);
        }
    }
} 