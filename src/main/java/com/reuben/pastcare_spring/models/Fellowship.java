package com.reuben.pastcare_spring.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Fellowship extends TenantBaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "fellowships")
    private List<Member> members;

    @ManyToMany(mappedBy = "fellowships")
    private List<User> users;
}
