# 🍽️ RecommendationsVibeApp

A full-stack application that converts speech to personalized meal recommendations using AI.

## 🎯 Features

- **🎤 Speech-to-Text**: Record voice requests using Google Cloud Speech API
- **🤖 AI Recommendations**: Get personalized meal suggestions via Perplexity AI
- **🍕 Menu Focus**: Specific dish recommendations with cooking instructions
- **📱 React Frontend**: Modern, responsive user interface
- **☁️ Cloud Ready**: Deployed on Render.com

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐
│   React Frontend │────│ Spring Boot API  │
│   (Port 3000)    │    │   (Port 8080)    │
└─────────────────┘    └──────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
            ┌───────▼────────┐ ┌────────▼────────┐
            │ Google Speech  │ │ Perplexity AI   │
            │      API       │ │      API        │
            └────────────────┘ └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.6+
- Google Cloud Speech API credentials
- Perplexity AI API key

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/RecommendationsVibeApp.git
   cd RecommendationsVibeApp
   ```

2. **Start Backend (Spring Boot)**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   Backend runs on: http://localhost:8080

3. **Start Frontend (React)**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Frontend runs on: http://localhost:3000

## 📡 API Endpoints

- `GET /api/menu/hello` - Health check
- `POST /api/menu/speech-to-menu-recommendations` - Speech to meal recommendations
- `POST /api/menu/text-to-menu-recommendations` - Text to meal recommendations
- `POST /api/menu/speech-to-text` - Speech transcription only

## 🔧 Configuration

### Environment Variables
- `PERPLEXITY_API_KEY` - Your Perplexity AI API key
- `GOOGLE_APPLICATION_CREDENTIALS` - Path to Google Cloud credentials

### Application Properties
See `backend/src/main/resources/application.properties` for configuration options.

## 🌐 Deployment

This app is configured for easy deployment on Render.com:

1. Push code to GitHub
2. Connect Render to your repository
3. Deploy backend as Web Service
4. Deploy frontend as Static Site

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.5.0** - Java framework
- **Google Cloud Speech API** - Speech-to-text conversion
- **Perplexity AI API** - Meal recommendations
- **Maven** - Dependency management

### Frontend
- **React 19** - UI framework
- **MediaRecorder API** - Audio recording
- **Fetch API** - HTTP requests

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📞 Support

For questions or issues, please open a GitHub issue.
