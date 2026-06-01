import { useContext, useState, useEffect } from 'react';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { useNavigate } from 'react-router-dom';

export default function Checkout() {
  const { cart, restaurantId, getCartTotal, clearCart, removeFromCart } = useContext(CartContext);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [ccNum, setCcNum] = useState('');
  const [message, setMessage] = useState('');
  
  // NEW: Reservation Selection State
  const [reservations, setReservations] = useState([]);
  const [selectedResId, setSelectedResId] = useState('');

  useEffect(() => {
    if (!restaurantId || !user) return;
    // Fetch user's reservations to link the order
    api.get(`/reservations/customers/${user.profileId}`)
       .then(res => {
         const active = res.data.filter(r => r.restaurantId === restaurantId && (r.status === 'PENDING' || r.status === 'CONFIRMED'));
         setReservations(active);
         if (active.length > 0) setSelectedResId(active[0].resId);
       });
  }, [restaurantId, user]);

  const handleCheckout = async (e) => {
    e.preventDefault();
    try {
      // FIXED: Now passing the specific Reservation ID to the backend!
      const payload = {
        resId: selectedResId, 
        ccNum: ccNum,
        items: cart.map(item => ({ menuItemId: item.menuItemId, quantity: item.quantity }))
      };
      
      await api.post(`/customers/${user.profileId}/orders`, payload);
      setMessage("Order placed successfully!");
      clearCart();
      setTimeout(() => navigate('/customer/dashboard'), 2000);
    } catch (err) {
      setMessage(err.response?.data?.message || "Failed to place order.");
    }
  };

  if (cart.length === 0 && !message) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
        <button onClick={() => navigate(-1)} className="text-blue-600 font-bold hover:underline">Go Back</button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center">
        <button onClick={() => navigate(-1)} className="text-blue-600 font-bold">← Back</button>
        <h1 className="text-xl font-bold">Checkout</h1>
        <div className="w-12"></div>
      </header>

      <main className="p-4 max-w-lg mx-auto space-y-6 mt-4">
        {message && <div className="bg-green-100 text-green-700 p-4 rounded-xl font-bold text-center">{message}</div>}
        
        {/* Guard: Force them to book a table first! */}
        {reservations.length === 0 ? (
          <div className="bg-red-50 p-6 rounded-2xl text-center border border-red-100">
             <h2 className="text-lg font-bold text-red-700 mb-2">Wait a second!</h2>
             <p className="text-red-600 mb-4">You must book a table at this restaurant before you can pre-order food.</p>
             <button onClick={() => navigate(-1)} className="bg-red-600 text-white font-bold px-6 py-2 rounded-xl">Go back to Menu</button>
          </div>
        ) : (
          <>
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
               <h2 className="text-lg font-bold mb-4">Order Summary</h2>
               {cart.map(item => (
                  <div key={item.menuItemId} className="flex justify-between items-center mb-3">
                     <div><span className="font-bold text-blue-600">{item.quantity}x</span> <span className="font-bold text-gray-900">{item.name}</span></div>
                     <div className="flex items-center gap-4">
                       <span className="font-bold text-green-600">${(item.price * item.quantity).toFixed(2)}</span>
                       <button onClick={() => removeFromCart(item.menuItemId)} className="text-red-500 text-xs font-bold bg-red-50 px-2 py-1 rounded">Remove</button>
                     </div>
                  </div>
               ))}
               <div className="border-t border-gray-100 pt-4 mt-4 flex justify-between items-center font-black text-xl">
                 <span>Total:</span><span className="text-green-600">${getCartTotal().toFixed(2)}</span>
               </div>
            </div>

            {!message && (
            <form onSubmit={handleCheckout} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200 space-y-4">
               <div>
                 <label className="block text-sm font-bold text-gray-700 mb-1">Attach to Reservation</label>
                 <select className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white" value={selectedResId} onChange={e => setSelectedResId(e.target.value)}>
                   {reservations.map(r => (
                     <option key={r.resId} value={r.resId}>Table for {r.numPeople} on {new Date(r.resDate).toLocaleDateString()} at {r.startTime}</option>
                   ))}
                 </select>
               </div>
               <div>
                 <label className="block text-sm font-bold text-gray-700 mb-1">Credit Card Number</label>
                 <input type="text" required placeholder="1234 5678 9101 1121" className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none bg-gray-50" value={ccNum} onChange={e => setCcNum(e.target.value)} />
               </div>
               <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-xl shadow-md transition active:scale-[0.98]">Submit Payment</button>
            </form>
            )}
          </>
        )}
      </main>
    </div>
  );
}