package com.mtheory7.legoinventoryservice.controller;

import com.mtheory7.legoinventoryservice.service.LegoInventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LegoInventoryController {
    private final LegoInventoryService legoInventoryService;

    public LegoInventoryController(LegoInventoryService legoInventoryService) {
        this.legoInventoryService = legoInventoryService;
    }

    @GetMapping("/refreshInventory")
    public String refreshInventory() throws InterruptedException {
        return legoInventoryService.refreshInventory();
    }

    @GetMapping("/displayInventory")
    public String displayInventory() {
        return legoInventoryService.getDisplayString();
    }

    @GetMapping("/generateFile")
    public String generateFile() {
        return legoInventoryService.generateFile();
    }
}


