package org.engicodes.userservice.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.host}")
    private String host;
    private final ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void configurationPropertiesOnConsole() {
        ReactiveStringRedisTemplate redisTemplate = applicationContext.getBean(ReactiveStringRedisTemplate.class);
        String key = "testKey";
        String value = "Redis is up and running!";
        System.out.println("🎯 Redis server details: host:" + host + " | port:" + port);
        redisTemplate.opsForValue().set(key, value)
                .doOnSuccess(success -> System.out.println("✅ Redis SET Success: " + success))
                .flatMap(ignore -> redisTemplate.opsForValue().get(key))
                .doOnNext(retrievedValue -> {
                    System.out.println("✅ Redis GET Success: " + retrievedValue);
                    // ✅ Publish event after successful Redis verification
                    applicationContext.publishEvent(new RedisReadyEvent(this));
                })
                .doOnError(error -> System.err.println("❌ Redis Error: " + error.getMessage()))
                .subscribe();
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfigurations = new RedisStandaloneConfiguration(host, port);
        LettuceClientConfiguration lettuceConfiguration = LettuceClientConfiguration.builder().build();
        return new LettuceConnectionFactory(redisConfigurations, lettuceConfiguration);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, redisSerializerContext());
    }

    private RedisSerializationContext<String, String> redisSerializerContext() {
        return RedisSerializationContext.<String, String>newSerializationContext(new StringRedisSerializer())
                .build();
    }

    public static class RedisReadyEvent { // ✅ Define custom event
        public RedisReadyEvent(Object __) {
            System.out.println("🎯 Redis Event published!");
        }
    }
}