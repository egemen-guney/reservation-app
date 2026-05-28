package com.resapp.app.menu;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * RESTAURANTS CANNOT VIEW OTHER RESTAURANTS' MENU ITEMS
     */
    @GetMapping("/{menuId}/items")
    @ResponseStatus(HttpStatus.OK)
    public List<MenuItem> getMenuItems(@PathVariable UUID menuId) {
        return menuService.getMenuItems(menuId);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PostMapping("/{menuId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMenuItem(@PathVariable UUID menuId, @Valid @RequestBody MenuItemRequest request) {
        menuService.addMenuItem(menuId, request);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PutMapping("/{menuId}/items/{menuItemId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMenuItem(
            @PathVariable UUID menuId,
            @PathVariable UUID menuItemId,
            @Valid @RequestBody MenuItemRequest request) {
        menuService.updateMenuItem(menuId, menuItemId, request);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @DeleteMapping("/{menuId}/items/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable UUID menuId, @PathVariable UUID menuItemId) {
        menuService.deleteMenuItem(menuId, menuItemId);
    }
}
