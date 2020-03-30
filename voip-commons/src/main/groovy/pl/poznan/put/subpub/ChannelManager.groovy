package pl.poznan.put.subpub

import groovy.util.logging.Log
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

@Log
class ChannelManager {

    private final Jedis publisher
    private final Jedis subscriber
    private final Map<String, JedisPubSub> channels = new HashMap<>()
    private final Map<String, Thread> subscriberThreads = new HashMap<>()

    ChannelManager(String host, int port) {
        publisher = new Jedis(host, port)
        subscriber = new Jedis(host, port)
    }

    void subscribeChannel(String channelName) {
        JedisPubSub channel = new JedisPubSub() {
            @Override
            void onMessage(String channel, String message) {
                log.info("channel " + channel + " has sent a message : " + message)
                Message messageObj = Message.parseJSON(message)
                if (messageObj.getMessageType() == MessageType.SHUTDOWN) {
                    log.info('closing channel')
                    unsubscribe(channel)
                }
            }

            @Override
            void onSubscribe(String channel, int subscribedChannels) {
                log.info("client is Subscribed to channel : " + channel)
                log.info("client is Subscribed to " + subscribedChannels + " no. of channels")
            }

            @Override
            void onUnsubscribe(String channel, int subscribedChannels) {
                log.info("client is Unsubscribed from channel : " + channel)
                log.info("client is Subscribed to " + subscribedChannels + " no. of channels")
            }
        }
        Thread t = new Thread({ subscriber.subscribe(channel, channelName) })
        subscriberThreads.put(channelName, t)
        t.start()
        sleep(1000)
        channels.put(channelName, channel)
    }

    boolean closeChannel(String channelName) {
        if (channels.containsKey(channelName)) {
            publishMessage(channelName, MessageFactory.createMessage(MessageType.SHUTDOWN))
            channels.remove(channelName)
            subscriberThreads.get(channelName).join()
            return true
        }
        return false
    }

    boolean publishMessage(String channelName, Message message) {
        if (channels.containsKey(channelName)) {
            publisher.publish(channelName, message.toJSON().toString())
            return true
        }
        return false
    }

}
