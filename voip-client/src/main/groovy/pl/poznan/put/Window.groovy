package pl.poznan.put

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener


class Window {
    private JFrame frame
    JMenuBar menuBar
    JMenu fileMenu
    JMenu helpMenu

    Window() {
        //Creating the Frame
        this.frame = new JFrame("CanPhony")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setSize(250, 200)

        //Creating the MenuBar and adding components
        this.menuBar = new JMenuBar()
        this.fileMenu = new JMenu("File")
        this.helpMenu = new JMenu("Help")
        this.menuBar.add(fileMenu)
        this.menuBar.add(helpMenu)
        JMenuItem fmenuOpen = new JMenuItem("Open")
        JMenuItem fmenuSave = new JMenuItem("Save as")
        fileMenu.add(fmenuOpen)
        fileMenu.add(fmenuSave)
    }

    void login() {
        //Cleaning frame
        this.frame.getContentPane().removeAll()
        this.frame.repaint()

        //Creating the panel for components
        JPanel panel = new JPanel()
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT))

        JLabel loginLabel = new JLabel("Login")
        loginLabel.setHorizontalAlignment(SwingConstants.RIGHT)
        JTextField loginField = new JTextField(16)
        loginLabel.setLabelFor(loginField)

        JLabel passLabel = new JLabel("Password")
        JPasswordField passField = new JPasswordField(16)
        passLabel.setLabelFor(passField)

        JButton displayPassButton = new JButton("Display Password")
        displayPassButton.setLayout((new FlowLayout(FlowLayout.LEFT)))
        displayPassButton.addActionListener(
                new ActionListener() {

                    void actionPerformed(ActionEvent e) {
                        String password = new String(passField.getPassword())
                        JOptionPane.showMessageDialog(frame,
                                "Password: " + password)
                    }
                })

        JButton loginButton = new JButton("Login")

        panel.add(loginLabel) // Components Added using Flow Layout
        panel.add(loginField)
        panel.add(passLabel)
        panel.add(passField)
        panel.add(displayPassButton)
        panel.add(loginButton)


        //Adding Components to the frame.
        this.frame.getContentPane().add(BorderLayout.CENTER, panel)
        this.frame.getContentPane().add(BorderLayout.NORTH, menuBar)
        this.frame.setVisible(true)
    }

    void connection() {

        //Cleaning frame
        this.frame.getContentPane().removeAll()
        this.frame.repaint()

        //Creating the panel for components
        JPanel panel = new JPanel()
        panel.setLayout(new FlowLayout(FlowLayout.CENTER))

        JButton connectButton = new JButton("Connect")
        JButton disconnectButton = new JButton("Disconnect")

        panel.add(connectButton)
        panel.add(disconnectButton)

        //Adding Components to the frame.
        this.frame.getContentPane().add(BorderLayout.CENTER, panel)
        this.frame.getContentPane().add(BorderLayout.NORTH, this.menuBar)
        this.frame.setVisible(true)
    }
}