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
class ChangePasswordWindow extends Window implements SaveClientConfig {

    JTextField serverAddressField
    JTextField serverPortField
    JTextField usernameField
    JPasswordField passwordField
    JPasswordField newPasswordField
    JPasswordField confirmNewPasswordField

    ChangePasswordWindow(ClientConfig config) {
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

    private ActionListener createChangePasswordButtonListener() {
        new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked change password button")

                try {
                    config.httpClient = new VoipHttpClient(serverAddressField.getText(), serverPortField.getText())
                } catch (ConnectException | IllegalArgumentException ignored) {
                    JOptionPane.showMessageDialog(frame, "Could not connect to server.")
                    return
                }

                String username = usernameField.getText()
                String currentPassword = passwordField.getPassword()
                String newPassword = newPasswordField.getPassword()
                String confirmNewPassword = confirmNewPasswordField.getPassword()

                if (username.isBlank() || currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please fill out all fields.")
                    return
                }

                if (currentPassword == newPassword) {
                    JOptionPane.showMessageDialog(frame, "New password cannot be the same as the old one.")
                    return
                }
                if (newPassword != confirmNewPassword) {
                    JOptionPane.showMessageDialog(frame, "Passwords does not match.")
                    return
                }

                AccountStatus accountStatus = config.httpClient.changePassword(username, currentPassword, newPassword)
                if (accountStatus == null) {
                    JOptionPane.showMessageDialog(frame, "Wrong current password.")
                } else if (accountStatus == AccountStatus.SUCCESS) {
                    JOptionPane.showMessageDialog(frame, "Password changed successfully.")
                    config.serverAddress = serverAddressField.getText()
                    config.serverPort = serverPortField.getText()
                    writeConfigToFile(config)
                    new LoggedOutWindow(config).create(frame)
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
        newPasswordField = new JPasswordField(17)
        confirmNewPasswordField = new JPasswordField(17)


        JComponent[] inputs = [usernameField, passwordField, newPasswordField, confirmNewPasswordField]
        String[] labels = ["Username:", "Current password:", "Password:", "Confirm password:"]
        return getTwoColumnLayout(labels, inputs)
    }

    private JPanel createControlsPanel() {
        JButton backButton = new JButton("Back")
        backButton.addActionListener(createBackButtonListener())

        JButton changePasswordButton = new JButton("Change Password")
        changePasswordButton.addActionListener(createChangePasswordButtonListener())

        JPanel controlsPanel = new JPanel()
        controlsPanel.setLayout(new GridLayout(1, 2))
        controlsPanel.add(backButton)
        controlsPanel.add(changePasswordButton)
        return controlsPanel
    }

    void create(JFrame frame) {
        super.create(frame)
        SwingUtilities.invokeLater {
            frame.getContentPane().removeAll()
            frame.repaint()
            frame.setSize(420, 260)
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
