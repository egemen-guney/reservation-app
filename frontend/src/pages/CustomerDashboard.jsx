import { useNavigate } from 'react-router-dom';
import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';

export default function CustomerDashboard() {
  const [restaurants, setRestaurants] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/restaurants')
       .then(async res => {
         if (Array.isArray(res.data)) {
           const restaurantsWithAddresses = await Promise.all(
             res.data.map(async (rest) => {
               try {
                 const addrRes = await api.get(`/restaurants/${rest.restaurantId}/address`);
                 return { ...rest, address: addrRes.data };
               } catch (err) {
                 return { ...rest, address: null };
               }
             })
           );
           setRestaurants(restaurantsWithAddresses);
         } else {
           setRestaurants([]);
         }
       })
       .catch(err => {
         console.error("Failed to fetch restaurants:", err);
         setRestaurants([]);
       })
       .finally(() => setIsLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <h1 className="text-2xl font-bold text-blue-600">Discover</h1>
        <div className="flex gap-4 items-center">
          <button onClick={() => navigate('/customer/reservations')} className="text-sm font-bold text-gray-700 hover:text-blue-600 transition">
            Bookings
          </button>
          <button onClick={logout} className="text-sm font-bold text-red-500 hover:text-red-700 transition">
            Log Out
          </button>
        </div>
      </header>

      <main className="p-4 space-y-4">
        {isLoading ? (
          <p className="text-gray-500 font-bold animate-pulse text-center py-12">Loading restaurants...</p>
        ) : restaurants.length > 0 ? (
          restaurants.map(rest => (
            <div key={rest.restaurantId} 
              onClick={() => navigate(`/customer/restaurant/${rest.restaurantId}`)}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden active:scale-[0.98] transition-transform cursor-pointer">
              <div className="h-32 bg-gray-200">
                 <div className="w-full h-full object-cover bg-blue-100" />
              </div>
              <div className="p-4">
                <div className="flex justify-between items-start">
                  <h2 className="text-lg font-bold text-gray-900">{rest.name}</h2>
                  <span className="flex items-center text-sm font-bold text-yellow-500">★ {(rest.stars || 3.0).toFixed(1)}</span>
                </div>
                
                <p className="text-sm text-gray-700 mt-2 font-medium flex items-center gap-1">
                  <span className="text-red-500">📍</span> 
                  {rest.address ? `${rest.address.street}, ${rest.address.city}, ${rest.address.state}, ${rest.address.country}` : 'Address unavailable'}
                </p>
                
                <p className="text-sm text-gray-500 mt-1 font-medium">Closes at {rest.closingHours}</p>
              </div>
            </div>
          ))
        ) : (
          <div className="py-12 text-center">
            <p className="text-gray-500 font-bold text-lg">No restaurants available right now.</p>
          </div>
        )}
      </main>
    </div>
  );
}