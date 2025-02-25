package org.engicodes.userservice.redis;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)  // Ensures Spring Context is loaded in JUnit 5
public class SpringRedisTest {

    @Autowired  // Use @Autowired to inject RedisTemplate
    private ReactiveStringRedisTemplate redisTemplate;

    @Test
    void testRedisConnection() {
        assertNotNull(redisTemplate, "🔍 RedisTemplate should be autowired correctly!");

        // Key-Value for testing
        String key = "testingKey";
        String value = "testingValue";

        // Set key-value pair in Redis
        redisTemplate.opsForValue().set(key, value)
                .as(StepVerifier::create) // Use StepVerifier to handle Mono
                .expectNext(true) // Redis `set` should return true
                .verifyComplete();

        // Retrieve the value and validate
        redisTemplate.opsForValue().get(key)
                .as(StepVerifier::create)
                .expectNext(value)
                .verifyComplete();
    }
}
