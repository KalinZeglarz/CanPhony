package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.pubsub.Message
import pl.poznan.put.pubsub.MessageAction
import pl.poznan.put.pubsub.RedisClient
import pl.poznan.put.security.EncryptionSuite
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.structures.LoginResponse
import pl.poznan.put.windows.Window

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static pl.poznan.put.GlobalConstants.DH_POSTFIX

@Slf4j
class LoggedOutWindow extends Window implements SaveClientConfig {

    JTextField serverAddressField
    JTextField serverPortField
    JTextField usernameField
    JPasswordField passwordField
    boolean encryptionSetUpped = false

    LoggedOutWindow(ClientConfig config) {
        super(config)
    }

    private ActionListener createRegisterButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked register button')
                config.serverAddress = serverAddressField.getText()
                config.serverPort = serverPortField.getText()
                new RegistrationWindow(config).create(frame)
            }
        }
    }

    private void redisKeyExchangeSubscribe(String username) {
        config.redisClient.encryptionSuites.put(username, new EncryptionSuite())
        config.redisClient.encryptionSuites[username].generateKeys()
        config.redisClient.subscribeChannel(username + DH_POSTFIX, username) { String channelName, Message message ->
            String serverPublicKey = message.content
            config.redisClient.encryptionSuites[username].generateCommonSecretKey(serverPublicKey)
            config.redisClient.unsubscribe(channelName)
            redisEncryptionOkSubscribe(username)
        }

        String clientPublicKey = config.redisClient.encryptionSuites[username].serializePublicKey()
        Message message = new Message(action: MessageAction.KEY_EXCHANGE, sender: username, content: clientPublicKey)
        config.redisClient.publishMessage(username + DH_POSTFIX, 'server', message)
    }

    private void redisEncryptionOkSubscribe(String username) {
        config.redisClient.subscribeChannel(username, username) { String channelName, Message message ->
            String decryptedMessage = message.content
            assert decryptedMessage == 'OK!'
            config.redisClient.unsubscribe(channelName)
            encryptionSetUpped = true
        }

        Message message = new Message(action: MessageAction.KEY_EXCHANGE, sender: username, content: "OK!")
        config.redisClient.publishMessage(username, 'server', message)
    }

    private ActionListener createLoginButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked login button')
                String username = usernameField.getText()
                String password = passwordField.getPassword()
                try {
                    config.httpClient = new VoipHttpClient(serverAddressField.getText(), serverPortField.getText())
                } catch (ConnectException | IllegalArgumentException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }
                LoginResponse loginResponse = config.httpClient.login(username, password)
                if (loginResponse == null) {
                    JOptionPane.showMessageDialog(frame, "Something went wrong.")
                    return
                }
                if (loginResponse.message.isEmpty()) {
                    config.serverAddress = serverAddressField.getText()
                    config.serverPort = serverPortField.getText()
                    config.username = usernameField.getText()
                    writeConfigToFile(config)
                    config.username = username
                    config.redisClient = new RedisClient(loginResponse.pubSubHost)

                    redisKeyExchangeSubscribe(config.username)
                    while (!encryptionSetUpped) {
                        sleep(100)
                    }
                    new LoggedInWindow(config).create(frame)
                } else {
                    JOptionPane.showMessageDialog(frame, loginResponse.message)
                }
            }
        }
    }

    private JPanel createServerPanel() {
        JLabel serverAddressLabel = new JLabel("           Server:")
        serverAddressLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        serverAddressField = new JTextField(8)
        serverAddressField.setText(config.serverAddress)
        serverAddressLabel.setLabelFor(serverAddressField)

        JLabel serverPortLabel = new JLabel("      Port:")
        serverPortLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        serverPortField = new JTextField(4)
        serverPortField.setText(config.serverPort)
        serverPortLabel.setLabelFor(serverPortField)

        JPanel serverPanel = new JPanel()
        serverPanel.add(serverAddressLabel)
        serverPanel.add(serverAddressField)
        serverPanel.add(serverPortLabel)
        serverPanel.add(serverPortField)
        return serverPanel
    }

    private JPanel createUsernamePanel() {
        JLabel usernameLabel = new JLabel("     Username:")
        usernameField = new JTextField(18)

        JPanel usernamePanel = new JPanel()
        usernamePanel.add(usernameLabel)
        usernamePanel.add(usernameField)
        return usernamePanel
    }

    private JPanel createPasswordPanel() {
        JLabel passwordLabel = new JLabel("      Password:")
        passwordField = new JPasswordField(18)

        JPanel passwordPanel = new JPanel()
        passwordPanel.add(passwordLabel)
        passwordPanel.add(passwordField)
        return passwordPanel
    }

    private JPanel createControlsPanel() {
        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(createRegisterButtonListener())

        JButton loginButton = new JButton("Login")
        loginButton.addActionListener(createLoginButtonListener())

        JPanel controlsPanel = new JPanel()
        controlsPanel.setLayout(new GridLayout(1, 2))
        controlsPanel.add(registerButton)
        controlsPanel.add(loginButton)
        return controlsPanel
    }

    void create(JFrame frame) {
        super.create(frame)
        SwingUtilities.invokeLater {
            frame.getContentPane().removeAll()
            frame.repaint()
            frame.setSize(420, 180)
            frame.setResizable(false)

            JPanel mainPanel = new JPanel()
            mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))
            mainPanel.add(createServerPanel())
            mainPanel.add(createUsernamePanel())
            mainPanel.add(createPasswordPanel())
            mainPanel.add(createControlsPanel())

            frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
            frame.setVisible(true)
        }
    }

}
