package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.structures.LoginResponse
import pl.poznan.put.subpub.RedisClient

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

@Slf4j
class LoginWindow extends Window implements SaveServerAddress {

    private VoipHttpClient httpClient
    private String serverAddress = ''
    private String serverPort = ''

    LoginWindow(String[] configs) {
        this.serverAddress = configs[0]
        this.serverPort = configs[1]
    }

    void create(JFrame frame) {

        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()
        frame.setSize(420, 180)

        // Main panel
        JPanel mainPanel = new JPanel()
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))

        // Server
        JPanel serverPanel = new JPanel()

        JLabel serverAddressLabel = new JLabel("           Server:")
        serverAddressLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField serverAddressField = new JTextField(8)
        serverAddressField.setText(serverAddress)
        serverAddressLabel.setLabelFor(serverAddressField)

        JLabel serverPortLabel = new JLabel("      Port:")
        serverPortLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField serverPortField = new JTextField(4)
        serverPortField.setText(serverPort)
        serverPortLabel.setLabelFor(serverPortField)

        serverPanel.add(serverAddressLabel)
        serverPanel.add(serverAddressField)
        serverPanel.add(serverPortLabel)
        serverPanel.add(serverPortField)

        // Username
        JPanel usernamePanel = new JPanel()

        JLabel usernameLabel = new JLabel("     Username:")
        JTextField usernameField = new JTextField(18)

        usernamePanel.add(usernameLabel)
        usernamePanel.add(usernameField)

        // Password
        JPanel passwordPanel = new JPanel()

        JLabel passwordLabel = new JLabel("      Password:")
        JPasswordField passwordField = new JPasswordField(18)

        passwordPanel.add(passwordLabel)
        passwordPanel.add(passwordField)

        // Controls
        JPanel controlsPanel = new JPanel()
        controlsPanel.setLayout(new GridLayout(1,2))

        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked register button')
                serverAddress = saveServerAddress(serverAddressField.getText(), serverPortField.getText())
                new RegistrationWindow(serverAddress, serverPort).create(frame)
            }
        })

        JButton loginButton = new JButton("Login")
        loginButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked login button')
                String username = usernameField.getText()
                String password = passwordField.getPassword()
                try {
                    httpClient = new VoipHttpClient(serverAddressField.getText(), serverPortField.getText())
                } catch (ConnectException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }
                LoginResponse loginResponse = httpClient.login(username, password)
                if (loginResponse != null) {
                    serverAddress = saveServerAddress(serverAddressField.getText(), serverPortField.getText())
                    httpClient.username = username
                    RedisClient redisClient = new RedisClient(loginResponse.subPubHost)
                    new ConnectionWindow(httpClient, redisClient).create(frame)
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect login or password.")
                }
            }
        })

        controlsPanel.add(registerButton)
        controlsPanel.add(loginButton)

        // Adding components to main panel
        mainPanel.add(serverPanel)
        mainPanel.add(usernamePanel)
        mainPanel.add(passwordPanel)
        mainPanel.add(controlsPanel)

        // Adding main panel to the frame
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
        frame.setVisible(true)
    }

}
