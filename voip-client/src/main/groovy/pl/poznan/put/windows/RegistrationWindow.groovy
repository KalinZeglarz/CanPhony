package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.ClientConfig
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.structures.AccountStatus
import pl.poznan.put.structures.PasswordPolicy
import pl.poznan.put.windows.Window

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

@Slf4j
class RegistrationWindow extends Window implements SaveClientConfig {

    JTextField serverAddressField
    JTextField serverPortField
    JTextField usernameField
    JPasswordField passwordField
    JPasswordField confirmPasswordField

    RegistrationWindow(ClientConfig config) {
        super(config)
    }

    private ActionListener createBackButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked back button")
                config.serverAddress = serverAddressField.getText()
                config.serverPort = serverPortField.getText()
                writeConfigToFile(config)
                new LoggedOutWindow(config).create(frame)
            }
        }
    }

    private static JPanel mapToPanel(Map<Object, Object> map) {
        JPanel mapPanel = new JPanel()
        mapPanel.setLayout(new GridLayout(0, 2))
        for (Map.Entry<Object, Object> entry in map) {
            mapPanel.add(new JLabel(entry.getKey().toString()))
            mapPanel.add(new JLabel(" : " + entry.getValue().toString()))
        }
        return mapPanel
    }

    private ActionListener createRegisterButtonListener() {
        new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked register button")

                try {
                    config.httpClient = new VoipHttpClient(serverAddressField.getText(), serverPortField.getText())
                } catch (ConnectException | IllegalArgumentException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }

                String username = usernameField.getText()
                String password = passwordField.getPassword()
                String confirmPassword = confirmPasswordField.getPassword()

                if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please fill out all fields.")
                    return
                }

                if (password != confirmPassword) {
                    JOptionPane.showMessageDialog(frame, "Passwords does not match.")
                    return
                }


                AccountStatus accountStatus = config.httpClient.register(username, password)
                if (accountStatus == AccountStatus.SUCCESS) {
                    JOptionPane.showMessageDialog(frame, "Account created successfully.")
                    config.serverAddress = serverAddressField.getText()
                    config.serverPort = serverPortField.getText()
                    writeConfigToFile(config)
                    new LoggedOutWindow(config).create(frame)
                } else if (accountStatus == AccountStatus.USERNAME_USED) {
                    JOptionPane.showMessageDialog(frame, "Username already in use.")
                } else if (accountStatus == AccountStatus.PASSWORD_POLICY_NOT_MATCHED) {
                    PasswordPolicy policy = config.httpClient.getPasswordPolicy()
                    String messageTitle = "Password does not match password policy"
                    JOptionPane.showMessageDialog(frame, mapToPanel(policy.toPrettyMap()), messageTitle,
                            JOptionPane.PLAIN_MESSAGE)
                } else {
                    JOptionPane.showMessageDialog(frame, "Something went wrong.")
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

    private JPanel createCredentialsPanel() {
        usernameField = new JTextField(17)
        passwordField = new JPasswordField(17)
        confirmPasswordField = new JPasswordField(17)


        JComponent[] inputs = [usernameField, passwordField, confirmPasswordField]
        String[] labels = ["Username:", "Password:", "Confirm password:"]
        return getTwoColumnLayout(labels, inputs)
    }

    private JPanel createControlsPanel() {
        JButton backButton = new JButton("Back")
        backButton.addActionListener(createBackButtonListener())

        JButton registerButton = new JButton("Register")
        registerButton.addActionListener(createRegisterButtonListener())

        JPanel controlsPanel = new JPanel()
        controlsPanel.setLayout(new GridLayout(1, 2))
        controlsPanel.add(backButton)
        controlsPanel.add(registerButton)
        return controlsPanel
    }

    void create(JFrame frame) {
        super.create(frame)
        SwingUtilities.invokeLater {
            frame.getContentPane().removeAll()
            frame.repaint()
            frame.setSize(420, 220)
            frame.setResizable(false)

            JPanel mainPanel = new JPanel()
            mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))
            mainPanel.add(createServerPanel())
            mainPanel.add(createCredentialsPanel())
            mainPanel.add(createControlsPanel())

            frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
            frame.setVisible(true)
        }
    }

}
