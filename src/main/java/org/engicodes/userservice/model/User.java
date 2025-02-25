package org.engicodes.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.engicodes.userservice.util.BaseEntity;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(unique = true)
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only alphabets and numbers")
    private String userName;
    @Column(unique = true)
    @Email(message = "email is not correct format!")
    private String email;
    @Min(value = 18, message = "Age should be minimum 18")
    @Max(value = 80, message = "Age should be maximum 80")
    private Integer age;
    @Enumerated(EnumType.STRING)
    private Roles role;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    public Set<String> getAuthorities() {
        return role.getAuthorities();
    }
}