package org.maheshz.LAFbackend.repository;

import org.maheshz.LAFbackend.entity.Item;
import org.maheshz.LAFbackend.entity.User;
import org.maheshz.LAFbackend.enums.ItemStatus;
import org.maheshz.LAFbackend.enums.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    @Query("SELECT i FROM Item i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:type IS NULL OR i.type = :type) AND " +
            "(:myPostsUser IS NULL OR i.reportedBy = :myPostsUser) AND " +
            "(:search = '' OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:location = '' OR LOWER(i.location.addressText) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Item> findAllWithFilters(@Param("status") ItemStatus status,
                                  @Param("type") ItemType type,
                                  @Param("myPostsUser") User myPostsUser,
                                  @Param("search") String search,
                                  @Param("location") String location,
                                  Pageable pageable);

    long countByReportedByAndCreatedAtAfter(User user, OffsetDateTime date);
}