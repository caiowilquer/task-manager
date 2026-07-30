package com.caiowilquer.taskmanager.repository;

import com.caiowilquer.taskmanager.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = "owner")
    @Query("select p from Project p join ProjectMember pm on pm.project = p " +
            "where pm.user.id = :userId")
    Page<Project> findAccessibleByUserId(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findDetailedById(@Param("id") UUID id);
}
