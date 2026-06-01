import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function RestaurantDashboard() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [activeTab, setActiveTab] = useState('RESERVATIONS');

  const [reservations, setReservations] = useState([]);
  const [orders, setOrders] = useState({}); 
  const [menuItems, setMenuItems] = useState([]);
  const [expandedResId, setExpandedResId] = useState(null);
  const [orderItemsCache, setOrderItemsCache] = useState({});

  const [seatingAreas, setSeatingAreas] = useState([]);
  const [newSeatingName, setNewSeatingName] = useState('INDOOR');
  const [newSeatingCapacity, setNewSeatingCapacity] = useState('');
  
  const [editingAreaId, setEditingAreaId] = useState(null);
  const [editCapacity, setEditCapacity] = useState('');

  const fetchSeatingAreas = () => {
    api.get(`/restaurants/${user.profileId}/seating`)
       .then(res => {
         if (Array.isArray(res.data)) setSeatingAreas(res.data);
       }).catch(() => console.log("No seating areas found."));
  };

  useEffect(() => {
    api.get(`/restaurants/${user.profileId}`)
       .then(rRes => {
         if (rRes.data.menuId) {
           api.get(`/menus/${rRes.data.menuId}/items`)
              .then(mRes => setMenuItems(mRes.data))
              .catch(e => console.log(e));
         }
       });

    api.get(`/reservations/restaurants/${user.profileId}`)
       .then(res => {
         setReservations(res.data);
         res.data.forEach(reservation => {
           api.get(`/customers/${reservation.customerId}/orders/reservations/${reservation.resId}`)
              .then(orderRes => {
                if (orderRes.data && typeof orderRes.data === 'object' && orderRes.data.orderId) {
                  setOrders(prev => ({ ...prev, [reservation.resId]: orderRes.data }));
                }
              }).catch(() => {});
         });
       });

    fetchSeatingAreas();

  }, [user.profileId]);

  const updateStatus = async (resId, newStatus) => {
    try {
      await api.patch(`/reservations/restaurants/${user.profileId}/${resId}/status?status=${newStatus}`);
      setReservations(prev => prev.map(r => r.resId === resId ? { ...r, status: newStatus } : r));
    } catch (err) { alert("Failed to update status."); }
  };

  const toggleOrderDetails = async (res, order) => {
    if (expandedResId === res.resId) {
      setExpandedResId(null);
      return;
    }
    setExpandedResId(res.resId);

    if (order && !orderItemsCache[order.orderId]) {
      try {
        const itemsRes = await api.get(`/customers/${res.customerId}/orders/${order.orderId}/items`);
        setOrderItemsCache(prev => ({ ...prev, [order.orderId]: itemsRes.data }));
      } catch (err) { console.error("Failed to fetch order items", err); }
    }
  };

  const getMenuItem = (menuItemId) => menuItems.find(m => m.menuItemId === menuItemId);

  const handleAddSeating = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/restaurants/${user.profileId}/seating`, {
        areaName: newSeatingName,
        capacity: parseInt(newSeatingCapacity)
      });
      fetchSeatingAreas();
      setNewSeatingCapacity('');
    } catch (err) {
      alert("Failed to add seating area.");
    }
  };

  const handleDeleteSeating = async (areaId) => {
    if (!window.confirm("Delete this seating area?")) return;
    try {
      await api.delete(`/restaurants/${user.profileId}/seating/${areaId}`);
      fetchSeatingAreas();
    } catch (err) {
      alert("Failed to delete seating area.");
    }
  };

  const handleUpdateSeating = async (areaId, areaName) => {
    try {
      await api.put(`/restaurants/${user.profileId}/seating/${areaId}`, {
        areaName: areaName,
        capacity: parseInt(editCapacity)
      });
      setEditingAreaId(null);
      fetchSeatingAreas();
    } catch (err) {
      alert("Failed to update seating capacity. Verify your backend has a PUT endpoint configured.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <h1 className="text-xl font-bold text-gray-900">Restaurant Terminal</h1>
        <button onClick={logout} className="text-sm font-semibold text-gray-500 hover:text-red-500 transition">Log Out</button>
      </header>

      <main className="p-4 max-w-4xl mx-auto space-y-6 mt-4">
        
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-gray-200 pb-4">
            <div className="flex bg-gray-200 p-1 rounded-xl">
              <button 
                onClick={() => setActiveTab('RESERVATIONS')} 
                className={`px-4 py-2 text-sm font-bold rounded-lg transition-all ${activeTab === 'RESERVATIONS' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              >
                Live Reservations
              </button>
              <button 
                onClick={() => setActiveTab('SEATING')} 
                className={`px-4 py-2 text-sm font-bold rounded-lg transition-all ${activeTab === 'SEATING' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
              >
                Seating Manager
              </button>
            </div>
            
            <button onClick={() => navigate('/restaurant/menu')} className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-4 py-2 rounded-lg font-bold text-sm transition">
              Open Menu Editor
            </button>
        </div>
        
        {activeTab === 'RESERVATIONS' && (
          <div>
            {reservations.length === 0 ? (
              <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-center">
                <p className="text-gray-500 font-medium">No reservations booked yet.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {reservations.map(res => {
                  const attachedOrder = orders[res.resId]; 
                  const isExpanded = expandedResId === res.resId;
                  return (
                    <div key={res.resId} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 flex flex-col gap-3">
                      <div className="flex justify-between items-center">
                        <span className="font-extrabold text-lg text-gray-900">Party of {res.numPeople}</span>
                        <span className={`px-3 py-1 text-xs font-black rounded-md ${res.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' : res.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                          {res.status}
                        </span>
                      </div>
                      <div className="text-sm font-medium text-gray-600">
                          <p>📅 {new Date(res.resDate).toLocaleDateString()} | ⏰ {res.startTime}</p>
                          {res.note && <p className="mt-2 text-gray-500 italic">" {res.note} "</p>}
                      </div>
                      {attachedOrder && (
                        <div className="bg-yellow-50 rounded-xl border border-yellow-200 overflow-hidden mt-2">
                          <div onClick={() => toggleOrderDetails(res, attachedOrder)} className="p-3 flex justify-between items-center cursor-pointer hover:bg-yellow-100/50 transition-colors">
                            <div>
                               <span className="block text-xs font-bold text-yellow-700 uppercase tracking-wider">Food Pre-Ordered</span>
                               <span className="font-black text-gray-900">Revenue: <span className="text-green-600">${attachedOrder.totalPrice.toFixed(2)}</span></span>
                            </div>
                            <div className="flex items-center gap-3">
                              <span className="bg-white text-gray-600 font-mono text-xs px-2 py-1 rounded shadow-sm border border-yellow-200">ID: {attachedOrder.orderId.substring(0,6)}</span>
                              <span className="text-yellow-600 font-bold ml-1">{isExpanded ? '▲' : '▼'}</span>
                            </div>
                          </div>
                          {isExpanded && (
                            <div className="p-4 bg-white border-t border-yellow-200 space-y-3">
                              {!orderItemsCache[attachedOrder.orderId] ? (
                                <p className="text-sm text-gray-500 text-center animate-pulse">Loading ticket...</p>
                              ) : (
                                orderItemsCache[attachedOrder.orderId].map(item => {
                                  const menuData = getMenuItem(item.menuItemId);
                                  return (
                                    <div key={item.menuItemId} className="flex justify-between items-center text-sm border-b border-gray-50 pb-2 last:border-0 last:pb-0">
                                      <div><span className="font-bold text-blue-600 mr-2">{item.quantity}x</span><span className="font-bold text-gray-800">{menuData ? menuData.name : 'Unknown Item'}</span></div>
                                    </div>
                                  );
                                })
                              )}
                            </div>
                          )}
                        </div>
                      )}
                      {(res.status === 'PENDING' || res.status === 'CONFIRMED') && (
                          <div className="flex gap-2 mt-2 pt-3 border-t border-gray-100">
                            <button onClick={() => updateStatus(res.resId, 'CONFIRMED')} className="bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-xl text-sm font-bold flex-1 transition active:scale-95">Confirm</button>
                            <button onClick={() => updateStatus(res.resId, 'CANCELLED')} className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-xl text-sm font-bold flex-1 transition active:scale-95">Cancel</button>
                            <button onClick={() => updateStatus(res.resId, 'COMPLETED')} className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-xl text-sm font-bold flex-1 transition active:scale-95">Complete</button>
                          </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {activeTab === 'SEATING' && (
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
              <h3 className="text-lg font-bold text-gray-900 mb-4">Add New Seating Area</h3>
              <form onSubmit={handleAddSeating} className="flex flex-col sm:flex-row gap-4">
                
                <select 
                  required 
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50 font-bold text-gray-700"
                  value={newSeatingName} 
                  onChange={e => setNewSeatingName(e.target.value)}
                >
                  <option value="INDOOR">INDOOR</option>
                  <option value="OUTDOOR">OUTDOOR</option>
                  <option value="BAR">BAR</option>
                </select>

                <input 
                  type="number" required min="1" placeholder="Capacity (e.g., 40)" 
                  className="w-full sm:w-48 px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50"
                  value={newSeatingCapacity} onChange={e => setNewSeatingCapacity(e.target.value)}
                />
                <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white font-bold px-6 py-3 rounded-xl transition active:scale-95 whitespace-nowrap">
                  Add Area
                </button>
              </form>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="p-4 bg-gray-50 border-b border-gray-200">
                <h3 className="font-bold text-gray-700 uppercase tracking-wider text-sm">Configured Layouts</h3>
              </div>
              <ul className="divide-y divide-gray-100">
                {seatingAreas.length === 0 ? (
                  <li className="p-6 text-center text-gray-500 font-medium">No seating areas configured.</li>
                ) : (
                  seatingAreas.map(area => (
                    <li key={area.areaId} className="p-4 flex justify-between items-center hover:bg-gray-50 transition-colors">
                      
                      {editingAreaId === area.areaId ? (
                        <div className="flex items-center gap-4 w-full">
                          <span className="font-bold text-gray-900 w-24">{area.areaName}</span>
                          <input 
                            type="number" 
                            min="1" 
                            className="px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 w-32" 
                            value={editCapacity} 
                            onChange={e => setEditCapacity(e.target.value)} 
                          />
                          <div className="flex gap-2 ml-auto">
                            <button onClick={() => handleUpdateSeating(area.areaId, area.areaName)} className="bg-green-500 hover:bg-green-600 text-white font-bold px-4 py-2 rounded-lg transition-colors">Save</button>
                            <button onClick={() => setEditingAreaId(null)} className="bg-gray-200 hover:bg-gray-300 text-gray-700 font-bold px-4 py-2 rounded-lg transition-colors">Cancel</button>
                          </div>
                        </div>
                      ) : (
                        <>
                          <div>
                            <span className="block font-bold text-gray-900 text-lg">{area.areaName}</span>
                            <span className="text-sm font-medium text-gray-500">Capacity: {area.capacity} people</span>
                          </div>
                          <div className="flex gap-2">
                            <button 
                              onClick={() => { setEditingAreaId(area.areaId); setEditCapacity(area.capacity); }}
                              className="text-blue-500 hover:text-blue-700 hover:bg-blue-50 p-2 rounded-lg transition-colors"
                              title="Edit Capacity"
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-5 h-5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L6.832 19.82a4.5 4.5 0 01-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 011.13-1.897L16.863 4.487zm0 0L19.5 7.125" />
                              </svg>
                            </button>
                            <button 
                              onClick={() => handleDeleteSeating(area.areaId)}
                              className="text-red-500 hover:text-red-700 hover:bg-red-50 p-2 rounded-lg transition-colors"
                              title="Delete Area"
                            >
                              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor" className="w-5 h-5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                              </svg>
                            </button>
                          </div>
                        </>
                      )}
                    </li>
                  ))
                )}
              </ul>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}