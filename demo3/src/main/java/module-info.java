module com.example.demo3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.firefox_driver;
    requires org.seleniumhq.selenium.edge_driver;
    requires org.seleniumhq.selenium.support;
    requires io.github.bonigarcia.webdrivermanager;
    requires java.prefs;
    requires dev.failsafe.core;
    requires org.slf4j;
    requires com.google.gson;

    opens com.example.demo3 to javafx.fxml;
    exports com.example.demo3;
}