import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';

export default function AdminDashboard() {
  const { logout } = useContext(AuthContext);

  const [restaurants, setRestaurants] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchAdminData = async () => {
      setIsLoading(true);
      try {
        // 1. Fetch Restaurants & Addresses
        const restRes = await api.get('/restaurants');
        if (Array.isArray(restRes.data)) {
          const restaurantsWithAddresses = await Promise.all(
            restRes.data.map(async (rest) => {
              try {
                const addrRes = await api.get(`/restaurants/${rest.restaurantId}/address`);
                return { ...rest, address: addrRes.data };
              } catch (err) {
                return { ...rest, address: null };
              }
            })
          );
          setRestaurants(restaurantsWithAddresses);
        }

        // 2. Fetch All Reservations
        // Note: Make sure your backend has a GET /reservations endpoint for Admins
        try {
          const resvData = await api.get('/reservations');
          if (Array.isArray(resvData.data)) setReservations(resvData.data);
        } catch (err) {
          console.error("Could not fetch reservations:", err);
        }

        // 3. Fetch All Reviews
        // Note: Make sure your backend has a GET /reviews endpoint for Admins
        try {
          const revData = await api.get('/reviews');
          if (Array.isArray(revData.data)) setReviews(revData.data);
        } catch (err) {
          console.error("Could not fetch reviews:", err);
        }

      } catch (error) {
        console.error("Failed to load admin data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAdminData();
  }, []);

  // --- DELETE HANDLERS ---
  const handleDeleteRestaurant = async (id) => {
    if (!window.confirm("Are you sure you want to delete this restaurant? This action cannot be undone.")) return;
    try {
      await api.delete(`/admins/delete/restaurant/${id}`);
      setRestaurants(prev => prev.filter(r => r.restaurantId !== id));
    } catch (err) {
      alert("Failed to delete restaurant.");
    }
  };

  const handleDeleteReservation = async (id) => {
    if (!window.confirm("Are you sure you want to delete this reservation?")) return;
    try {
      await api.delete(`/reservations/${id}`);
      setReservations(prev => prev.filter(r => r.resId !== id));
    } catch (err) {
      alert("Failed to delete reservation.");
    }
  };

  const handleDeleteReview = async (id) => {
    if (!window.confirm("Are you sure you want to delete this review?")) return;
    try {
      await api.delete(`/reviews/${id}`);
      setReviews(prev => prev.filter(r => r.reviewId !== id));
    } catch (err) {
      alert("Failed to delete review.");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-gray-900 shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <h1 className="text-2xl font-bold text-white">Admin Control Panel</h1>
        <button onClick={logout} className="text-sm font-bold text-red-400 hover:text-red-300 transition">
          Log Out
        </button>
      </header>

      <main className="p-4 max-w-5xl mx-auto space-y-10 mt-4">
        {isLoading ? (
          <p className="text-gray-500 font-bold animate-pulse text-center py-12">Loading platform data...</p>
        ) : (
          <>
            {/* SECTION: RESTAURANTS */}
            <section>
              <h2 className="text-xl font-extrabold text-gray-900 mb-4 border-b border-gray-200 pb-2">Manage Restaurants</h2>
              {restaurants.length === 0 ? (
                <p className="text-gray-500 italic">No restaurants found.</p>
              ) : (
                <div className="space-y-4">
                  {restaurants.map(rest => (
                    <div key={rest.restaurantId} className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <h3 className="font-bold text-lg text-gray-900">{rest.name}</h3>
                          <span className="text-yellow-500 font-bold text-sm">★ {(rest.stars || 3.0).toFixed(1)}</span>
                        </div>
                        
                        <p className="text-sm text-gray-700 font-medium flex items-center gap-1">
                          <span className="text-red-500">📍</span> 
                          {rest.address ? `${rest.address.street}, ${rest.address.city}, ${rest.address.stateLoc || rest.address.state}, ${rest.address.zipCode}, ${rest.address.country}` : 'Address unavailable'}
                        </p>
                        
                        <div className="text-sm text-gray-600 grid grid-cols-1 sm:grid-cols-2 gap-x-4 mt-2">
                          <p>🏢 <span className="font-semibold text-blue-800">Business Phone:</span> {rest.busPhone || "N/A"}</p>
                          <p>📱 <span className="font-semibold text-blue-800">Account Phone:</span> {rest.phone || "Hidden"}</p>
                          <p className="sm:col-span-2">✉️ <span className="font-semibold text-blue-800">Email:</span> {rest.email || "Hidden"}</p>
                        </div>
                      </div>
                      <button 
                        onClick={() => handleDeleteRestaurant(rest.restaurantId)} 
                        className="w-full md:w-auto bg-red-100 hover:bg-red-600 text-red-600 hover:text-white font-bold px-6 py-3 rounded-lg transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* SECTION: RESERVATIONS */}
            <section>
              <h2 className="text-xl font-extrabold text-gray-900 mb-4 border-b border-gray-200 pb-2">Manage Reservations</h2>
              {reservations.length === 0 ? (
                <p className="text-gray-500 italic">No reservations found.</p>
              ) : (
                <div className="space-y-4">
                  {reservations.map(res => (
                    <div key={res.resId} className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 flex justify-between items-center gap-4">
                      <div>
                        <p className="font-bold text-gray-900">Party of {res.numPeople} <span className="text-gray-400 font-normal">|</span> <span className={`text-xs px-2 py-1 rounded-md ${res.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' : res.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>{res.status}</span></p>
                        <p className="text-sm text-gray-600 mt-1">📅 {new Date(res.resDate).toLocaleDateString()} | ⏰ {res.startTime}</p>
                      </div>
                      <button 
                        onClick={() => handleDeleteReservation(res.resId)} 
                        className="bg-red-100 hover:bg-red-600 text-red-600 hover:text-white font-bold px-4 py-2 rounded-lg transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* SECTION: REVIEWS */}
            <section>
              <h2 className="text-xl font-extrabold text-gray-900 mb-4 border-b border-gray-200 pb-2">Manage Reviews</h2>
              {reviews.length === 0 ? (
                <p className="text-gray-500 italic">No reviews found.</p>
              ) : (
                <div className="space-y-4">
                  {reviews.map(rev => (
                    <div key={rev.reviewId} className="bg-white p-5 rounded-xl shadow-sm border border-gray-200 flex justify-between items-center gap-4">
                      <div>
                        <p className="text-yellow-500 font-bold">★ {rev.rating?.toFixed(1)}</p>
                        <p className="text-gray-700 text-sm mt-1">"{rev.comment || 'No comment provided.'}"</p>
                      </div>
                      <button 
                        onClick={() => handleDeleteReview(rev.reviewId)} 
                        className="bg-red-100 hover:bg-red-600 text-red-600 hover:text-white font-bold px-4 py-2 rounded-lg transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}