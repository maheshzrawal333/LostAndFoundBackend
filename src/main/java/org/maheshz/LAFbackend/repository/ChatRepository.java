package org.maheshz.LAFbackend.repository;

import org.maheshz.LAFbackend.entity.Chat;
import org.maheshz.LAFbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByFinderOrClaimerOrderByUpdatedAtDesc(User finder, User claimer);
}
