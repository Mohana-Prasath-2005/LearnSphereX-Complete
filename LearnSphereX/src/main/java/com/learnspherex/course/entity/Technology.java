package com.learnspherex.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technologies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String version;

    @Column(length = 1000)
    private String description;
}