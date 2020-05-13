package pl.poznan.put.windows

import groovy.util.logging.Slf4j
import pl.poznan.put.PhoneCallClient
import pl.poznan.put.VoipHttpClient
import pl.poznan.put.structures.PhoneCallResponse
import pl.poznan.put.subpub.Message
import pl.poznan.put.subpub.MessageFactory
import pl.poznan.put.subpub.RedisClient
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
class ConnectionWindow extends Window {

    private static final int USER_LIST_REQUEST_PERIOD = 30000

    final String username
    final String serverAddress
    final VoipHttpClient httpClient
    final RedisClient redisClient
    PhoneCallClient phoneCallClient = null
    Integer currentSessionId = null
    Thread userListListener

    ConnectionWindow(VoipHttpClient httpClient, RedisClient redisClient) {
        this.httpClient = httpClient
        this.redisClient = redisClient
        username = httpClient.username
        serverAddress = httpClient.serverAddress.split(':')[0]
        redisCallRequestSubscribe(httpClient.username)
    }

    private void redisCallRequestSubscribe(String username) {
        log.info("[${username}] subscribing with call request callback")
        redisClient.subscribeChannel(username) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == CALL_REQUEST && phoneCallClient == null) {
                log.info("[${channelName}] received call request: " + message.content)
                redisClient.unsubscribe(username)
                PhoneCallResponse phoneCallResponse = PhoneCallResponse.parseJSON(message.content)
                currentSessionId = phoneCallResponse.sessionId

                redisEndCallSubscribe(currentSessionId)
                redisClient.publishMessage(currentSessionId, MessageFactory.createMessage(ACCEPT_CALL, username))
                phoneCallClient = new PhoneCallClient(serverAddress, phoneCallResponse.forwarderPort)
                phoneCallClient.start()
            }
        }
    }

    private void redisEndCallSubscribe(int sessionId) {
        log.info("[${sessionId}] subscribing with end call callback")
        redisClient.subscribeChannel(sessionId.toString()) { String channelName, String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == END_CALL && phoneCallClient != null) {
                log.info("[${channelName}] received end call")
                redisClient.unsubscribe(channelName)
                phoneCallClient.stop()
                phoneCallClient = null
                currentSessionId = null
                redisCallRequestSubscribe(httpClient.username)
            }
        }
    }

    private void redisStartCallSubscribe(PhoneCallResponse response) {
        log.info("[${response.sessionId}] subscribing with start call callback")
        redisClient.unsubscribe(username)
        redisClient.subscribeChannel(response.sessionId.toString()) { String channelName,
                                                                      String messageString ->
            Message message = Message.parseJSON(messageString)
            if (message.sender == username) {
                return
            }
            if (message.action == ACCEPT_CALL && phoneCallClient == null) {
                log.info("[${channelName}] call request accepted")
                phoneCallClient = new PhoneCallClient(serverAddress, response.forwarderPort)
                phoneCallClient.start()
            } else if (message.action == END_CALL && phoneCallClient != null) {
                log.info("[${channelName}] received end call: " + message.content)
                redisClient.unsubscribe(channelName)
                phoneCallClient.stop()
                phoneCallClient = null
                currentSessionId = null
                redisCallRequestSubscribe(httpClient.username)
            }
        }
    }

    void create(JFrame frame) {

        // Cleaning frame
        frame.getContentPane().removeAll()
        frame.repaint()
        frame.setSize(420, 350)

        // Main panel
        JPanel mainPanel = new JPanel()
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER))

        // Username
        JPanel usernamePanel = new JPanel()
        usernamePanel.setPreferredSize(new Dimension(400, 20))
        JLabel yourUsernameLabel = new JLabel("Logged in as: " + username)
        usernamePanel.add(yourUsernameLabel)

        // Search
        JPanel searchPanel = new JPanel()

        searchPanel.setLayout(new GridLayout(2, 1))

        JLabel searchLabel = new JLabel("Select or search user to make a call: ")
        JTextField searchField = new JTextField(16)

        searchPanel.add(searchLabel)
        searchPanel.add(searchField)

        // Contacts
        JPanel contactsPanel = new JPanel()

        DefaultTableModel model = new DefaultTableModel()
        model.addColumn("Usernames")
        JTable contactsTable = new JTable(model)
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model)
        contactsTable.setRowSorter(sorter)

        userListListener = new Thread({
            while (!Thread.currentThread().isInterrupted()) {
                Set<String> userList = this.httpClient.getUserList()
                model.setRowCount(0)
                for (String user in userList) {
                    model.addRow(user)
                }
                sleep(USER_LIST_REQUEST_PERIOD)
            }
        })
        userListListener.start()

        JScrollPane scrollPane = new JScrollPane(contactsTable)
        scrollPane.setPreferredSize(new Dimension(350, 150))
        contactsPanel.add(scrollPane)

        // Controls
        JPanel controlsPanel = new JPanel()
        controlsPanel.setPreferredSize(new Dimension(200, 50))
        controlsPanel.setLayout(new GridLayout(1, 2))

        JButton connectButton = new JButton("Connect")
        connectButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked connect button")
                if (!searchField.getText().isEmpty()) {
                    PhoneCallResponse response = httpClient.startCall(searchField.getText())
                    currentSessionId = response.sessionId
                    redisStartCallSubscribe(response)
                }
            }
        })

        JButton disconnectButton = new JButton("Disconnect")
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                log.info("clicked disconnect button")
                if (phoneCallClient != null) {
                    redisClient.unsubscribe(currentSessionId)
                    redisClient.publishMessage(currentSessionId, MessageFactory.createMessage(END_CALL, username))
                    phoneCallClient.stop()
                    phoneCallClient = null
                    currentSessionId = null
                    redisCallRequestSubscribe(httpClient.username)
                } else {
                    log.warn('phone call client is not assigned')
                }
            }
        })
        controlsPanel.add(connectButton)
        controlsPanel.add(disconnectButton)

        // Listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
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
        })

        // Adding components to main panel
        mainPanel.add(usernamePanel, BorderLayout.CENTER)
        mainPanel.add(searchPanel, BorderLayout.CENTER)
        mainPanel.add(contactsPanel, BorderLayout.CENTER)
        mainPanel.add(controlsPanel, BorderLayout.CENTER)

        // Adding main panel to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel)
        frame.setVisible(true)
    }

}
