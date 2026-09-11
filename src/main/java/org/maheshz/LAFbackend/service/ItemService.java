package org.maheshz.LAFbackend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.maheshz.LAFbackend.dto.CreateItemDTO;
import org.maheshz.LAFbackend.dto.ItemResponseDTO;
import org.maheshz.LAFbackend.dto.PaginatedResponseDTO;
import org.maheshz.LAFbackend.entity.GeoLocation;
import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;
import org.maheshz.LAFbackend.exception.ResourceNotFoundException;
import org.maheshz.LAFbackend.repository.ClaimRepository;
import org.maheshz.LAFbackend.repository.ItemRepository;
import org.maheshz.LAFbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    // --- Enterprise Pre-OTP Rate Limiting Validation ---
    @Transactional(readOnly = true)
    public void validateUserPostLimit(String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);
        long recentPostsCount = itemRepository.countByReportedByAndCreatedAtAfter(currentUser, oneWeekAgo);

        if (recentPostsCount >= 3) {
            log.warn("[SPAM PREVENTION] User {} hit the 3-post weekly limit.", currentUser.getId());
            throw new IllegalStateException("You have reached the maximum limit of 3 posts per week to prevent spam. Please try again later.");
        }
    }

    @Transactional(readOnly = true)
    public ItemResponseDTO getItemById(UUID id, String currentUserEmail) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return mapToDTO(item, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ItemResponseDTO> getAllItems(ItemType type, ItemStatus status, Boolean myPosts, String search, String location, String currentUserEmail, int page, int size) {
        User myPostsUser = null;
        if (Boolean.TRUE.equals(myPosts) && currentUserEmail != null) {
            myPostsUser = userRepository.findByEmail(currentUserEmail).orElse(null);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        String safeSearch = (search != null && !search.isBlank()) ? search : "";
        String safeLocation = (location != null && !location.isBlank()) ? location : "";

        Page<Item> itemsPage = itemRepository.findAllWithFilters(
                status, type, myPostsUser, safeSearch, safeLocation, pageable
        );

        List<ItemResponseDTO> content = itemsPage.getContent().stream()
                .map(item -> mapToDTO(item, currentUserEmail))
                .toList();

        return new PaginatedResponseDTO<>(
                content,
                itemsPage.getNumber(),
                itemsPage.getSize(),
                itemsPage.getTotalElements(),
                itemsPage.getTotalPages(),
                itemsPage.isLast()
        );
    }

    @Transactional
    public ItemResponseDTO createItem(CreateItemDTO dto, String userEmail, HttpServletRequest request) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Double-check the limit right before database insertion to prevent race conditions
        validateUserPostLimit(userEmail);

        GeoLocation geoLocation = new GeoLocation(dto.getLatitude(), dto.getLongitude(), dto.getAddressText());

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");

        Item item = Item.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .type(dto.getType())
                .description(dto.getDescription())
                .imageUrls(dto.getImageUrls())
                .location(geoLocation)
                .dateLostOrFound(dto.getDateLostOrFound())
                .status(ItemStatus.OPEN)
                .uploaderIp(ipAddress)
                .uploaderUserAgent(userAgent)
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
        log.info("[AUDIT] New Item Created. Item ID: {} | User ID: {}", savedItem.getId(), currentUser.getId());
        return mapToDTO(savedItem, userEmail);
    }

    private ItemResponseDTO mapToDTO(Item item, String currentUserEmail) {
        boolean alreadyClaimed = false;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null) {
                alreadyClaimed = claimRepository.existsByItemIdAndClaimerId(item.getId(), user.getId());
            }
        }

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
                .reportedByUserId(item.getReportedBy().getId())
                .createdAt(item.getCreatedAt())
                .secretVerificationQuestion(item.getSecretVerificationQuestion())
                .hasAlreadyClaimed(alreadyClaimed)
                .build();
    }
}