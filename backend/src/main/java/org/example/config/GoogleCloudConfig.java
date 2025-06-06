package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

@Configuration
public class GoogleCloudConfig {

    @Value("${google.cloud.credentials.path}")
    private String credentialsPath;
    
    @PostConstruct
    public void init() {
        // Set the environment variable for Google Cloud credentials
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
            System.out.println("Set Google credentials path to: " + credentialsPath);
        } else {
            System.out.println("Using Google credentials from environment variable: " + 
                    System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        }
    }
}