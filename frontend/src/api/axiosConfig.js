import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// REQUEST INTERCEPTOR: Attach the token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// RESPONSE INTERCEPTOR: Handle dead tokens globally
api.interceptors.response.use(
  (response) => {
    // If the request succeeds, just return the response normally
    return response;
  },
  (error) => {
    // If the backend says we are unauthorized or forbidden...
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.warn("Session expired or token invalid. Forcing logout.");
      
      // 1. Nuke the dead credentials
      localStorage.clear();
      
      // 2. Kick the user back to the login screen
      window.location.href = '/login'; 
    }
    
    return Promise.reject(error);
  }
);

export default api;