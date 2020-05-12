package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageFactory
import pl.poznan.put.subpub.RedisClient

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static pl.poznan.put.subpub.MessageAction.*

@Slf4j
class ConnectionWindow extends Window {

    final String username
    final String serverAddress
    final VoipHttpClient httpClient
    final RedisClient redisClient
    PhoneCallClient phoneCallClient = null
    Integer currentSessionId = null

    ConnectionWindow(VoipHttpClient httpClient, RedisClient redisClient) {
        this.httpClient = httpClient
        this.redisClient = redisClient
        username = httpClient.username
        serverAddress = httpClient.serverAddress.split(':')[0]
        redisCallRequestSubscribe(httpClient.username)
    }

    private void redisCallRequestSubscribe(String username) {
        log.info("[${username}] subscribing with call request callback")
        redisClient.subscribeChannel(username) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == CALL_REQUEST && phoneCallClient == null) {
                log.info("[${channelName}] received call request: " + message.content)
                redisClient.unsubscribe(username)
                PhoneCallResponse phoneCallResponse = PhoneCallResponse.parseJSON(message.content)
                currentSessionId = phoneCallResponse.sessionId

                redisEndCallSubscribe(currentSessionId)
                redisClient.publishMessage(currentSessionId, MessageFactory.createMessage(ACCEPT_CALL, username))
                phoneCallClient = new PhoneCallClient(serverAddress, phoneCallResponse.forwarderPort)
                phoneCallClient.start()
            }
        }
    }

    private void redisEndCallSubscribe(int sessionId) {
        log.info("[${sessionId}] subscribing with end call callback")
        redisClient.subscribeChannel(sessionId.toString()) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == END_CALL && phoneCallClient != null) {
                log.info("[${channelName}] received end call")
                redisClient.unsubscribe(channelName)
                phoneCallClient.stop()
                phoneCallClient = null
                currentSessionId = null
                redisCallRequestSubscribe(httpClient.username)
            }
        }
    }

    private void redisStartCallSubscribe(PhoneCallResponse response) {
        log.info("[${response.sessionId}] subscribing with start call callback")
        redisClient.unsubscribe(username)
        redisClient.subscribeChannel(response.sessionId.toString()) { String channelName,
                                                           String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == ACCEPT_CALL && phoneCallClient == null) {
                log.info("[${channelName}] call request accepted")
                phoneCallClient = new PhoneCallClient(serverAddress, response.forwarderPort)
                phoneCallClient.start()
            } else if (message.action == END_CALL && phoneCallClient != null) {
                log.info("[${channelName}] received end call: " + message.content)
                redisClient.unsubscribe(channelName)
                phoneCallClient.stop()
                phoneCallClient = null
                currentSessionId = null
                redisCallRequestSubscribe(httpClient.username)
            }
        }
    }

    void create(JFrame frame) {
        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()

        // Creating the panel for components
        JPanel panel = new JPanel()
        panel.setLayout(new FlowLayout(FlowLayout.CENTER))

        JLabel yourUsernameLabel = new JLabel("Logged in as: " + username)

        JLabel usernameLabel = new JLabel("Username")
        usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField usernameField = new JTextField(16)
        usernameLabel.setLabelFor(usernameField)

        JButton connectButton = new JButton("Connect")
        connectButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked connect button")
                if (!usernameField.getText().isEmpty()) {
                    PhoneCallResponse response = httpClient.startCall(usernameField.getText())
                    currentSessionId = response.sessionId
                    redisStartCallSubscribe(response)
                }
            }
        })

        JButton disconnectButton = new JButton("Disconnect")
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked disconnect button")
                if (phoneCallClient != null) {
                    redisClient.unsubscribe(currentSessionId)
                    redisClient.publishMessage(currentSessionId, MessageFactory.createMessage(END_CALL, username))
                    phoneCallClient.stop()
                    phoneCallClient = null
                    currentSessionId = null
                    redisCallRequestSubscribe(httpClient.username)
                } else {
                    log.warn('phone call client is not assigned')
                }
            }
        })

        panel.add(yourUsernameLabel)
        panel.add(usernameField)
        panel.add(connectButton)
        panel.add(disconnectButton)

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, panel)
        frame.setVisible(true)
    }

}
