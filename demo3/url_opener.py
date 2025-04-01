from PyQt5.QtWidgets import QApplication, QMainWindow, QWidget, QVBoxLayout, QLineEdit, QPushButton
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
import sys

class URLOpener(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("URL Açıcı")
        self.setGeometry(100, 100, 400, 150)
        
        # Ana widget ve layout oluşturma
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        layout = QVBoxLayout(central_widget)
        
        # URL giriş kutusu
        self.url_input = QLineEdit()
        self.url_input.setPlaceholderText("URL giriniz (örn: https://www.google.com)")
        layout.addWidget(self.url_input)
        
        # Aç butonu
        self.open_button = QPushButton("Aç")
        self.open_button.clicked.connect(self.open_url)
        layout.addWidget(self.open_button)
        
    def open_url(self):
        url = self.url_input.text()
        if url:
            # Chrome ayarlarını yapılandırma
            chrome_options = Options()
            chrome_options.add_experimental_option("detach", True)  # Tarayıcı penceresi açık kalır
            
            # WebDriver'ı başlatma
            service = Service(ChromeDriverManager().install())
            driver = webdriver.Chrome(service=service, options=chrome_options)
            
            # URL'yi açma
            driver.get(url)

if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = URLOpener()
    window.show()
    sys.exit(app.exec_()) 