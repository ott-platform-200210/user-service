package org.engicodes.userservice.util;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.Instant;

@Getter
@Setter
public abstract class BaseEntity {
    @CreatedDate
    private Instant createdAt;  // ✅ Stores when record is created

    @LastModifiedDate
    private Instant updatedAt;  // ✅ Stores when record is updated
}
