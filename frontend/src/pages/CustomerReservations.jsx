import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function CustomerReservations() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [orders, setOrders] = useState([]); 
  
  const [expandedResId, setExpandedResId] = useState(null);
  const [orderItemsCache, setOrderItemsCache] = useState({});
  const [menuCache, setMenuCache] = useState({});

  // Edit State
  const [editingResId, setEditingResId] = useState(null);
  const [editPartySize, setEditPartySize] = useState(1);
  
  const [editCcName, setEditCcName] = useState('');
  const [editCcNum, setEditCcNum] = useState('');
  const [editCcExpiry, setEditCcExpiry] = useState('');
  const [editCcCvv, setEditCcCvv] = useState('');

  // Rating State
  const [customerReviews, setCustomerReviews] = useState({});
  const [ratingResId, setRatingResId] = useState(null);
  const [ratingStars, setRatingStars] = useState(5);
  const [ratingComment, setRatingComment] = useState('');

  const fetchReservationsData = () => {
    // 1. Fetch Reservations
    api.get(`/reservations/customers/${user.profileId}`)
       .then(res => setReservations(res.data))
       .catch(err => console.error("Failed to fetch reservations", err));
       
    // 2. Fetch Orders
    api.get(`/customers/${user.profileId}/orders`)
       .then(res => setOrders(res.data))
       .catch(err => console.error("Failed to fetch orders", err));

    // 3. Fetch Existing Reviews
    api.get(`/reviews/customers/${user.profileId}`)
       .then(res => {
         if (Array.isArray(res.data)) {
           const reviewMap = {};
           res.data.forEach(rev => {
             reviewMap[rev.restaurantId] = rev;
           });
           setCustomerReviews(reviewMap);
         }
       })
       .catch(err => console.error("Failed to fetch reviews", err));
  };

  useEffect(() => {
    fetchReservationsData();
  }, [user.profileId]);

  const handleCancel = async (resId) => {
    if (!window.confirm("Are you sure you want to cancel this reservation?")) return;
    try {
      await api.delete(`/reservations/customers/${user.profileId}/${resId}`);
      setReservations(prev => prev.filter(r => r.resId !== resId));
    } catch (err) { alert("Failed to cancel reservation."); }
  };

  const toggleDetails = async (res, order) => {
    if (expandedResId === res.resId) {
      setExpandedResId(null);
      return;
    }
    setExpandedResId(res.resId);

    if (!order) return;

    if (!menuCache[res.restaurantId]) {
      try {
        const restRes = await api.get(`/restaurants/${res.restaurantId}`);
        if (restRes.data.menuId) {
          const menuRes = await api.get(`/menus/${restRes.data.menuId}/items`);
          setMenuCache(prev => ({ ...prev, [res.restaurantId]: menuRes.data }));
        }
      } catch (err) { console.error("Menu fetch failed", err); }
    }

    if (!orderItemsCache[order.orderId]) {
      try {
        const itemsRes = await api.get(`/customers/${user.profileId}/orders/${order.orderId}/items`);
        setOrderItemsCache(prev => ({ ...prev, [order.orderId]: itemsRes.data }));
      } catch (err) { console.error("Order items fetch failed", err); }
    }
  };

  const getMenuItem = (restaurantId, menuItemId) => {
    return menuCache[restaurantId]?.find(m => m.menuItemId === menuItemId);
  };

  const handleEditClick = (res) => {
    setEditingResId(res.resId);
    setEditPartySize(res.numPeople);
    
    // Reset CC fields
    setEditCcName(''); setEditCcNum(''); setEditCcExpiry(''); setEditCcCvv('');
  };

  const handleSaveModifications = async (resId) => {
    try {
      const booking = reservations.find(r => r.resId === resId);
      
      const reservationPayload = {
        areaId: booking.areaId,
        date: booking.resDate,        
        start: booking.startTime,     
        end: booking.endTime,         
        size: editPartySize,          
        note: booking.note || ""
      };

      await api.put(`/reservations/customers/${user.profileId}/${resId}`, reservationPayload);
      
      setEditingResId(null);
      fetchReservationsData();
    } catch (err) {
      alert("Failed to modify reservation.");
    }
  };

  const handleSubmitReview = async (res) => {
    try {
      const payload = {
        restaurantId: res.restaurantId,
        customerId: user.profileId,
        stars: ratingStars,
        comment: ratingComment
      };
      
      await api.post(`/reviews/${user.profileId}`, payload);
      
      // Immediately refetch the reviews so we get the database-generated reviewId
      // This ensures the user can delete it instantly without having to refresh the page
      const revsRes = await api.get(`/reviews/customers/${user.profileId}`);
      if (Array.isArray(revsRes.data)) {
         const reviewMap = {};
         revsRes.data.forEach(rev => reviewMap[rev.restaurantId] = rev);
         setCustomerReviews(reviewMap);
      }
      
      alert("Thank you for your review!");
      setRatingResId(null);
    } catch (err) {
      alert("Failed to submit review. Ensure your backend accepts this payload.");
    }
  };

  // NEW: Delete Review Handler
  const handleDeleteReview = async (reviewId, restaurantId) => {
    if (!window.confirm("Are you sure you want to delete this review?")) return;
    try {
      await api.delete(`/reviews/${reviewId}`);
      
      // Remove it from the local state so the UI instantly updates
      setCustomerReviews(prev => {
        const updated = { ...prev };
        delete updated[restaurantId];
        return updated;
      });
      
    } catch (err) {
      alert("Failed to delete review.");
    }
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
              const existingReview = customerReviews[res.restaurantId];

              // EDIT MODE
              if (editingResId === res.resId) {
                return (
                  <div key={res.resId} className="bg-white p-5 rounded-2xl shadow-sm border border-blue-200 transition-all">
                    <h3 className="font-extrabold text-lg text-gray-900 mb-4">Modify Reservation</h3>
                    <div className="space-y-4">
                      
                      <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Party Size</label>
                        <input type="number" min="1" value={editPartySize} onChange={e => setEditPartySize(parseInt(e.target.value))} className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" />
                      </div>

                      <div className="border-t border-gray-100 pt-4 mt-2">
                         <h4 className="font-bold text-gray-900 mb-3">Confirm Payment Details</h4>
                         <div className="space-y-3">
                           <div>
                             <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Name on Card</label>
                             <input type="text" required placeholder="John Doe" className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editCcName} onChange={e => setEditCcName(e.target.value)} />
                           </div>
                           <div>
                             <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Card Number</label>
                             <input type="text" required placeholder="1234 5678 9101 1121" className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editCcNum} onChange={e => setEditCcNum(e.target.value)} />
                           </div>
                           <div className="grid grid-cols-2 gap-4">
                             <div>
                               <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Expiration</label>
                               <input type="text" required placeholder="MM/YY" className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editCcExpiry} onChange={e => setEditCcExpiry(e.target.value)} />
                             </div>
                             <div>
                               <label className="block text-xs font-bold text-gray-500 uppercase mb-1">CVV</label>
                               <input type="text" required placeholder="123" className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editCcCvv} onChange={e => setEditCcCvv(e.target.value)} />
                             </div>
                           </div>
                         </div>
                         <p className="text-xs text-gray-500 font-bold text-center mt-3">Note: We will only charge it once when the order is confirmed.</p>
                      </div>

                      <div className="flex gap-2 pt-2">
                         <button onClick={() => handleSaveModifications(res.resId)} className="bg-blue-600 hover:bg-blue-700 text-white flex-1 py-3 rounded-xl font-bold transition">Save Changes</button>
                         <button onClick={() => setEditingResId(null)} className="bg-gray-200 hover:bg-gray-300 text-gray-700 px-6 py-3 rounded-xl font-bold transition">Cancel</button>
                      </div>
                    </div>
                  </div>
                );
              }

              // STANDARD VIEW
              return (
                <div key={res.resId} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-200 transition-all">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-extrabold text-lg text-gray-900">Party of {res.numPeople}</h3>
                    <div className="flex flex-col items-end gap-2">
                      <span className={`px-3 py-1 text-xs font-black rounded-md ${
                          res.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' : 
                          res.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                      }`}>{res.status}</span>
                      
                      {res.status === 'PENDING' && (
                        <button onClick={() => handleEditClick(res)} className="text-xs font-bold text-blue-600 hover:underline">
                          Modify Booking
                        </button>
                      )}
                    </div>
                  </div>
                  
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>📅 {new Date(res.resDate).toLocaleDateString()} | ⏰ {res.startTime} to {res.endTime}</p>
                    {res.note && <p className="italic text-gray-500 mt-2">" {res.note} "</p>}
                  </div>

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

                  {/* DISPLAY EXISTING REVIEW WITH TRASH ICON */}
                  {res.status === 'COMPLETED' && existingReview && (
                    <div className="mt-4 bg-gray-50 p-4 rounded-xl border border-gray-200 shadow-sm relative">
                      <div className="flex justify-between items-center mb-1">
                        <h4 className="font-bold text-gray-900">Your Review</h4>
                        <div className="flex items-center gap-2">
                          <span className="bg-green-100 text-green-800 text-xs font-bold px-2 py-1 rounded">Posted</span>
                          <button 
                            onClick={() => handleDeleteReview(existingReview.reviewId, res.restaurantId)} 
                            className="text-red-400 hover:bg-red-100 hover:text-red-600 p-1.5 rounded-lg transition-colors"
                            title="Delete Review"
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-4 h-4">
                              <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                            </svg>
                          </button>
                        </div>
                      </div>
                      <div className="text-yellow-500 text-lg tracking-widest mb-1">
                        {'★'.repeat(existingReview.stars)}{'☆'.repeat(5 - existingReview.stars)}
                      </div>
                      {existingReview.comment && (
                        <p className="text-sm text-gray-600 italic">"{existingReview.comment}"</p>
                      )}
                    </div>
                  )}

                  {/* SHOW RATING BUTTON IF NOT YET REVIEWED */}
                  {res.status === 'COMPLETED' && !existingReview && ratingResId !== res.resId && (
                    <button onClick={() => { setRatingResId(res.resId); setRatingStars(5); setRatingComment(''); }} className="mt-4 w-full bg-yellow-50 text-yellow-700 hover:bg-yellow-100 font-bold py-3 rounded-xl transition">
                      Leave a Review
                    </button>
                  )}

                  {/* RATING FORM */}
                  {ratingResId === res.resId && (
                    <div className="mt-4 bg-gray-50 p-4 rounded-xl border border-gray-200">
                      <h4 className="font-bold text-gray-900 mb-2">Rate your experience</h4>
                      <div className="flex gap-2 mb-3">
                        {[1, 2, 3, 4, 5].map(star => (
                          <button key={star} onClick={() => setRatingStars(star)} className={`text-3xl transition-colors ${ratingStars >= star ? 'text-yellow-400' : 'text-gray-300'}`}>
                            ★
                          </button>
                        ))}
                      </div>
                      <textarea
                        placeholder="Leave an optional comment..."
                        value={ratingComment}
                        onChange={e => setRatingComment(e.target.value)}
                        className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-white outline-none focus:ring-2 focus:ring-blue-500 mb-3"
                        rows="2"
                      />
                      <div className="flex gap-2">
                        <button onClick={() => handleSubmitReview(res)} className="bg-blue-600 hover:bg-blue-700 text-white flex-1 py-2 rounded-xl font-bold transition">Submit Review</button>
                        <button onClick={() => setRatingResId(null)} className="bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-xl font-bold transition">Cancel</button>
                      </div>
                    </div>
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