import { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext'; // <-- Import Cart!

export default function RestaurantDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { cart, addToCart, decreaseQuantity, removeFromCart } = useContext(CartContext);

  const [reviews, setReviews] = useState([]);
  const [seatingAreas, setSeatingAreas] = useState([]);
  const [menuItems, setMenuItems] = useState([]); // <-- New Menu State
  const [areaId, setAreaId] = useState('');

  const [date, setDate] = useState('');
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [size, setSize] = useState(2);
  const [note, setNote] = useState('');
  const [bookingMessage, setBookingMessage] = useState('');

  useEffect(() => {
    // Fetch Reviews
    api.get(`/reviews/restaurants/${id}`)
       .then(res => setReviews(res.data))
       .catch(err => console.error("Could not fetch reviews", err));

    // Fetch Seating
    api.get(`/restaurants/${id}/seating`)
       .then(res => {
         setSeatingAreas(res.data);
         if (res.data.length > 0) setAreaId(res.data[0].areaId);
       })
       .catch(err => console.error("Could not fetch seating areas", err));

    // NEW: Fetch Menu Items
    api.get(`/restaurants/${id}`)
       .then(res => {
         if (res.data.menuId) return api.get(`/menus/${res.data.menuId}/items`);
       })
       .then(res => {
         if (res) setMenuItems(res.data);
       })
       .catch(err => console.error("Could not fetch menu items", err));
  }, [id]);

  const handleBooking = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/reservations/customers/${user.profileId}`, {
        areaId, date, start: start + ":00Z", end: end + ":00Z", size: parseInt(size), note
      });
      setBookingMessage("Reservation confirmed!");
    } catch (err) {
      setBookingMessage(err.response?.data?.message || "Failed to book table.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <button onClick={() => navigate(-1)} className="text-blue-600 font-bold">← Back</button>
        <h1 className="text-xl font-bold text-gray-900">Restaurant Hub</h1>
        <div className="w-12"></div>
      </header>

      <main className="p-4 space-y-6 max-w-4xl mx-auto mt-4">
        
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Menu</h2>
            {/* Show checkout button if cart has items */}
            {cart.length > 0 && (
              <button onClick={() => navigate('/customer/checkout')} className="bg-green-500 hover:bg-green-600 text-white px-5 py-2 rounded-xl text-sm font-black shadow-md transition active:scale-95">
                Checkout ({cart.length} items)
              </button>
            )}
          </div>
          
          {menuItems.length === 0 ? (
            <p className="text-gray-500 italic">No menu available.</p>
          ) : (
            <div className="space-y-4">
              {menuItems.map(item => {
                const isItemAvailable = item.available !== undefined ? item.available : item.isAvailable;
                const cartItem = cart.find(i => i.menuItemId === item.menuItemId); // Check if in cart!

                return (
                  <div key={item.menuItemId} className="flex justify-between items-center border-b border-gray-100 pb-4 last:border-0 last:pb-0">
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-gray-900 text-lg">{item.name}</h3>
                        <span className="bg-gray-100 text-gray-600 px-2 py-0.5 rounded text-xs font-bold uppercase">{item.category}</span>
                      </div>
                      <p className="text-sm text-gray-600">{item.description}</p>
                      <div className="text-green-600 font-bold mt-1">${item.price.toFixed(2)}</div>
                    </div>
                    
                    {/* NEW: Dynamic Cart UI */}
                    {cartItem ? (
                      <div className="flex items-center gap-3 bg-blue-50 px-2 py-1 rounded-xl border border-blue-100 shadow-sm">
                        {cartItem.quantity === 1 ? (
                          <button onClick={() => removeFromCart(item.menuItemId)} className="text-red-500 font-bold px-3 py-1 hover:bg-red-100 rounded-lg transition">🗑️</button>
                        ) : (
                          <button onClick={() => decreaseQuantity(item.menuItemId)} className="text-blue-600 font-bold px-3 py-1 hover:bg-blue-100 rounded-lg transition">-</button>
                        )}
                        <span className="font-black text-blue-900 w-4 text-center">{cartItem.quantity}</span>
                        <button onClick={() => addToCart(item, id)} className="text-blue-600 font-bold px-3 py-1 hover:bg-blue-100 rounded-lg transition">+</button>
                      </div>
                    ) : (
                      <button 
                        disabled={!isItemAvailable}
                        onClick={() => addToCart(item, id)}
                        className={`px-5 py-2 rounded-xl font-black text-sm transition active:scale-95 ${isItemAvailable ? 'bg-blue-100 text-blue-700 hover:bg-blue-200 shadow-sm' : 'bg-gray-100 text-gray-400 cursor-not-allowed'}`}
                      >
                        {isItemAvailable ? 'Add to Cart' : 'Out of Stock'}
                      </button>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {/* Existing Booking Form */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Make a Reservation</h2>
          {bookingMessage && (
            <div className={`p-3 rounded-lg mb-4 text-center font-bold ${bookingMessage.includes('confirmed') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
              {bookingMessage}
            </div>
          )}
          <form onSubmit={handleBooking} className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Seating Area</label>
              <select required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-white" value={areaId} onChange={e => setAreaId(e.target.value)}>
                {seatingAreas.map(area => (
                  <option key={area.areaId} value={area.areaId}>{area.areaName} (Capacity: {area.capacity})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Date</label>
              <input type="date" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500" value={date} onChange={e => setDate(e.target.value)} />
            </div>
            <div className="flex gap-4">
              <div className="w-1/2">
                <label className="block text-sm font-semibold text-gray-700 mb-1">Start</label>
                <input type="time" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none" value={start} onChange={e => setStart(e.target.value)} />
              </div>
              <div className="w-1/2">
                <label className="block text-sm font-semibold text-gray-700 mb-1">End</label>
                <input type="time" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none" value={end} onChange={e => setEnd(e.target.value)} />
              </div>
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Party Size</label>
              <input type="number" min="1" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none" value={size} onChange={e => setSize(e.target.value)} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Special Requests</label>
              <textarea placeholder="Allergies, anniversaries, etc." rows="2" className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500" value={note} onChange={e => setNote(e.target.value)}></textarea>
            </div>
            <button type="submit" disabled={seatingAreas.length === 0} className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 rounded-xl transition mt-2">Confirm Reservation</button>
          </form>
        </div>

      </main>
    </div>
  );
}