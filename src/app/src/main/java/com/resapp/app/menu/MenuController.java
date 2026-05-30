package com.resapp.app.menu;

import com.resapp.app.account.AccountPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public List<MenuItem> getMenuItems(@PathVariable UUID menuId, @AuthenticationPrincipal AccountPrincipal principal) {
        return menuService.getMenuItems(menuId, principal.getAccount());
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PostMapping("/{menuId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMenuItem(@PathVariable UUID menuId, @Valid @RequestBody MenuItemRequest request,
                            @AuthenticationPrincipal AccountPrincipal principal) {
        menuService.addMenuItem(menuId, request, principal.getAccount().getAccountId());
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PutMapping("/{menuId}/items/{menuItemId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateMenuItem(
            @PathVariable UUID menuId,
            @PathVariable UUID menuItemId,
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody MenuItemRequest request) {
        menuService.updateMenuItem(menuId, menuItemId, principal.getAccount().getAccountId(), request);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @DeleteMapping("/{menuId}/items/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable UUID menuId, @PathVariable UUID menuItemId,
                               @AuthenticationPrincipal AccountPrincipal principal) {
        menuService.deleteMenuItem(menuId, menuItemId, principal.getAccount().getAccountId());
    }
}
