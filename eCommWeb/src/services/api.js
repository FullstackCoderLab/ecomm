import axios from 'axios';

// API Gateway base URL (Eureka Registry + Load Balancer on port 8761)
export const GATEWAY_BASE_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8761';

const apiClient = axios.create({
  baseURL: GATEWAY_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request Interceptor: Attach JWT Bearer Token if available
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('ecomm_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Capture Gateway Tracking Headers
apiClient.interceptors.response.use(
  (response) => {
    // Capture tracking headers added by eCommAPIGateway
    const metadata = {
      loadBalancedInstance: response.headers['x-loadbalanced-instance'] || 'N/A',
      correlationId: response.headers['x-correlation-id'] || 'N/A',
      gatewayDurationMs: response.headers['x-gateway-duration-ms'] || 'N/A',
    };
    response.gatewayMetadata = metadata;
    return response;
  },
  (error) => {
    // Standardize error message from Gateway or Backend
    const backendMessage =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'Network error: Please ensure eCommAPIGateway is running on port 8761.';
    
    error.friendlyMessage = backendMessage;
    return Promise.reject(error);
  }
);

/**
 * 1. User Registration Service
 * Endpoint: POST http://localhost:8761/api/v1/auth/register
 */
export const registerUser = async ({ name, email, password, role = 'ROLE_USER' }) => {
  const payload = { name, email, password, role };
  const response = await apiClient.post('/api/v1/auth/register', payload);
  return {
    data: response.data,
    metadata: response.gatewayMetadata,
  };
};

/**
 * 2. User Login Service
 * Endpoint: POST http://localhost:8761/api/v1/auth/login
 */
export const loginUser = async ({ email, password }) => {
  const payload = { email, password };
  const response = await apiClient.post('/api/v1/auth/login', payload);
  return {
    data: response.data,
    metadata: response.gatewayMetadata,
  };
};

/**
 * 3. Get Current User Profile (Protected JWT Endpoint)
 * Endpoint: GET http://localhost:8761/api/v1/users/me
 */
export const fetchCurrentUser = async () => {
  const response = await apiClient.get('/api/v1/users/me');
  return {
    data: response.data,
    metadata: response.gatewayMetadata,
  };
};

/**
 * 4. Gateway Health & Discovery Endpoints
 */
export const checkGatewayHealth = async () => {
  try {
    const response = await apiClient.get('/actuator/health');
    return { status: response.data.status || 'UP', ok: true };
  } catch (err) {
    return { status: 'DOWN', ok: false };
  }
};

export const fetchGatewayInstances = async () => {
  try {
    const response = await apiClient.get('/gateway/instances');
    return response.data;
  } catch (err) {
    return null;
  }
};

export default apiClient;
