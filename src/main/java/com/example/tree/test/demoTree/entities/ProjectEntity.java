package com.example.tree.test.demoTree.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.catalina.User;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "projects")
public class ProjectEntity {
    @Id
    private UUID id;

    private String name;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "root_id")
    private UUID rootId;

    @Column(name = "root_name")
    private String rootName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private ProjectEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_id", insertable = false, updatable = false)
    private ProjectEntity root;
}
