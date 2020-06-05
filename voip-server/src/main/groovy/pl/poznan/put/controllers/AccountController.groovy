package pl.poznan.put.controllers

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.poznan.put.GlobalConstants
import pl.poznan.put.managers.DatabaseManager
import pl.poznan.put.managers.PubSubManager
import pl.poznan.put.pubsub.Message
import pl.poznan.put.pubsub.MessageAction
import pl.poznan.put.pubsub.MessageFactory
import pl.poznan.put.security.EncryptionSuite
import pl.poznan.put.structures.AccountStatus
import pl.poznan.put.structures.PasswordPolicy
import pl.poznan.put.structures.StringMessage
import pl.poznan.put.structures.UserStatus
import pl.poznan.put.structures.api.*

import javax.servlet.http.HttpServletRequest

import static pl.poznan.put.GlobalConstants.getDH_POSTFIX
import static pl.poznan.put.structures.AccountStatus.*

@Slf4j
@RestController
@RequestMapping(value = "/account")
class AccountController {

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info('received login request')
        AccountStatus accountStatus = DatabaseManager.checkAccount(loginRequest)
        if (accountStatus != SUCCESS) {
            LoginResponse response = null
            if (accountStatus == AccountStatus.NOT_EXISTS) {
                response = new LoginResponse(message: "Wrong username.")
            } else if (accountStatus == AccountStatus.INCORRECT_PASSWORD) {
                response = new LoginResponse(message: "Wrong password.")
            }
            return new ResponseEntity(response, HttpStatus.CONFLICT)
        }

        DatabaseManager.updateUserAddress(loginRequest.username, request.remoteAddr)
        DatabaseManager.setUserStatus(loginRequest.username, UserStatus.ACTIVE)

        LoginResponse response = new LoginResponse(pubSubHost: PubSubManager.getRedisHost(),
                pubSubPort: GlobalConstants.REDIS_PORT)

        redisKeyExchangeSubscribe(loginRequest.username)
        return new ResponseEntity(response, HttpStatus.CREATED)
    }

    @DeleteMapping(value = "/logout")
    @ResponseBody
    ResponseEntity logout(@RequestParam String username) {
        log.info("received logout request from user ${username}")
        DatabaseManager.updateUserAddress(username, null)
        DatabaseManager.setUserStatus(username, UserStatus.INACTIVE)
        PubSubManager.redisClient.unsubscribe(username)
        return new ResponseEntity(HttpStatus.OK)
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> register(@RequestBody LoginRequest loginRequest) {
        log.info('received register request: ' + loginRequest.toJSON().toString())

        PasswordPolicy policy = DatabaseManager.getPasswordPolicy()
        if (!policy.validatePassword(loginRequest.password)) {
            return new ResponseEntity(new MessageResponse(message: PASSWORD_POLICY_NOT_MATCHED), HttpStatus.BAD_REQUEST)
        }

        boolean created = DatabaseManager.createAccount(loginRequest)
        log.info('user created: ' + created)
        if (created) {
            return new ResponseEntity(new MessageResponse(message: SUCCESS), HttpStatus.CREATED)
        } else {
            return new ResponseEntity(new MessageResponse(message: USERNAME_USED), HttpStatus.CONFLICT)
        }
    }

    @GetMapping(value = "/user-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> userList() {
        log.debug('received user list request')
        Map<String, UserStatus> userList = DatabaseManager.getUserList()
        return new ResponseEntity(new UserListResponse(userList: userList), HttpStatus.OK)
    }

    @GetMapping(value = "/password-policy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<PasswordPolicy> passwordPolicy() {
        log.info('received password policy')
        PasswordPolicy policy = DatabaseManager.getPasswordPolicy()
        return new ResponseEntity(policy.toJSON().toString(), HttpStatus.OK)
    }

    private static void redisKeyExchangeSubscribe(String username) {
        PubSubManager.redisClient.encryptionSuites.put(username, new EncryptionSuite())
        PubSubManager.redisClient.encryptionSuites[username].generateKeys()
        PubSubManager.redisClient.subscribeChannel(username + DH_POSTFIX, "server") { String channelName, Message message ->
            String clientPublicKey = StringMessage.fromJSON(message.content).str
            PubSubManager.redisClient.encryptionSuites[username].generateCommonSecretKey(clientPublicKey)
            PubSubManager.redisClient.unsubscribe(channelName)

            String serverPublicKey = PubSubManager.redisClient.encryptionSuites[username].serializePublicKey()
            Message messageToClient = MessageFactory.createMessage(MessageAction.KEY_EXCHANGE, 'server', serverPublicKey)
            PubSubManager.redisClient.publishMessage(username + DH_POSTFIX, username, messageToClient)
            redisEncryptionOkSubscribe(username)
        }
    }

    private static void redisEncryptionOkSubscribe(String username) {
        PubSubManager.redisClient.subscribeChannel(username, "server") { String channelName, Message message ->
            String decryptedMessage = StringMessage.fromJSON(message.content).str
            assert decryptedMessage == 'OK!'
            PubSubManager.redisClient.unsubscribe(channelName)

            Message okMessage = MessageFactory.createMessage(MessageAction.KEY_EXCHANGE, 'server', "OK!")
            PubSubManager.redisClient.publishMessage(username, okMessage)
            redisMessageForwardSubscribe(username)
        }
    }

    private static void redisMessageForwardSubscribe(String username) {
        PubSubManager.redisClient.subscribeChannel(username, 'server') { String _, Message message ->
            message.sender = 'server'
            PubSubManager.redisClient.publishMessage(message.target, message)
        }
    }

}