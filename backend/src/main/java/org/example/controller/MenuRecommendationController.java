package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.RecommendationResponse;
import org.example.service.GoogleSpeechRecognitionService;
import org.example.service.PerplexityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MenuRecommendationController {

    private final GoogleSpeechRecognitionService speechRecognitionService;
    private final PerplexityService perplexityService;
    private final ObjectMapper objectMapper;

    @Value("${app.default.location:San Francisco, CA}")
    private String defaultLocation;

    @Autowired
    public MenuRecommendationController(GoogleSpeechRecognitionService speechRecognitionService,
                                      PerplexityService perplexityService,
                                      ObjectMapper objectMapper) {
        this.speechRecognitionService = speechRecognitionService;
        this.perplexityService = perplexityService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from Menu Recommendation Service!";
    }
    
    @PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> convertSpeechToText(
            @RequestParam("file") MultipartFile speechFile) {
        
        try {
            // Log the received file
            System.out.println("Received speech file for menu transcription: " + speechFile.getOriginalFilename() + 
                    ", size: " + speechFile.getSize() + " bytes");
            
            // Check if file is empty
            if (speechFile.isEmpty()) {
                System.out.println("Error: File is empty");
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            // Check file type
            String contentType = speechFile.getContentType();
            System.out.println("File content type: " + contentType);
            
            // Process the audio file using Google Cloud Speech-to-Text
            String transcribedText = speechRecognitionService.recognizeSpeech(speechFile);
            
            // Print the transcribed text to the terminal
            System.out.println("----------------------------------------");
            System.out.println("TRANSCRIBED SPEECH FOR MENU SEARCH:");
            System.out.println(transcribedText);
            System.out.println("----------------------------------------");
            
            // Return a simple success message
            return ResponseEntity.ok("Speech processed successfully for menu search");
            
        } catch (Exception e) {
            System.out.println("Error processing speech file for menu search: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to process speech file: " + e.getMessage());
        }
    }

    @PostMapping(value = "/speech-to-menu-recommendations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendationResponse> speechToMenuRecommendations(
            @RequestParam("file") MultipartFile speechFile,
            @RequestParam(value = "location", required = false) String location) {

        try {
            // Log the received file
            System.out.println("Received speech file for menu recommendations: " + speechFile.getOriginalFilename() +
                    ", size: " + speechFile.getSize() + " bytes");

            // Check if file is empty
            if (speechFile.isEmpty()) {
                System.out.println("Error: File is empty");
                return ResponseEntity.badRequest().build();
            }

            // Step 1: Convert speech to text
            String transcribedText = speechRecognitionService.recognizeSpeech(speechFile);

            System.out.println("----------------------------------------");
            System.out.println("TRANSCRIBED SPEECH FOR MENU SEARCH:");
            System.out.println(transcribedText);
            System.out.println("----------------------------------------");

            // Step 2: Get menu recommendations from Perplexity
            String searchLocation = location != null ? location : defaultLocation;
            RecommendationResponse menuRecommendations = perplexityService.getMenuRecommendations(
                transcribedText, searchLocation);

            System.out.println("Generated " + menuRecommendations.getRecommendations().size() + " menu recommendations");

            // Print the final JSON response that will be sent to the UI
            try {
                String jsonResponse = objectMapper.writeValueAsString(menuRecommendations);
                System.out.println("========================================");
                System.out.println("FINAL MENU RECOMMENDATIONS JSON TO UI:");
                System.out.println(jsonResponse);
                System.out.println("========================================");
            } catch (Exception e) {
                System.err.println("Error serializing menu recommendations to JSON: " + e.getMessage());
            }

            return ResponseEntity.ok(menuRecommendations);

        } catch (Exception e) {
            System.out.println("Error processing speech to menu recommendations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/text-to-menu-recommendations")
    public ResponseEntity<RecommendationResponse> textToMenuRecommendations(
            @RequestParam("query") String query,
            @RequestParam(value = "location", required = false) String location) {

        try {
            System.out.println("Received text query for menu recommendations: " + query);

            String searchLocation = location != null ? location : defaultLocation;
            RecommendationResponse menuRecommendations = perplexityService.getMenuRecommendations(
                query, searchLocation);

            System.out.println("Generated " + menuRecommendations.getRecommendations().size() + " menu recommendations");

            // Print the final JSON response that will be sent to the UI
            try {
                String jsonResponse = objectMapper.writeValueAsString(menuRecommendations);
                System.out.println("========================================");
                System.out.println("FINAL MENU RECOMMENDATIONS JSON TO UI:");
                System.out.println(jsonResponse);
                System.out.println("========================================");
            } catch (Exception e) {
                System.err.println("Error serializing menu recommendations to JSON: " + e.getMessage());
            }

            return ResponseEntity.ok(menuRecommendations);

        } catch (Exception e) {
            System.out.println("Error processing text to menu recommendations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
