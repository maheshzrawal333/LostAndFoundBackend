package org.maheshz.LAFbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.CreateItemDTO;
import org.maheshz.LAFbackend.dto.ItemResponseDTO;
import org.maheshz.LAFbackend.enums.ItemType;
import org.maheshz.LAFbackend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location) {

        // Calls the service to apply the filters and return the safe DTO list
        List<ItemResponseDTO> items = itemService.getAllItems(type, search, location);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody CreateItemDTO dto, Principal principal) {
        // Principal contains the email extracted from the JWT
        String userEmail = principal.getName();
        ItemResponseDTO savedItem = itemService.createItem(dto, userEmail);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }
}