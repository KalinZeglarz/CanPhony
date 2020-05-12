package pl.poznan.put.controllers

import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.Tuple2
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.poznan.put.managers.DatabaseManager
import pl.poznan.put.managers.PhoneCallManager
import pl.poznan.put.structures.PhoneCallParamsFactory
import pl.poznan.put.structures.PhoneCallRequest
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageAction
import pl.poznan.put.subpub.MessageFactory

@Slf4j
@RestController()
@RequestMapping(value = "/phone-call")
class PhoneCallController {

    @PostMapping(value = "/start-call", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<PhoneCallResponse> call(@RequestBody PhoneCallRequest phoneCallRequest) {
        log.info("received call request: " + phoneCallRequest.toJSON().toString())
        String sourceUserAddress = DatabaseManager.getUserAddress(phoneCallRequest.sourceUsername)
        String targetUserAddress = DatabaseManager.getUserAddress(phoneCallRequest.targetUsername)

        Tuple2<PhoneCallResponse, PhoneCallResponse> phoneCallResponses = PhoneCallManager
                .addPhoneCall(PhoneCallParamsFactory.createPhoneCallParams(sourceUserAddress, targetUserAddress))

        Message message = MessageFactory.createMessage(MessageAction.CALL_REQUEST, phoneCallResponses.getItem2())
        SubPubManager.redisClient.publishMessage(phoneCallRequest.targetUsername, message)

        return new ResponseEntity(phoneCallResponses.getItem1().toJSON().toString(), HttpStatus.OK)
    }

}
