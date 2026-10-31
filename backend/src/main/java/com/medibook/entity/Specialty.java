package com.medibook.entity;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="specialty") public class Specialty {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=80) private String name;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();@Column(nullable=false) private Instant updatedAt=Instant.now();@PreUpdate void update(){updatedAt=Instant.now();}
 protected Specialty(){} public Specialty(String name){this.name=name;} public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
}
