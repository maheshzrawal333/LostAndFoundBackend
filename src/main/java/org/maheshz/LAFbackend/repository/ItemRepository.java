package org.maheshz.LAFbackend.repository;

import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByTypeOrderByCreatedAtDesc(ItemType type);
    List<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
