package pl.poznan.put.controllers

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.poznan.put.GlobalConstants
import pl.poznan.put.managers.DatabaseManager
import pl.poznan.put.structures.*

import javax.servlet.http.HttpServletRequest

@Slf4j
@RestController
@RequestMapping(value = "/account")
class AccountController {

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info('received login request')
        AccountStatus accountStatus = DatabaseManager.checkAccount(loginRequest)
        if (accountStatus != AccountStatus.SUCCESS) {
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

        LoginResponse response = new LoginResponse(subPubHost: SubPubManager.getRedisHost(),
                subPubPort: GlobalConstants.REDIS_PORT)
        return new ResponseEntity(response, HttpStatus.CREATED)
    }

    @DeleteMapping(value = "/logout")
    @ResponseBody
    ResponseEntity logout(@RequestBody LoginRequest loginRequest) {
        log.info('received logout request')
        DatabaseManager.updateUserAddress(loginRequest.username, null)
        DatabaseManager.setUserStatus(loginRequest.username, UserStatus.INACTIVE)
        return new ResponseEntity(HttpStatus.OK)
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> register(@RequestBody LoginRequest loginRequest) {
        log.info('received register request: ' + loginRequest.toJSON().toString())
        boolean created = DatabaseManager.createAccount(loginRequest)
        log.info('user created: ' + created)
        if (created) {
            return new ResponseEntity(new MessageResponse(message: "user created"), HttpStatus.CREATED)
        } else {
            return new ResponseEntity(new MessageResponse(message: "username already in use"), HttpStatus.CONFLICT)
        }
    }

    @GetMapping(value = "/user-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> userList() {
        log.info('received user list request')
        Map<String, UserStatus> userList = DatabaseManager.getUserList()
        return new ResponseEntity(new UserListResponse(userList: userList), HttpStatus.OK)
    }

    @GetMapping(value = "/password-policy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<PasswordPolicy> passwordPolicy() {
        log.info('received user list request')
        PasswordPolicy policy = DatabaseManager.getPasswordPolicy()
        return new ResponseEntity(policy.toJSON().toString(), HttpStatus.OK)
    }

}
