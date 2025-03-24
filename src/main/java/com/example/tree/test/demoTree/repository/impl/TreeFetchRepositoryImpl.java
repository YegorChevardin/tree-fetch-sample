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
              WITH\s
                                  -- First get all projects with users
                                  projects_with_users AS (
                                      SELECT DISTINCT project_id\s
                                      FROM users
                                  ),
                                  -- Get paginated root projects that qualify
                                  paginated_roots AS (
                                      SELECT p.id
                                      FROM projects p
                                      WHERE p.parent_id IS NULL
                                      AND (
                                          -- Root has users directly
                                          p.id IN (SELECT project_id FROM projects_with_users)
                                          OR
                                          -- Or has children with users
                                          EXISTS (
                                              SELECT 1 FROM projects child
                                              WHERE child.parent_id = p.id
                                              AND child.id IN (SELECT project_id FROM projects_with_users)
                                          )
                                          OR
                                          -- Or has grandchildren with users
                                          EXISTS (
                                              SELECT 1 FROM projects child
                                              JOIN projects grandchild ON grandchild.parent_id = child.id
                                              WHERE child.parent_id = p.id
                                              AND grandchild.id IN (SELECT project_id FROM projects_with_users)
                                          )
                                      )
                                      ORDER BY p.name
                                      LIMIT :limit OFFSET :offset
                                  )
                                  
                                  -- Get complete 3-level hierarchy for paginated roots
                                  SELECT p.*
                                  FROM projects p
                                  WHERE\s
                                      -- Include the paginated roots
                                      p.id IN (SELECT id FROM paginated_roots)
                                     \s
                                      OR
                                      -- Include their direct children that have users or have children with users
                                      (
                                          p.parent_id IN (SELECT id FROM paginated_roots)
                                          AND (
                                              p.id IN (SELECT project_id FROM projects_with_users)
                                              OR EXISTS (
                                                  SELECT 1 FROM projects child
                                                  WHERE child.parent_id = p.id
                                                  AND child.id IN (SELECT project_id FROM projects_with_users)
                                              )
                                          )
                                      )
                                     \s
                                      OR
                                      -- Include grandchildren that have users
                                      (
                                          p.parent_id IN (
                                              SELECT id FROM projects\s
                                              WHERE parent_id IN (SELECT id FROM paginated_roots)
                                          )
                                          AND p.id IN (SELECT project_id FROM projects_with_users)
                                      )
                                  ORDER BY\s
                                      CASE WHEN parent_id IS NULL THEN id ELSE parent_id END,
                                      COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'),
                                      name;
        """;

        Query queryResult = entityManager.createNativeQuery(query, ProjectEntity.class)
                .setParameter("limit", limit)
                .setParameter("offset", offset);

        List<ProjectEntity> result = queryResult.getResultList();

        return new PageImpl<>(result, pageable, result.size());
    }
}
