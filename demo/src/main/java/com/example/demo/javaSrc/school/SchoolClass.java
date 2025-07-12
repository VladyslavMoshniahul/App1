package com.example.demo.javaSrc.school;

import jakarta.persistence.*;

@Entity
@Table(name = "classes")
public class SchoolClass {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String name;

    public SchoolClass() {}

    public SchoolClass(Long schoolId, String name) {
        this.schoolId = schoolId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
