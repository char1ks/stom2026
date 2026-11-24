package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DiscountInProcent extends JFrame {
    private final int WIDTH = 450;
    private final int HEIGHT = 250;
    private static JPanel panel;
    private JLabel aboutLabel;
    private JButton submitButton;
    private Double discount;
    private String discountWithProcent;
    DiscountInProcent() {
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel = new JPanel();
        getContentPane().add(panel);
        setTitle("Выберите процент скидки");
        placeComponents(panel);
        setVisible(true);
    }

    public void placeComponents(JPanel panel) {
        panel.setLayout(null);
        aboutLabel = new JLabel("Выберите скидку для пациента");
        aboutLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        aboutLabel.setBounds(100, 10, 250, 25);
        panel.add(aboutLabel);
        String[] discountsList = {"10%", "15%", "20%", "25%", "30%", "35%", "40%", "50%", "60%", "75%"};
        JComboBox discounts = new JComboBox<>(discountsList);
        discounts.setBounds(135, 50, 150, 25);
        discounts.setBackground(Color.WHITE);
        discounts.setForeground(Color.BLACK);
        panel.add(discounts);
        discounts.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        submitButton = new JButton("Отправить");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 16));
        submitButton.setForeground(Color.WHITE);
        submitButton.setBackground(new Color(119, 155, 191));
        submitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        submitButton.setBounds(110, 150, 200, 25);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedDiscount = (String) discounts.getSelectedItem();
                String discountWithoutSymbol = selectedDiscount.replaceAll("[^0-9.]", "");
                Double discount = Double.parseDouble(discountWithoutSymbol);
                discountGetWithProcent(selectedDiscount);
                setDiscount(discount);
                dispose();
            }
        });
        JButton manualButton = new JButton("Ввести вручную");
        manualButton.setFont(new Font("Arial", Font.PLAIN, 16));
        manualButton.setForeground(Color.WHITE);
        manualButton.setBackground(new Color(119, 155, 191));
        manualButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        manualButton.setBounds(110, 90, 200, 25);
        manualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String discountText = JOptionPane.showInputDialog(null, "Введите процент скидки (0-100):");
                try {
                    double discount = Double.parseDouble(discountText);
                    if (discount >= 0 && discount <= 100) {
                        String discountWithProcent = discountText + "%";
                        setDiscount(discount);
                        discountGetWithProcent(discountWithProcent);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Некорректный процент скидки. Введите число от 0 до 100.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Некорректный процент скидки. Введите число от 0 до 100.");
                }
            }
        });
        panel.add(manualButton);
        panel.add(submitButton);
    }
    public void setDiscount(Double discount){
        this.discount=discount;
    }
    public Double getDiscount(){
        return this.discount;
    }
    public void discountGetWithProcent(String Discount){
        this.discountWithProcent=Discount;
    }
    public String getDiscountWithProcent(){
        return this.discountWithProcent;
    }
}
