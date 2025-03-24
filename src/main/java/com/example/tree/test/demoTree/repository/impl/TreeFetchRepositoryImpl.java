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
               WITH RECURSIVE
                         -- First identify all root projects that qualify (have users or descendants with users)
                         qualified_roots AS (
                             SELECT p.id, p.name
                             FROM projects p
                             WHERE p.parent_id IS NULL
                             AND (
                                 -- Root has direct users
                                 EXISTS (SELECT 1 FROM users u WHERE u.project_id = p.id)
                                 OR
                                 -- Or has any descendant with users (using efficient path check)
                                 EXISTS (
                                     WITH RECURSIVE descendant_check AS (
                                         SELECT id FROM projects WHERE parent_id = p.id
                                         UNION
                                         SELECT child.id FROM projects child
                                         JOIN descendant_check dc ON child.parent_id = dc.id
                                     )
                                     SELECT 1 FROM descendant_check dc
                                     JOIN users u ON u.project_id = dc.id
                                     LIMIT 1
                                 )
                             )
                             ORDER BY p.name
                         ),
                         
                         -- Get paginated root IDs only (this is where pagination happens)
                         paginated_root_ids AS (
                             SELECT id FROM qualified_roots
                             LIMIT :limit OFFSET :offset
                         ),
                         
                         -- Now get complete hierarchy for these paginated roots
                         complete_hierarchy AS (
                             -- Start with the paginated roots
                             SELECT p.*
                             FROM projects p
                             JOIN paginated_root_ids pri ON p.id = pri.id
                            \s
                             UNION ALL
                            \s
                             -- Recursively get all descendants that either:
                             -- 1. Have users directly, OR
                             -- 2. Are ancestors of projects with users
                             SELECT p.*
                             FROM projects p
                             JOIN complete_hierarchy ch ON p.parent_id = ch.id
                             WHERE (
                                 -- Project has users directly
                                 EXISTS (SELECT 1 FROM users u WHERE u.project_id = p.id)
                                 OR
                                 -- Or leads to projects with users
                                 EXISTS (
                                     WITH RECURSIVE descendant_check AS (
                                         SELECT id FROM projects WHERE parent_id = p.id
                                         UNION
                                         SELECT child.id FROM projects child
                                         JOIN descendant_check dc ON child.parent_id = dc.id
                                     )
                                     SELECT 1 FROM descendant_check dc
                                     JOIN users u ON u.project_id = dc.id
                                     LIMIT 1
                                 )
                             )
                         )
                         
                         -- Final results with proper ordering
                         SELECT * FROM complete_hierarchy
                         ORDER BY\s
                             CASE WHEN parent_id IS NULL THEN id ELSE parent_id END,
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
