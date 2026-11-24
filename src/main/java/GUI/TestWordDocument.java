package GUI;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;

public class TestWordDocument {
    public static void main(String[] args) {
        String docPath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\dogovor.docx";
        
        try (FileInputStream fis = new FileInputStream(docPath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            System.out.println("=== АНАЛИЗ WORD ДОКУМЕНТА ===");
            System.out.println("Количество параграфов: " + document.getParagraphs().size());
            System.out.println("Количество таблиц: " + document.getTables().size());
            
            // Анализ параграфов с детализацией
            System.out.println("\n=== ПАРАГРАФЫ ===");
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                XWPFParagraph paragraph = document.getParagraphs().get(i);
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    System.out.println("Параграф " + i + ": " + text);
                    System.out.println("  Кол-во runs: " + paragraph.getRuns().size());
                    for (int r = 0; r < paragraph.getRuns().size(); r++) {
                        XWPFRun run = paragraph.getRuns().get(r);
                        String runText = run.getText(0);
                        if (runText != null && !runText.trim().isEmpty()) {
                            System.out.println("    Run " + r + ": [" + runText + "]");
                        }
                    }
                }
            }
            
            // Детальный анализ таблиц
            System.out.println("\n=== ТАБЛИЦЫ ===");
            for (int i = 0; i < document.getTables().size(); i++) {
                XWPFTable table = document.getTables().get(i);
                System.out.println("\n--- ТАБЛИЦА " + i + " ---");
                System.out.println("Количество строк: " + table.getRows().size());
                System.out.println("Количество колонок: " + (table.getRows().size() > 0 ? table.getRow(0).getTableCells().size() : 0));
                
                for (int j = 0; j < table.getRows().size(); j++) {
                    XWPFTableRow row = table.getRows().get(j);
                    System.out.println("Строка " + j + " (кол-во ячеек: " + row.getTableCells().size() + "):");
                    
                    for (int k = 0; k < row.getTableCells().size(); k++) {
                        XWPFTableCell cell = row.getTableCells().get(k);
                        String cellText = cell.getText().trim();
                        System.out.println("  Ячейка " + k + ": [" + cellText + "]");
                        
                        // Детализация содержимого ячейки
                        System.out.println("    Кол-во параграфов в ячейке: " + cell.getParagraphs().size());
                        for (int p = 0; p < cell.getParagraphs().size(); p++) {
                            XWPFParagraph cellPara = cell.getParagraphs().get(p);
                            System.out.println("    Параграф " + p + ": [" + cellPara.getText() + "]");
                            System.out.println("      Кол-во runs: " + cellPara.getRuns().size());
                            
                            for (int r = 0; r < cellPara.getRuns().size(); r++) {
                                XWPFRun run = cellPara.getRuns().get(r);
                                String runText = run.getText(0);
                                if (runText != null) {
                                    System.out.println("        Run " + r + ": [" + runText + "]");
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("\n=== АНАЛИЗ ЗАВЕРШЕН ===");
            
        } catch (Exception e) {
            System.out.println("Ошибка при анализе документа: " + e.getMessage());
            e.printStackTrace();
        }
    }
}