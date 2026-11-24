package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UIEnhancements {
    
    // Современная цветовая схема
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);      // Синий
    public static final Color SECONDARY_COLOR = new Color(46, 204, 113);    // Зеленый
    public static final Color ACCENT_COLOR = new Color(241, 196, 15);       // Желтый
    public static final Color DANGER_COLOR = new Color(231, 76, 60);        // Красный
    public static final Color BACKGROUND_COLOR = new Color(245, 246, 250);  // Светло-серый
    public static final Color TEXT_COLOR = new Color(44, 62, 80);           // Темно-серый
    public static final Color BORDER_COLOR = new Color(189, 195, 199);      // Серый
    public static final Color WHITE = Color.WHITE;
    
    // Современные шрифты
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    /**
     * Настраивает современный Look and Feel для приложения
     */
    public static void setupModernLookAndFeel() {
        try {
            // Пробуем установить системный Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                // Если системный не работает, пробуем Nimbus (современный)
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e2) {
                try {
                    // Если Nimbus не работает, пробуем Metal
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                } catch (Exception e3) {
                    try {
                        // Последняя попытка - установить по умолчанию
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception e4) {
                        System.err.println("Не удалось установить Look and Feel: " + e4.getMessage());
                        return; // Выходим, если ничего не работает
                    }
                }
            }
        }
        
        try {
            // Кастомизируем цвета только если Look and Feel установлен успешно
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", WHITE);
            UIManager.put("Button.font", BUTTON_FONT);
            UIManager.put("Button.border", BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("Label.foreground", TEXT_COLOR);
            UIManager.put("Label.font", BODY_FONT);
            
            UIManager.put("Table.background", WHITE);
            UIManager.put("Table.foreground", TEXT_COLOR);
            UIManager.put("Table.font", BODY_FONT);
            UIManager.put("Table.gridColor", BORDER_COLOR);
            UIManager.put("Table.selectionBackground", PRIMARY_COLOR);
            UIManager.put("Table.selectionForeground", WHITE);
            
            UIManager.put("TextField.background", WHITE);
            UIManager.put("TextField.foreground", TEXT_COLOR);
            UIManager.put("TextField.font", BODY_FONT);
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            
        } catch (Exception e) {
            System.err.println("Не удалось настроить цвета UI: " + e.getMessage());
        }
    }
    
    /**
     * Создает современную кнопку с эффектами наведения
     */
    public static JButton createModernButton(String text, Color backgroundColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Рисуем фон с закругленными углами
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                
                // Рисуем текст
                g2d.setColor(getForeground());
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(BUTTON_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
        
        // Добавляем эффекты наведения
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
                button.repaint();
            }
        });
        
        return button;
    }
    
    /**
     * Создает основную синюю кнопку
     */
    public static JButton createPrimaryButton(String text) {
        return createModernButton(text, PRIMARY_COLOR, PRIMARY_COLOR.darker());
    }
    
    /**
     * Создает зеленую кнопку успеха
     */
    public static JButton createSuccessButton(String text) {
        return createModernButton(text, SECONDARY_COLOR, SECONDARY_COLOR.darker());
    }
    
    /**
     * Создает красную кнопку опасности
     */
    public static JButton createDangerButton(String text) {
        return createModernButton(text, DANGER_COLOR, DANGER_COLOR.darker());
    }
    
    /**
     * Создает желтую кнопку предупреждения
     */
    public static JButton createWarningButton(String text) {
        return createModernButton(text, ACCENT_COLOR, ACCENT_COLOR.darker());
    }
    
    /**
     * Создает красивую панель с тенью
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Рисуем тень
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 2, getHeight() - 2, 10, 10));
                
                // Рисуем основной фон
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 2, getHeight() - 2, 10, 10));
                
                g2d.dispose();
            }
        };
        
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setOpaque(false);
        
        return panel;
    }
    
    /**
     * Создает заголовок
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
    
    /**
     * Создает подзаголовок
     */
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }
    
    /**
     * Создает индикатор загрузки
     */
    public static JProgressBar createLoadingIndicator() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Загрузка...");
        progressBar.setFont(BODY_FONT);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(BACKGROUND_COLOR);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return progressBar;
    }
    
    /**
     * Создает модальное окно загрузки
     */
    public static JDialog createLoadingDialog(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "Загрузка", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(WHITE);
        
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(BODY_FONT);
        messageLabel.setForeground(TEXT_COLOR);
        
        JProgressBar progressBar = createLoadingIndicator();
        
        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        dialog.add(panel);
        return dialog;
    }
    
    /**
     * Создает красивое текстовое поле
     */
    public static JTextField createModernTextField(String placeholder) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Рисуем фон
                g2d.setColor(getBackground());
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        
        textField.setFont(BODY_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        textField.setBackground(WHITE);
        textField.setForeground(TEXT_COLOR);
        
        // Placeholder text
        if (placeholder != null && !placeholder.isEmpty()) {
            textField.setToolTipText(placeholder);
        }
        
        return textField;
    }
} 