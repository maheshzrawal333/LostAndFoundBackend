package org.maheshz.LAFbackend.repository;

import org.maheshz.LAFbackend.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {
}
