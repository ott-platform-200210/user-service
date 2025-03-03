package org.engicodes.userservice.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.engicodes.userservice.util.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
@Data
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    private UUID id;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only alphabets and numbers")
    private String userName;
    @NotBlank
    private String fullName;
    @Email(message = "email is not correct format!")
    private String email;
    @Min(value = 18, message = "Age should be minimum 18")
    @Max(value = 80, message = "Age should be maximum 80")
    private Integer age;
    private Roles role;
    private SubscriptionStatus subscriptionStatus;
    private boolean emailVerified;
    public Set<String> getAuthorities() {
        return role.getAuthorities();
    }
}