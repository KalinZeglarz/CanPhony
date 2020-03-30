package pl.poznan.put.controllers

import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import pl.poznan.put.subpub.ChannelManager
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageFactory
import pl.poznan.put.streaming.UdpAudioForwarder

@RestController()
@RequestMapping(value = "/phone-call")
class PhoneCallController {

    private UdpAudioForwarder forwarder
    ChannelManager channelManager = new ChannelManager("127.0.0.1", 6381)

    @RequestMapping(value = "/start-call", method = RequestMethod.GET)
    ResponseEntity call() {
        final Integer streamerPort = 50000
        final Integer forwarderPort = 50001
        final Integer receiverPort = 50002

        if (forwarder != null) {
            return new ResponseEntity("there is ongoing phone call", HttpStatus.CONFLICT)
        }

        forwarder = new UdpAudioForwarder(
                streamerPort: streamerPort,
                receiverPort: receiverPort,
                forwarderPort: forwarderPort,
                bufferSize: 4096
        )

        channelManager.subscribeChannel('temp')
        forwarder.start()

        JSONObject json = new JSONObject()
        json.put('channelName', 'temp')
        json.put('streamerPort', streamerPort)
        json.put('receiverPort', receiverPort)
        json.put('forwarderPort', forwarderPort)
        Message message = MessageFactory.createMessage(json)
        return new ResponseEntity(message.toJSON().toString(), HttpStatus.OK)
    }

    @RequestMapping(value = "/end-call", method = RequestMethod.GET)
    String endCall() {
        if (forwarder != null) {
            forwarder.stop()
            forwarder = null
            return "your call has ended"
        }
        return "there is no phone call"
    }

}
