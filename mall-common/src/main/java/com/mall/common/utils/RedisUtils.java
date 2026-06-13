package com.mall.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ============================= common ============================

    /**
     * 指定缓存失效时间
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis判断key是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除缓存
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    // ============================ String =============================

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取缓存失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 设置缓存
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis设置缓存失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置缓存失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 递增
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /**
     * 递减
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        Long result = redisTemplate.opsForValue().increment(key, -delta);
        return result != null ? result : 0;
    }

    // ================================ Map =================================

    /**
     * 获取Hash
     */
    public Object hget(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            log.error("Redis获取Hash失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Hash的所有键值对
     */
    public Map<Object, Object> hmget(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis获取Hash所有键值对失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 设置Hash
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("Redis设置Hash失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 设置Hash并设置过期时间
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置Hash失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 向一张Hash表中放入数据
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("Redis设置Hash失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 向一张Hash表中放入数据并设置过期时间
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置Hash失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除Hash表中的值
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断Hash表中是否有该项的值
     */
    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            log.error("Redis判断Hash表中是否有该项的值失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Hash递增
     */
    public double hincr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(key, item, by);
        } catch (Exception e) {
            log.error("Redis Hash递增失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Hash递减
     */
    public double hdecr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(key, item, -by);
        } catch (Exception e) {
            log.error("Redis Hash递减失败: {}", e.getMessage());
            return 0;
        }
    }

    // ============================ Set =============================

    /**
     * 获取Set
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis获取Set失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Set中是否存在该值
     */
    public boolean sHasKey(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis判断Set中是否存在该值失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将数据放入Set
     */
    public long sSet(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis将数据放入Set失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 将数据放入Set并设置过期时间
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis将数据放入Set失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 获取Set的长度
     */
    public long sGetSetSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Redis获取Set的长度失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 移除Set中值为value的元素
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis移除Set中元素失败: {}", e.getMessage());
            return 0;
        }
    }

    // ============================ List =============================

    /**
     * 获取List
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis获取List失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取List的长度
     */
    public long lGetListSize(String key) {
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Redis获取List的长度失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 通过索引获取List中的值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("Redis通过索引获取List中的值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将数据放入List
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis将数据放入List失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将数据放入List并设置过期时间
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis将数据放入List失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将多个数据放入List
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis将多个数据放入List失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将多个数据放入List并设置过期时间
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis将多个数据放入List失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据索引修改List中的某条数据
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("Redis修改List中的某条数据失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 移除N个值为value的元素
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove != null ? remove : 0;
        } catch (Exception e) {
            log.error("Redis移除List中元素失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 原子性设置缓存（仅当key不存在时）
     * 使用 SET key value NX EX time 命令
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（秒）
     * @return true-设置成功（获取到锁），false-key已存在
     */
    public boolean setIfAbsent(String key, Object value, long time) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis setIfAbsent失败: {}", e.getMessage());
            return false;
        }
    }

}