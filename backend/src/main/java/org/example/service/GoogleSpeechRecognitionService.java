package org.example.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class GoogleSpeechRecognitionService {

    @Value("${google.cloud.credentials.path}")
    private String credentialsPath;

    public String recognizeSpeech(MultipartFile audioFile) throws IOException {
        // Get file content as bytes
        byte[] audioBytes = audioFile.getBytes();
        
        // Determine audio format from content type and file content
        String contentType = audioFile.getContentType();
        RecognitionConfig.AudioEncoding encoding = determineAudioEncoding(contentType, audioBytes);

        System.out.println("Detected content type: " + contentType);
        System.out.println("Selected encoding: " + encoding);
        
        // Configure recognition request
        RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setLanguageCode("en-US");

        // Set sample rate based on encoding type
        if (encoding == RecognitionConfig.AudioEncoding.OGG_OPUS) {
            // For OPUS, let Google auto-detect the sample rate or use 48000
            configBuilder.setSampleRateHertz(48000);
        } else if (encoding == RecognitionConfig.AudioEncoding.WEBM_OPUS) {
            // For WEBM OPUS, let Google auto-detect the sample rate
            configBuilder.setSampleRateHertz(48000);
        } else {
            // For other formats like LINEAR16 (WAV)
            configBuilder.setSampleRateHertz(16000);
        }

        RecognitionConfig config = configBuilder.build();
        
        // Prepare the audio data
        ByteString audioData = ByteString.copyFrom(audioBytes);
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioData)
                .build();
        
        // Perform the speech recognition
        try (SpeechClient speechClient = createSpeechClient()) {
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            
            if (results.isEmpty()) {
                return "No speech detected";
            }
            
            // Get the top alternative (most likely transcription)
            StringBuilder transcription = new StringBuilder();
            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                transcription.append(alternative.getTranscript());
            }
            
            return transcription.toString();
        } catch (Exception e) {
            throw new IOException("Error recognizing speech: " + e.getMessage(), e);
        }
    }

    private SpeechClient createSpeechClient() throws IOException {
        try {
            GoogleCredentials credentials;

            // Try to get credentials from environment variable first (for production)
            String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                System.out.println("Using Google credentials from environment variable");
                credentials = GoogleCredentials.fromStream(
                    new java.io.ByteArrayInputStream(credentialsJson.getBytes())
                );
            } else {
                // Fallback to file-based credentials (for local development)
                System.out.println("Using Google credentials from file: " + credentialsPath);
                credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
                );
            }

            // Create SpeechSettings with the credentials
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

            return SpeechClient.create(speechSettings);
        } catch (Exception e) {
            throw new IOException("Failed to create Speech client. Tried environment variable GOOGLE_CREDENTIALS_JSON and file: " + credentialsPath + ". Error: " + e.getMessage(), e);
        }
    }

    private RecognitionConfig.AudioEncoding determineAudioEncoding(String contentType, byte[] audioBytes) {
        // First, try to detect format from file content (magic numbers)
        RecognitionConfig.AudioEncoding detectedFromContent = detectFromFileContent(audioBytes);
        if (detectedFromContent != null) {
            System.out.println("Format detected from file content: " + detectedFromContent);
            return detectedFromContent;
        }

        // Fallback to content type detection
        if (contentType == null) {
            return RecognitionConfig.AudioEncoding.LINEAR16; // Default to WAV
        }

        contentType = contentType.toLowerCase();

        if (contentType.contains("wav") || contentType.contains("x-wav")) {
            return RecognitionConfig.AudioEncoding.LINEAR16;
        } else if (contentType.contains("flac")) {
            return RecognitionConfig.AudioEncoding.FLAC;
        } else if (contentType.contains("mp3")) {
            return RecognitionConfig.AudioEncoding.MP3;
        } else if (contentType.contains("webm")) {
            return RecognitionConfig.AudioEncoding.WEBM_OPUS;
        } else if (contentType.contains("ogg") || contentType.contains("opus")) {
            return RecognitionConfig.AudioEncoding.OGG_OPUS;
        } else {
            // Default to LINEAR16 (WAV)
            return RecognitionConfig.AudioEncoding.LINEAR16;
        }
    }

    private RecognitionConfig.AudioEncoding detectFromFileContent(byte[] audioBytes) {
        if (audioBytes == null || audioBytes.length < 12) {
            return null;
        }

        // Check for WEBM signature
        if (audioBytes.length >= 4 &&
            audioBytes[0] == 0x1A && audioBytes[1] == 0x45 &&
            audioBytes[2] == (byte)0xDF && audioBytes[3] == (byte)0xA3) {
            return RecognitionConfig.AudioEncoding.WEBM_OPUS;
        }

        // Check for OGG signature
        if (audioBytes.length >= 4 &&
            audioBytes[0] == 0x4F && audioBytes[1] == 0x67 &&
            audioBytes[2] == 0x67 && audioBytes[3] == 0x53) {
            return RecognitionConfig.AudioEncoding.OGG_OPUS;
        }

        // Check for WAV signature
        if (audioBytes.length >= 12 &&
            audioBytes[0] == 0x52 && audioBytes[1] == 0x49 &&
            audioBytes[2] == 0x46 && audioBytes[3] == 0x46 &&
            audioBytes[8] == 0x57 && audioBytes[9] == 0x41 &&
            audioBytes[10] == 0x56 && audioBytes[11] == 0x45) {
            return RecognitionConfig.AudioEncoding.LINEAR16;
        }

        // Check for FLAC signature
        if (audioBytes.length >= 4 &&
            audioBytes[0] == 0x66 && audioBytes[1] == 0x4C &&
            audioBytes[2] == 0x61 && audioBytes[3] == 0x43) {
            return RecognitionConfig.AudioEncoding.FLAC;
        }

        return null; // Unknown format
    }
}