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
    private String serverPort = ''

    RegistrationWindow(String[] configs) {
        this.serverAddress = configs[0]
        this.serverPort = configs[1]
    }


    void create(JFrame frame) {
        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()
        frame.setSize(420, 220)
        frame.setResizable(false)

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

        // Password confirm
        JPanel passConfirmPanel = new JPanel()

        JLabel passConfirmLabel = new JLabel("Re-password:")
        JPasswordField passConfirmField = new JPasswordField(18)
        passConfirmLabel.setLabelFor(passwordField)

        passConfirmPanel.add(passConfirmLabel)
        passConfirmPanel.add(passConfirmField)

        // Controls
        JPanel controlsPanel = new JPanel()
        controlsPanel.setLayout(new GridLayout(1,2))

        JButton backButton = new JButton("Back")
        backButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked back button')
                String[] configs = [serverAddressField.getText(), serverPortField.getText()]
                serverAddress = saveServerAddress(configs)[0]
                serverPort = saveServerAddress(configs)[1]
                saveServerAddress(configs)
                new LoggedOutWindow(configs).create(frame)
            }
        })

        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info('clicked register button')
                String username = usernameField.getText()
                String password = passwordField.getPassword()
                String passwordConfirm = passConfirmField.getPassword()
                if (password != passwordConfirm) {
                    JOptionPane.showMessageDialog(frame, "Passwords does not match.")
                    return
                }
                try {
                    httpClient = new VoipHttpClient(serverAddressField.getText(), serverPortField.getText())
                } catch (ConnectException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }
                boolean registered = httpClient.register(username, password)
                if (registered) {
                    JOptionPane.showMessageDialog(frame, "Account created successfully.")
                    String[] configs = [serverAddressField.getText(), serverPortField.getText()]
                    saveServerAddress(configs)
                    new LoggedOutWindow(configs).create(frame)
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect login or password.")
                }
            }
        })

        controlsPanel.add(backButton)
        controlsPanel.add(registerButton)

        // Adding components to main panel
        mainPanel.add(serverPanel)
        mainPanel.add(usernamePanel)
        mainPanel.add(passwordPanel)
        mainPanel.add(passConfirmPanel)
        mainPanel.add(controlsPanel)

        // Adding main panel to the frame
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
        frame.setVisible(true)
    }

}
