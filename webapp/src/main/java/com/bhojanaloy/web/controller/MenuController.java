package com.bhojanaloy.web.controller;

import com.bhojanaloy.dao.MenuItemDAO;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.web.dto.MenuItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();

    /** Public (but session-protected) menu list — only items marked available. */
    @GetMapping
    public List<MenuItemResponse> list() {
        List<MenuItem> all = menuItemDAO.getAll();
        return all.stream()
                .filter(MenuItem::isAvailable)
                .map(MenuItemResponse::from)
                .collect(Collectors.toList());
    }
}
