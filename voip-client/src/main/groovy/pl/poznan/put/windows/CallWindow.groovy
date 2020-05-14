package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageFactory
import pl.poznan.put.subpub.RedisClient
import pl.poznan.put.windows.Window

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.TimeUnit

import static pl.poznan.put.subpub.MessageAction.*

@Slf4j
class CallWindow extends Window {

    final String username
    final String serverAddress
    final VoipHttpClient httpClient
    final RedisClient redisClient
    PhoneCallClient phoneCallClient = null
    Integer currentSessionId = null
    String[] usersToCall
    Thread timer

    CallWindow(VoipHttpClient httpClient, RedisClient redisClient, String[] usersToCall) {
        this.httpClient = httpClient
        this.redisClient = redisClient
        username = httpClient.username
        serverAddress = httpClient.serverAddress.split(':')[0]
        redisCallRequestSubscribe(httpClient.username)
        this.usersToCall = usersToCall
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

    void create(JFrame frame) {

        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()
        frame.setSize(420, 350)

        // Main panel
        JPanel mainPanel = new JPanel()
        mainPanel.setLayout(new GridLayout(3,1))

        // Username
        JPanel usernamePanel = new JPanel()
        usernamePanel.setPreferredSize(new Dimension(400, 20))
        JLabel yourUsernameLabel = new JLabel("Call with:  " + usersToCall)
        usernamePanel.add(yourUsernameLabel)

        // Controls
        JPanel controlsPanel = new JPanel()
        controlsPanel.setPreferredSize(new Dimension(200, 50))
        controlsPanel.setLayout(new GridLayout(1, 2))


        JButton endCallButton = new JButton("End Call")
        endCallButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked endcall button")
                new LoggedInWindow(httpClient, redisClient).create(frame)
            }
        })
        controlsPanel.add(endCallButton)

        //Timer
        JPanel timerPanel = new JPanel()
        JLabel timerLabel = new JLabel()
        timerPanel.add(timerLabel)

        // Adding components to main panel
        mainPanel.add(usernamePanel, BorderLayout.CENTER)
        mainPanel.add(timerPanel, BorderLayout.CENTER)
        mainPanel.add(controlsPanel, BorderLayout.CENTER)

        // Timer thread
        timer = new Thread({
            int hours = 0
            int minutes = 0
            int seconds = 0

            while (!Thread.currentThread().isInterrupted()) {
                String timeToShow = ''
                seconds++
                if(seconds==60){
                    seconds = 0
                    minutes++
                }
                if(minutes==60){
                    minutes = 0
                    hours++
                }

                if(hours<10){
                    timeToShow += '0' + hours + ':'
                }
                else timeToShow += hours + ':'

                if(minutes<10){
                    timeToShow += '0' + minutes + ':'
                }
                else timeToShow += minutes + ':'

                if(seconds<10){
                    timeToShow += '0' + seconds
                }
                else timeToShow += seconds
                timerLabel.setText(timeToShow)
                SwingUtilities.invokeLater {
                    mainPanel.updateUI()
                }
                TimeUnit.SECONDS.sleep(1)
            }
        })
        timer.start()

        // Adding main panel to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
        frame.setVisible(true)
    }

}
