package com.example.tree.test.demoTree.entities;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true)
public class Project {
    @Id
    private UUID id;

    private String name;

    private List<Project> children;
}
