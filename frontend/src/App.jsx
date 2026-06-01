import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';

import Login from './pages/Login';
import Register from './pages/Register';
import CustomerDashboard from './pages/CustomerDashboard';
import RestaurantDetails from './pages/RestaurantDetails';
import Checkout from './pages/Checkout';
import RestaurantDashboard from './pages/RestaurantDashboard';
import AdminDashboard from './pages/AdminDashboard';
import MenuEditor from './pages/MenuEditor'; 
import CustomerReservations from './pages/CustomerReservations';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider> 
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/customer/dashboard" element={<CustomerDashboard />} />
            <Route path="/customer/restaurant/:id" element={<RestaurantDetails />} /> 
            
            <Route path="/restaurant/dashboard" element={<RestaurantDashboard />} /> 
            <Route path="/restaurant/menu" element={<MenuEditor />} /> 
            
            <Route path="/admin/dashboard" element={<AdminDashboard />} /> 
            <Route path="/customer/reservations" element={<CustomerReservations />} />
            <Route path="/customer/checkout" element={<Checkout />} /> {/* Added this one too! */}
            
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </CartProvider> 
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;