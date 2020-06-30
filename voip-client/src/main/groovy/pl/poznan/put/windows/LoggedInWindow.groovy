package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.pubsub.Message
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.structures.UserStatus
import pl.poznan.put.structures.api.CallHistoryResponse
import pl.poznan.put.structures.api.PhoneCallResponse
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

import static pl.poznan.put.pubsub.MessageAction.*

@Slf4j
class LoggedInWindow extends Window {

    private static final int USER_LIST_REQUEST_PERIOD = 5000
    private Thread userListListenerThread
    private stopUserListListener = false

    JTextField searchField
    DefaultTableModel contactsModel
    JTable contactsTable
    TableRowSorter<TableModel> contactsSorter
    boolean callRequestResponded = false
    boolean ignoreCall = false

    LoggedInWindow(ClientConfig config) {
        super(config)
        redisCallRequestSubscribe(this.config.username)
    }

    private void redisCallRequestSubscribe(String username) {
        log.info("[server] subscribing with call request callback")
        config.redisClient.subscribeChannelWithUnsubscribeAll(username, username) { String channelName, Message message ->
            if (message.action == CALL_REQUEST && config.phoneCallClient == null) {
                log.info("[${channelName}] received call request: " + message.content)
                PhoneCallResponse phoneCallResponse = PhoneCallResponse.parseJSON(message.content)
                SwingUtilities.invokeLater {
                    boolean accepted = !JOptionPane.showOptionDialog(frame,
                            "${phoneCallResponse.sourceUsername} wants to start a call with you.",
                            "Call Request",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            ["Accept", "Reject"] as String[],
                            "Accept")

                    if (ignoreCall) {
                        JOptionPane.showOptionDialog(frame,
                                "Call request reached 30s timeout.",
                                "Call request timeout",
                                JOptionPane.OK_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                ['Ok'] as String[],
                                'Ok')
                        ignoreCall = false
                        return
                    }

                    if (accepted) {
                        config.redisClient.unsubscribe(username)

                        config.currentCallUsername = phoneCallResponse.sourceUsername
                        config.redisClient.publishMessage(username, config.currentCallUsername, new Message(
                                action: ACCEPT_CALL, sender: username))
                        startCall(config.serverAddress, phoneCallResponse.forwarderPort)
                    } else {
                        config.httpClient.rejectCall(phoneCallResponse.targetUsername, phoneCallResponse.sourceUsername)
                    }
                }
            } else if (message.action == END_CALL) {
                ignoreCall = true
            }
        }
    }

    private void redisStartCallSubscribe(PhoneCallResponse response) {
        log.info("[${config.username}] subscribing with start call callback")
        callRequestResponded = false
        config.redisClient.subscribeChannelWithUnsubscribeAll(config.username, config.username) { String channelName, Message message ->
            if (ignoreCall || message.action == REJECT_CALL) {
                log.info("[${channelName}] ignored call or received call reject: " + message.content)
                config.redisClient.publishMessage(config.username, config.currentCallUsername, new Message(action: END_CALL,
                        sender: config.currentCallUsername))
                redisCallRequestSubscribe(config.username)
                if (ignoreCall) {
                    return
                }
                SwingUtilities.invokeLater {
                    JOptionPane.showOptionDialog(frame,
                            "User ${response.targetUsername} rejected your call request.",
                            "Call rejected",
                            JOptionPane.OK_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            ['Ok'] as String[],
                            'Ok')
                }
                ignoreCall = false
                return
            }
            callRequestResponded = true
            if (message.action == ACCEPT_CALL && config.phoneCallClient == null) {
                log.info("[${channelName}] call request accepted")
                startCall(config.serverAddress, response.forwarderPort)
            }
        }
    }

    private void startCall(String serverAddress, int forwarderPort) {
        config.phoneCallClient = new PhoneCallClient(serverAddress, forwarderPort, config.encryptionSuite)
        config.phoneCallClient.start()
        stopUserListListener = true
        new CallWindow(config).create(frame)
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
                config.currentCallUsername = contactsModel.getValueAt(row, 0).toString()

                //Check if busy/inactive
                UserStatus targetUserStatus = UserStatus.valueOf(contactsModel.getValueAt(row, 1).toString().toUpperCase())

                if (targetUserStatus == UserStatus.BUSY || targetUserStatus == UserStatus.INACTIVE) {
                    String status = targetUserStatus.toString().toLowerCase()
                    JOptionPane.showOptionDialog(frame,
                            "${config.currentCallUsername} is ${status} now. Try again later.",
                            "User unreachable",
                            JOptionPane.OK_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            ['Ok'] as String[],
                            'Ok')
                } else {
                    // Try to connect
                    PhoneCallResponse response = config.httpClient.startCall(config.username, config.currentCallUsername)
                    redisStartCallSubscribe(response)
                    int timeout = 0
                    while (!callRequestResponded && timeout < 150) {
                        sleep(20)
                        timeout++
                    }
                    if (timeout >= 150) {
                        ignoreCall = true
                        JOptionPane.showOptionDialog(frame,
                                "Call request reached 30s timeout.",
                                "Call request timeout",
                                JOptionPane.OK_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                ['Ok'] as String[],
                                'Ok')
                    }
                }
            }
        }
    }

    private ActionListener createLogOutButtonListener() {
        return new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked log out button")
                stopUserListListener = true
                config.httpClient.logout(config.username)
                config.redisClient.unsubscribe(config.username + "_beacon")
                config.username = null
                new LoggedOutWindow(config).create(frame)
            }
        }
    }

    private void createUserListListenerThread() {
        userListListenerThread = new Thread({
            while (!stopUserListListener) {
                Map<String, UserStatus> userList = config.httpClient.getUserList(config.username)
                contactsModel.setRowCount(0)
                for (Map.Entry<String, UserStatus> user in userList) {
                    contactsModel.addRow(user.key, user.value.toString().toLowerCase())
                }
                sleep(USER_LIST_REQUEST_PERIOD)
            }
            log.info("stopped user list listener thread")
        })
        userListListenerThread.start()
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
                    contactsSorter.setRowFilter(null)
                } else {
                    String caseInsensitive = convertToCaseInsensitiveRegex(text)
                    contactsSorter.setRowFilter(RowFilter.regexFilter(caseInsensitive))
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

    private JTabbedPane createTabMenu() {
        JTabbedPane tabbedPane = new JTabbedPane()
        tabbedPane.addTab("Contacts", createContactsPanel())
        tabbedPane.addTab("History", createHistoryPanel())
        return tabbedPane
    }

    private JPanel createContactsPanel() {
        contactsModel = new DefaultTableModel()
        contactsModel.addColumn("Username")
        contactsModel.addColumn("Status")
        contactsTable = new JTable(contactsModel)
        contactsTable.setDefaultEditor(Object.class, null)
        contactsSorter = new TableRowSorter<TableModel>(contactsModel)
        contactsTable.setRowSorter(contactsSorter)

        JScrollPane scrollPane = new JScrollPane(contactsTable)
        scrollPane.setPreferredSize(new Dimension(350, 150))

        JPanel contactsPanel = new JPanel()
        contactsPanel.add(scrollPane)
        return contactsPanel
    }

    private JPanel createHistoryPanel() {
        DefaultTableModel historyModel = new DefaultTableModel()
        historyModel.addColumn("Username")
        historyModel.addColumn("Date")
        historyModel.addColumn("Duration")
        JTable historyTable = new JTable(historyModel)
        historyTable.setDefaultEditor(Object.class, null)
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(historyModel)
        historyTable.setRowSorter(sorter)

        JScrollPane scrollPane = new JScrollPane(historyTable)
        scrollPane.setPreferredSize(new Dimension(350, 150))

        JPanel historyPanel = new JPanel()
        historyPanel.add(scrollPane)

        CallHistoryResponse callHistory = config.httpClient.getCallHistory(config.username)
        for (int i = 0; i < callHistory.getSize(); i++) {
            historyModel.addRow(callHistory.usernames[i], callHistory.dates[i], callHistory.durations[i])
        }
        return historyPanel
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
            frame.setSize(420, 390)
            frame.setResizable(false)

            JPanel mainPanel = new JPanel()
            mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))
            mainPanel.add(createUsernamePanel(), BorderLayout.CENTER)
            mainPanel.add(createSearchPanel(), BorderLayout.CENTER)
            mainPanel.add(createTabMenu(), BorderLayout.CENTER)
            mainPanel.add(createControlsPanel(), BorderLayout.CENTER)

            // Additional listeners
            searchField.getDocument().addDocumentListener(createSearchFieldListener(mainPanel))
            createUserListListenerThread()

            frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
            frame.setVisible(true)
        }
    }

}
