package com.reuben.pastcare_spring.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Fellowship extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "fellowships")
    private List<Member> members;

    @ManyToMany(mappedBy = "fellowships")
    private List<User> users;
}
