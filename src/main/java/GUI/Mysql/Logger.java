package GUI.Mysql;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void logError(String message, Exception e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("[ERROR] " + LocalDateTime.now().format(formatter) + " - " + message);
            if (e != null) {
                writer.println("Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace(writer);
            }
            writer.println("----------------------------------------");
        } catch (IOException logException) {
            System.err.println("Не удалось записать в лог файл: " + logException.getMessage());
        }
    }
    
    public static void logInfo(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("[INFO] " + LocalDateTime.now().format(formatter) + " - " + message);
        } catch (IOException logException) {
            System.err.println("Не удалось записать в лог файл: " + logException.getMessage());
        }
    }
    
    public static void logWarning(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("[WARNING] " + LocalDateTime.now().format(formatter) + " - " + message);
        } catch (IOException logException) {
            System.err.println("Не удалось записать в лог файл: " + logException.getMessage());
        }
    }
} 