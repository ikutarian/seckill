package com.okada.seckill.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.okada.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }


    private static final String KEY_BASE = "seckill:";
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId) {
        // redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = KEY_BASE + seckillId;
                // redis并没有实现内部序列化操作，所以需要自己实现序列化
                // get->byte[] -> 反序列化 -> Object(Seckill)
                // 所以需要自己实现序列化的方式：ProtocolBuffer
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    Seckill seckill = schema.newMessage();  // 空对象
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    // seckill 被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        // 把对象序列化，存入redis
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = KEY_BASE + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                // 超时缓存
                int timeout = 60 * 60; // 设置缓存时间为一小时
                return jedis.setex(key.getBytes(), timeout, bytes);
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
