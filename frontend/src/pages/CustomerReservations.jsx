import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function CustomerReservations() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [orders, setOrders] = useState([]); 
  
  // NEW: Accordion & Caching States
  const [expandedResId, setExpandedResId] = useState(null);
  const [orderItemsCache, setOrderItemsCache] = useState({}); // orderId -> items
  const [menuCache, setMenuCache] = useState({}); // restaurantId -> menu items

  useEffect(() => {
    api.get(`/reservations/customers/${user.profileId}`)
       .then(res => setReservations(res.data))
       .catch(err => console.error("Failed to fetch reservations", err));
       
    api.get(`/customers/${user.profileId}/orders`)
       .then(res => setOrders(res.data))
       .catch(err => console.error("Failed to fetch orders", err));
  }, [user.profileId]);

  const handleCancel = async (resId) => {
    if (!window.confirm("Are you sure you want to cancel this reservation?")) return;
    try {
      await api.delete(`/reservations/customers/${user.profileId}/${resId}`);
      setReservations(prev => prev.filter(r => r.resId !== resId));
    } catch (err) { alert("Failed to cancel reservation."); }
  };

  // NEW: The Smart Dropdown Toggle
  const toggleDetails = async (res, order) => {
    if (expandedResId === res.resId) {
      setExpandedResId(null); // Close it if it's already open
      return;
    }
    setExpandedResId(res.resId);

    if (!order) return;

    // 1. Fetch the menu if we haven't seen this restaurant before
    if (!menuCache[res.restaurantId]) {
      try {
        const restRes = await api.get(`/restaurants/${res.restaurantId}`);
        if (restRes.data.menuId) {
          const menuRes = await api.get(`/menus/${restRes.data.menuId}/items`);
          setMenuCache(prev => ({ ...prev, [res.restaurantId]: menuRes.data }));
        }
      } catch (err) { console.error("Menu fetch failed", err); }
    }

    // 2. Fetch the specific items inside this order
    if (!orderItemsCache[order.orderId]) {
      try {
        const itemsRes = await api.get(`/customers/${user.profileId}/orders/${order.orderId}/items`);
        setOrderItemsCache(prev => ({ ...prev, [order.orderId]: itemsRes.data }));
      } catch (err) { console.error("Order items fetch failed", err); }
    }
  };

  // Helper to translate an ID to a human-readable menu item
  const getMenuItem = (restaurantId, menuItemId) => {
    return menuCache[restaurantId]?.find(m => m.menuItemId === menuItemId);
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <button onClick={() => navigate('/customer/dashboard')} className="text-blue-600 font-bold">← Discover</button>
        <h1 className="text-xl font-bold text-gray-900">My Bookings</h1>
        <div className="w-12"></div>
      </header>

      <main className="p-4 max-w-2xl mx-auto mt-4">
        {reservations.length === 0 ? (
          <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-center">
            <p className="text-gray-500 font-medium">You have no upcoming reservations.</p>
            <button onClick={() => navigate('/customer/dashboard')} className="mt-4 bg-blue-100 text-blue-700 px-4 py-2 rounded-lg font-bold">Find a Table</button>
          </div>
        ) : (
          <div className="space-y-4">
            {reservations.map(res => {
              const attachedOrder = orders.find(o => o.resId === res.resId);
              const isExpanded = expandedResId === res.resId;

              return (
                <div key={res.resId} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-200 transition-all">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-extrabold text-lg text-gray-900">Party of {res.numPeople}</h3>
                    <span className={`px-3 py-1 text-xs font-black rounded-md ${
                        res.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' : 
                        res.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                    }`}>{res.status}</span>
                  </div>
                  
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>📅 {new Date(res.resDate).toLocaleDateString()} | ⏰ {res.startTime} to {res.endTime}</p>
                    {res.note && <p className="italic text-gray-500 mt-2">" {res.note} "</p>}
                  </div>

                  {/* NEW: Interactive Dropdown Section */}
                  {attachedOrder && (
                    <div className="mt-4 bg-gray-50 rounded-xl border border-gray-200 overflow-hidden">
                      <div 
                        onClick={() => toggleDetails(res, attachedOrder)}
                        className="p-4 flex justify-between items-center cursor-pointer hover:bg-gray-100 transition-colors"
                      >
                        <div>
                          <span className="block text-xs font-bold text-gray-500 uppercase tracking-wider">Food Pre-Ordered</span>
                          <span className="font-black text-gray-900">Total: <span className="text-green-600">${attachedOrder.totalPrice.toFixed(2)}</span></span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="bg-green-100 text-green-700 px-2 py-1 text-xs font-black uppercase rounded shadow-sm">Paid</span>
                          <span className="text-gray-400 font-bold ml-1">{isExpanded ? '▲' : '▼'}</span>
                        </div>
                      </div>

                      {/* Dropdown Body */}
                      {isExpanded && (
                        <div className="p-4 bg-white border-t border-gray-200 space-y-3">
                          {!orderItemsCache[attachedOrder.orderId] ? (
                            <p className="text-sm text-gray-500 text-center animate-pulse">Loading receipt...</p>
                          ) : orderItemsCache[attachedOrder.orderId].length === 0 ? (
                            <p className="text-sm text-gray-500 text-center">No items found.</p>
                          ) : (
                            orderItemsCache[attachedOrder.orderId].map(item => {
                              const menuData = getMenuItem(res.restaurantId, item.menuItemId);
                              return (
                                <div key={item.menuItemId} className="flex justify-between items-center text-sm border-b border-gray-50 pb-2 last:border-0 last:pb-0">
                                  <div>
                                    <span className="font-bold text-blue-600 mr-2">{item.quantity}x</span>
                                    <span className="font-bold text-gray-800">{menuData ? menuData.name : 'Unknown Item'}</span>
                                  </div>
                                  <span className="text-gray-600 font-mono font-medium">
                                    ${menuData ? (menuData.price * item.quantity).toFixed(2) : '0.00'}
                                  </span>
                                </div>
                              );
                            })
                          )}
                        </div>
                      )}
                    </div>
                  )}

                  {res.status !== 'CANCELLED' && res.status !== 'COMPLETED' && (
                    <button onClick={() => handleCancel(res.resId)} className="mt-4 w-full bg-red-50 text-red-600 hover:bg-red-100 font-bold py-3 rounded-xl transition">
                      Cancel Reservation
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}