package org.maheshz.LAFbackend.service;

import lombok.RequiredArgsConstructor;
import org.maheshz.LAFbackend.dto.CreateItemDTO;
import org.maheshz.LAFbackend.dto.ItemResponseDTO;
import org.maheshz.LAFbackend.entity.GeoLocation;
import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.ItemRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ItemResponseDTO getItemById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return mapToDTO(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDTO> getAllItems(ItemType type, String search, String location) {
        return itemRepository.findAll().stream()
                // 1. Filter by Type (LOST / FOUND)
                .filter(item -> type == null || item.getType() == type)

                // 2. Filter by Search Query (Title or Description)
                .filter(item -> search == null || search.isBlank() ||
                        item.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(search.toLowerCase()))

                // 3. Filter by Location
                .filter(item -> location == null || location.isBlank() ||
                        (item.getLocation() != null && item.getLocation().getAddressText().toLowerCase().contains(location.toLowerCase())))

                // Map the filtered results to DTOs
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public ItemResponseDTO createItem(CreateItemDTO dto, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GeoLocation geoLocation = new GeoLocation(dto.getLatitude(), dto.getLongitude(), dto.getAddressText());

        Item item = Item.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .type(dto.getType())
                .description(dto.getDescription())
                .imageUrls(dto.getImageUrls())
                .location(geoLocation)
                .dateLostOrFound(dto.getDateLostOrFound())
                .status(ItemStatus.OPEN)
                .reportedBy(currentUser)
                .build();

        if (dto.getSecretVerificationQuestion() != null && dto.getSecretAnswer() != null) {
            item.setHasSecretDetail(true);
            item.setSecretVerificationQuestion(dto.getSecretVerificationQuestion());
            item.setSecretAnswerHash(passwordEncoder.encode(dto.getSecretAnswer()));
        } else {
            item.setHasSecretDetail(false);
        }

        Item savedItem = itemRepository.save(item);
        return mapToDTO(savedItem);
    }

    private ItemResponseDTO mapToDTO(Item item) {
        return ItemResponseDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .category(item.getCategory())
                .type(item.getType())
                .description(item.getDescription())
                .imageUrls(item.getImageUrls())
                .location(item.getLocation())
                .dateLostOrFound(item.getDateLostOrFound())
                .status(item.getStatus())
                .hasSecretDetail(item.isHasSecretDetail())
                .reportedByUserId(item.getReportedBy().getId()) // Safely flattens the user
                .createdAt(item.getCreatedAt())
                .secretVerificationQuestion(item.getSecretVerificationQuestion())
                .build();
    }
}