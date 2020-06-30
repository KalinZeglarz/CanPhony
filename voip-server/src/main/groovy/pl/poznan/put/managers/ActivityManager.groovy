package pl.poznan.put.managers

import groovy.util.logging.Slf4j
import pl.poznan.put.pubsub.Message
import pl.poznan.put.structures.UserStatus
import redis.clients.jedis.exceptions.JedisException

@Slf4j
class ActivityManager {

    private static Map<String, Integer> activeUsers = new HashMap<>()
    private static Thread checkerThread

    static void start() {
        checkerThread = new Thread({
            while (!Thread.currentThread().isInterrupted()) {
                for (String username in DatabaseManager.getUserList().keySet()) {
                    if (!activeUsers.containsKey(username) || activeUsers.get(username) >= 3) {
                        removeUser(username)
                        continue
                    }
                    userBeacon(username)
                }
                sleep(5000)
            }
        })
        checkerThread.start()
        PubSubManager.redisClient
        log.info("Activity manager started")
    }

    static void addUser(String username) {
        activeUsers.put(username, 0)
    }

    static void removeUser(String username) {
        DatabaseManager.setUserStatus(username, UserStatus.INACTIVE)
        if (!activeUsers.containsKey(username)) {
            return
        }
        activeUsers.remove(username)
        PubSubManager.redisClient.unsubscribe(username)
        PubSubManager.redisClient.unsubscribe(username + "_beacon")
        PhoneCallManager.removePhoneCall(username)
    }

    private static userBeacon(String username) {
        Thread beaconThread = new Thread({
            boolean okReceived = false
            try {
                PubSubManager.redisClient.subscribeChannelWithUnsubscribeAll(username + "_beacon", "server") { String channelName, Message message ->
                    okReceived = message.content == "OK!"
                }
                PubSubManager.redisClient.publishMessage(username + "_beacon", "server", username, "beacon")
                sleep(5000)
                PubSubManager.redisClient.unsubscribe(username + "_beacon")
                if (!activeUsers.containsKey(username)) {
                    return false
                }
                if (!okReceived) {
                    activeUsers.put(username, activeUsers.get(username) + 1)
                } else {
                    activeUsers.put(username, 0)
                }
            } catch (JedisException ignored) {
            }
        })
        beaconThread.start()
    }

}
