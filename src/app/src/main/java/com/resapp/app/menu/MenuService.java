package com.resapp.app.menu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuRepository menuRepository, MenuItemRepository menuItemRepository) {
        this.menuRepository = menuRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public void addMenuItem(UUID menuId, MenuItemRequest request) {
        menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu with this ID does not exist."));

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
    public List<MenuItem> getMenuItems(UUID menuId) {
        return menuItemRepository.findByMenuId(menuId);
    }
}