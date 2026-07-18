package com.yourname.campusplacement.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        // Cloudinary SDK automatically parses the cloudinary:// URL if we pass it in the config map
        // or we can just initialize it with the URL directly
        return new Cloudinary(cloudinaryUrl);
    }
}
