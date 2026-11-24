package GUI.Mysql;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;


public class DataBaseCreateIfNotExist {
    // Класс больше не является JFrame и не показывает диалог
    // Используется только для автоматического создания баз данных

    public static void checkAndCreateDatabaseSchema() {
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                try (Statement statement = connection.createStatement()) {
                    // Проверяем, существует ли таблица pacient
                    ResultSet schemaResultSetPacient = connection.getMetaData().getTables(null, "addperson", "pacient", null);
                    if (schemaResultSetPacient.next()) {
                        // Получаем список столбцов в таблице pacient
                        ResultSet columnsResultSet = connection.getMetaData().getColumns(null, "addperson", "pacient", null);
                        java.util.List<String> existingColumns = new java.util.ArrayList<>();
                        while (columnsResultSet.next()) {
                            existingColumns.add(columnsResultSet.getString("COLUMN_NAME"));
                        }

                        // Список необходимых полей
                        java.util.List<String> requiredColumns = java.util.Arrays.asList(
                                "cardNumber", "name", "age", "telephoneNumber", "parentName", "WorkTelephoneNum",
                                "gender", "documentName", "seriaOfDocument", "WhoGiveDocument", "whenDocumentGet",
                                "placeOfWork", "addres", "birthdate", "dateRegistration", "diagnose", "discount",
                                "doctorName", "dateOfRecord", "timeOfRecord", "medicine", "sum", "allergies"
                        );

                        // Проверяем, есть ли все необходимые поля
                        boolean allColumnsExist = requiredColumns.stream().allMatch(existingColumns::contains);
                        if (!allColumnsExist) {
                            // Добавляем недостающие поля
                            for (String column : requiredColumns) {
                                if (!existingColumns.contains(column)) {
                                    String addColumnSql = "ALTER TABLE addperson.pacient ADD COLUMN " + column + " VARCHAR(150) NOT NULL";
                                    statement.executeUpdate(addColumnSql);
                                }
                            }
                            // Убрали диалоговое окно
                        }
                    } else {
                        // Убрали диалоговое окно
                    }

                    // Проверяем, существует ли таблица medicine
                    ResultSet schemaResultSetMedicine = connection.getMetaData().getTables(null, "resources", "medicine", null);
                    if (schemaResultSetMedicine.next()) {
                        // Получаем список столбцов в таблице medicine
                        ResultSet columnsResultSetMedicine = connection.getMetaData().getColumns(null, "resources", "medicine", null);
                        java.util.List<String> existingColumnsMedicine = new java.util.ArrayList<>();
                        while (columnsResultSetMedicine.next()) {
                            existingColumnsMedicine.add(columnsResultSetMedicine.getString("COLUMN_NAME"));
                        }

                        // Список необходимых полей для medicine (id должен быть всегда, поэтому не проверяем его для ALTER)
                        java.util.List<String> requiredColumnsMedicine = java.util.Arrays.asList(
                                "code", "medicine", "price"
                        );

                        // Проверяем, есть ли все необходимые поля
                        boolean allColumnsExistMedicine = requiredColumnsMedicine.stream().allMatch(existingColumnsMedicine::contains);
                        if (!allColumnsExistMedicine) {
                            // Добавляем недостающие поля
                            for (String column : requiredColumnsMedicine) {
                                if (!existingColumnsMedicine.contains(column)) {
                                    String addColumnSql;
                                    if (column.equals("code")) {
                                        // Добавляем code колонку как nullable для существующих записей
                                        addColumnSql = "ALTER TABLE resources.medicine ADD COLUMN " + column + " VARCHAR(50) NULL";
                                    } else if (column.equals("medicine")) {
                                        addColumnSql = "ALTER TABLE resources.medicine ADD COLUMN " + column + " VARCHAR(1300)";
                                    } else if (column.equals("price")) {
                                        addColumnSql = "ALTER TABLE resources.medicine ADD COLUMN " + column + " VARCHAR(18)";
                                    } else {
                                        addColumnSql = "ALTER TABLE resources.medicine ADD COLUMN " + column + " VARCHAR(150)";
                                    }
                                    try {
                                        statement.executeUpdate(addColumnSql);
                                        System.out.println("Добавлена колонка: " + column);
                                    } catch (SQLException ex) {
                                        System.out.println("Ошибка при добавлении колонки " + column + ": " + ex.getMessage());
                                    }
                                }
                            }
                            
                            // Если добавили колонку code, обновляем коды для услуг из списка, но НЕ удаляем существующие записи
                            if (!existingColumnsMedicine.contains("code")) {
                                try {
                                    // Обновляем коды для услуг, которые есть в списке medicines
                                    String[] medicines = {
                                        "В01.065.007,Прием ( осмотр,консультация) врача-стомотолога первичный,800",
                                        "С001,Справка о санации,300",
                                        "А06.07.003,Прицельная внутриротовая контактная рентгенография,600",
                                        "А06.07.003,Прицельная внутриротовая контактная рентгенография 3 и более,550",
                                        "A001,Абсорбирующая прокладка,180",
                                        "В01.003.004.002,Проводниковая анестезия,950",
                                        "В01.003.004.005,Инфильтрационная анестезия,0",
                                        "А16.07.030.003,Временное пломбирование лекарственным препаратом корневого канала,1350",
                                        "А16.07.030.003,Временное пломбирование лекарственным препаратом корневого канала повторное,650",
                                        "А16.07.031.001,Восстановление зуба пломбировочными материалами с использованием стекловолоконного штифта,4600",
                                        "А16.07.031,Восстановление зуба пломбировочными материалами с использованием анкерного штифта,4300",
                                        "А16.07.002.009,Наложение временной пломбы,300",
                                        "А16.07.057,Запечатывание фиссуры зуба герметиком,950",
                                        "А16.07.008.003,Закрытие перфорации стенки корневого канала зуба,2000",
                                        "А16.07.030.001,Инструментальная и медикаментозная обработка корневого канала зуба,2000",
                                        "А16.07.030.001,Инструментальная и медикаментозная обработка 2х канал,3500",
                                        "А16.07.030.001,Инструментальная и медикаментозная обработка 3х канал,4500",
                                        "А16.07.030.001,Инструментальная и медикаментозная обработка 4х канал,5000",
                                        "А11.07.027,Наложение девитализирующей пасты,400",
                                        "А16.07.050.002,Профессиональное отбеливание зубов. Каппа для домашнего отбеливания 1 челюсть,5000",
                                        "А16.07.008.002,Пломбирование корневого канала зуба гуттаперчивыми штифтами,2900",
                                        "А16.07.008.002,Пломбирование двух корневых каналов зуба гуттаперчивыми штифтами,4100",
                                        "А16.07.008.002,Пломбирование трех корневых каналов зуба гуттаперчивыми штифтами,4600",
                                        "А16.07.008.002,Пломбирование четырех корневых каналов зуба гуттаперчивыми штифтами,5000",
                                        "В04.065.006.001,Использование коффердама,600",
                                        "А16.07.002.013,Восстановление молочного зуба пломбой I,V,VI класс по Блэку с использованием материалов из фотополимеров,2900",
                                        "А16.07.002.014,Восстановление молочного зуба пломбой с нарушением контактного пункта II,III класс по Блэку с использованием материалов из фотополимеров,2900",
                                        "А16.07.002.015,Восстановление молочного зуба пломбой IV класс по Блэку с использованием материалов из фотополимеров,2900",
                                        "А16.07.002.010,Восстановление зуба пломбой I класс по Блэку с использованием материалов из фотополимеров,4000",
                                        "А16.07.002.011,Восстановление зуба пломбой с нарушением контактного пункта II класс по Блэку с использованием материалов из фотополимеров,4450",
                                        "А16.07.002.011,Восстановление зуба пломбой с нарушением контактного пункта III класс по Блэку с использованием материалов из фотополимеров,4000",
                                        "А16.07.002.012,Восстановление зуба пломбой IV класс по Блэку с использованием материалов из фотополимеров,4000",
                                        "А16.07.002.010,Восстановление зуба пломбой V класс по Блэку с использованием материалов из фотополимеров,4000",
                                        "А16.07.082.001,Распломбировка корневого канала,2500",
                                        "А16.07.082.001,Распломбировка двух корневых каналов,4000",
                                        "А16.07.082.001,Распломбировка трех корневых каналов,5000",
                                        "А16.07.082.001,Распломбировка четырех корневых каналов,5500",
                                        "S001,Серебрение Saforaid,800",
                                        "S002,Серебрение Аргинат,400",
                                        "А16.07.051,Профессиональная гигиена полости рта и зубов (ультрозвук.Air-Flow,полировка),4600",
                                        "А16.07.094,Удаление внутриканального штифта/вкладки,1500",
                                        "А16.07.094.001,Удаление фрагмента сломонного инструмента,1500",
                                        "А11.07.012,Глубокое фторирование эмали зуба,350",
                                        "А16.07.003.001,Восстановление зуба виниром E-max - предоплата,9000",
                                        "А16.07.003.001,Восстановление зуба виниром E-maх -сдача,5500",
                                        "А16.07.033,Восстановление зуба с использованием цельнолитой культевой вкладки,3450",
                                        "А16.07.004.005,Восстановление зуба коронкой пластмассовой клинического изготавления,1500",
                                        "А16.07.004.006,Восстановление зуба коронкой пластмассовой лабораторного изготавления,1500",
                                        "А16.07.111.047,Изготовление индивидуальной жесткой ложки,1500",
                                        "А16.07.004.002,Восстановление зуба коронкой цельнокерамической. E-max - предоплата,9000",
                                        "А16.07.004.002,Восстановление зуба коронкой цельнокерамической. E-max - сдача,5500",
                                        "А16.07.004.004,Восстановление зуба коронкой металлокерамической - предоплата,6000",
                                        "Ф16.07.004.004,Восстановление зуба коронкой металлокерамической - сдача,4000",
                                        "А16.07.004.012,Восстановление зуба коронкой цельнолитой из кобальт-хромового сплава - предоплата,3000",
                                        "А16.07.004.012,Восстановление зуба коронкой цельнолитой из кобальт-хромового сплава - сдача,2000",
                                        "А16.07.004.003,Восстановление зуба коронкой безметалловой цельноциркониевой - предоплата,9000",
                                        "А16.07.004.003,Восстановление зуба коронкой безметалловой цельноциркониевой -сдача,6000",
                                        "А23.07.002.037,Починка перелома базиса съёмного протеза,1350",
                                        "А16.07.036,Протезирование сьемными бюгельными протезами -предоплата,25000",
                                        "А16.07.036,Протезирование сьемными бюгельными протезами-сдача,25000",
                                        "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - предоплата,13500",
                                        "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - сдача,11500",
                                        "А16.07.035,Протезирование зубов частичными съёмными пластиночными протезами - предоплата,12000",
                                        "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - сдача,10000",
                                        "А16.07.053,Снятие несьемной ортопедической конструкции, металлокерамической коронки,450",
                                        "А16.07.053.002,Снятие несьемной ортопедической конструкции, коронки штампованной,300",
                                        "А16.07.049.003,Фиксация на композитный цемент,700",
                                        "А16.07.049.001,Фиксация постояной коронки ( на постояный цемент),400",
                                        "А16.07.049.001,Фиксация постояной коронки не нашим,700",
                                        "А16.07.012,Вскрытие и дренирование одонтогенного абсцесса,1000",
                                        "А16.07.011,Вскрытие подслизистого или поднадкостничного очага воспаления в полости рта,2000",
                                        "А16.07.013,Отсроченный кюретаж лунки удаленного зуба,1500",
                                        "А16.07.042,Пластика уздечки верхней губы,5000",
                                        "А16.07.043,Пластика уздечки нижней губы,4500",
                                        "А16.07.044,Пластика уздечки языка,4500",
                                        "А16.07.046,Снятие швов,1000",
                                        "А16.07.001.002,Удаление постоянного зуба простое,3000",
                                        "А16.07.001.003,Удаление постоянного зуба сложное,5000",
                                        "А16.07.001.005,Удаление постоянного зуба с патологической подвижностью,2000",
                                        "А16.007.001.004,Удаление постоянного зуба ретинированного, дистопированного,7500",
                                        "А16.07.001.002,Удаление постоянного зуба простое. Удаление 8,5000",
                                        "А16.07.001.003,Удаление постоянного зуба сложное. Удаление 8 сложное,7000",
                                        "А16.07.001.001,Удаление временного зуба,2000",
                                        "А16.07.058,Лечение перекоронита ( промывание, рассечение и/или иссечение капюшона ),2000",
                                        "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая в составе мостовидного протеза - предоплата,7000",
                                        "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая в составе мостовидного протеза - сдача,5000",
                                        "3А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая с титановым абатментом - предоплата,10000",
                                        "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая с титановым абатментом - сдача,5000",
                                        "А16.07.006.001,Протезирование зуба с использованием имплантата.Коронка циркониевая с титановым абатментом - предоплата,14000",
                                        "А16.07.006.001,Протезирование зуба с использованием имплантата.Коронка циркониевая с титановым абатментом - сдача,6000",
                                        "А16.07.006.009,Протезирование зуба с использованием имплантата.Коронка циркониевая в составе мостовидного протеза - предоплата,11000",
                                        "А16.07.006.009,Протезирование зуба с использованием имплантата.Коронка циркониевая в составе мостовидного протеза - сдача,6000",
                                        "А16.07.055.003,Синус-лифтинг над областью установки одного дентального импланта при крестальном доступе (включая стоимость материалов),15000",
                                        "А16.07.055.001,Синус-лифтинг над областью установки одного дентального импланта при доступе через латеральное окно (включая стоимость материалов),45000",
                                        "А16.07.054.001,Внутрикостная дентальная имплантация «DIO»,32000",
                                        "А16.07.054,Установка формирователя десны,3000",
                                        "А16.07.041.002,Костная пластика челюстно-лицевой области с применением биодеградируемых материалов для увеличения толщены костного гребня(включая стоимость материалов),35000",
                                        "А16.07.089.003,Гингивопластика в области одного имплантата,13000",
                                        "А16.07.097,Наложения шва на слизистую оболочку рта,500"
                                    };
                                    
                                    PreparedStatement updateStatement = connection.prepareStatement(
                                        "UPDATE resources.medicine SET code = ? WHERE medicine = ?");
                                    
                                    int updatedCount = 0;
                                    for (String medicineData : medicines) {
                                        String[] parts = medicineData.split(",");
                                        if (parts.length >= 2) {
                                            String code = parts[0];
                                            String medicineName = parts[1];
                                            updateStatement.setString(1, code);
                                            updateStatement.setString(2, medicineName);
                                            int rows = updateStatement.executeUpdate();
                                            if (rows > 0) {
                                                updatedCount++;
                                            }
                                        }
                                    }
                                    System.out.println("Обновлено кодов для услуг: " + updatedCount);
                                } catch (SQLException ex) {
                                    System.out.println("Ошибка при обновлении кодов: " + ex.getMessage());
                                }
                            }
                            
                            // Убрали диалоговое окно, чтобы не сыпать ошибки
                        }
                    } else {
                        // Убрали диалоговое окно, чтобы не сыпать ошибки
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Ошибка при работе с базой данных: " + e.getMessage(), e);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при подключении к базе данных: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации базы данных: " + e.getMessage(), e);
        }
    }

    public static void autoCreateAllDatabases() {
        try {
            Properties proper = new Properties();
            try (FileInputStream input = new FileInputStream("\\IntellijIdeaJava\\Cart\\src\\main\\java\\GUI\\Mysql\\sql.properties")) {
                proper.load(input);
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

            String createSchemaSql = "CREATE SCHEMA IF NOT EXISTS addperson DEFAULT CHARACTER SET utf8;";
            String useSchemaSql = "USE addperson";
            String createTableSql = "CREATE TABLE IF NOT EXISTS addperson.pacient("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "cardNumber VARCHAR(50) NOT NULL,"
                    + "name VARCHAR(150) NOT NULL,"
                    + "age VARCHAR(150) NOT NULL,"
                    + "telephoneNumber VARCHAR(50) NOT NULL,"
                    + "parentName VARCHAR(50) NOT NULL,"
                    + "WorkTelephoneNum VARCHAR(50) NOT NULL,"
                    + "gender VARCHAR(20) NOT NULL,"
                    + "documentName VARCHAR(150) NOT NULL,"
                    + "seriaOfDocument VARCHAR(150) NOT NULL,"
                    + "WhoGiveDocument VARCHAR(150) NOT NULL,"
                    + "whenDocumentGet VARCHAR(150) NOT NULL,"
                    + "placeOfWork VARCHAR(150) NOT NULL,"
                    + "addres VARCHAR(160) NOT NULL,"
                    + "birthdate VARCHAR(150) NOT NULL,"
                    + "dateRegistration VARCHAR(150) NOT NULL,"
                    + "diagnose VARCHAR(1100) NOT NULL,"
                    + "discount VARCHAR(1150) NOT NULL,"
                    + "doctorName VARCHAR(150) NOT NULL,"
                    + "dateOfRecord VARCHAR(150),"
                    + "timeOfRecord VARCHAR(145),"
                    + "medicine VARCHAR(1900) NOT NULL,"
                    + "sum VARCHAR(161) NOT NULL,"
                    + "allergies VARCHAR(1500) NOT NULL"
                    + ")";
            String createSchemaSqlDoctors = "CREATE SCHEMA IF NOT EXISTS doctors DEFAULT CHARACTER SET utf8;";
            String useSchemaSqlDoctors = "USE doctors";
            String createDoctorsTableSql = "CREATE TABLE IF NOT EXISTS doctors.alldoctors ("
                    + "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                    + "doctorName VARCHAR(150) NOT NULL,"
                    + "timeWork VARCHAR(150) NOT NULL,"
                    + "telephoneNumber VARCHAR(150) NOT NULL"
                    + ")";
            String createSchemacardSchablon = "CREATE SCHEMA IF NOT EXISTS schablonCard DEFAULT CHARACTER SET utf8;";
            String useSchemacardSchablon = "USE schablonCard";
            String createcardSchablon = "CREATE TABLE IF NOT EXISTS schablonCard.cards ("
                    + "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                    + "name VARCHAR(150) NOT NULL,"
                    + "text VARCHAR(5900) NOT NULL"
                    + ")";
            String createSchemaDiagnose = "CREATE SCHEMA IF NOT EXISTS diagnose DEFAULT CHARACTER SET utf8;";
            String useSchemaSqlDiagnose = "USE diagnose";
            String createDiagnoseTable = "CREATE TABLE IF NOT EXISTS diagnose.alldiagnose ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "diagnoseName VARCHAR(150) NOT NULL"
                    + ")";
            String createSchemaCons = "CREATE SCHEMA IF NOT EXISTS resources DEFAULT CHARACTER SET utf8;";
            String useSchemaSqlCons = "USE resources";
            String createDiagnoseCons = "CREATE TABLE IF NOT EXISTS resources.medicine ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "code VARCHAR(50) NULL,"
                    + "medicine VARCHAR(1300) NOT NULL, "
                    + "price VARCHAR(18) NOT NULL"
                    + ")";
            String createSchemaRecords = "CREATE SCHEMA IF NOT EXISTS records DEFAULT CHARACTER SET utf8;";
            String useSchemaSqlRecords = "USE records";
            String createDiagnoseRecords = "CREATE TABLE IF NOT EXISTS records.allrecords ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "record VARCHAR(1100) NOT NULL "
                    + ")";
            String createDatabaseTeethFormula = "CREATE DATABASE IF NOT EXISTS teeth_formula;";
            String useDatabaseTeethFormula = "USE teeth_formula;";
            String createTableTeethSets = "CREATE TABLE IF NOT EXISTS teeth_sets ("
                    + "`key` INT PRIMARY KEY,"
                    + "`teeth_data` BLOB"
                    + ")";
            String createSchemaDogovor = "CREATE SCHEMA IF NOT EXISTS dogovor_numbers DEFAULT CHARACTER SET utf8;";
            String useSchemaDogovor = "USE dogovor_numbers;";
            String createDogovorNumbersTable = "CREATE TABLE IF NOT EXISTS dogovor_numbers.contract_numbers ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "contract_number INT NOT NULL,"
                    + "year INT NOT NULL"
                    + ")";
            String createPatientContractNumbersTable = "CREATE TABLE IF NOT EXISTS dogovor_numbers.patient_contract_numbers ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,"
                    + "patient_id INT NOT NULL,"
                    + "contract_number INT NOT NULL,"
                    + "year INT NOT NULL,"
                    + "UNIQUE KEY unique_patient_year (patient_id, year)"
                    + ")";
            String createContractCounterTable = "CREATE TABLE IF NOT EXISTS dogovor_numbers.contract_counter ("
                    + "id INT PRIMARY KEY NOT NULL,"
                    + "current_number INT NOT NULL"
                    + ")";

            try (Connection connection = DriverManager.getConnection(proper.getProperty("database.url"), proper.getProperty("database.login"), proper.getProperty("database.pass"))) {
                try (Statement statement = connection.createStatement()) {
                    // Создаем все схемы и таблицы автоматически
                    statement.executeUpdate(createSchemaSql);
                    statement.executeUpdate(useSchemaSql);
                    statement.executeUpdate(createTableSql);

                    statement.executeUpdate(createSchemacardSchablon);
                    statement.executeUpdate(useSchemacardSchablon);
                    statement.executeUpdate(createcardSchablon);

                    statement.executeUpdate(createSchemaSqlDoctors);
                    statement.executeUpdate(useSchemaSqlDoctors);
                    statement.executeUpdate(createDoctorsTableSql);

                    statement.executeUpdate(createSchemaDiagnose);
                    statement.executeUpdate(useSchemaSqlDiagnose);
                    statement.executeUpdate(createDiagnoseTable);

                    statement.executeUpdate(createSchemaCons);
                    statement.executeUpdate(useSchemaSqlCons);
                    statement.executeUpdate(createDiagnoseCons);

                    statement.executeUpdate(createSchemaRecords);
                    statement.executeUpdate(useSchemaSqlRecords);
                    statement.executeUpdate(createDiagnoseRecords);

                    statement.executeUpdate(createDatabaseTeethFormula);
                    statement.executeUpdate(useDatabaseTeethFormula);
                    statement.executeUpdate(createTableTeethSets);

                    statement.executeUpdate(createSchemaDogovor);
                    statement.executeUpdate(useSchemaDogovor);
                    statement.executeUpdate(createDogovorNumbersTable);
                    statement.executeUpdate(createPatientContractNumbersTable);
                    statement.executeUpdate(createContractCounterTable);
                    
                    // Инициализируем счетчик договоров значением 1, если он еще не существует
                    String checkCounter = "SELECT COUNT(*) as count FROM dogovor_numbers.contract_counter";
                    ResultSet rs = statement.executeQuery(checkCounter);
                    if (rs.next() && rs.getInt("count") == 0) {
                        String insertCounter = "INSERT INTO dogovor_numbers.contract_counter (id, current_number) VALUES (1, 1)";
                        statement.executeUpdate(insertCounter);
                    }

                    // Вставляем данные через PreparedStatement для безопасности
                    insertInitialData(connection);
                }

                // Создаем индексы в отдельном потоке, чтобы не блокировать GUI
                final Connection indexConnection = DriverManager.getConnection(
                    proper.getProperty("database.url"), 
                    proper.getProperty("database.login"), 
                    proper.getProperty("database.pass")
                );
                
                Thread indexThread = new Thread(() -> {
                    try {
                        try (Statement indexStatement = indexConnection.createStatement()) {
                            createIndexes(indexStatement);
                        }
                        indexConnection.close();
                    } catch (SQLException e) {
                        System.out.println("Ошибка при создании индексов в фоне: " + e.getMessage());
                    }
                });
                indexThread.setDaemon(true); // Поток-демон не блокирует закрытие приложения
                indexThread.start();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании баз данных: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации: " + e.getMessage(), e);
        }
    }
    
    private static void createIndexes(Statement statement) throws SQLException {
        // Быстрая проверка существования индексов через SHOW INDEX
        java.util.Set<String> existingIndexes = new java.util.HashSet<>();
        try {
            ResultSet indexRs = statement.executeQuery("SHOW INDEX FROM addperson.pacient");
            while (indexRs.next()) {
                String indexName = indexRs.getString("Key_name");
                if (indexName != null && !indexName.equals("PRIMARY")) {
                    existingIndexes.add(indexName);
                }
            }
            indexRs.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке индексов: " + e.getMessage());
            return;
        }

        // Создаем индексы только если их нет
        java.util.List<String> indexesToCreate = new java.util.ArrayList<>();
        
        if (!existingIndexes.contains("idx_pacient_name")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_name ON addperson.pacient(name)");
        }
        if (!existingIndexes.contains("idx_pacient_cardnumber")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_cardnumber ON addperson.pacient(cardNumber)");
        }
        if (!existingIndexes.contains("idx_pacient_telephone")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_telephone ON addperson.pacient(telephoneNumber)");
        }
        if (!existingIndexes.contains("idx_pacient_doctor")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_doctor ON addperson.pacient(doctorName)");
        }
        if (!existingIndexes.contains("idx_pacient_birthdate")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_birthdate ON addperson.pacient(birthdate)");
        }
        if (!existingIndexes.contains("idx_pacient_registration")) {
            indexesToCreate.add("CREATE INDEX idx_pacient_registration ON addperson.pacient(dateRegistration)");
        }

        // Создаем индексы
        for (String createIndexSql : indexesToCreate) {
            try {
                statement.executeUpdate(createIndexSql);
                System.out.println("Индекс создан: " + createIndexSql);
            } catch (SQLException e) {
                System.out.println("Ошибка при создании индекса: " + e.getMessage());
            }
        }

        if (indexesToCreate.isEmpty()) {
            System.out.println("Все индексы уже существуют");
        } else {
            System.out.println("Создано индексов: " + indexesToCreate.size());
        }
    }
    
    private static void insertInitialData(Connection connection) throws SQLException {
        // НЕ удаляем существующие записи! Только добавляем новые, которых еще нет в базе
        
        // Вставляем лекарства через PreparedStatement с проверкой на дубликаты
        PreparedStatement insertStatement = connection.prepareStatement(
            "INSERT IGNORE INTO resources.medicine (code, medicine, price) VALUES (?, ?, ?)");
        
        String[] medicines = {
            "В01.065.007,Прием ( осмотр,консультация) врача-стомотолога первичный,800",
            "С001,Справка о санации,300",
            "А06.07.003,Прицельная внутриротовая контактная рентгенография,600",
            "А06.07.003,Прицельная внутриротовая контактная рентгенография 3 и более,550",
            "A001,Абсорбирующая прокладка,180",
            "В01.003.004.002,Проводниковая анестезия,950",
            "В01.003.004.005,Инфильтрационная анестезия,0",
            "А16.07.030.003,Временное пломбирование лекарственным препаратом корневого канала,1350",
            "А16.07.030.003,Временное пломбирование лекарственным препаратом корневого канала повторное,650",
            "А16.07.031.001,Восстановление зуба пломбировочными материалами с использованием стекловолоконного штифта,4600",
            "А16.07.031,Восстановление зуба пломбировочными материалами с использованием анкерного штифта,4300",
            "А16.07.002.009,Наложение временной пломбы,300",
            "А16.07.057,Запечатывание фиссуры зуба герметиком,950",
            "А16.07.008.003,Закрытие перфорации стенки корневого канала зуба,2000",
            "А16.07.030.001,Инструментальная и медикаментозная обработка корневого канала зуба,2000",
            "А16.07.030.001,Инструментальная и медикаментозная обработка 2х канал,3500",
            "А16.07.030.001,Инструментальная и медикаментозная обработка 3х канал,4500",
            "А16.07.030.001,Инструментальная и медикаментозная обработка 4х канал,5000",
            "А11.07.027,Наложение девитализирующей пасты,400",
            "А16.07.050.002,Профессиональное отбеливание зубов. Каппа для домашнего отбеливания 1 челюсть,5000",
            "А16.07.008.002,Пломбирование корневого канала зуба гуттаперчивыми штифтами,2900",
            "А16.07.008.002,Пломбирование двух корневых каналов зуба гуттаперчивыми штифтами,4100",
            "А16.07.008.002,Пломбирование трех корневых каналов зуба гуттаперчивыми штифтами,4600",
            "А16.07.008.002,Пломбирование четырех корневых каналов зуба гуттаперчивыми штифтами,5000",
            "В04.065.006.001,Использование коффердама,600",
            "А16.07.002.013,Восстановление молочного зуба пломбой I,V,VI класс по Блэку с использованием материалов из фотополимеров,2900",
            "А16.07.002.014,Восстановление молочного зуба пломбой с нарушением контактного пункта II,III класс по Блэку с использованием материалов из фотополимеров,2900",
            "А16.07.002.015,Восстановление молочного зуба пломбой IV класс по Блэку с использованием материалов из фотополимеров,2900",
            "А16.07.002.010,Восстановление зуба пломбой I класс по Блэку с использованием материалов из фотополимеров,4000",
            "А16.07.002.011,Восстановление зуба пломбой с нарушением контактного пункта II класс по Блэку с использованием материалов из фотополимеров,4450",
            "А16.07.002.011,Восстановление зуба пломбой с нарушением контактного пункта III класс по Блэку с использованием материалов из фотополимеров,4000",
            "А16.07.002.012,Восстановление зуба пломбой IV класс по Блэку с использованием материалов из фотополимеров,4000",
            "А16.07.002.010,Восстановление зуба пломбой V класс по Блэку с использованием материалов из фотополимеров,4000",
            "А16.07.082.001,Распломбировка корневого канала,2500",
            "А16.07.082.001,Распломбировка двух корневых каналов,4000",
            "А16.07.082.001,Распломбировка трех корневых каналов,5000",
            "А16.07.082.001,Распломбировка четырех корневых каналов,5500",
            "S001,Серебрение Saforaid,800",
            "S002,Серебрение Аргинат,400",
            "А16.07.051,Профессиональная гигиена полости рта и зубов (ультрозвук.Air-Flow,полировка),4600",
            "А16.07.094,Удаление внутриканального штифта/вкладки,1500",
            "А16.07.094.001,Удаление фрагмента сломонного инструмента,1500",
            "А11.07.012,Глубокое фторирование эмали зуба,350",
            "А16.07.003.001,Восстановление зуба виниром E-max - предоплата,9000",
            "А16.07.003.001,Восстановление зуба виниром E-maх -сдача,5500",
            "А16.07.033,Восстановление зуба с использованием цельнолитой культевой вкладки,3450",
            "А16.07.004.005,Восстановление зуба коронкой пластмассовой клинического изготавления,1500",
            "А16.07.004.006,Восстановление зуба коронкой пластмассовой лабораторного изготавления,1500",
            "А16.07.111.047,Изготовление индивидуальной жесткой ложки,1500",
            "А16.07.004.002,Восстановление зуба коронкой цельнокерамической. E-max - предоплата,9000",
            "А16.07.004.002,Восстановление зуба коронкой цельнокерамической. E-max - сдача,5500",
            "А16.07.004.004,Восстановление зуба коронкой металлокерамической - предоплата,6000",
            "Ф16.07.004.004,Восстановление зуба коронкой металлокерамической - сдача,4000",
            "А16.07.004.012,Восстановление зуба коронкой цельнолитой из кобальт-хромового сплава - предоплата,3000",
            "А16.07.004.012,Восстановление зуба коронкой цельнолитой из кобальт-хромового сплава - сдача,2000",
            "А16.07.004.003,Восстановление зуба коронкой безметалловой цельноциркониевой - предоплата,9000",
            "А16.07.004.003.2,Востановление зуба коронкой безметалловой цельноциркониевой -сдача,6000",
            "А23.07.002.037,Починка перелома базиса съёмного протеза,1350",
            "А16.07.036,Протезирование сьемными бюгельными протезами -предоплата,25000",
            "А16.07.036,Протезирование сьемными бюгельными протезами-сдача,25000",
            "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - предоплата,13500",
            "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - сдача,11500",
            "А16.07.035,Протезирование зубов частичными съёмными пластиночными протезами - предоплата,12000",
            "А16.07.023,Протезирование зубов полными съёмными пластиночными протезами - сдача,10000",
            "А16.07.053,Снятие несьемной ортопедической конструкции, металлокерамической коронки,450",
            "А16.07.053.002,Снятие несьемной ортопедической конструкции, коронки штампованной,300",
            "А16.07.049.003,Фиксация на композитный цемент,700",
            "А16.07.049.001,Фиксация постояной коронки ( на постояный цемент),400",
            "А16.07.049.001,Фиксация постояной коронки не нашим,700",
            "А16.07.012,Вскрытие и дренирование одонтогенного абсцесса,1000",
            "А16.07.011,Вскрытие подслизистого или поднадкостничного очага воспаления в полости рта,2000",
            "А16.07.013,Отсроченный кюретаж лунки удаленного зуба,1500",
            "А16.07.042,Пластика уздечки верхней губы,5000",
            "А16.07.043,Пластика уздечки нижней губы,4500",
            "А16.07.044,Пластика уздечки языка,4500",
            "А16.07.046,Снятие швов,1000",
            "А16.07.001.002,Удаление постоянного зуба простое,3000",
            "А16.07.001.003,Удаление постоянного зуба сложное,5000",
            "А16.07.001.005,Удаление постоянного зуба с патологической подвижностью,2000",
            "А16.007.001.004,Удаление постоянного зуба ретинированного, дистопированного,7500",
            "А16.07.001.002,Удаление постоянного зуба простое. Удаление 8,5000",
            "А16.07.001.003,Удаление постоянного зуба сложное. Удаление 8 сложное,7000",
            "А16.07.001.001,Удаление временного зуба,2000",
            "А16.07.058,Лечение перекоронита ( промывание, рассечение и/или иссечение капюшона ),2000",
            "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая в составе мостовидного протеза - предоплата,7000",
            "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая в составе мостовидного протеза - сдача,5000",
            "3А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая с титановым абатментом - предоплата,10000",
            "А16.07.006.003,Протезирование зуба с использованием имплантата.Коронка металлокерамическая с титановым абатментом - сдача,5000",
            "А16.07.006.001,Протезирование зуба с использованием имплантата.Коронка циркониевая с титановым абатментом - предоплата,14000",
            "А16.07.006.001,Протезирование зуба с использованием имплантата.Коронка циркониевая с титановым абатментом - сдача,6000",
            "А16.07.006.009,Протезирование зуба с использованием имплантата.Коронка циркониевая в составе мостовидного протеза - предоплата,11000",
            "А16.07.006.009,Протезирование зуба с использованием имплантата.Коронка циркониевая в составе мостовидного протеза - сдача,6000",
            "А16.07.055.003,Синус-лифтинг над областью установки одного дентального импланта при крестальном доступе (включая стоимость материалов),15000",
            "А16.07.055.001,Синус-лифтинг над областью установки одного дентального импланта при доступе через латеральное окно (включая стоимость материалов),45000",
            "А16.07.054.001,Внутрикостная дентальная имплантация «DIO»,32000",
            "А16.07.054,Установка формирователя десны,3000",
            "А16.07.041.002,Костная пластика челюстно-лицевой области с применением биодеградируемых материалов для увеличения толщены костного гребня(включая стоимость материалов),35000",
            "А16.07.089.003,Гингивопластика в области одного имплантата,13000",
            "А16.07.097,Наложения шва на слизистую оболочку рта,500"
        };
        
        for (String medicine : medicines) {
            try {
                String[] parts = medicine.split(",");
                if (parts.length == 3) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    String price = parts[2].trim();
                    
                    // Пропускаем только записи с пустыми полями (но разрешаем цену 0)
                    if (price.isEmpty() || code.isEmpty() || name.isEmpty()) {
                        System.out.println("Пропущена запись с некорректными данными: " + medicine);
                        continue;
                    }
                    
                    insertStatement.setString(1, code);
                    insertStatement.setString(2, name);
                    insertStatement.setString(3, price);
                    insertStatement.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println("Ошибка при вставке лекарства: " + medicine + " - " + e.getMessage());
            }
        }
        
        // Вставляем шаблоны через PreparedStatement
        PreparedStatement shablonStatement = connection.prepareStatement(
            "INSERT IGNORE INTO schablonCard.cards (name, text) VALUES (?, ?)");
        
        String[][] shablons = {
            {"№1 обострение хронического периодонтита", "«____»______________ 202_г.\\nЖалобы: постоянная боль на верхней\\\\нижней челюсти\\nсправа\\\\слева, боль при накусывании, чувство выросшего зуба, в\\nтечение 1-2 дней.\\nОбъективно: зуб _____ - глубокая кариозная полость,\\nзондирование дна и стенок безболезненно, реакция на холод\\nотрицательная, перкуссия болезненная. Переходная складка в\\nобласти проекции корней\\\\ня _______ зуба болезненна при\\nпальпации, отечна.\\nДиагноз: обострение хронического периодонтита _____ зуба.\\nЛечение: препарирование кариозной полости_____ зуба, вскрыта и\\nраскрыта пульпарная полость, инструментальная и\\nмедикаментозная обработка _____ каналов, зуб оставлен открытым\\n\\n______________"},
            {"№2 Хронический периодонтит", "Жалобы: разрушенность зуба на верхней, нижней челюсти справа, слева.\\n Объективно: при внешнем осмотре изменений не определяется.\\n Поднижнечелюстные лимфатические узлы не увеличены, безболезненны\\n при пальпации. Рот открывается свободно.\\n В полости рта-коронка ____ разрушена ниже десневого края.\\n На десне с вестибулярной стороны в области проекции верхушек/ки\\n корней/ня отека слизистой оболочки не определяется, пальпация этой\\n области безболезненная.\\n Диагноз: Хронический периодонтит ____ зуба.\\n Лечение: под аппликационной, нёбной/мандибулярной и\\n инфильтрационной анестезией Sol.Lidocaini 2%-6ml произведено\\n удаление ____зуба, выполнен кюретаж лунок, края лунки сжаты, лунка\\n заполнилась сгустком крови.\\n\\n__________"},
            {"№3 Хронический периодонтит 2", "«____»______________ 202_г.\\n Жалобы: разрушенность зуба на верхней, нижней челюсти справа, слева.\\n Объективно: при внешнем осмотре изменений не определяется.\\n Поднижнечелюстные лимфатические узлы не увеличены, безболезненны\\n при пальпации. Рот открывается свободно.\\n В полости рта-коронка ____ разрушена ниже десневого края.\\n На десне с вестибулярной стороны в области проекции верхушек/ки\\n корней/ня отека слизистой оболочки не определяется, пальпация этой\\n области безболезненная.\\n Диагноз: Хронический периодонтит ____ зуба.\\n Лечение: под аппликационной, нёбной/мандибулярной и\\n инфильтрационной анестезией Sol.Lidocaini 2%-6ml произведено\\n сложное удаление ____зуба с разъединением корней, выполнен кюретаж\\n лунок, края лунки сжаты, лунка заполнилась сгустком крови.\\n\\n________"},
            {"№4 ПЕРИОСТИТ", "«__»______________ 202_г.\\n Жалобы: припухлость щеки справа\\\\слева, в области верхней\\\\нижней губы, боль в этой\\n области, повышение температуры тела до 38°С.\\n Объективно: при внешнем осмотре определяется нарушение конфигурации лица за счет\\n припухлости в щечной и подглазничной областях справа\\\\слева, в области верхней\\\\нижней\\n губы. Кожа над ней в цвете не изменена, собирается в складку. Поднижнечелюстные\\n лимфатические узлы справа\\\\слева увеличены, слабоболезненны при пальпации. Рот\\n открывается свободно.\\n В полости рта-коронка ____ разрушена ниже десневого края, глубокая КП на МО\\n поверхности. Переходная складка сглажена в области _______________ зубов, при\\n пальпации болезненная, определяется положительный симптом «флюктуации».\\n Диагноз: острый гнойный периостит верхней\\\\нижней челюсти справа\\\\слева в области\\n ______________ зубов.\\n Лечение: под аппликационной, нёбной/мандибулярной и инфильтрационной анестезией\\n Sol.Lidocaini 2%-6ml произведен разрез по переходной складке в области_______ зубов,\\n дренирование, края раны сшиты.\\n\\n____________"},
            {"№5 Глубокий кариес", "«____»______________ 202_г.\\n Жалобы: кратковременная боль на верхней\\\\нижней челюсти справа\\\\слева\\n от всех видов раздражителей, боль появилась 2 недели назад.\\n Объективно: зуб _____ -на МО поверхности глубокая кариозная полость,\\n зондирование по всему дну умеренно болезненное, дно плотное, реакция\\n на холод кратковременная.\\n Диагноз: глубокий кариес _____ зуба, II класс по Блэку.\\n Лечение: под аппликационной, нёбной/мандибулярной и\\n инфильтрационной анестезией Sol.Lidocaini 2%-6ml препарирование\\n кариозной полости_____ зуба, медикаментозная обработка, поставлена\\n пломба из композитного материала химического отверждения «Эвикрол».\\n \\n _______________"},
            {"№6 Острый очаговый пульпит", "«____»______________ 202_г.\\n Жалобы: самопроизвольная локализованная боль на верхней\\\\нижней\\n челюсти справа\\\\слева, длительная боль от всех видов раздражителей,\\n ночная боль в течение 1-2 дней.\\n Объективно: зуб _____ глубокая кариозная полость, выполненная\\n размягченным дентином, зондирование дна резко болезненно в одной\\n точке, реакция на холод длительная.\\n Диагноз: острый очаговый пульпит ______ зуба.\\n Лечение: под аппликационной, нёбной/мандибулярной и\\n инфильтрационной анестезией Sol.Lidocaini 2%-6ml препарирование\\n кариозной полости_____ зуба, вскрыта и раскрыта пульпарная полость,\\n ампутация и экстирпация пульпы, инструментальная и медикаментозная\\n обработка _____ каналов. Каналы запломбированы цинкоксидэвгенольным\\n цементом «Тиэдент», постановка временной пломбы.\\n\\n__________"},
            {"№7 Острый очаговый пульпит 2", "«____»______________ 202_г.\\n Зуб_____. Жалоб-нет.\\n Объективно: зуб _____ под временной пломбой.\\n Диагноз: острый очаговый пульпит ______ зуба.\\n Лечение: снятие временной пломбы, препарирование кариозной\\n полости_____ зуба, медикаментозная обработка, поставлена пломба из\\n композитного материала химического отверждения «Эвикрол».\\n    _______________"}
        };
        
        for (String[] shablon : shablons) {
            shablonStatement.setString(1, shablon[0]);
            shablonStatement.setString(2, shablon[1]);
            shablonStatement.executeUpdate();
        }
    }
}