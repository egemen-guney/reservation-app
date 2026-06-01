import { useEffect, useState, useContext } from 'react';
import api from '../api/axiosConfig';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function MenuEditor() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [menuId, setMenuId] = useState(null);
  const [items, setItems] = useState([]);
  
  const [itemName, setItemName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState('');
  const [isAvailable, setIsAvailable] = useState(true);

  // Modification State
  const [editingItemId, setEditingItemId] = useState(null);
  const [editForm, setEditForm] = useState({ name: '', description: '', price: '', category: '', isAvailable: true });

  useEffect(() => {
    if (!user || !user.profileId) return;

    api.get(`/restaurants/${user.profileId}`)
       .then(res => {
         const id = res.data.menuId;
         setMenuId(id);
         if (id) return api.get(`/menus/${id}/items`);
       })
       .then(res => {
         if (res) setItems(res.data);
       })
       .catch(err => console.error("Failed to load menu", err));
  }, [user]); 

  const handleAddItem = async (e) => {
    e.preventDefault();
    try {
      // Sending both to ensure Jackson maps it regardless of backend naming config
      await api.post(`/menus/${menuId}/items`, {
        name: itemName,
        description,
        price: parseFloat(price),
        category,
        isAvailable: isAvailable,
        available: isAvailable
      });
      
      const updated = await api.get(`/menus/${menuId}/items`);
      setItems(updated.data);
      
      setItemName(''); setDescription(''); setPrice(''); setCategory(''); setIsAvailable(true); 
    } catch (err) { 
      alert("Failed to add item. Check your backend console for validation errors!"); 
    }
  };

  const handleEditClick = (item) => {
    setEditingItemId(item.menuItemId);
    setEditForm({
      name: item.name,
      description: item.description || '',
      price: item.price,
      category: item.category || '',
      isAvailable: item.available !== undefined ? item.available : item.isAvailable
    });
  };

  const handleUpdateItem = async (e, itemId) => {
    e.preventDefault();
    try {
      await api.put(`/menus/${menuId}/items/${itemId}`, {
        ...editForm,
        price: parseFloat(editForm.price),
        available: editForm.isAvailable // Spring Boot fallback
      });
      const updated = await api.get(`/menus/${menuId}/items`);
      setItems(updated.data);
      setEditingItemId(null);
    } catch (err) { alert("Failed to update item."); }
  };

  const handleDeleteItem = async (menuItemId) => {
    if (!window.confirm("Are you sure you want to completely remove this item from your menu?")) return;
    try {
      await api.delete(`/menus/${menuId}/items/${menuItemId}`);
      setItems(prevItems => prevItems.filter(item => item.menuItemId !== menuItemId));
    } catch (err) { alert("Failed to delete item."); }
  };

  if (!user || !menuId) return <div className="p-8 text-center font-bold text-gray-500 mt-20">Loading Menu Engine...</div>;

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <header className="bg-white shadow-sm p-4 sticky top-0 flex justify-between items-center z-10">
        <button onClick={() => navigate('/restaurant/dashboard')} className="text-blue-600 font-bold">← Dashboard</button>
        <h1 className="text-xl font-bold text-gray-900">Menu Editor</h1>
        <div className="w-12"></div>
      </header>

      <main className="p-4 max-w-3xl mx-auto space-y-6 mt-4">
        
        {/* Add Item Form (Unchanged) */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
          <h2 className="text-xl font-bold mb-4 text-gray-900">Add New Menu Item</h2>
          <form onSubmit={handleAddItem} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Item Name</label>
                <input type="text" placeholder="e.g. Truffle Fries" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={itemName} onChange={e => setItemName(e.target.value)} />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Category</label>
                <input type="text" placeholder="e.g. Appetizers" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={category} onChange={e => setCategory(e.target.value)} />
              </div>
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Description</label>
              <textarea placeholder="Ingredients, allergies, etc." className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={description} onChange={e => setDescription(e.target.value)}></textarea>
            </div>
            <div className="grid grid-cols-2 gap-4 items-center">
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Price ($)</label>
                <input type="number" step="0.01" min="0" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={price} onChange={e => setPrice(e.target.value)} />
              </div>
              <div className="flex items-center gap-3 mt-4 ml-4">
                <input type="checkbox" className="w-6 h-6 text-blue-600 rounded-md cursor-pointer" checked={isAvailable} onChange={e => setIsAvailable(e.target.checked)} />
                <label className="font-bold text-gray-700 cursor-pointer" onClick={() => setIsAvailable(!isAvailable)}>Is Available?</label>
              </div>
            </div>
            <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-black py-4 rounded-xl shadow-md transition active:scale-[0.98]">Add to Menu</button>
          </form>
        </div>

        {/* Current Items List */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
          <h2 className="text-xl font-bold mb-4 text-gray-900">Current Menu</h2>
          {items.length === 0 && <p className="text-gray-500 italic">Your menu is empty.</p>}
          <div className="space-y-4">
            {items.map(item => {
              const isItemAvailable = item.available !== undefined ? item.available : item.isAvailable;

              // EDIT MODE LAYOUT
              if (editingItemId === item.menuItemId) {
                return (
                  <div key={item.menuItemId} className="bg-white p-4 rounded-xl border border-blue-200 shadow-sm relative overflow-hidden">
                    <form onSubmit={(e) => handleUpdateItem(e, item.menuItemId)} className="space-y-4">
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Item Name</label>
                          <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editForm.name} onChange={e => setEditForm({...editForm, name: e.target.value})} />
                        </div>
                        <div>
                          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Category</label>
                          <input type="text" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editForm.category} onChange={e => setEditForm({...editForm, category: e.target.value})} />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Description</label>
                        <textarea className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editForm.description} onChange={e => setEditForm({...editForm, description: e.target.value})}></textarea>
                      </div>
                      <div className="grid grid-cols-2 gap-4 items-center">
                        <div>
                          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Price ($)</label>
                          <input type="number" step="0.01" min="0" required className="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 focus:bg-white transition" value={editForm.price} onChange={e => setEditForm({...editForm, price: e.target.value})} />
                        </div>
                        <div className="flex items-center gap-3 mt-4 ml-4">
                          <input type="checkbox" className="w-6 h-6 text-blue-600 rounded-md cursor-pointer" checked={editForm.isAvailable} onChange={e => setEditForm({...editForm, isAvailable: e.target.checked})} />
                          <label className="font-bold text-gray-700 cursor-pointer" onClick={() => setEditForm({...editForm, isAvailable: !editForm.isAvailable})}>Is Available?</label>
                        </div>
                      </div>
                      <div className="flex gap-2 pt-2">
                        <button type="submit" className="flex-1 bg-green-500 hover:bg-green-600 text-white font-bold py-3 rounded-xl transition">Save Changes</button>
                        <button type="button" onClick={() => setEditingItemId(null)} className="flex-1 bg-gray-200 text-gray-700 font-bold py-3 rounded-xl transition">Cancel</button>
                      </div>
                    </form>
                  </div>
                );
              }

              // STANDARD LAYOUT (Unchanged)
              return (
                <div key={item.menuItemId} className="flex justify-between items-start bg-white p-4 rounded-xl border border-gray-200 shadow-sm relative overflow-hidden">
                  <div className={`absolute top-0 left-0 w-1 h-full ${isItemAvailable ? 'bg-green-500' : 'bg-red-500'}`}></div>
                  <div className="pl-2">
                    <div className="flex items-center gap-3">
                      <h3 className="font-black text-gray-900 text-lg">{item.name}</h3>
                      <span className="bg-gray-100 text-gray-600 px-2 py-1 rounded-md text-xs font-bold uppercase">{item.category}</span>
                      {isItemAvailable ? (
                        <span className="bg-green-100 text-green-700 px-2 py-1 rounded-md text-xs font-black uppercase tracking-wide">In Stock</span>
                      ) : (
                        <span className="bg-red-100 text-red-700 px-2 py-1 rounded-md text-xs font-black uppercase tracking-wide">Out of Stock</span>
                      )}
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{item.description}</p>
                    <div className="flex gap-4 mt-2 text-sm font-bold items-center">
                      <span className="text-green-600">${item.price.toFixed(2)}</span>
                      <span className="text-blue-500 text-xs bg-blue-50 px-2 py-1 rounded">
                        {item.satisfaction ? `Satisfaction: ${item.satisfaction}/100` : 'Satisfaction: No ratings yet'}
                      </span>
                    </div>
                  </div>
                  <div className="flex flex-col gap-2">
                    <button onClick={() => handleEditClick(item)} className="text-blue-500 font-bold hover:bg-blue-50 px-3 py-1 rounded-lg transition">Edit</button>
                    <button onClick={() => handleDeleteItem(item.menuItemId)} className="text-red-500 font-bold hover:bg-red-50 px-3 py-1 rounded-lg transition">Delete</button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}