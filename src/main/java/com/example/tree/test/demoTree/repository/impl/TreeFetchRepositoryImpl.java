package com.example.tree.test.demoTree.repository.impl;

import com.example.tree.test.demoTree.entities.ProjectEntity;
import com.example.tree.test.demoTree.repository.TreeFetchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TreeFetchRepositoryImpl implements TreeFetchRepository {
    private final EntityManager entityManager;

    @Override
    public Page<ProjectEntity> findProjectTreePaginated(int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        String query = """
                WITH RECURSIVE\s
                            -- First find all projects that have users (at any level)
                            projects_with_users AS (
                                SELECT DISTINCT p.id
                                FROM projects p
                                JOIN users u ON u.project_id = p.id
                            ),
                            
                            -- Find all paths from roots to projects with users
                            paths_to_users AS (
                                -- Start with projects that have users
                                SELECT p.id, p.parent_id, ARRAY[p.id] AS path
                                FROM projects p
                                JOIN projects_with_users pu ON p.id = pu.id
                               \s
                                UNION
                               \s
                                -- Recursively find all ancestors
                                SELECT p.id, p.parent_id, p.id || pt.path
                                FROM projects p
                                JOIN paths_to_users pt ON p.id = pt.parent_id
                            ),
                            
                            -- Get distinct projects that are either:
                            -- 1. Projects with users, or
                            -- 2. Their ancestors (to maintain tree structure)
                            all_qualified_projects AS (
                                SELECT DISTINCT unnest(path) AS project_id
                                FROM paths_to_users
                            ),
                            
                            -- Get paginated root projects that are in the qualified set
                            paginated_roots AS (
                                SELECT p.id
                                FROM projects p
                                JOIN all_qualified_projects aqp ON p.id = aqp.project_id
                                WHERE p.parent_id IS NULL
                                ORDER BY p.name
                                LIMIT :limit OFFSET :offset
                            ),
                            
                            -- Get complete qualified hierarchy for paginated roots
                            project_hierarchy AS (
                                -- Start with paginated roots (must be included as they're qualified)
                                SELECT p.*
                                FROM projects p
                                JOIN paginated_roots pr ON p.id = pr.id
                               \s
                                UNION ALL
                               \s
                                -- Recursively get qualified descendants
                                SELECT p.*
                                FROM projects p
                                JOIN project_hierarchy ph ON p.parent_id = ph.id
                                JOIN all_qualified_projects aqp ON p.id = aqp.project_id
                            )
                            
                            -- Final select with original ordering
                            SELECT * FROM project_hierarchy
                            ORDER BY\s
                                CASE WHEN parent_id IS NULL THEN id ELSE parent_id END,\s
                                COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'),\s
                                name;
        """;

        Query queryResult = entityManager.createNativeQuery(query, ProjectEntity.class)
                .setParameter("limit", limit)
                .setParameter("offset", offset);

        List<ProjectEntity> result = queryResult.getResultList();

        return new PageImpl<>(result, pageable, result.size());
    }
}
