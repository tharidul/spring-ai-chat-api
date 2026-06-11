package com.tharidulakmal.springaichatapi.entity.auth;

import com.tharidulakmal.springaichatapi.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false,length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean enabled = true;
}
