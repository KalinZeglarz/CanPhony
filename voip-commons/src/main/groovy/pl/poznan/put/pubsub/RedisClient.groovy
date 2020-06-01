package pl.poznan.put.pubsub

import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.exceptions.JedisConnectionException

@Slf4j
class RedisClient {

    protected final String redisHost
    protected final Jedis publisher
    protected final Map<String, Tuple2<Jedis, JedisPubSub>> channels = new HashMap<>()
    protected final Map<String, Thread> subscriberThreads = new HashMap<>()

    RedisClient(String redisHost) {
        this.redisHost = redisHost
        publisher = new Jedis(redisHost, GlobalConstants.REDIS_PORT, 15)
    }

    void subscribeChannel(String channelName, Closure onMessage) {
        log.info("[${channelName}] subscribing")
        if (channels.containsKey(channelName)) {
            unsubscribe(channelName)
        }
        JedisPubSub channel = new JedisPubSub() {
            @Override
            void onMessage(String channel, String messageString) {
                log.info("[${channel}] received message: ${messageString}")
                if (onMessage != null) {
                    onMessage(channel, messageString)
                } else {
                    log.warn("[${channel}] empty onMessage callback")
                }
            }

            @Override
            void onSubscribe(String channel, int subscribedChannels) {
            }

            @Override
            void onUnsubscribe(String channel, int subscribedChannels) {
            }
        }

        Jedis subscriber = new Jedis(redisHost, GlobalConstants.REDIS_PORT, 15)
        Thread t = new Thread({
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    subscriber.subscribe(channel, channelName)
                } catch (JedisConnectionException e) {
                    log.info("[${channelName}] error occurred: ${e.getMessage()}")
                }
            }
        })
        subscriberThreads.put(channelName, t)
        t.start()
        t.setName(t.getName().replaceAll('Thread', 'pubsub'))
        sleep(500) /* needed for thread initialization */
        channels.put(channelName, new Tuple2(channel, subscriber))
        log.info("[${channelName}] finished subscription (thread name: ${t.getName()})")
        log.info("subscribed channels: ${channels.keySet()}")
    }

    boolean unsubscribe(int channelName) {
        unsubscribe(channelName.toString())
    }

    boolean unsubscribe(String channelName) {
        log.info("[${channelName}] unsubscribing")
        if (channels.containsKey(channelName)) {
            channels.remove(channelName)
            subscriberThreads.get(channelName).interrupt()
            log.info("[${channelName}] finished unsubscription")
            log.info("subscribed channels: ${channels.keySet()}")
            return true
        }
        log.info("[${channelName}] unsubscription failure")
        log.info("subscribed channels: ${channels.keySet()}")
        return false
    }

    void publishMessage(int channelName, Message message) {
        publishMessage(channelName.toString(), message)
    }

    void publishMessage(String channelName, Message message) {
        log.info("[${channelName}] publishing message: ${message.toJSON().toString()}")
        publisher.publish(channelName, message.toJSON().toString())
    }

}
