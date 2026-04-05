package com.varniga.requestmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestType {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


    @Column(nullable = false, unique = true)
    private String code;  // THIS is what you're querying
        private String name;


    @OneToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;
}