package com.resapp.app.menu;

import com.resapp.app.account.Account;
import com.resapp.app.account.AccountRole;
import com.resapp.app.restaurant.Restaurant;
import com.resapp.app.restaurant.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuService(MenuRepository menuRepository, MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void addMenuItem(UUID menuId, MenuItemRequest request, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                        .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getMenuId().equals(menuId)) {
            throw new AccessDeniedException("You are only authorized to add items to your own menus");
        }

        menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found."));

        UUID newMenuItemId = UUID.randomUUID();
        MenuItem newMenuItem = MenuItem.builder()
                .menuItemId(newMenuItemId)
                .menuId(menuId)
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .isAvailable(request.isAvailable())
                .build();

        menuItemRepository.create(newMenuItem);
    }

    @Transactional
    public void updateMenuItem(UUID menuId, UUID menuItemId, UUID loggedinId, MenuItemRequest request) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

        if (!myRestaurant.getMenuId().equals(menuId)) {
            throw new AccessDeniedException("You are only authorized to update menus for your own restaurant.");
        }

        MenuItem existingMenuItem = menuItemRepository.findByItemId(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found."));

        if (!existingMenuItem.getMenuId().equals(menuId)) {
            throw new IllegalStateException("This item does not belong to the given menu.");
        }

        existingMenuItem.setName(request.name());
        existingMenuItem.setDescription(request.description());
        existingMenuItem.setCategory(request.category());
        existingMenuItem.setPrice(request.price());
        existingMenuItem.setAvailable(request.isAvailable());

        menuItemRepository.update(existingMenuItem);
    }

    @Transactional
    public void deleteMenuItem(UUID menuId, UUID menuItemId, UUID loggedinId) {
        Restaurant myRestaurant = restaurantRepository.findByAccountId(loggedinId)
                .orElseThrow(() -> new IllegalStateException("Restaurant progile not found."));

        if (!myRestaurant.getMenuId().equals(menuId)) {
            throw new AccessDeniedException("You are only authorized to update menus for your own restaurant.");
        }

        MenuItem existingMenuItem = menuItemRepository.findByItemId(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found."));

        if (!existingMenuItem.getMenuId().equals(menuId)) {
            throw new IllegalStateException("This item does not belong to the given menu.");
        }

        menuItemRepository.delete(menuItemId);
    }

    public List<MenuItem> getMenuItems(UUID menuId, Account account) {
        if (account.getRole() == AccountRole.RESTAURANT) {
            Restaurant myRestaurant = restaurantRepository.findByAccountId(account.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Restaurant profile not found."));

            if (!myRestaurant.getMenuId().equals(menuId)) {
                throw new AccessDeniedException("You are only authorized to view menus for your own restaurant.");
            }
        }
        return menuItemRepository.findByMenuId(menuId);
    }
}