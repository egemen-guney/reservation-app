package com.resapp.app.menu;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{menuId}/items")
    @ResponseStatus(HttpStatus.OK)
    public List<MenuItem> getMenuItems(@PathVariable UUID menuId) {
        return menuService.getMenuItems(menuId);
    }

    @PostMapping("/{menuId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMenuItem(@PathVariable UUID menuId, @Valid @RequestBody MenuItemRequest request) {
        menuService.addMenuItem(menuId, request);
    }
}
