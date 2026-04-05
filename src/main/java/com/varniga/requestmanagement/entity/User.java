package com.varniga.requestmanagement.entity;

import com.varniga.requestmanagement.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ✔ Soft delete instead of hard delete
    private Boolean active = true;

    // OPTIONAL: if you want relation mapping (important for real systems)
    // Uncomment ONLY if you have Request entity mapped properly

    /*
    @OneToMany(mappedBy = "createdBy")
    private List<Request> requests;
    */

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}