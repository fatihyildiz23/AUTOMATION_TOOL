package com.example.demo3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.AbstractButton;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.datatransfer.*;

public class URLOpener extends JFrame {
    private JTextField urlInput;
    private JTextField locatorInput;
    private JTextField textInput;
    private JComboBox<String> actionSelector;
    private JComboBox<String> locatorTypeSelector;
    private JButton executeButton;
    private WebDriver driver;
    private JTable testStepsTable;
    private DefaultTableModel tableModel;
    private List<TestStep> testSteps = new ArrayList<>();
    private JButton editStepButton;
    private int selectedRowIndex = -1;

    // Test adımı için iç sınıf
    private class TestStep {
        String locatorType;
        String locator;
        String action;
        String text;

        TestStep(String locatorType, String locator, String action, String text) {
            this.locatorType = locatorType;
            this.locator = locator;
            this.action = action;
            this.text = text;
        }
    }

    public URLOpener() {
        setTitle("Selenium Otomasyon Aracı");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // URL Panel
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlInput = new JTextField();
        urlPanel.add(new JLabel("URL: "), BorderLayout.WEST);
        urlPanel.add(urlInput, BorderLayout.CENTER);
        mainPanel.add(urlPanel, BorderLayout.NORTH);

        // Orta Panel (Test Adımları Girişi)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // İşlem Paneli
        JPanel operationsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Locator Tipi ComboBox
        gbc.gridx = 0; gbc.gridy = 0;
        operationsPanel.add(new JLabel("Locator Tipi:"), gbc);
        gbc.gridx = 1;
        locatorTypeSelector = new JComboBox<>(new String[]{"XPATH", "ID", "Name", "Class", "CSS"});
        operationsPanel.add(locatorTypeSelector, gbc);

        // Locator Input
        gbc.gridx = 0; gbc.gridy = 1;
        operationsPanel.add(new JLabel("Locator:"), gbc);
        gbc.gridx = 1;
        locatorInput = new JTextField(20);
        operationsPanel.add(locatorInput, gbc);

        // İşlem ComboBox
        gbc.gridx = 0; gbc.gridy = 2;
        operationsPanel.add(new JLabel("İşlem:"), gbc);
        gbc.gridx = 1;
        actionSelector = new JComboBox<>(new String[]{"Tıkla", "Yazdır", "Bekle", "Temizle", "Metin Al"});
        operationsPanel.add(actionSelector, gbc);

        // Text Input
        gbc.gridx = 0; gbc.gridy = 3;
        operationsPanel.add(new JLabel("Text:"), gbc);
        gbc.gridx = 1;
        textInput = new JTextField(20);
        operationsPanel.add(textInput, gbc);

        // Tablo
        String[] columnNames = {"Locator Tipi", "Locator", "İşlem", "Text"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücrelerin düzenlenmesini engelle
            }
        };
        testStepsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(testStepsTable); // Tablo için kaydırma paneli ekle
        testStepsTable.setDragEnabled(true);
        testStepsTable.setDropMode(DropMode.INSERT_ROWS);
        testStepsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        testStepsTable.getTableHeader().setReorderingAllowed(false); // Başlıkların sürüklenmesini engelle
        testStepsTable.getTableHeader().setResizingAllowed(true); // Sütun genişliklerinin değiştirilebilmesine izin ver
        
        // Sürükle-bırak işleyicisini ekle
        testStepsTable.setTransferHandler(new TransferHandler() {
            public int getSourceActions(JComponent c) {
                return TransferHandler.MOVE;
            }

            protected Transferable createTransferable(JComponent c) {
                JTable table = (JTable) c;
                int row = table.getSelectedRow();
                if (row != -1) {
                    TestStep step = testSteps.get(row);
                    return new StringSelection(row + "");
                }
                return null;
            }

            protected void exportDone(JComponent c, Transferable t, int action) {
                if (action == TransferHandler.MOVE) {
                    JTable table = (JTable) c;
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                    }
                }
            }

            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                JTable table = (JTable) support.getComponent();
                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
                int dropRow = dl.getRow();
                
                try {
                    String rowStr = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    int sourceRow = Integer.parseInt(rowStr);
                    
                    if (dropRow > sourceRow) {
                        dropRow--;
                    }
                    
                    // Test adımını taşı
                    TestStep step = testSteps.remove(sourceRow);
                    testSteps.add(dropRow, step);
                    
                    // Tabloyu güncelle
                    tableModel.insertRow(dropRow, new Object[]{
                        step.locatorType,
                        step.locator,
                        step.action,
                        step.text
                    });
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        // Tablo seçim listener'ı ekle
        testStepsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRowIndex = testStepsTable.getSelectedRow();
                editStepButton.setEnabled(selectedRowIndex != -1);
                
                if (selectedRowIndex != -1) {
                    // Seçili adımın bilgilerini form alanlarına doldur
                    TestStep step = testSteps.get(selectedRowIndex);
                    locatorTypeSelector.setSelectedItem(step.locatorType);
                    locatorInput.setText(step.locator);
                    actionSelector.setSelectedItem(step.action);
                    textInput.setText(step.text);
                }
            }
        });

        // Butonlar Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addStepButton = new JButton("Adım Ekle");
        editStepButton = new JButton("Adımı Düzenle");
        JButton runTestButton = new JButton("Testi Çalıştır");
        JButton clearStepsButton = new JButton("Adımları Temizle");

        editStepButton.setEnabled(false); // Başlangıçta devre dışı
        
        addStepButton.addActionListener(e -> addTestStep());
        editStepButton.addActionListener(e -> editSelectedStep());
        runTestButton.addActionListener(e -> runAllSteps());
        clearStepsButton.addActionListener(e -> clearSteps());

        // Yukarı-Aşağı butonları
        JPanel moveButtonsPanel = new JPanel(new FlowLayout());
        JButton moveUpButton = new JButton("↑");
        JButton moveDownButton = new JButton("↓");

        moveUpButton.addActionListener(e -> moveStep(-1));
        moveDownButton.addActionListener(e -> moveStep(1));

        moveButtonsPanel.add(moveUpButton);
        moveButtonsPanel.add(moveDownButton);

        buttonPanel.add(addStepButton);
        buttonPanel.add(editStepButton);
        buttonPanel.add(runTestButton);
        buttonPanel.add(clearStepsButton);
        buttonPanel.add(moveButtonsPanel);

        // Panelleri yerleştirme
        centerPanel.add(operationsPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack(); // Pencere boyutunu içeriğe göre ayarla
        setSize(600, 500); // Minimum pencere boyutu
    }

    private void executeAction() {
        if (driver == null) {
            showError("Önce bir URL açmalısınız!");
            return;
        }

        String locator = locatorInput.getText().trim();
        if (locator.isEmpty()) {
            showError("Locator boş olamaz!");
            return;
        }

        try {
            By by;
            String selectedLocatorType = getSelectedLocatorType();
            
            switch (selectedLocatorType) {
                case "XPATH":
                    by = By.xpath(locator);
                    break;
                case "ID":
                    by = By.id(locator);
                    break;
                case "Name":
                    by = By.name(locator);
                    break;
                case "Class":
                    by = By.className(locator);
                    break;
                case "CSS":
                    by = By.cssSelector(locator);
                    break;
                default:
                    by = By.xpath(locator);
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));

            String selectedAction = (String) actionSelector.getSelectedItem();
            switch (selectedAction) {
                case "Tıkla":
                    wait.until(ExpectedConditions.elementToBeClickable(element));
                    element.click();
                    break;
                case "Yazdır":
                    element.sendKeys(textInput.getText());
                    break;
                case "Bekle":
                    Thread.sleep(3000);
                    break;
                case "Temizle":
                    element.clear();
                    break;
                case "Metin Al":
                    String text = element.getText();
                    JOptionPane.showMessageDialog(this, "Alınan Metin: " + text);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("İşlem hatası: " + e.getMessage());
        }
    }

    private String getSelectedLocatorType() {
        return (String) locatorTypeSelector.getSelectedItem();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        if (driver != null) {
            driver.quit();
        }
        super.dispose();
    }

    private void addTestStep() {
        String selectedLocatorType = (String) locatorTypeSelector.getSelectedItem();
        String locator = locatorInput.getText().trim();
        String action = (String) actionSelector.getSelectedItem();
        String text = textInput.getText().trim();

        if (locator.isEmpty()) {
            showError("Locator boş olamaz!");
            return;
        }

        TestStep step = new TestStep(selectedLocatorType, locator, action, text);
        testSteps.add(step);

        // Tabloya ekleme
        tableModel.addRow(new Object[]{
            selectedLocatorType,
            locator,
            action,
            text
        });
    }

    private void clearSteps() {
        testSteps.clear();
        tableModel.setRowCount(0);
    }

    private void runAllSteps() {
        String url = urlInput.getText().trim();
        if (url.isEmpty()) {
            showError("URL boş olamaz!");
            return;
        }

        if (testSteps.isEmpty()) {
            showError("Çalıştırılacak test adımı yok!");
            return;
        }

        try {
            // İlk önce tarayıcıyı aç ve URL'ye git
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            driver = new ChromeDriver(options);
            driver.get(url);

            // Sayfanın yüklenmesi için kısa bir bekleme
            Thread.sleep(2000);

            // Test adımlarını çalıştır
            for (TestStep step : testSteps) {
                By by;
                switch (step.locatorType) {
                    case "XPATH":
                        by = By.xpath(step.locator);
                        break;
                    case "ID":
                        by = By.id(step.locator);
                        break;
                    case "Name":
                        by = By.name(step.locator);
                        break;
                    case "Class":
                        by = By.className(step.locator);
                        break;
                    case "CSS":
                        by = By.cssSelector(step.locator);
                        break;
                    default:
                        by = By.xpath(step.locator);
                }

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));

                switch (step.action) {
                    case "Tıkla":
                        wait.until(ExpectedConditions.elementToBeClickable(element));
                        element.click();
                        break;
                    case "Yazdır":
                        element.sendKeys(step.text);
                        break;
                    case "Bekle":
                        Thread.sleep(3000);
                        break;
                    case "Temizle":
                        element.clear();
                        break;
                    case "Metin Al":
                        String text = element.getText();
                        JOptionPane.showMessageDialog(this, "Alınan Metin: " + text);
                        break;
                }
                // Her adım arasında kısa bir bekleme
                Thread.sleep(1000);
            }
            JOptionPane.showMessageDialog(this, "Test başarıyla tamamlandı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Test çalıştırma hatası: " + e.getMessage());
        } finally {
            // Test başarılı olsun veya olmasın, tarayıcıyı kapat
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        }
    }

    // Yeni metod: Seçili adımı düzenle
    private void editSelectedStep() {
        if (selectedRowIndex == -1) return;

        String selectedLocatorType = (String) locatorTypeSelector.getSelectedItem();
        String locator = locatorInput.getText().trim();
        String action = (String) actionSelector.getSelectedItem();
        String text = textInput.getText().trim();

        if (locator.isEmpty()) {
            showError("Locator boş olamaz!");
            return;
        }

        // Test adımını güncelle
        TestStep updatedStep = new TestStep(selectedLocatorType, locator, action, text);
        testSteps.set(selectedRowIndex, updatedStep);

        // Tabloyu güncelle
        tableModel.setValueAt(selectedLocatorType, selectedRowIndex, 0);
        tableModel.setValueAt(locator, selectedRowIndex, 1);
        tableModel.setValueAt(action, selectedRowIndex, 2);
        tableModel.setValueAt(text, selectedRowIndex, 3);

        // Form alanlarını temizle
        clearFormFields();
        selectedRowIndex = -1;
        editStepButton.setEnabled(false);
    }

    // Form alanlarını temizleme metodu
    private void clearFormFields() {
        locatorInput.setText("");
        textInput.setText("");
        locatorTypeSelector.setSelectedIndex(0);
        actionSelector.setSelectedIndex(0);
    }

    // Adımı taşıma metodu
    private void moveStep(int direction) {
        int selectedRow = testStepsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int newPosition = selectedRow + direction;
        if (newPosition < 0 || newPosition >= testSteps.size()) return;

        // Test adımını taşı
        TestStep step = testSteps.remove(selectedRow);
        testSteps.add(newPosition, step);

        // Tabloyu güncelle
        tableModel.moveRow(selectedRow, selectedRow, newPosition);
        testStepsTable.setRowSelectionInterval(newPosition, newPosition);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            URLOpener app = new URLOpener();
            app.setVisible(true);
        });
    }
} 