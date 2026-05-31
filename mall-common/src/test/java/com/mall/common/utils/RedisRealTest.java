package com.mall.common.utils;

import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisRealTest {

    private static RedisUtils redisUtils;
    private static RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    static void setUp() {
        // 配置Redis连接
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("dingchunru.cn");
        config.setPort(6379);
        config.setPassword("7259.Dcr.");

        // 创建连接工厂
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        // 创建RedisTemplate
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();

        // 创建RedisUtils并设置redisTemplate
        redisUtils = new RedisUtils();
        try {
            java.lang.reflect.Field field = RedisUtils.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(redisUtils, redisTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        // 清理测试数据
        redisUtils.del("test:key");
        redisUtils.del("test:expire:key");
        redisUtils.del("test:counter");
        redisUtils.del("test:hash");
        redisUtils.del("test:set");
        redisUtils.del("test:list");
    }

    @Test
    @Order(1)
    void testSetAndGet() {
        String key = "test:key";
        String value = "testValue";

        // 测试set
        boolean setResult = redisUtils.set(key, value);
        assertThat(setResult).isTrue();

        // 测试get
        Object retrieved = redisUtils.get(key);
        assertThat(retrieved).isEqualTo(value);
    }

    @Test
    @Order(2)
    void testSetWithExpire() {
        String key = "test:expire:key";
        String value = "testValue";
        long expire = 60L;

        boolean result = redisUtils.set(key, value, expire);
        assertThat(result).isTrue();

        // 验证过期时间
        long ttl = redisUtils.getExpire(key);
        assertThat(ttl).isLessThanOrEqualTo(expire);
    }

    @Test
    @Order(3)
    void testHasKey() {
        String key = "test:key";

        boolean exists = redisUtils.hasKey(key);
        assertThat(exists).isTrue();
    }

    @Test
    @Order(4)
    void testDelete() {
        String key = "test:key";

        redisUtils.del(key);

        boolean exists = redisUtils.hasKey(key);
        assertThat(exists).isFalse();
    }

    @Test
    @Order(5)
    void testIncrAndDecr() {
        String key = "test:counter";
    
        redisUtils.set(key, 0);
    
        long incrResult = redisUtils.incr(key, 5);
        assertThat(incrResult).isEqualTo(5L);
    
        long decrResult = redisUtils.decr(key, 2);
        assertThat(decrResult).isEqualTo(3L);

        Object value = redisUtils.get(key);
        // 修复：将值转换为Number再比较
        assertThat(value).isInstanceOf(Number.class);
        if (value instanceof Long) {
            assertThat((Long) value).isEqualTo(3L);
        } else if (value instanceof Integer) {
            assertThat((Integer) value).isEqualTo(3);
        } else {
            assertThat(((Number) value).longValue()).isEqualTo(3L);
        }
    }

    @Test
    @Order(6)
    void testExpire() {
        String key = "test:expire:key";

        boolean result = redisUtils.expire(key, 30L);
        assertThat(result).isTrue();

        long ttl = redisUtils.getExpire(key);
        assertThat(ttl).isLessThanOrEqualTo(30L);
    }

    @Test
    @Order(7)
    void testHashOperations() {
        String key = "test:hash";
        String field1 = "field1";
        String field2 = "field2";
        String value1 = "value1";
        String value2 = "value2";

        // 测试hset
        boolean hset1 = redisUtils.hset(key, field1, value1);
        assertThat(hset1).isTrue();

        boolean hset2 = redisUtils.hset(key, field2, value2);
        assertThat(hset2).isTrue();

        // 测试hget
        Object retrieved1 = redisUtils.hget(key, field1);
        assertThat(retrieved1).isEqualTo(value1);

        Object retrieved2 = redisUtils.hget(key, field2);
        assertThat(retrieved2).isEqualTo(value2);

        // 测试hHasKey
        boolean hasField1 = redisUtils.hHasKey(key, field1);
        assertThat(hasField1).isTrue();

        boolean hasField3 = redisUtils.hHasKey(key, "field3");
        assertThat(hasField3).isFalse();

        // 测试hdel
        redisUtils.hdel(key, field1);
        hasField1 = redisUtils.hHasKey(key, field1);
        assertThat(hasField1).isFalse();
    }

    @Test
    @Order(8)
    void testSetOperations() {
        String key = "test:set";
        String member1 = "member1";
        String member2 = "member2";

        // 测试sSet
        long addCount = redisUtils.sSet(key, member1, member2);
        assertThat(addCount).isEqualTo(2);

        // 测试sHasKey
        boolean hasMember1 = redisUtils.sHasKey(key, member1);
        assertThat(hasMember1).isTrue();

        boolean hasMember3 = redisUtils.sHasKey(key, "member3");
        assertThat(hasMember3).isFalse();

        // 测试sGetSetSize
        long size = redisUtils.sGetSetSize(key);
        assertThat(size).isEqualTo(2);

        // 测试sGet
        java.util.Set<Object> members = redisUtils.sGet(key);
        assertThat(members).contains(member1, member2);

        // 测试setRemove
        long removeCount = redisUtils.setRemove(key, member1);
        assertThat(removeCount).isEqualTo(1);

        size = redisUtils.sGetSetSize(key);
        assertThat(size).isEqualTo(1);
    }

    @Test
    @Order(9)
    void testListOperations() {
        String key = "test:list";
        String value1 = "value1";
        String value2 = "value2";

        // 测试lSet
        boolean result1 = redisUtils.lSet(key, value1);
        assertThat(result1).isTrue();

        boolean result2 = redisUtils.lSet(key, value2);
        assertThat(result2).isTrue();

        // 测试lGetListSize
        long size = redisUtils.lGetListSize(key);
        assertThat(size).isEqualTo(2);

        // 测试lGetIndex
        Object retrieved1 = redisUtils.lGetIndex(key, 0);
        assertThat(retrieved1).isEqualTo(value1);

        Object retrieved2 = redisUtils.lGetIndex(key, 1);
        assertThat(retrieved2).isEqualTo(value2);

        // 测试lGet
        java.util.List<Object> list = redisUtils.lGet(key, 0, -1);
        assertThat(list).hasSize(2);
        assertThat(list).contains(value1, value2);

        // 测试lRemove
        long removeCount = redisUtils.lRemove(key, 1, value1);
        assertThat(removeCount).isEqualTo(1);

        size = redisUtils.lGetListSize(key);
        assertThat(size).isEqualTo(1);
    }
}