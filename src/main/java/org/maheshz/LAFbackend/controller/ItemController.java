package org.maheshz.LAFbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.CreateItemDTO;
import org.maheshz.LAFbackend.dto.ItemResponseDTO;
import org.maheshz.LAFbackend.dto.PaginatedResponseDTO;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;
import org.maheshz.LAFbackend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<ItemResponseDTO>> getAllItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) Boolean myPosts,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Principal principal) {

        String email = principal != null ? principal.getName() : null;
        PaginatedResponseDTO<ItemResponseDTO> paginatedItems = itemService.getAllItems(type, status, myPosts, search, location, email, page, size);
        return ResponseEntity.ok(paginatedItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable UUID id, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(itemService.getItemById(id, email));
    }

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody CreateItemDTO dto, Principal principal, HttpServletRequest request) {
        String userEmail = principal.getName();
        ItemResponseDTO savedItem = itemService.createItem(dto, userEmail, request);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }
}