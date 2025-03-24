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
                                              -- Get paginated root projects that have users or descendants with users
                                              qualified_roots AS (
                                                  SELECT p.id
                                                  FROM projects p
                                                  WHERE p.parent_id IS NULL
                                                  AND EXISTS (
                                                      WITH RECURSIVE user_descendants AS (
                                                          -- Start with the root project
                                                          SELECT id, parent_id FROM projects WHERE id = p.id
                                                          UNION
                                                          -- Get all descendants
                                                          SELECT child.id, child.parent_id FROM projects child
                                                          JOIN user_descendants ud ON child.parent_id = ud.id
                                                      )
                                                      -- Check if any descendant has users
                                                      SELECT 1 FROM user_descendants ud
                                                      JOIN users u ON u.project_id = ud.id
                                                      LIMIT 1
                                                  )
                                                  ORDER BY p.name
                                                  LIMIT :limit OFFSET :offset
                                              ),
                                              
                                              -- Get complete qualified hierarchy for paginated roots
                                              project_hierarchy AS (
                                                  -- Start with paginated roots (must be included)
                                                  SELECT p.*
                                                  FROM projects p
                                                  JOIN qualified_roots qr ON p.id = qr.id
                                                 \s
                                                  UNION ALL
                                                 \s
                                                  -- Recursively get qualified descendants
                                                  SELECT p.*
                                                  FROM projects p
                                                  JOIN project_hierarchy ph ON p.parent_id = ph.id
                                                  WHERE EXISTS (
                                                      -- Either has users directly
                                                      SELECT 1 FROM users u WHERE u.project_id = p.id
                                                      UNION
                                                      -- Or has descendants with users
                                                      SELECT 1 FROM (
                                                          WITH RECURSIVE user_children AS (
                                                              SELECT id FROM projects WHERE parent_id = p.id
                                                              UNION
                                                              SELECT child.id FROM projects child
                                                              JOIN user_children uc ON child.parent_id = uc.id
                                                          )
                                                          SELECT 1 FROM user_children uc
                                                          JOIN users u ON u.project_id = uc.id
                                                          LIMIT 1
                                                      ) AS subquery
                                                  )
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
