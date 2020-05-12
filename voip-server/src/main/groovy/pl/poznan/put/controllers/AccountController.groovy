package pl.poznan.put.controllers

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.poznan.put.GlobalConstants
import pl.poznan.put.managers.DatabaseManager
import pl.poznan.put.structures.ApiResponse
import pl.poznan.put.structures.LoginRequest
import pl.poznan.put.structures.LoginResponse
import pl.poznan.put.structures.MessageResponse

import javax.servlet.http.HttpServletRequest

@Slf4j
@RestController
@RequestMapping(value = "/account")
class AccountController {

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (!DatabaseManager.checkAccount(loginRequest)) {
            ApiResponse response = new MessageResponse(message: "incorrect username or password")
            return new ResponseEntity(response, HttpStatus.CONFLICT)
        }

        DatabaseManager.updateUserAddress(loginRequest.username, request.remoteAddr)

        ApiResponse response = new LoginResponse(subPubHost: SubPubManager.getRedisHost(),
                subPubPort: GlobalConstants.REDIS_PORT)
        return new ResponseEntity(response, HttpStatus.CREATED)
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<ApiResponse> register(@RequestBody LoginRequest loginRequest) {
        log.info('received register request: ' + loginRequest.toJSON().toString())
        if (DatabaseManager.accountExists(loginRequest.username)) {
            MessageResponse response = new MessageResponse(message: "username already in use")
            return new ResponseEntity(response, HttpStatus.CONFLICT)
        }
        boolean created = DatabaseManager.createAccount(loginRequest)
        log.info('user created: ' + created)
        if (created) {
            return new ResponseEntity(new MessageResponse(message: "user created"), HttpStatus.CREATED)
        } else {
            return new ResponseEntity(new MessageResponse(message: "server error"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}
