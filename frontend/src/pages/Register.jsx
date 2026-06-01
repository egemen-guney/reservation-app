import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';

export default function Register() {
  const navigate = useNavigate();
  
  // Toggle between CUSTOMER and RESTAURANT
  const [role, setRole] = useState('CUSTOMER'); 
  
  // Shared Account Fields
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');

  // Password
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  // Customer-Specific Fields
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  
  // Restaurant-Specific Fields
  const [restaurantName, setRestaurantName] = useState('');
  const [busPhone, setBusPhone] = useState('');
  const [street, setStreet] = useState('');
  const [city, setCity] = useState('');
  const [stateLoc, setStateLoc] = useState(''); // Renamed to avoid React 'state' conflicts
  const [zipCode, setZipCode] = useState('');
  const [country, setCountry] = useState('');
  const [openingHours, setOpeningHours] = useState('09:00');
  const [closingHours, setClosingHours] = useState('22:00');
  
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setIsLoading(true);

    try {
      const endpoint = role === 'CUSTOMER' ? '/customers/register' : '/restaurants/register';
      
      // Build the JSON payload dynamically
      const payload = role === 'CUSTOMER' 
        ? { email, password, firstName, lastName, phone }
        : { 
            email, password, name: restaurantName, busPhone, phone, 
            street, city, state: stateLoc, zipCode, country,
            openingHours, closingHours, isOpen: false // Satisfies all Restaurant fields!
          };

      await api.post(endpoint, payload);
      
      window.alert("Registration successful! You can now log in.");
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Check your inputs or try a different email.");
    } finally {
      setIsLoading(false);
    }
  };

  // Helper component for the Eye Icon SVG to keep the JSX clean
  const EyeIcon = ({ isVisible }) => (
    isVisible ? (
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
      </svg>
    ) : (
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-5 h-5">
        <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    )
  );

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative pb-20">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Create an Account
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-sm border border-gray-200 sm:rounded-2xl sm:px-10">
          
          {/* Role Toggle Switch */}
          <div className="flex bg-gray-100 p-1 rounded-xl mb-6">
            <button
              type="button"
              onClick={() => setRole('CUSTOMER')}
              className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all ${
                role === 'CUSTOMER' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Customer
            </button>
            <button
              type="button"
              onClick={() => setRole('RESTAURANT')}
              className={`flex-1 py-2 text-sm font-bold rounded-lg transition-all ${
                role === 'RESTAURANT' ? 'bg-white shadow-sm text-blue-600' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Restaurant Owner
            </button>
          </div>

          {error && (
            <div className="mb-4 bg-red-50 text-red-600 p-3 rounded-lg text-sm font-bold text-center border border-red-100">
              {error}
            </div>
          )}

          <form className="space-y-4" onSubmit={handleRegister}>
            {/* Shared Identification Fields */}
            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">Email</label>
              <input type="email" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={email} onChange={e => setEmail(e.target.value)} />
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">Phone Number</label>
              <input type="tel" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={phone} onChange={e => setPhone(e.target.value)} />
            </div>
            
            {/* Customer Specific */}
            {role === 'CUSTOMER' && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">First Name</label>
                  <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={firstName} onChange={e => setFirstName(e.target.value)} />
                </div>
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Last Name</label>
                  <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={lastName} onChange={e => setLastName(e.target.value)} />
                </div>
              </div>
            )}

            {/* Restaurant Specific Fields */}
            {role === 'RESTAURANT' && (
              <div className="space-y-4 border-t border-b border-gray-100 py-4 my-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Restaurant Name</label>
                  <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={restaurantName} onChange={e => setRestaurantName(e.target.value)} />
                </div>
                
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Business Phone</label>
                  <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={busPhone} onChange={e => setBusPhone(e.target.value)} />
                </div>

                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1">Street Address</label>
                  <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={street} onChange={e => setStreet(e.target.value)} />
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">City</label>
                    <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={city} onChange={e => setCity(e.target.value)} />
                  </div>
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">State / Province</label>
                    <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={stateLoc} onChange={e => setStateLoc(e.target.value)} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Zip / Postal Code</label>
                    <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={zipCode} onChange={e => setZipCode(e.target.value)} />
                  </div>
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Country</label>
                    <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={country} onChange={e => setCountry(e.target.value)} />
                  </div>
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Opening Time</label>
                    <input type="time" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={openingHours} onChange={e => setOpeningHours(e.target.value)} />
                  </div>
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-1">Closing Time</label>
                    <input type="time" required className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50" value={closingHours} onChange={e => setClosingHours(e.target.value)} />
                  </div>
                </div>
              </div>
            )}

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">Password</label>
              <div className="relative flex items-center">
                <input 
                  type={showPassword ? "text" : "password"} 
                  required 
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50 pr-12" 
                  value={password} 
                  onChange={e => setPassword(e.target.value)} 
                />
                <button 
                  type="button" 
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 text-gray-400 hover:text-blue-600 focus:outline-none transition-colors"
                >
                  <EyeIcon isVisible={showPassword} />
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-bold text-gray-700 mb-1">Confirm Password</label>
              <div className="relative flex items-center">
                <input 
                  type={showConfirmPassword ? "text" : "password"} 
                  required 
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50 pr-12" 
                  value={confirmPassword} 
                  onChange={e => setConfirmPassword(e.target.value)} 
                />
                <button 
                  type="button" 
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-4 text-gray-400 hover:text-blue-600 focus:outline-none transition-colors"
                >
                  <EyeIcon isVisible={showConfirmPassword} />
                </button>
              </div>
            </div>

            <button type="submit" disabled={isLoading} className="w-full bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-xl shadow-md transition active:scale-[0.98] mt-6 disabled:bg-blue-400">
              {isLoading ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="font-bold text-blue-600 hover:text-blue-500">
                Log in here
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}