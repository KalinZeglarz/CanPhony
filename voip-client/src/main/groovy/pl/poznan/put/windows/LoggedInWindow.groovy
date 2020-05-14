package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageFactory
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

import static pl.poznan.put.subpub.MessageAction.*

@Slf4j
class LoggedInWindow extends Window {

    private static final int USER_LIST_REQUEST_PERIOD = 30000
    private Thread userListListener

    JTextField searchField
    DefaultTableModel model
    JTable contactsTable
    TableRowSorter<TableModel> sorter

    LoggedInWindow(ClientConfig config) {
        super(config)
        redisCallRequestSubscribe(this.config.username)
    }

    private void redisCallRequestSubscribe(String username) {
        log.info("[${username}] subscribing with call request callback")
        config.redisClient.subscribeChannel(username) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == CALL_REQUEST && config.phoneCallClient == null) {
                log.info("[${channelName}] received call request: " + message.content)
                config.redisClient.unsubscribe(username)
                PhoneCallResponse phoneCallResponse = PhoneCallResponse.parseJSON(message.content)
                config.currentSessionId = phoneCallResponse.sessionId

                config.currentCallUsername = phoneCallResponse.sourceUsername
                config.redisClient.publishMessage(config.currentSessionId, MessageFactory.createMessage(ACCEPT_CALL, username))
                config.phoneCallClient = new PhoneCallClient(config.serverAddress, phoneCallResponse.forwarderPort)
                config.phoneCallClient.start()
                userListListener.interrupt()
                new CallWindow(config).create(frame)
            }
        }
    }

    private void redisStartCallSubscribe(PhoneCallResponse response) {
        log.info("[${response.sessionId}] subscribing with start call callback")
        config.redisClient.unsubscribe(config.username)
        config.redisClient.subscribeChannel(response.sessionId.toString()) { String channelName,
                                                                             String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == config.username) {
                return
            }
            if (message.action == ACCEPT_CALL && config.phoneCallClient == null) {
                log.info("[${channelName}] call request accepted")
                config.phoneCallClient = new PhoneCallClient(config.serverAddress, response.forwarderPort)
                config.phoneCallClient.start()
                userListListener.interrupt()
                new CallWindow(config).create(frame)
            } else if (message.action == END_CALL && config.phoneCallClient != null) {
                log.info("[${channelName}] received end call: " + message.content)
                config.redisClient.unsubscribe(channelName)
                config.phoneCallClient.stop()
                config.phoneCallClient = null
                config.currentSessionId = null
                redisCallRequestSubscribe(config.httpClient.username)
            }
        }
    }

    private ActionListener createConnectButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked connect button")

                // Get selected user
                int row = contactsTable.getSelectedRow()
                if (row < 0 || row >= contactsTable.rowCount) {
                    return
                }
                config.currentCallUsername = contactsTable.getModel().getValueAt(row, 0).toString()
                // Try to connect
                PhoneCallResponse response = config.httpClient.startCall(config.currentCallUsername)
                config.currentSessionId = response.sessionId
                redisStartCallSubscribe(response)
            }
        }
    }

    private ActionListener createLogOutButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked disconnect button")
                new LoggedOutWindow(config).create(frame)
            }
        }
    }

    private void createUserListListenerThread() {
        userListListener = new Thread({
            while (!Thread.currentThread().isInterrupted()) {
                Set<String> userList = config.httpClient.getUserList()
                model.setRowCount(0)
                for (String user in userList) {
                    model.addRow(user)
                }
                sleep(USER_LIST_REQUEST_PERIOD)
            }
        })
        userListListener.start()
    }

    private DocumentListener createSearchFieldListener(JPanel mainPanel) {
        return new DocumentListener() {
            void changedUpdate(DocumentEvent e) {
                filter()
            }

            void removeUpdate(DocumentEvent e) {
                filter()
            }

            void insertUpdate(DocumentEvent e) {
                filter()
            }

            void filter() {
                String text = searchField.getText()
                if (text.length() == 0) {
                    sorter.setRowFilter(null)
                } else {
                    String caseInsensitive = convertToCaseInsensitiveRegex(text)
                    sorter.setRowFilter(RowFilter.regexFilter(caseInsensitive))
                }
                mainPanel.updateUI()
            }

            private String convertToCaseInsensitiveRegex(String text) {
                StringBuilder result = new StringBuilder()
                for (String letter in text) {
                    result.append("[${letter.toLowerCase()}${letter.toUpperCase()}]")
                }
                return result.toString()
            }
        }
    }

    private JPanel createUsernamePanel() {
        JLabel yourUsernameLabel = new JLabel("You are logged in as: " + config.username)
        Font usernameFont = yourUsernameLabel.getFont()
        yourUsernameLabel.setFont(new Font(usernameFont.getName(), usernameFont.getStyle(), 20))

        JPanel usernamePanel = new JPanel()
        usernamePanel.setPreferredSize(new Dimension(400, 40))
        usernamePanel.add(yourUsernameLabel)
        return usernamePanel
    }

    private JPanel createSearchPanel() {
        JLabel searchLabel = new JLabel("Select or search user to make a call: ")
        searchField = new JTextField(16)

        JPanel searchPanel = new JPanel()
        searchPanel.setLayout(new GridLayout(2, 1))
        searchPanel.add(searchLabel)
        searchPanel.add(searchField)
        return searchPanel
    }

    private JPanel createContactsPanel() {
        model = new DefaultTableModel()
        model.addColumn("Usernames")
        contactsTable = new JTable(model)
        sorter = new TableRowSorter<TableModel>(model)
        contactsTable.setRowSorter(sorter)

        JScrollPane scrollPane = new JScrollPane(contactsTable)
        scrollPane.setPreferredSize(new Dimension(350, 150))

        JPanel contactsPanel = new JPanel()
        contactsPanel.add(scrollPane)
        return contactsPanel
    }

    private JPanel createControlsPanel() {
        JButton connectButton = new JButton("Connect")
        connectButton.addActionListener(createConnectButtonListener())

        JButton logOutButton = new JButton("Log Out")
        logOutButton.addActionListener(createLogOutButtonListener())

        JPanel controlsPanel = new JPanel()
        controlsPanel.setPreferredSize(new Dimension(200, 50))
        controlsPanel.setLayout(new GridLayout(1, 2))
        controlsPanel.add(connectButton)
        controlsPanel.add(logOutButton)
        return controlsPanel
    }

    void create(JFrame frame) {
        super.create(frame)
        SwingUtilities.invokeLater {
            frame.getContentPane().removeAll()
            frame.repaint()
            frame.setSize(420, 350)
            frame.setResizable(false)

            JPanel mainPanel = new JPanel()
            mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))
            mainPanel.add(createUsernamePanel(), BorderLayout.CENTER)
            mainPanel.add(createSearchPanel(), BorderLayout.CENTER)
            mainPanel.add(createContactsPanel(), BorderLayout.CENTER)
            mainPanel.add(createControlsPanel(), BorderLayout.CENTER)

            // Additional listeners
            searchField.getDocument().addDocumentListener(createSearchFieldListener(mainPanel))
            createUserListListenerThread()

            frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
            frame.setVisible(true)
        }
    }

}
