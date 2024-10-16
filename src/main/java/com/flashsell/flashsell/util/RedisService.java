package com.flashsell.flashsell.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
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

    /**
     *
     * @param key
     * @param value
     */
    public void setValue(String key, String value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value);
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
                return false;
            } else {
                System.out.println("Congratulations, the flash sale was successful.");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("stock deduction failed" + throwable.toString());
            return false;
        }
    }

    /*
    * 超时未支付 Redis 库存回滚
    * Time out, payment incomplete, add the stock back to redis
    * @param key
    */
    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }

    /*
    * Check if user is in the purchase buy bang list
    * @param activityId * @param userId
    * @return
    */
    public boolean isInLimitMember(long activityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        boolean sismember = jedisClient.sismember("seckillActivity_users:" +
        activityId, String.valueOf(userId));
        jedisClient.close();
        log.info("userId:{} activityId:{} int the list of buyers:{}", userId, activityId, sismember);
        return sismember;
    }

    /**
    * add user to buyers list in redis 
    *
    * @param activityId * @param userId
    */
    public void addLimitMember(long activityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.sadd("seckillActivity_users:" + activityId, String.valueOf(userId));
        jedisClient.close();
    }

    /**
    * remove user from buyers list in redis
    *
    * @param activityId * @param userId
    */
    public void removeLimitMember(long activityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.srem("seckillActivity_users:" + activityId, String.valueOf(userId));
        log.info("userId:{} activityId:{} removed from the list of buyers", userId, activityId);
        jedisClient.close();
    }

    /**
    * get distributed lock
    * @param lockKey
    * @param requestId * @param expireTime * @return
    */
    public  boolean tryGetDistributedLock(String lockKey, String requestId, int
expireTime) {
        Jedis jedisClient = jedisPool.getResource();
        /*
         * two options for nxxx:
         * "NX": not exists, only when key doesn't exist will key value set to redis.
         * "XX": is exists, only when key exist, sest key value to redis
         * two options for "expx":
         * "EX": seconds 
         * "PX": milliseconds
         */
        String result = jedisClient.set(lockKey, requestId, "NX","PX",expireTime);
        jedisClient.close();
        if ("OK".equals(result)) {
            return true;
        }
        return false;
    }

    /**
    * Unlock the lockKey *
    * @param lockKey 
    * @param requestId request id
    * @return boolean: if unlock is successful */
    public boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedisClient = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = (Long) jedisClient.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        jedisClient.close();
        if (result == 1L) {
            return true;
        }
        return false;
    }
}
