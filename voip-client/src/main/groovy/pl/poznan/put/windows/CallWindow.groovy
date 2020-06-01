package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.pubsub.Message
import pl.poznan.put.pubsub.MessageFactory
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.windows.Window

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.TimeUnit

import static pl.poznan.put.pubsub.MessageAction.END_CALL

@Slf4j
class CallWindow extends Window {

    Thread timerThread

    JLabel timerLabel

    CallWindow(ClientConfig config) {
        super(config)
        redisEndCallSubscribe(config.currentSessionId)
    }

    private void redisEndCallSubscribe(int sessionId) {
        log.info("[${sessionId}] subscribing with end call callback")
        config.redisClient.subscribeChannel(sessionId.toString()) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.action == END_CALL && config.phoneCallClient != null) {
                log.info("[${channelName}] received end call")
                config.redisClient.unsubscribe(channelName)
                config.phoneCallClient.stop()
                config.phoneCallClient = null
                config.currentSessionId = null
                new LoggedInWindow(config).create(frame)
            }
        }
    }

    private JPanel createUsernamePanel() {
        JLabel callUsernameLabel = new JLabel("Call with:  " + config.currentCallUsername)
        Font usernameFont = callUsernameLabel.getFont()
        callUsernameLabel.setFont(new Font(usernameFont.getName(), usernameFont.getStyle(), 40))

        JPanel usernamePanel = new JPanel()
        usernamePanel.setPreferredSize(new Dimension(400, 20))
        usernamePanel.add(callUsernameLabel)
        return usernamePanel
    }

    private JPanel createControlsPanel() {
        JButton endCallButton = new JButton("End Call")
        endCallButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked end call button")
                config.redisClient.publishMessage(config.currentSessionId,
                        MessageFactory.createMessage(END_CALL, config.username))
                new LoggedInWindow(config).create(frame)
            }
        })

        JPanel controlsPanel = new JPanel()
        controlsPanel.setPreferredSize(new Dimension(200, 70))
        controlsPanel.setLayout(new GridLayout(1, 2))
        controlsPanel.add(endCallButton)
        return controlsPanel
    }

    private JPanel createTimerPanel() {
        timerLabel = new JLabel()
        Font timerFont = timerLabel.getFont()
        timerLabel.setFont(new Font(timerFont.getName(), timerFont.getStyle(), 50))

        JPanel timerPanel = new JPanel()
        timerPanel.add(timerLabel)
        return timerPanel
    }

    private void createTimerThread(JPanel mainPanel) {
        timerThread = new Thread({
            int hours = 0
            int minutes = 0
            int seconds = 0

            while (!Thread.currentThread().isInterrupted()) {
                String timeToShow = ''
                seconds++
                if (seconds == 60) {
                    seconds = 0
                    minutes++
                }
                if (minutes == 60) {
                    minutes = 0
                    hours++
                }

                if (hours < 10) {
                    timeToShow += '0' + hours + ':'
                } else timeToShow += hours + ':'

                if (minutes < 10) {
                    timeToShow += '0' + minutes + ':'
                } else timeToShow += minutes + ':'

                if (seconds < 10) {
                    timeToShow += '0' + seconds
                } else timeToShow += seconds
                timerLabel.setText(timeToShow)
                SwingUtilities.invokeLater {
                    mainPanel.updateUI()
                }
                TimeUnit.SECONDS.sleep(1)
            }
        })
        timerThread.start()
    }

    void create(JFrame frame) {
        super.create(frame)
        SwingUtilities.invokeLater {
            frame.getContentPane().removeAll()
            frame.repaint()
            frame.setSize(420, 350)
            frame.setResizable(false)

            JPanel mainPanel = new JPanel()
            mainPanel.setLayout(new GridLayout(3, 1))
            mainPanel.add(createUsernamePanel(), BorderLayout.CENTER)
            mainPanel.add(createTimerPanel(), BorderLayout.CENTER)
            mainPanel.add(createControlsPanel(), BorderLayout.CENTER)

            createTimerThread(mainPanel)

            // Adding main panel to the frame.
            frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
            frame.setVisible(true)
        }
    }

}
