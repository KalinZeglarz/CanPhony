package pl.poznan.put.structures

import groovy.transform.ToString
import org.json.JSONObject
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.pubsub.RedisClient

@ToString
class ClientConfig implements JSONable {

    /* used at runtime */
    VoipHttpClient httpClient = null
    RedisClient redisClient = null
    PhoneCallClient phoneCallClient = null
    Integer currentSessionId = null
    String currentCallUsername = null

    /* saved to file */
    String serverAddress = ""
    String serverPort = ""
    String username = ""

    static ClientConfig parseJSON(String text) {
        return parseJSON(new JSONObject(text))
    }

    static ClientConfig parseJSON(JSONObject object) {
        String serverAddress = object.getString('serverAddress')
        String serverPort = object.getString('serverPort')
        String username = object.getString('username')
        return new ClientConfig(serverAddress: serverAddress, serverPort: serverPort, username: username)
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put('serverAddress', serverAddress)
                .put('serverPort', serverPort)
                .put('username', username)
    }

}
