package org.monarchinitiative.fenominal.gui;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.monarchinitiative.fenominal.gui.guitools.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Component
public class StageInitializer implements ApplicationListener<FenominalApplication.StageReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInitializer.class);

    @Value("classpath:/fenominal-main.fxml")
    private Resource fenominalFxmResource;
    private final String applicationTitle;

    private final ApplicationContext applicationContext;


    public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle, ApplicationContext context) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = context;
    }


    @Override
    public void onApplicationEvent(FenominalApplication.StageReadyEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(fenominalFxmResource.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 800, 600));
            stage.setTitle(applicationTitle);
            stage.setResizable(false);
            readAppIcon().ifPresent(stage.getIcons()::add);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Image> readAppIcon() {
        if (Platform.isMacintosh()) {
            try {
                URL iconURL = StageInitializer.class.getResource("/img/phenomenon.png");
                if (iconURL == null) return Optional.empty();
                java.awt.Image macimage = new ImageIcon(iconURL).getImage();
                // not working, need replacement for Java 17
                //com.apple.eawt.Application.getApplication().setDockIconImage(macimage);
            } catch (Exception e) {
                // Won't work on Windows or Linux. Just skip it!
            }
        }
        try (InputStream is = StageInitializer.class.getResourceAsStream("/img/phenomenon.png")) {
            if (is != null) {
                return Optional.of(new Image(is));
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading app icon {}", e.getMessage());
        }
        return Optional.empty();
    }
}
