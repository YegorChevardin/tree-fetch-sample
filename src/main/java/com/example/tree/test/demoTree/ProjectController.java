package com.example.tree.test.demoTree;

import com.example.tree.test.demoTree.entities.Project;
import com.example.tree.test.demoTree.entities.ProjectEntity;
import com.example.tree.test.demoTree.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectRepository projectRepository;

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(
            @RequestParam(name = "offset", required = true) Integer offset,
            @RequestParam(name = "limit", required = true) Integer limit
    ) {
        return ResponseEntity.ok(buildProjectTree(projectRepository.findProjectTreePaginated(limit, offset).getContent()));
    }

    private List<Project> buildProjectTree(List<ProjectEntity> projects) {
        var parents = projects.stream().map(ProjectEntity::getParent).filter(Objects::nonNull).toList();
        var projectsWithParents = ListUtils.union(parents, projects);

        var rootProjects = projects.stream().map(project -> {
            if (Objects.isNull(project.getRoot())) {
                return project;
            }
            return project.getRoot();
        }).collect(Collectors.toSet());

        Map<UUID, Set<ProjectEntity>> childrenMap = projectsWithParents.stream()
                .filter(a -> a.getParent() != null)
                .collect(Collectors.groupingBy(a -> a.getParent().getId(), Collectors.toSet()));

        List<Project> result = new ArrayList<>();
        for (ProjectEntity root : rootProjects) {
            result.add(convertToDto(root, childrenMap));
        }
        result.sort(Comparator.comparing(Project::getId));

        return result;
    }

    private Project convertToDto(ProjectEntity project, Map<UUID, Set<ProjectEntity>> childrenMap) {
        var dto = new Project();
        dto.setId(project.getId());
        dto.setName(project.getName());

        List<Project> childrenDtos = new ArrayList<>();
        Set<ProjectEntity> children = childrenMap.getOrDefault(project.getId(), new HashSet<ProjectEntity>());
        if (CollectionUtils.isNotEmpty(children)) {
            for (ProjectEntity child : children) {
                childrenDtos.add(convertToDto(child, childrenMap));
            }
            childrenDtos.sort(Comparator.comparing(Project::getId));
            dto.setChildren(childrenDtos);
        }

        return dto;
    }
}
