package pl.poznan.put.pubsub

import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import pl.poznan.put.security.EncryptionSuite
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.exceptions.JedisConnectionException

@Slf4j
class RedisClient {

    public final Map<String, EncryptionSuite> encryptionSuites = new HashMap<>()

    private boolean checkMessageTarget = true
    private final String redisHost
    private Jedis publisher
    private final Map<String, Tuple2<Jedis, JedisPubSub>> channels = new HashMap<>()
    private final Map<String, Thread> subscriberThreads = new HashMap<>()

    RedisClient(String redisHost, boolean checkMessageTarget) {
        this.checkMessageTarget = checkMessageTarget
        this.redisHost = redisHost
        publisher = new Jedis(redisHost, GlobalConstants.REDIS_PORT, GlobalConstants.REDIS_TIMEOUT)
    }

    RedisClient(String redisHost) {
        this.redisHost = redisHost
        publisher = new Jedis(redisHost, GlobalConstants.REDIS_PORT, GlobalConstants.REDIS_TIMEOUT)
    }

    void subscribeChannelWithUnsubscribeAll(String channelName, String currentSubscriber, Closure onMessage) {
        unsubscribe(channelName)
        subscribeChannel(channelName, currentSubscriber, onMessage)
    }

    void subscribeChannel(String channelName, String currentSubscriber, Closure onMessage) throws JedisConnectionException {
        log.info("[${channelName}] subscribing")
        JedisPubSub channel = createPubSub(currentSubscriber, onMessage)

        Jedis subscriber = new Jedis(redisHost, GlobalConstants.REDIS_PORT, GlobalConstants.REDIS_TIMEOUT)
        Thread t = new Thread({
            try {
                subscriber.subscribe(channel, channelName)
            } catch (JedisConnectionException e) {
                log.info("[${channelName}] error occurred: ${e.getMessage()}")
                throw e
            }
        })
        subscriberThreads.put(channelName, t)
        t.start()
        t.setName(t.getName().replaceAll("Thread", "pubsub"))
        sleep(500) /* needed for thread initialization */
        channels.put(channelName, new Tuple2(subscriber, channel))
        log.info("[${channelName}] finished subscription (thread name: ${t.getName()})")
        log.info("subscribed channels: ${channels.keySet()}")
    }

    boolean unsubscribe(String channelName) {
        log.info("[${channelName}] unsubscribing")
        if (channels.containsKey(channelName)) {
            channels[channelName].v2.unsubscribe(channelName)
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

    void publishMessage(String target, Message message) {
        publishMessage(target, target, message)
    }

    void publishMessage(String channelName, String sender, String target, String content) {
        Message message = new Message(sender: sender, target: target, content: content)
        publishMessageFinish(channelName, message.toJSON().toString())
    }

    void publishMessage(String channelName, String target, Message message) {
        message.target = target
        publishMessageFinish(channelName, message.toJSON().toString())
    }

    EncryptionSuite getEncryptionSuite(String username) {
        return encryptionSuites.get(username)
    }

    private void publishMessageFinish(String channelName, String message) {
        log.info("[${channelName}] publishing message: ${message}")
        if (encryptionSuites[channelName.split('_').first()] != null) {
            message = encryptionSuites[channelName.split('_').first()].encrypt(message)
            log.info("[${channelName}] publishing encrypted message: ${message}")
        }
        try {
            publisher.publish(channelName, message)
        } catch (JedisConnectionException ignored) {
            publisher = new Jedis(redisHost, GlobalConstants.REDIS_PORT, 60)
            publisher.publish(channelName, message)
        }
    }

    private JedisPubSub createPubSub(String currentSubscriber, Closure onMessage) {
        return new JedisPubSub() {
            @Override
            void onMessage(String channel, String messageString) {
                if (encryptionSuites[channel.split('_').first()] != null) {
                    log.info("[${channel}] received encrypted message: ${messageString}")
                    messageString = encryptionSuites[channel.split('_').first()].decrypt(messageString)
                }
                Message message = Message.parseJSON(messageString)
                if (message.sender == currentSubscriber) {
                    return
                }
                log.info("[${channel}] received message: ${messageString}")
                if (checkMessageTarget && message.target != currentSubscriber) {
                    log.info("[${channel}] received message to someone else")
                }

                if (onMessage != null) {
                    onMessage(channel, message)
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
    }

}
