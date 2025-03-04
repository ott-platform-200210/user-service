package org.engicodes.userservice.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private static final Duration CACHE_MEMORY_TIME = Duration.ofMinutes(10);

}
