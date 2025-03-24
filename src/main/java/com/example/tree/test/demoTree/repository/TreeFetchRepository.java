package com.example.tree.test.demoTree.repository;

import com.example.tree.test.demoTree.entities.ProjectEntity;
import org.springframework.data.domain.Page;

public interface TreeFetchRepository {
    Page<ProjectEntity> findProjectTreePaginated(int limit, int offset);
}
