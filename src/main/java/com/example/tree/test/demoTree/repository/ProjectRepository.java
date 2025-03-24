package com.example.tree.test.demoTree.repository;

import com.example.tree.test.demoTree.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>, TreeFetchRepository {}
