import { createContext, useState } from 'react';

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState([]);
  const [restaurantId, setRestaurantId] = useState(null);

  const addToCart = (item, restId) => {
    if (restaurantId && restaurantId !== restId) {
      if (!window.confirm("You already have items from another restaurant. Clear cart and start new order?")) {
        return;
      }
      setCart([{ ...item, quantity: 1 }]);
      setRestaurantId(restId);
      return;
    }

    setRestaurantId(restId);
    setCart(prev => {
      const existing = prev.find(i => i.menuItemId === item.menuItemId);
      if (existing) {
        return prev.map(i => i.menuItemId === item.menuItemId ? { ...i, quantity: i.quantity + 1 } : i);
      }
      return [...prev, { ...item, quantity: 1 }];
    });
  };

  const decreaseQuantity = (itemId) => {
    setCart(prev => {
      const existing = prev.find(i => i.menuItemId === itemId);
      if (existing && existing.quantity > 1) {
        return prev.map(i => i.menuItemId === itemId ? { ...i, quantity: i.quantity - 1 } : i);
      }
      // If it drops below 1, remove it completely
      const updated = prev.filter(i => i.menuItemId !== itemId);
      if (updated.length === 0) setRestaurantId(null);
      return updated;
    });
  };

  const removeFromCart = (itemId) => {
    setCart(prev => {
      // FIXED: Strictly filtering by menuItemId
      const updated = prev.filter(i => i.menuItemId !== itemId);
      if (updated.length === 0) setRestaurantId(null);
      return updated;
    });
  };

  const clearCart = () => {
    setCart([]);
    setRestaurantId(null);
  };

  const getCartTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  return (
    // FIXED: Ensure all functions, including decreaseQuantity, are exported!
    <CartContext.Provider value={{ 
      cart, 
      restaurantId, 
      addToCart, 
      decreaseQuantity, 
      removeFromCart, 
      clearCart, 
      getCartTotal 
    }}>
      {children}
    </CartContext.Provider>
  );
};