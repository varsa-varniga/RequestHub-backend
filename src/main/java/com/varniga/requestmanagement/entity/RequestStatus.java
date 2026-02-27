package com.varniga.requestmanagement.entity;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "request_status")
public class RequestStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String displayName;

    private String colorCode;

    private Boolean isActive = true;
}

