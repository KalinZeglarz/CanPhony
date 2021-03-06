package pl.poznan.put

import groovy.transform.ToString
import org.json.JSONObject
import pl.poznan.put.pubsub.RedisClient
import pl.poznan.put.security.EncryptionSuite
import pl.poznan.put.structures.JSONable

@ToString
class ClientConfig implements JSONable {

    /* used at runtime */
    VoipHttpClient httpClient = null
    RedisClient redisClient = null
    EncryptionSuite encryptionSuite = null
    PhoneCallClient phoneCallClient = null
    String currentCallUsername = null

    /* saved to file */
    String serverAddress = ""
    String serverPort = ""
    String username = ""

    static ClientConfig parseJSON(String text) {
        return parseJSON(new JSONObject(text))
    }

    static ClientConfig parseJSON(JSONObject object) {
        String serverAddress = object.getString("serverAddress")
        String serverPort = object.getString("serverPort")
        String username = object.getString("username")
        return new ClientConfig(serverAddress: serverAddress, serverPort: serverPort, username: username)
    }

    @Override
    JSONObject toJSON() {
        return new JSONObject()
                .put("serverAddress", serverAddress)
                .put("serverPort", serverPort)
                .put("username", username)
    }

}
