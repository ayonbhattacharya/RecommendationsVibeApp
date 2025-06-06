import React, { useState, useRef } from 'react';
import './App.css';

function App() {
  const [recording, setRecording] = useState(false);
  const [audioURL, setAudioURL] = useState(null);
  const [recommendations, setRecommendations] = useState(null);
  const [loading, setLoading] = useState(false);
  const [location, setLocation] = useState('New York, NY');
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  const handleStartRecording = () => {
    audioChunksRef.current = [];
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => {
        const mediaRecorder = new MediaRecorder(stream);
        mediaRecorderRef.current = mediaRecorder;
        
        mediaRecorder.ondataavailable = (e) => {
          audioChunksRef.current.push(e.data);
        };
        
        mediaRecorder.onstop = () => {
          const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' });
          const url = URL.createObjectURL(audioBlob);
          setAudioURL(url);
        };
        
        mediaRecorder.start();
        setRecording(true);
      })
      .catch(error => console.error("Error accessing microphone:", error));
  };

  const handleStopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setRecording(false);
    }
  };

  const findRecommendation = async () => {
    // Validation: Check if audio is recorded
    if (!audioURL) {
      alert("Please record first!");
      return;
    }

    setLoading(true);
    setRecommendations(null);

    try {
      // Convert the audio blob to a file
      const response = await fetch(audioURL);
      const audioBlob = await response.blob();

      // Create a File object from the blob
      const audioFile = new File([audioBlob], 'recording.wav', { type: 'audio/wav' });

      // Create FormData for multipart request
      const formData = new FormData();
      formData.append('file', audioFile);
      formData.append('location', location);

      // Send POST request to Spring Boot backend
      const apiUrl = process.env.REACT_APP_API_URL || 'https://recommendationsvibeapp-backend4.onrender.com';
      console.log('Environment variable REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
      console.log('Using API URL:', apiUrl);
      const apiResponse = await fetch(`${apiUrl}/api/menu/speech-to-menu-recommendations`, {
        method: 'POST',
        body: formData,
      });

      if (apiResponse.ok) {
        const contentType = apiResponse.headers.get('content-type');
        console.log('Response content-type:', contentType);

        if (contentType && contentType.includes('application/json')) {
          const result = await apiResponse.json();
          console.log('JSON response:', result);
          setRecommendations(result);
        } else {
          const result = await apiResponse.text();
          console.log('Text response:', result);

          // Try to parse as JSON in case content-type header is wrong
          try {
            const jsonResult = JSON.parse(result);
            console.log('Parsed JSON from text:', jsonResult);
            setRecommendations(jsonResult);
          } catch (parseError) {
            console.log('Not JSON, showing as alert:', result);
            alert(`Recommendation: ${result}`);
          }
        }
      } else {
        const errorText = await apiResponse.text();
        console.error('API Error:', apiResponse.status, errorText);
        alert('Failed to get recommendation. Please try again.');
      }
    } catch (error) {
      console.error('Error sending audio to backend:', error);
      alert('Error connecting to the server. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>What would you like to eat Today?!</h1>

        <div className="main-table">
          <table className="feature-table">
            <thead>
              <tr>
                <th>Action</th>
                <th>Controls</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="step-description">Record Your Voice</td>
                <td className="step-controls">
                  {!recording ? (
                    <button onClick={handleStartRecording} className="record-button">
                      🎤 Start Recording
                    </button>
                  ) : (
                    <button onClick={handleStopRecording} className="stop-button">
                      ⏹️ Stop Recording
                    </button>
                  )}
                </td>
              </tr>
              <tr>
                <td className="step-description">Review Your Recording</td>
                <td className="step-controls">
                  <div className="audio-section">
                    <audio src={audioURL} controls className="audio-player" />
                  </div>
                </td>
              </tr>
              <tr>
                <td className="step-description">Download (Optional)</td>
                <td className="step-controls">
                  <a href={audioURL} download="recording.wav" className="download-button">
                    📥 Download Recording
                  </a>
                </td>
              </tr>
              <tr>
                <td className="step-description">Get Recommendation</td>
                <td className="step-controls">
                  <button onClick={findRecommendation} className="find-button" disabled={loading}>
                    {loading ? '🔄 Searching...' : '🔍 Find out!'}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* Recommendations Section */}
        {recommendations && (
          <div className="recommendations-section">
            <h2 className="recommendations-title">
              🍽️ Perfect Matches for "{recommendations.query}"
            </h2>
            <div className="recommendations-grid">
              {recommendations.recommendations.map((item, index) => (
                <div key={index} className="recommendation-card">
                  <div className="card-header">
                    <h3 className="dish-name">{item.name}</h3>
                    <span className="cuisine-type">{item.cuisine}</span>
                  </div>
                  <p className="description">{item.description}</p>
                  <div className="card-footer">
                    <a href={item.menuLink} target="_blank" rel="noopener noreferrer" className="menu-link">
                      🍽️ View Recipe
                    </a>
                  </div>
                </div>
              ))}
            </div>
            <div className="search-info">
              <p>Found {recommendations.totalFound} recommendations in {recommendations.searchLocation}</p>
            </div>
          </div>
        )}
      </header>
    </div>
  );
}

export default App;