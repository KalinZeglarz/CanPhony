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

    LoginWindow(String serverAddress) {
        this.serverAddress = serverAddress
    }

    void create(JFrame frame) {
        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()

        // Creating the panel for components
        JPanel panel = new JPanel()
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT))

        JLabel serverAddressLabel = new JLabel("Server Address")
        serverAddressLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField serverAddressField = new JTextField(16)
        serverAddressField.setText(serverAddress)
        serverAddressLabel.setLabelFor(serverAddressField)

        JLabel usernameLabel = new JLabel("Username")
        usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField usernameField = new JTextField(16)
        usernameLabel.setLabelFor(usernameField)

        JLabel passLabel = new JLabel("Password")
        JPasswordField passField = new JPasswordField(16)
        passLabel.setLabelFor(passField)

        JButton displayPassButton = new JButton("Display Password")
        displayPassButton.setLayout((new FlowLayout(FlowLayout.LEFT)))
        displayPassButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked display password button')
                String password = passField.getPassword()
                JOptionPane.showMessageDialog(frame, "Password: " + password)
            }
        })

        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked register button')
                serverAddress = saveServerAddress(serverAddressField.getText())
                new RegistrationWindow(serverAddress).create(frame)
            }
        })

        JButton loginButton = new JButton("Login")
        loginButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked login button')
                String username = usernameField.getText()
                String password = passField.getPassword()
                try {
                    httpClient = new VoipHttpClient(serverAddressField.getText())
                } catch (ConnectException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }
                LoginResponse loginResponse = httpClient.login(username, password)
                if (loginResponse != null) {
                    serverAddress = saveServerAddress(serverAddressField.getText())
                    httpClient.username = username
                    RedisClient redisClient = new RedisClient(loginResponse.subPubHost)
                    new ConnectionWindow(httpClient, redisClient).create(frame)
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect login or password.")
                }
            }
        })

        panel.add(serverAddressLabel) // Components Added using Flow Layout
        panel.add(serverAddressField)
        panel.add(usernameLabel)
        panel.add(usernameField)
        panel.add(passLabel)
        panel.add(passField)
        panel.add(displayPassButton)
        panel.add(registerButton)
        panel.add(loginButton)

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, panel)
        frame.setVisible(true)
    }

}
