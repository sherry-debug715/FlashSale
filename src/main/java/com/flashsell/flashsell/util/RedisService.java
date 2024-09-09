package com.flashsell.flashsell.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
    @Autowired
    private JedisPool jedisPool;
    /**
    *
    * @param key
    * @param value
    */
    public void setValue(String key, Long value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value.toString());
        jedisClient.close();
    }

    public String getValue(String key) {
        Jedis jedisClient = jedisPool.getResource();
        String value = jedisClient.get(key);
        jedisClient.close();
        return value;
    }

    /**
     * checking stock in cache and deduction
     * @param key
     * @return
     * @throws Exception
     */
    public boolean stockDeductValidator(String key) {
        try(Jedis jedisClient = jedisPool.getResource()) {

            String script = 
            "if redis.call('exists',KEYS[1]) == 1 then\n" +
            "   local stock = tonumber(redis.call('get', KEYS[1]))\n" +
            "   if( stock <= 0 ) then\n" +
            "       return -1\n" +
            "   end;\n" +
            "   redis.call('decr',KEYS[1]);\n" +
            "   return stock - 1;\n" +
            " end;\n" +
            " return -1;";

            Long stock = (Long) jedisClient.eval(script, Collections.singletonList(key), Collections.emptyList());

            if (stock < 0) {
                System.out.println("Out of stock");
            } else {
                System.out.println("Congratulations, the flash sale was successful.");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("stock deduction failed" + throwable.toString());
            return false;
        }
    }
}
