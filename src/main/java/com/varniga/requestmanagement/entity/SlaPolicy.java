package com.varniga.requestmanagement.entity;

import com.varniga.requestmanagement.enums.Urgency;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_policy",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"request_type", "urgency"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_type", nullable = false)
    private String requestType;   // IT, LEAVE, EXPENSE, etc

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency;

    @Column(name = "sla_hours", nullable = false)
    private Long slaHours;
}