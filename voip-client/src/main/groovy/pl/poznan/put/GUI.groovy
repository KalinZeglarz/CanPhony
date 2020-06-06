package pl.poznan.put

import groovy.util.logging.Slf4j
import org.pushingpixels.substance.api.skin.SubstanceNightShadeLookAndFeel
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.windows.LoggedOutWindow

import javax.swing.*
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Slf4j
class GUI extends JFrame {

    ClientConfig config

    GUI() {
        super("CanPhony")
    }

    void start() {
        config = readServerAddress()
        new LoggedOutWindow(config).create(this)
    }

    static ClientConfig readServerAddress() {
        File configFile = new File("clientConfig.json")
        if (!configFile.exists()) {
            return new ClientConfig()
        }
        return ClientConfig.parseJSON(configFile.getText())
    }

    static void main(String[] args) throws InterruptedException {
        setDefaultLookAndFeelDecorated(true)
        SwingUtilities.invokeLater {
            try {
                UIManager.setLookAndFeel(SubstanceNightShadeLookAndFeel.class.getCanonicalName())
            } catch (Exception ignored) {
                log.error("Substance failed to initialize")
                System.exit(1)
            }
            GUI gui = new GUI()
            gui.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gui/icon.png")))
            gui.setSize(320, 200)
            gui.setLocationRelativeTo()
            log.info("starting gui client")
            gui.setDefaultCloseOperation(EXIT_ON_CLOSE)
            gui.addWindowListener(new WindowAdapter() {
                @Override
                void windowClosed(WindowEvent e) {
                }

                @Override
                void windowClosing(WindowEvent e) {
                    if (gui.config.username != null) {
                        log.info("window closing")
                        gui.config.httpClient.logout(gui.config.username)
                    }
                }
            })
            gui.start()
        }
    }

}
