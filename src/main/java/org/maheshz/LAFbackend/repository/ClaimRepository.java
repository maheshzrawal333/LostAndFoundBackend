package org.maheshz.LAFbackend.repository;

import org.maheshz.LAFbackend.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    // NEW: Check if a user has already claimed this specific item
    boolean existsByItemIdAndClaimerId(UUID itemId, UUID claimerId);
}