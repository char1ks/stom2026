package GUI;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class WordDocumentHelper {
    
    /**
     * Генерирует акт с данными пациента
     */
    public static String generateAct(String templatePath, PatientData patientData, 
                                     List<ServiceData> services, boolean saveToFile, double discount) throws Exception {
        
        // Загружаем шаблон
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 1. Заполняем заголовок акта (Таблица 0)
            fillActHeader(document, patientData);
            
            // 2. Заполняем данные пациента и врача
            fillActPatientInfo(document, patientData);
            
            // 3. Создаем и заполняем таблицу с услугами (без группировки)
            createAndFillActServicesTable(document, services, discount, patientData);
            
            // 5. Удаляем пустые страницы
            removeEmptyPages(document);
            
            // Генерируем имя файла
            String outputFileName = "Акт_" + patientData.getUniqueKey() + ".docx";
            String outputPath = "C:\\IntellijIdeaJava\\Cart\\" + outputFileName;
            
            if (saveToFile) {
                // Сохраняем документ
                try (FileOutputStream out = new FileOutputStream(outputPath)) {
                    document.write(out);
                }
                return outputPath;
            } else {
                // Создаем временный файл для печати
                String tempPath = "C:\\IntellijIdeaJava\\Cart\\temp_act.docx";
                try (FileOutputStream out = new FileOutputStream(tempPath)) {
                    document.write(out);
                }
                return tempPath;
            }
        }
    }
    
    /**
     * Генерирует договор с данными пациента
     */
    public static String generateContract(String templatePath, PatientData patientData, 
                                          List<ServiceData> services, boolean saveToFile, boolean includeAppendix) throws Exception {
        
        // Загружаем шаблон
        try (FileInputStream fis = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 1. Заполняем заголовок (Таблица 0)
            fillContractHeader(document, patientData);
            
            // 2. Заполняем данные пациента в параграфе 5
            fillPatientInfo(document, patientData);
            
            // 2.5. Заполняем заголовок Приложения 1 в параграфе 86 - только если нужно приложение
            if (includeAppendix) {
                fillAppendixHeader(document, patientData);
            }
            
            // 2.6. Заполняем поля Врач и Заказчик в параграфах 89 и 90 - только если нужно приложение
            if (includeAppendix) {
                fillDoctorAndCustomerFields(document, patientData);
            }
            
            // 2.7. Добавляем печать в поле Исполнитель в параграфе 88 и в реквизитах
            addStampImages(document, includeAppendix);
            
            // 3. Создаем и заполняем таблицу с услугами после параграфа 37 (после подписи под "План лечения") БЕЗ ГРУППИРОВКИ
            createAndFillServicesTable(document, services, 37);
            
            // 4. Создаем и заполняем таблицу с реквизитами после параграфа 80 (сразу после заголовка "РЕКВИЗИТЫ И ПОДПИСИ СТОРОН")
            createAndFillPatientDetailsTable(document, patientData, 84);
            
            // 5. Создаем и заполняем дубликат таблицы услуг после параграфа 95 (после "Потребитель/Заказчик:_________________ (подпись)") БЕЗ ГРУППИРОВКИ - только если нужно приложение
            if (includeAppendix) {
                createAndFillServicesTableWithoutGrouping(document, services, 97);
            }
            
            // 6. Удаляем пустые страницы (пустые параграфы с разрывами страниц)
            removeEmptyPages(document);
            
            // Генерируем имя файла
            String outputFileName = generateOutputFileName(patientData);
            String outputPath = "C:\\IntellijIdeaJava\\Cart\\" + outputFileName;
            
            if (saveToFile) {
                // Сохраняем документ
                try (FileOutputStream out = new FileOutputStream(outputPath)) {
                    document.write(out);
                }
                return outputPath;
            } else {
                // Создаем временный файл для печати
                String tempPath = "C:\\IntellijIdeaJava\\Cart\\temp_contract.docx";
                try (FileOutputStream out = new FileOutputStream(tempPath)) {
                    document.write(out);
                }
                return tempPath;
            }
        }
    }
    
    /**
     * Заполняет заголовок акта (Таблица 0)
     */
    private static void fillActHeader(XWPFDocument document, PatientData patientData) {
        // Ищем первый параграф и заменяем его на заголовок
        if (document.getParagraphs().size() > 0) {
            XWPFParagraph firstPara = document.getParagraphs().get(0);
            
            // Формируем правильный заголовок с номером акта и датой
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            String currentDate = dateFormat.format(new Date());
            String formattedDate = "«" + currentDate + "» г.";
            
            String numberToUse = patientData.getContractNumber() != null && !patientData.getContractNumber().isEmpty() 
                                 ? patientData.getContractNumber() 
                                 : patientData.getUniqueKey();
            String newText = "Приложение № 1. Перечень платных медицинских услуг от договора № " + 
                             numberToUse + " " + formattedDate;
            
            // Заменяем текст в первом параграфе
            for (int j = 0; j < firstPara.getRuns().size(); j++) {
                firstPara.getRuns().get(j).setText("", 0);
            }
            if (firstPara.getRuns().size() > 0) {
                firstPara.getRuns().get(0).setText(newText, 0);
            }
        }
    }
    
    /**
     * Заполняет данные пациента и врача в акте
     */
    private static void fillActPatientInfo(XWPFDocument document, PatientData patientData) {
        // Ищем параграф с информацией об исполнителе и заказчике
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph para = document.getParagraphs().get(i);
            String text = para.getText();
            
            if (text != null && text.contains("Врач:")) {
                // Заменяем информацию о враче, сохраняя форматирование
                String newText = text.replace("Врач:", "Врач: " + patientData.getDoctorName());
                replaceTextInParagraphKeepingFormatting(para, newText);
            }
            
            if (text != null && text.contains("Заказчик:") && !text.contains("(подпись)")) {
                // Добавляем имя заказчика только в строках, где нет "(подпись)"
                String newText = text.replace("Заказчик:", "Заказчик: " + patientData.getFullName());
                replaceTextInParagraphKeepingFormatting(para, newText);
            }
            
            if (text != null && text.contains("Потребитель/Заказчик:") && text.contains("(подпись)")) {
                // В строке с подписью убираем имя, оставляем только "Потребитель/Заказчик: _________________ (подпись)"
                String newText = text.replaceFirst("Потребитель/Заказчик:.*\\(подпись\\)", "Потребитель/Заказчик: _________________ (подпись)");
                replaceTextInParagraphKeepingFormatting(para, newText);
            }
            
            if (text != null && text.contains("Исполнитель") && text.contains("подпись")) {
                // Добавляем печать после слова "Исполнитель"
                addStampToActParagraph(para);
            }
        }
    }
    
    /**
     * Добавляет печать в параграф акта после слова "Исполнитель"
     */
    private static void addStampToActParagraph(XWPFParagraph para) {
        try {
            String text = para.getText();
            
            // Разделяем текст на части до и после скобки "("
            String[] textParts = text.split("\\(", 2);
            String beforeParenthesis = textParts.length > 0 ? textParts[0] : "";
            String afterParenthesis = textParts.length > 1 ? textParts[1] : "";
            
            // Очищаем все runs
            for (int j = 0; j < para.getRuns().size(); j++) {
                para.getRuns().get(j).setText("", 0);
            }
            
            if (para.getRuns().size() > 0) {
                // Добавляем текст до скобки
                XWPFRun textRun1 = para.getRuns().get(0);
                textRun1.setText(beforeParenthesis);
                textRun1.setTextPosition(90); // Выравнивание текста выше печати (в 2 раза выше)
                
                // Добавляем изображение печати
                String stampPath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\stamp_transparent.png";
                FileInputStream fis = new FileInputStream(stampPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray();
                fis.close();
                
                XWPFRun imageRun = para.createRun();
                int width = 90;
                int height = 90;
                imageRun.addPicture(new ByteArrayInputStream(imageBytes), 
                                  XWPFDocument.PICTURE_TYPE_PNG, 
                                  "stamp.png", 
                                  Units.toEMU(width), 
                                  Units.toEMU(height));
                
                // Добавляем текст после скобки с выравниванием
                XWPFRun textRun2 = para.createRun();
                textRun2.setText("(" + afterParenthesis);
                textRun2.setTextPosition(90); // Выравнивание текста выше печати (в 2 раза выше)
            }
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении печати в акт: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Заменяет текст в параграфе, сохраняя форматирование
     */
    private static void replaceTextInParagraphKeepingFormatting(XWPFParagraph para, String newText) {
        if (para.getRuns().size() > 0) {
            // Сохраняем форматирование первого run
            XWPFRun firstRun = para.getRuns().get(0);
            boolean isBold = firstRun.isBold();
            
            // Очищаем все runs
            for (int j = 0; j < para.getRuns().size(); j++) {
                para.getRuns().get(j).setText("", 0);
            }
            
            // Устанавливаем новый текст в первый run с сохранением форматирования
            firstRun.setText(newText, 0);
            firstRun.setBold(isBold);
        }
    }
    
    /**
     * Создает и заполняет таблицу с услугами для акта (разбивает услуги на отдельные строки)
     * Если услуга имеет количество > 1, она разбивается на отдельные строки с единицей измерения = 1
     */
    private static void createAndFillActServicesTable(XWPFDocument document, List<ServiceData> services, double discount, PatientData patientData) {
        // Жестко задаем номер параграфа после которого вставляем таблицу
        int afterParagraphIndex = 5;
        
        if (afterParagraphIndex != -1 && document.getParagraphs().size() > afterParagraphIndex) {
            XWPFParagraph targetParagraph = document.getParagraphs().get(afterParagraphIndex);
            
            // Добавляем параграф с именем пациента перед таблицей
            XWPFParagraph patientNamePara = document.insertNewParagraph(targetParagraph.getCTP().newCursor());
            patientNamePara.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun patientNameRun = patientNamePara.createRun();
            patientNameRun.setText("Заказчик: " + patientData.getFullName());
            patientNameRun.setBold(false);
            
            // Создаем таблицу с простым списком услуг
            XWPFTable servicesTable = document.insertNewTbl(patientNamePara.getCTP().newCursor());
            
            // Создаем заголовок таблицы с дополнительным столбцом "Цена со скидкой"
            XWPFTableRow headerRow = servicesTable.getRow(0);
            setCellText(headerRow.getCell(0), "№");
            setCellText(headerRow.addNewTableCell(), "Код медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Наименование медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Условия ожидания медицинской услуги (экстренная, неотложная, плановая)");
            setCellText(headerRow.addNewTableCell(), "Ед. изм.");
            setCellText(headerRow.addNewTableCell(), "Стоимость медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Цена со скидкой");
            
            // Разбиваем услуги на отдельные строки
            int rowIndex = 1;
            for (ServiceData service : services) {
                // Рассчитываем цену за единицу
                double unitPrice = service.getQuantity() > 0 ? service.getTotalPrice() / service.getQuantity() : service.getTotalPrice();
                
                // Формируем название услуги с номером зуба (если есть)
                String serviceName = service.getName();
                if (service.getTooth() != null && !service.getTooth().isEmpty()) {
                    serviceName = "Зуб " + service.getTooth() + ". " + serviceName;
                }
                
                // Добавляем столько строк, сколько количество услуг
                for (int i = 0; i < service.getQuantity(); i++) {
                    XWPFTableRow newRow = servicesTable.createRow();
                    
                    List<XWPFTableCell> cells = newRow.getTableCells();
                    while (cells.size() < 7) {
                        newRow.addNewTableCell();
                        cells = newRow.getTableCells();
                    }
                    
                    setCellText(cells.get(0), String.valueOf(rowIndex)); // №
                    setCellText(cells.get(1), service.getCode()); // Код
                    setCellText(cells.get(2), serviceName); // Наименование с зубом
                    setCellText(cells.get(3), "плановая"); // Условия ожидания
                    setCellText(cells.get(4), "1"); // Единица измерения - ВСЕГДА 1
                    setCellText(cells.get(5), String.valueOf(unitPrice)); // Стоимость за единицу
                    
                    // Цена со скидкой за единицу
                    double discountedPrice = unitPrice * (1 - discount / 100);
                    setCellText(cells.get(6), String.valueOf(discountedPrice));
                    
                    rowIndex++;
                }
            }
            
            // Добавляем строку "Всего к оплате"
            XWPFTableRow totalRow = servicesTable.createRow();
            List<XWPFTableCell> totalCells = totalRow.getTableCells();
            
            while (totalCells.size() < 7) {
                totalRow.addNewTableCell();
                totalCells = totalRow.getTableCells();
            }
            
            // Вычисляем общую сумму с учетом скидки
            double totalSum = 0;
            for (ServiceData service : services) {
                totalSum += service.getTotalPrice() * (1 - discount / 100);
            }
            
            setCellText(totalCells.get(0), "");
            setCellText(totalCells.get(1), "");
            setCellText(totalCells.get(2), "Всего к оплате:");
            setCellText(totalCells.get(3), "");
            setCellText(totalCells.get(4), "");
            setCellText(totalCells.get(5), "");
            setCellText(totalCells.get(6), String.valueOf(totalSum));
        }
    }
    
    /**
     * Заполняет заголовок договора (Таблица 0)
     */
    private static void fillContractHeader(XWPFDocument document, PatientData patientData) {
        if (document.getTables().size() > 0) {
            XWPFTable headerTable = document.getTables().get(0);
            XWPFTableRow firstRow = headerTable.getRow(0);
            
            // Ячейка 0: № - ставим номер договора (используем contractNumber если есть, иначе uniqueKey)
            if (firstRow.getTableCells().size() > 0) {
                String currentText = firstRow.getTableCells().get(0).getText();
                String numberToUse = patientData.getContractNumber() != null && !patientData.getContractNumber().isEmpty() 
                                     ? patientData.getContractNumber() 
                                     : patientData.getUniqueKey();
                setCellText(firstRow.getTableCells().get(0), currentText + " " + numberToUse);
            }
            
            // Ячейка 2: дата (заменяем подчеркивания на реальную дату)
            if (firstRow.getTableCells().size() > 2) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
                String currentDate = dateFormat.format(new Date());
                String currentText = firstRow.getTableCells().get(2).getText();
                // Формируем правильную дату с кавычками как в документах: «12 сентября 2026» г.
                String formattedDate = "«" + currentDate + "» г.";
                // Заменяем весь текст ячейки на правильную дату
                setCellText(firstRow.getTableCells().get(2), formattedDate);
            }
        }
    }
    
    /**
     * Заполняет данные пациента в параграфе 5
     */
    private static void fillPatientInfo(XWPFDocument document, PatientData patientData) {
        // Параграф 5 содержит данные пациента
        if (document.getParagraphs().size() > 5) {
            XWPFParagraph patientParagraph = document.getParagraphs().get(5);
            
            // Получаем полный текст параграфа
            String fullText = patientParagraph.getText();
            
            // Заменяем данные в полном тексте
            String newText = fullText;
            
            // Заменяем ФИО: "Гр. Ф.И.О.________________________" -> "Гр. Ф.И.О. ФАМИЛИЯ ИМЯ ОТЧЕСТВО"
            newText = newText.replaceFirst("Гр\\. Ф\\.И\\.О\\._{50,}", "Гр. Ф.И.О. " + patientData.getFullName() + " ");
            
            // Заменяем дату рождения: "года рождения" -> "ДАТА года рождения"
            newText = newText.replaceFirst("года рождения", " " + patientData.getBirthDate() + " года рождения");
            
            // Заменяем адрес: "зарегистрированный(а) по адресу:_______________________________________________________" -> "зарегистрированный(а) по адресу: АДРЕС"
            newText = newText.replaceFirst("зарегистрированный\\(а\\) по адресу:_{50,}", "зарегистрированный(а) по адресу: " + patientData.getAddress());
            
            // Обновляем текст в параграфе
            for (int i = 0; i < patientParagraph.getRuns().size(); i++) {
                XWPFRun run = patientParagraph.getRuns().get(i);
                run.setText("", 0); // Очищаем все runs
            }
            
            // Устанавливаем новый текст в первый run
            if (patientParagraph.getRuns().size() > 0) {
                patientParagraph.getRuns().get(0).setText(newText, 0);
            }
        }
    }
    
    /**
     * Заполняет заголовок Приложения 1 (Параграф 86)
     */
    private static void fillAppendixHeader(XWPFDocument document, PatientData patientData) {
        // Параграф 86 содержит заголовок Приложения 1
        if (document.getParagraphs().size() > 86) {
            XWPFParagraph appendixParagraph = document.getParagraphs().get(86);
            
            // Получаем полный текст параграфа
            String fullText = appendixParagraph.getText();
            
            // Формируем новый текст с номером договора и датой
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            String currentDate = dateFormat.format(new Date());
            String formattedDate = "«" + currentDate + "»";
            String numberToUse = patientData.getContractNumber() != null && !patientData.getContractNumber().isEmpty() 
                                 ? patientData.getContractNumber() 
                                 : patientData.getUniqueKey();
            String newText = "Приложение № 1. Перечень платных медицинских услуг от договора № " + numberToUse + " " + formattedDate;
            
            // Обновляем текст в параграфе
            for (int i = 0; i < appendixParagraph.getRuns().size(); i++) {
                XWPFRun run = appendixParagraph.getRuns().get(i);
                run.setText("", 0); // Очищаем все runs
            }
            
            // Устанавливаем новый текст в первый run
            if (appendixParagraph.getRuns().size() > 0) {
                appendixParagraph.getRuns().get(0).setText(newText, 0);
            }
        }
    }
    
    /**
     * Заполняет поля Врач и Заказчик (Параграфы 89 и 90)
     */
    private static void fillDoctorAndCustomerFields(XWPFDocument document, PatientData patientData) {
        // Параграф 89: Врач:
        if (document.getParagraphs().size() > 89) {
            XWPFParagraph doctorParagraph = document.getParagraphs().get(89);
            String newDoctorText = "Врач: " + patientData.getDoctorName();
            
            for (int i = 0; i < doctorParagraph.getRuns().size(); i++) {
                XWPFRun run = doctorParagraph.getRuns().get(i);
                run.setText("", 0);
            }
            if (doctorParagraph.getRuns().size() > 0) {
                doctorParagraph.getRuns().get(0).setText(newDoctorText, 0);
            }
        }
        
        // Параграф 90: Заказчик:
        if (document.getParagraphs().size() > 90) {
            XWPFParagraph customerParagraph = document.getParagraphs().get(90);
            String newCustomerText = "Заказчик: " + patientData.getFullName();
            
            for (int i = 0; i < customerParagraph.getRuns().size(); i++) {
                XWPFRun run = customerParagraph.getRuns().get(i);
                run.setText("", 0);
            }
            if (customerParagraph.getRuns().size() > 0) {
                customerParagraph.getRuns().get(0).setText(newCustomerText, 0);
            }
        }
    }
    
    /**
     * Добавляет печать в поля Исполнитель (только для приложения)
     */
    private static void addStampImages(XWPFDocument document, boolean includeAppendix) {
        // Находим параграф с "Исполнитель: _______________ (подпись, М.П.)" по содержимому - только если нужно приложение
        if (includeAppendix) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                XWPFParagraph para = document.getParagraphs().get(i);
                String text = para.getText();
                if (text != null && text.contains("Исполнитель:") && text.contains("подпись, М.П.)")) {
                    // Нашли нужный параграф - добавляем печать
                    try {
                        // Очищаем существующие runs
                        for (int j = 0; j < para.getRuns().size(); j++) {
                            para.getRuns().get(j).setText("", 0);
                        }
                        
                        // Создаем run для первой части текста
                        if (para.getRuns().size() > 0) {
                            XWPFRun textRun1 = para.getRuns().get(0);
                            textRun1.setText("Исполнитель:", 0);
                        }
                        
                        // Создаем run для изображения
                        XWPFRun imageRun = para.createRun();
                        String stampPath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\stamp_transparent.png";
                        FileInputStream fis = new FileInputStream(stampPath);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        byte[] imageBytes = baos.toByteArray();
                        fis.close();
                        
                        int width = 60; // ширина в пунктах
                        int height = 60; // высота в пунктах
                        imageRun.addPicture(new ByteArrayInputStream(imageBytes), 
                                          XWPFDocument.PICTURE_TYPE_PNG, 
                                          "stamp.png", 
                                          Units.toEMU(width), 
                                          Units.toEMU(height));
                        
                        // Создаем run для второй части текста (без подчеркивания)
                        XWPFRun textRun2 = para.createRun();
                        textRun2.setText(" (подпись, М.П.)", 0);
                        
                        System.out.println("Печать добавлена в параграф " + i + ": " + text);
                        break; // Нашли и добавили, выходим из цикла
                    } catch (Exception e) {
                        System.err.println("Ошибка при добавлении печати в параграф " + i + ": " + e.getMessage());
                    }
                }
            }
        }
        // Печать в таблице реквизитов теперь добавляется при формировании таблицы
    }
    
    /**
     * Создает и заполняет таблицу с услугами после указанного параграфа
     */
    private static void createAndFillServicesTable(XWPFDocument document, List<ServiceData> services, int afterParagraphIndex) {
        System.out.println("=== СОЗДАНИЕ ТАБЛИЦЫ УСЛУГ ===");
        System.out.println("Количество услуг: " + services.size());
        for (ServiceData service : services) {
            System.out.println("Услуга: " + service.getName() + ", Код: " + service.getCode() + ", Цена: " + service.getTotalPrice());
        }
        
        // Находим параграф после которого нужно вставить таблицу
        if (document.getParagraphs().size() > afterParagraphIndex) {
            XWPFParagraph targetParagraph = document.getParagraphs().get(afterParagraphIndex);
            System.out.println("Параграф " + afterParagraphIndex + ": " + targetParagraph.getText());
            
            // Создаем таблицу после указанного параграфа
            XWPFTable servicesTable = document.insertNewTbl(targetParagraph.getCTP().newCursor());
            
            // Создаем заголовок таблицы
            XWPFTableRow headerRow = servicesTable.getRow(0);
            setCellText(headerRow.getCell(0), "№");
            setCellText(headerRow.addNewTableCell(), "Код медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Наименование медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Условия ожидания медицинской услуги (экстренная, неотложная, плановая)");
            setCellText(headerRow.addNewTableCell(), "Единица измерения");
            setCellText(headerRow.addNewTableCell(), "Стоимость медицинской услуги");
            
            // Добавляем строки для каждой услуги
            for (int i = 0; i < services.size(); i++) {
                ServiceData service = services.get(i);
                XWPFTableRow newRow = servicesTable.createRow();
                
                // Заполняем ячейки новой строки
                List<XWPFTableCell> cells = newRow.getTableCells();
                
                while (cells.size() < 6) {
                    newRow.addNewTableCell();
                    cells = newRow.getTableCells();
                }
                
                setCellText(cells.get(0), String.valueOf(i + 1)); // №
                setCellText(cells.get(1), service.getCode()); // Код
                setCellText(cells.get(2), service.getName()); // Наименование
                setCellText(cells.get(3), "плановая"); // Условия ожидания
                setCellText(cells.get(4), String.valueOf(service.getQuantity())); // Единица измерения
                setCellText(cells.get(5), String.valueOf(service.getTotalPrice())); // Стоимость
                
                System.out.println("Добавлена строка " + (i + 1) + ": " + service.getName());
            }
            
            // Добавляем строку "Всего к оплате"
            XWPFTableRow totalRow = servicesTable.createRow();
            List<XWPFTableCell> totalCells = totalRow.getTableCells();
            
            while (totalCells.size() < 6) {
                totalRow.addNewTableCell();
                totalCells = totalRow.getTableCells();
            }
            
            setCellText(totalCells.get(0), ""); // Пустой номер
            setCellText(totalCells.get(1), ""); // Пустой код
            setCellText(totalCells.get(2), "Всего к оплате:"); // Текст
            setCellText(totalCells.get(3), ""); // Пустые условия
            setCellText(totalCells.get(4), ""); // Пустые единицы
            
            double totalCost = services.stream().mapToDouble(ServiceData::getTotalPrice).sum();
            setCellText(totalCells.get(5), String.valueOf(totalCost)); // Общая сумма
            
            System.out.println("Общая сумма: " + totalCost);
            System.out.println("=== ТАБЛИЦА УСЛУГ СОЗДАНА ===");
        } else {
            System.out.println("ОШИБКА: Параграф " + afterParagraphIndex + " не найден. Всего параграфов: " + document.getParagraphs().size());
        }
    }
    
    /**
     * Создает и заполняет таблицу с услугами БЕЗ ГРУППИРОВКИ для Приложения 1
     * Каждая услуга записывается в отдельную строку, единицы измерения всегда = 1
     * Если услуга имеет количество > 1, она разбивается на отдельные строки
     */
    private static void createAndFillServicesTableWithoutGrouping(XWPFDocument document, List<ServiceData> services, int afterParagraphIndex) {
        System.out.println("=== СОЗДАНИЕ ТАБЛИЦЫ УСЛУГ БЕЗ ГРУППИРОВКИ (ПРИЛОЖЕНИЕ 1) ===");
        System.out.println("Количество услуг: " + services.size());
        
        // Находим параграф после которого нужно вставить таблицу
        if (document.getParagraphs().size() > afterParagraphIndex) {
            XWPFParagraph targetParagraph = document.getParagraphs().get(afterParagraphIndex);
            System.out.println("Параграф " + afterParagraphIndex + ": " + targetParagraph.getText());
            
            // Создаем таблицу после указанного параграфа
            XWPFTable servicesTable = document.insertNewTbl(targetParagraph.getCTP().newCursor());
            
            // Создаем заголовок таблицы
            XWPFTableRow headerRow = servicesTable.getRow(0);
            setCellText(headerRow.getCell(0), "№");
            setCellText(headerRow.addNewTableCell(), "Код медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Наименование медицинской услуги");
            setCellText(headerRow.addNewTableCell(), "Условия ожидания медицинской услуги (экстренная, неотложная, плановая)");
            setCellText(headerRow.addNewTableCell(), "Единица измерения");
            setCellText(headerRow.addNewTableCell(), "Стоимость медицинской услуги");
            
            // Добавляем строки для каждой услуги БЕЗ ГРУППИРОВКИ
            // Если услуга имеет количество > 1, разбиваем на отдельные строки
            int rowIndex = 1;
            for (ServiceData service : services) {
                // Рассчитываем цену за единицу
                double unitPrice = service.getQuantity() > 0 ? service.getTotalPrice() / service.getQuantity() : service.getTotalPrice();
                
                // Добавляем столько строк, сколько количество услуг
                for (int i = 0; i < service.getQuantity(); i++) {
                    XWPFTableRow newRow = servicesTable.createRow();
                    
                    // Заполняем ячейки новой строки
                    List<XWPFTableCell> cells = newRow.getTableCells();
                    
                    while (cells.size() < 6) {
                        newRow.addNewTableCell();
                        cells = newRow.getTableCells();
                    }
                    
                    setCellText(cells.get(0), String.valueOf(rowIndex)); // №
                    setCellText(cells.get(1), service.getCode()); // Код
                    setCellText(cells.get(2), service.getName()); // Наименование
                    setCellText(cells.get(3), "плановая"); // Условия ожидания
                    setCellText(cells.get(4), "1"); // Единица измерения - ВСЕГДА 1
                    setCellText(cells.get(5), String.valueOf(unitPrice)); // Стоимость за единицу
                    
                    System.out.println("Добавлена строка " + rowIndex + ": " + service.getName() + " (единица = 1, цена = " + unitPrice + ")");
                    rowIndex++;
                }
            }
            
            // Добавляем строку "Всего к оплате"
            XWPFTableRow totalRow = servicesTable.createRow();
            List<XWPFTableCell> totalCells = totalRow.getTableCells();
            
            while (totalCells.size() < 6) {
                totalRow.addNewTableCell();
                totalCells = totalRow.getTableCells();
            }
            
            setCellText(totalCells.get(0), ""); // Пустой номер
            setCellText(totalCells.get(1), ""); // Пустой код
            setCellText(totalCells.get(2), "Всего к оплате:"); // Текст
            setCellText(totalCells.get(3), ""); // Пустые условия
            setCellText(totalCells.get(4), ""); // Пустые единицы
            
            double totalCost = services.stream().mapToDouble(ServiceData::getTotalPrice).sum();
            setCellText(totalCells.get(5), String.valueOf(totalCost)); // Общая сумма
            
            System.out.println("Общая сумма: " + totalCost);
            System.out.println("=== ТАБЛИЦА УСЛУГ БЕЗ ГРУППИРОВКИ СОЗДАНА ===");
        } else {
            System.out.println("ОШИБКА: Параграф " + afterParagraphIndex + " не найден. Всего параграфов: " + document.getParagraphs().size());
        }
    }
    
    /**
     * Создает и заполняет таблицу с услугами с уменьшенным шрифтом после указанного параграфа
     */
    private static void createAndFillServicesTableWithSmallFont(XWPFDocument document, List<ServiceData> services, int afterParagraphIndex) {
        System.out.println("=== СОЗДАНИЕ ТАБЛИЦЫ УСЛУГ С МАЛЕНЬКИМ ШРИФТОМ ===");
        System.out.println("Количество услуг: " + services.size());
        
        // Находим параграф после которого нужно вставить таблицу
        if (document.getParagraphs().size() > afterParagraphIndex) {
            XWPFParagraph targetParagraph = document.getParagraphs().get(afterParagraphIndex);
            System.out.println("Параграф " + afterParagraphIndex + ": " + targetParagraph.getText());
            
            // Создаем таблицу после указанного параграфа
            XWPFTable servicesTable = document.insertNewTbl(targetParagraph.getCTP().newCursor());
            
            // Создаем заголовок таблицы с маленьким шрифтом
            XWPFTableRow headerRow = servicesTable.getRow(0);
            setCellTextWithFontSize(headerRow.getCell(0), "№", 8);
            setCellTextWithFontSize(headerRow.addNewTableCell(), "Код медицинской услуги", 8);
            setCellTextWithFontSize(headerRow.addNewTableCell(), "Наименование медицинской услуги", 8);
            setCellTextWithFontSize(headerRow.addNewTableCell(), "Условия ожидания (экстренная, неотложная, плановая)", 8);
            setCellTextWithFontSize(headerRow.addNewTableCell(), "Единица измерения", 8);
            setCellTextWithFontSize(headerRow.addNewTableCell(), "Стоимость", 8);
            
            // Добавляем строки для каждой услуги с маленьким шрифтом
            for (int i = 0; i < services.size(); i++) {
                ServiceData service = services.get(i);
                XWPFTableRow newRow = servicesTable.createRow();
                
                // Заполняем ячейки новой строки
                List<XWPFTableCell> cells = newRow.getTableCells();
                
                while (cells.size() < 6) {
                    newRow.addNewTableCell();
                    cells = newRow.getTableCells();
                }
                
                setCellTextWithFontSize(cells.get(0), String.valueOf(i + 1), 8); // №
                setCellTextWithFontSize(cells.get(1), service.getCode(), 8); // Код
                setCellTextWithFontSize(cells.get(2), service.getName(), 8); // Наименование
                setCellTextWithFontSize(cells.get(3), "плановая", 8); // Условия ожидания
                setCellTextWithFontSize(cells.get(4), String.valueOf(service.getQuantity()), 8); // Единица измерения
                setCellTextWithFontSize(cells.get(5), String.valueOf(service.getTotalPrice()), 8); // Стоимость
                
                System.out.println("Добавлена строка " + (i + 1) + ": " + service.getName());
            }
            
            // Добавляем строку "Всего к оплате" с маленьким шрифтом
            XWPFTableRow totalRow = servicesTable.createRow();
            List<XWPFTableCell> totalCells = totalRow.getTableCells();
            
            while (totalCells.size() < 6) {
                totalRow.addNewTableCell();
                totalCells = totalRow.getTableCells();
            }
            
            setCellTextWithFontSize(totalCells.get(0), "", 8); // Пустой номер
            setCellTextWithFontSize(totalCells.get(1), "", 8); // Пустой код
            setCellTextWithFontSize(totalCells.get(2), "Всего к оплате:", 8); // Текст
            setCellTextWithFontSize(totalCells.get(3), "", 8); // Пустые условия
            setCellTextWithFontSize(totalCells.get(4), "", 8); // Пустые единицы
            
            double totalCost = services.stream().mapToDouble(ServiceData::getTotalPrice).sum();
            setCellTextWithFontSize(totalCells.get(5), String.valueOf(totalCost), 8); // Общая сумма
            
            System.out.println("Общая сумма: " + totalCost);
            System.out.println("=== ТАБЛИЦА УСЛУГ С МАЛЕНЬКИМ ШРИФТОМ СОЗДАНА ===");
        } else {
            System.out.println("ОШИБКА: Параграф " + afterParagraphIndex + " не найден. Всего параграфов: " + document.getParagraphs().size());
        }
    }
    
    /**
     * Создает и заполняет таблицу с реквизитами после указанного параграфа
     */
    private static void createAndFillPatientDetailsTable(XWPFDocument document, PatientData patientData, int afterParagraphIndex) {
        // Находим параграф после которого нужно вставить таблицу
        if (document.getParagraphs().size() > afterParagraphIndex) {
            XWPFParagraph targetParagraph = document.getParagraphs().get(afterParagraphIndex);
            
            // Создаем таблицу с реквизитами сразу после параграфа
            XWPFTable detailsTable = document.insertNewTbl(targetParagraph.getCTP().newCursor());
            
            // Устанавливаем ширину таблицы на 100% страницы
            detailsTable.setWidth("100%");
            
            // Создаем строку с двумя ячейками
            XWPFTableRow row = detailsTable.getRow(0);
            
            // Добавляем вторую ячейку, если её нет
            if (row.getTableCells().size() < 2) {
                row.addNewTableCell();
            }
            
            // Устанавливаем ширину столбцов: первый 70%, второй 30%
            List<XWPFTableCell> cells = row.getTableCells();
            cells.get(0).setWidth("70%");
            cells.get(1).setWidth("30%");
            
            // Первая ячейка - данные исполнителя с жирным заголовком
            String executorInfo = "ООО «Доктор Дент»\n" +
                                "Адрес: 352330, Краснодарский край, г. Усть-Лабинск, ул. Красная, 176А\n" +
                                "ОГРН 1232300051268 ИНН 2373024174 / КПП 237301001\n" +
                                "р/с 40702810780020000033 АО «Альфа-Банк» Краснодарский край\n" +
                                "к/с 30101810200000000593 БИК 044525593\n" +
                                "+7(918)-347-56-77 doktordent@internet.ru\n" +
                                "Директор - главный врач/Дегтярев М.О./";
            setCellTextWithBoldHeader(row.getCell(0), "ИСПОЛНИТЕЛЬ", executorInfo, true);
            
            // Вторая ячейка - данные пациента с правильным форматом и жирным заголовком
            String patientInfo = "Ф.И.О. " + patientData.getFullName() + "\n" +
                                "Дата рождения " + patientData.getBirthDate() + "\n" +
                                "Паспорт: серия " + patientData.getDocumentSeries() + " № " + patientData.getDocumentNumber() + "\n" +
                                "Выдан " + patientData.getDocumentIssuer() + "\n" +
                                "дата " + patientData.getDocumentDate() + "\n" +
                                "Адрес места жительства " + patientData.getAddress() + "\n" +
                                "Телефон " + patientData.getPhone() + "\n" +
                                "E-mail _________________\n" +
                                "Статус: потребитель и заказчик\n\n" +
                                "_____________________/_______________________/";
            setCellTextWithBoldHeader(row.getCell(1), "ПАЦИЕНТ(ПОТРЕБИТЕЛЬИЗАКАЗЧИК)", patientInfo);
        }
    }
    
    /**
     * Устанавливает текст в ячейку с жирным заголовком
     */
    private static void setCellTextWithBoldHeader(XWPFTableCell cell, String header, String content) {
        setCellTextWithBoldHeader(cell, header, content, false);
    }
    
    private static void setCellTextWithBoldHeader(XWPFTableCell cell, String header, String content, boolean addStamp) {
        // Удаляем все параграфы из ячейки
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }
        
        // Добавляем параграф с жирным заголовком
        XWPFParagraph headerParagraph = cell.addParagraph();
        XWPFRun headerRun = headerParagraph.createRun();
        headerRun.setBold(true);
        headerRun.setText(header);
        
        // Разбиваем содержимое на строки и добавляем каждую строку в отдельный параграф
        String[] lines = content.split("\n");
        for (String line : lines) {
            XWPFParagraph contentParagraph = cell.addParagraph();
            
            if (addStamp && (line.contains("___________") || line.contains("Директор - главный врач"))) {
                // Добавляем печать перед /Дегтярев М.О./ или заменяем подчеркивание
                String beforeStamp = "";
                String afterStamp = "";
                
                if (line.contains("___________")) {
                    String[] textParts = line.split("___________", 2);
                    beforeStamp = textParts.length > 0 ? textParts[0] : "";
                    afterStamp = textParts.length > 1 ? textParts[1] : "";
                } else if (line.contains("Директор - главный врач")) {
                    // Добавляем печать перед /Дегтярев М.О./
                    int slashIndex = line.indexOf("/");
                    if (slashIndex > 0) {
                        beforeStamp = line.substring(0, slashIndex);
                        afterStamp = line.substring(slashIndex);
                    } else {
                        beforeStamp = line;
                    }
                }
                
                // Добавляем текст до печати с выравниванием
                XWPFRun textRun1 = contentParagraph.createRun();
                textRun1.setText(beforeStamp);
                textRun1.setTextPosition(70); // Смещение вверх для выравнивания с центром изображения
                
                // Добавляем изображение печати
                try {
                    String stampPath = "C:\\IntellijIdeaJava\\Cart\\src\\main\\resources\\stamp_transparent.png";
                    FileInputStream fis = new FileInputStream(stampPath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    byte[] imageBytes = baos.toByteArray();
                    fis.close();
                    
                    XWPFRun imageRun = contentParagraph.createRun();
                    int width = 60; // ширина в пунктах
                    int height = 60; // высота в пунктах
                    imageRun.addPicture(new ByteArrayInputStream(imageBytes), 
                                      XWPFDocument.PICTURE_TYPE_PNG, 
                                      "stamp.png", 
                                      Units.toEMU(width), 
                                      Units.toEMU(height));
                } catch (Exception e) {
                    System.err.println("Ошибка при добавлении печати: " + e.getMessage());
                }
                
                // Добавляем текст после печати с вертикальным выравниванием
                XWPFRun textRun2 = contentParagraph.createRun();
                textRun2.setText(afterStamp);
                // Настраиваем смещение текста вверх для лучшего выравнивания с центром изображения
                textRun2.setTextPosition(70); // Смещение вверх на 70 пунктов
            } else {
                // Обычный текст без печати
                XWPFRun contentRun = contentParagraph.createRun();
                contentRun.setText(line);
            }
        }
    }
    
    /**
     * Создает и заполняет таблицу с реквизитами после последней таблицы в документе
     */
    private static void createAndFillPatientDetailsTableAfterLastTable(XWPFDocument document, PatientData patientData) {
        // Находим последнюю таблицу в документе
        int tableCount = document.getTables().size();
        if (tableCount > 0) {
            XWPFTable lastTable = document.getTables().get(tableCount - 1);
            
            // Добавляем пустой параграф после последней таблицы для разделения
            XWPFParagraph emptyParagraph = document.insertNewParagraph(lastTable.getCTTbl().newCursor());
            
            // Создаем таблицу с реквизитами после пустого параграфа
            XWPFTable detailsTable = document.insertNewTbl(emptyParagraph.getCTP().newCursor());
            
            // Создаем строку с двумя ячейками
            XWPFTableRow row = detailsTable.getRow(0);
            
            // Первая ячейка - данные исполнителя с жирным заголовком
            String executorInfo = "ООО «Доктор Дент»\n" +
                                "Адрес: 352330, Краснодарский край, г. Усть-Лабинск, ул. Красная, 176А\n" +
                                "ОГРН 1232300051268 ИНН 2373024174 / КПП 237301001\n" +
                                "р/с 40702810780020000033 АО «Альфа-Банк» Краснодарский край\n" +
                                "к/с 30101810200000000593 БИК 044525593\n" +
                                "+7(918)-347-56-77 doktordent@internet.ru\n" +
                                "Директор - главный врач/Дегтярев М.О./";
            setCellTextWithBoldHeader(row.getCell(0), "ИСПОЛНИТЕЛЬ", executorInfo, true);
            
            // Вторая ячейка - данные пациента с правильным форматом и жирным заголовком
            String patientInfo = "Ф.И.О. " + patientData.getFullName() + "\n" +
                                "Дата рождения " + patientData.getBirthDate() + "\n" +
                                "Паспорт: серия " + patientData.getDocumentSeries() + " № " + patientData.getDocumentNumber() + "\n" +
                                "Выдан " + patientData.getDocumentIssuer() + "\n" +
                                "дата " + patientData.getDocumentDate() + "\n" +
                                "Адрес места жительства " + patientData.getAddress() + "\n" +
                                "Телефон " + patientData.getPhone() + "\n" +
                                "E-mail _________________\n" +
                                "Статус: потребитель и заказчик\n\n" +
                                "_____________________/_______________________/";
            setCellTextWithBoldHeader(row.addNewTableCell(), "ПАЦИЕНТ(ПОТРЕБИТЕЛЬИЗАКАЗЧИК)", patientInfo);
        }
    }
    
    /**
     * Добавляет разрыв страницы перед указанным параграфом
     */
    private static void addPageBreakBeforeParagraph(XWPFDocument document, int paragraphIndex) {
        if (document.getParagraphs().size() > paragraphIndex) {
            XWPFParagraph paragraph = document.getParagraphs().get(paragraphIndex);
            // Добавляем разрыв страницы перед параграфом
            paragraph.setPageBreak(true);
        }
    }
    
    /**
     * Удаляет пустые страницы (пустые параграфы с разрывами страниц или без текста)
     * Улучшенная логика для удаления параграфов, создающих пустые страницы
     */
    private static void removeEmptyPages(XWPFDocument document) {
        System.out.println("=== НАЧАЛО УДАЛЕНИЯ ПУСТЫХ СТРАНИЦ ===");
        System.out.println("Всего параграфов до удаления: " + document.getParagraphs().size());
        
        // Проходим по всем параграфам в обратном порядке, чтобы можно было удалять
        for (int i = document.getParagraphs().size() - 1; i >= 0; i--) {
            XWPFParagraph paragraph = document.getParagraphs().get(i);
            String text = paragraph.getText().trim();
            
            // Проверяем, есть ли разрыв страницы
            boolean hasPageBreak = paragraph.isPageBreak();
            
            // Проверяем все runs параграфа
            boolean hasContent = false;
            for (XWPFRun run : paragraph.getRuns()) {
                if (run.getText(0) != null && !run.getText(0).trim().isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
            
            // Если параграф пустой (нет текста) или содержит только пробелы/табы
            // Удаляем его независимо от наличия разрыва страницы
            if (!hasContent) {
                // Удаляем все runs из параграфа
                while (paragraph.getRuns().size() > 0) {
                    paragraph.removeRun(0);
                }
                System.out.println("Удален пустой параграф #" + i);
            }
        }
        
        System.out.println("Всего параграфов после удаления: " + document.getParagraphs().size());
        System.out.println("=== КОНЕЦ УДАЛЕНИЯ ПУСТЫХ СТРАНИЦ ===");
    }
    
    /**
     * Устанавливает текст в ячейку
     */
    private static void setCellText(XWPFTableCell cell, String text) {
        // Удаляем все параграфы из ячейки (по одному, так как коллекция unmodifiable)
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }
        
        // Добавляем новый параграф с текстом
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }
    
    /**
     * Устанавливает текст в ячейку с указанным размером шрифта
     */
    private static void setCellTextWithFontSize(XWPFTableCell cell, String text, int fontSize) {
        // Удаляем все параграфы из ячейки
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }
        
        // Добавляем новый параграф с текстом и размером шрифта
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
    }
    
    /**
     * Генерирует имя файла для сохранения
     */
    private static String generateOutputFileName(PatientData patientData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        return "Договор_" + patientData.getFullName().replaceAll(" ", "_") + "_" + timestamp + ".docx";
    }
    
    /**
     * Группирует одинаковые услуги и суммирует количество и цену
     * @param services список услуг для группировки
     * @param considerTooth учитывать ли номер зуба при группировке (true - да, false - нет)
     */
    public static List<ServiceData> groupServices(List<ServiceData> services, boolean considerTooth) {
        Map<String, ServiceData> groupedServices = new LinkedHashMap<>();
        
        for (ServiceData service : services) {
            String key;
            if (considerTooth) {
                // Группируем по комбинации кода, названия услуги и номера зуба
                // Услуги с разными зубами не группируются вместе
                String tooth = service.getTooth() != null ? service.getTooth() : "NO_TOOTH";
                key = service.getCode() + "|" + service.getName() + "|" + tooth;
            } else {
                // Группируем только по коду и названию услуги (для договора)
                key = service.getCode() + "|" + service.getName();
            }
            
            if (groupedServices.containsKey(key)) {
                // Если услуга уже есть, увеличиваем количество и цену
                ServiceData existing = groupedServices.get(key);
                existing.setQuantity(existing.getQuantity() + service.getQuantity());
                existing.setTotalPrice(existing.getTotalPrice() + service.getTotalPrice());
            } else {
                // Если услуги нет, добавляем
                groupedServices.put(key, new ServiceData(service));
            }
        }
        
        return new ArrayList<>(groupedServices.values());
    }
    
    /**
     * Группирует одинаковые услуги и суммирует количество и цену (без учета зуба)
     * Оставлен для обратной совместимости
     */
    public static List<ServiceData> groupServices(List<ServiceData> services) {
        return groupServices(services, false);
    }
    
    /**
     * Класс для данных пациента
     */
    public static class PatientData {
        private String uniqueKey;
        private String fullName;
        private String birthDate;
        private String address;
        private String documentSeries;
        private String documentNumber;
        private String documentIssuer;
        private String documentDate;
        private String phone;
        private String doctorName;
        private String contractNumber;
        
        public PatientData(String uniqueKey, String fullName, String birthDate, String address,
                         String documentSeries, String documentNumber, String documentIssuer, 
                         String documentDate, String phone, String doctorName) {
            this.uniqueKey = uniqueKey;
            this.fullName = fullName;
            this.birthDate = birthDate;
            this.address = address;
            this.documentSeries = documentSeries;
            this.documentNumber = documentNumber;
            this.documentIssuer = documentIssuer;
            this.documentDate = documentDate;
            this.phone = phone;
            this.doctorName = doctorName;
            this.contractNumber = uniqueKey; // По умолчанию используем uniqueKey
        }
        
        // Конструктор с номером договора
        public PatientData(String uniqueKey, String fullName, String birthDate, String address,
                         String documentSeries, String documentNumber, String documentIssuer, 
                         String documentDate, String phone, String doctorName, String contractNumber) {
            this.uniqueKey = uniqueKey;
            this.fullName = fullName;
            this.birthDate = birthDate;
            this.address = address;
            this.documentSeries = documentSeries;
            this.documentNumber = documentNumber;
            this.documentIssuer = documentIssuer;
            this.documentDate = documentDate;
            this.phone = phone;
            this.doctorName = doctorName;
            this.contractNumber = contractNumber;
        }
        
        // Геттеры
        public String getUniqueKey() { return uniqueKey; }
        public String getFullName() { return fullName; }
        public String getBirthDate() { return birthDate; }
        public String getAddress() { return address; }
        public String getDoctorName() { return doctorName; }
        public String getDocumentSeries() { return documentSeries; }
        public String getDocumentNumber() { return documentNumber; }
        public String getDocumentIssuer() { return documentIssuer; }
        public String getDocumentDate() { return documentDate; }
        public String getPhone() { return phone; }
        public String getContractNumber() { return contractNumber; }
    }
    
    /**
     * Класс для данных услуги
     */
    public static class ServiceData {
        private String code;
        private String name;
        private int quantity;
        private double totalPrice;
        private String tooth; // Номер зуба
        
        public ServiceData(String code, String name, int quantity, double totalPrice) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.tooth = null;
        }
        
        public ServiceData(String code, String name, int quantity, double totalPrice, String tooth) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.tooth = tooth;
        }
        
        // Конструктор копирования
        public ServiceData(ServiceData other) {
            this.code = other.code;
            this.name = other.name;
            this.quantity = other.quantity;
            this.totalPrice = other.totalPrice;
            this.tooth = other.tooth;
        }
        
        // Сеттеры
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
        public void setTooth(String tooth) { this.tooth = tooth; }
        
        // Геттеры
        public String getCode() { return code; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public double getTotalPrice() { return totalPrice; }
        public String getTooth() { return tooth; }
        public double getPrice() { 
            return quantity > 0 ? totalPrice / quantity : 0; 
        }
    }
}