package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.VoipHttpClient

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

@Slf4j
class RegistrationWindow extends Window implements SaveServerAddress {

    private VoipHttpClient httpClient
    private String serverAddress = ''

    RegistrationWindow(String serverAddress) {
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

        JLabel passConfirmLabel = new JLabel("Confirm password")
        JPasswordField passConfirmField = new JPasswordField(16)
        passConfirmLabel.setLabelFor(passField)

        JButton backButton = new JButton("Back")
        backButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked back button')
                saveServerAddress(serverAddressField.getText())
                new LoginWindow(serverAddress).create(frame)
            }
        })

        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked register button')
                String username = usernameField.getText()
                String password = passField.getPassword()
                String passwordConfirm = passConfirmField.getPassword()
                if (password != passwordConfirm) {
                    JOptionPane.showMessageDialog(frame, "Passwords does not match.")
                    return
                }
                try {
                    httpClient = new VoipHttpClient(serverAddressField.getText())
                } catch (ConnectException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }
                boolean registered = httpClient.register(username, password)
                if (registered) {
                    JOptionPane.showMessageDialog(frame, "Account created successfully.")
                    saveServerAddress(serverAddressField.getText())
                    new LoginWindow(serverAddress).create(frame)
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect login or password.")
                }
            }
        })

        panel.add(serverAddressLabel) // Components Added using Flow Layout
        panel.add(serverAddressField)
        panel.add(usernameLabel) // Components Added using Flow Layout
        panel.add(usernameField)
        panel.add(passLabel)
        panel.add(passField)
        panel.add(passConfirmLabel)
        panel.add(passConfirmField)
        panel.add(backButton)
        panel.add(registerButton)

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, panel)
        frame.setVisible(true)
    }

}
