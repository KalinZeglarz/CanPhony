package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import pl.poznan.put.pubsub.RedisClient

@Slf4j
class SubPubManager {

    private static String redisHost = "localhost"
    private static RedisClient redisClient = new RedisClient(redisHost)

    static String getRedisHost() {
        return redisHost
    }

    static RedisClient getRedisClient() {
        return redisClient
    }

}
