package pl.poznan.put

import groovy.util.logging.Slf4j
import org.pushingpixels.substance.api.skin.SubstanceNightShadeLookAndFeel
import pl.poznan.put.structures.ClientConfig
import pl.poznan.put.windows.LoggedOutWindow

import javax.swing.*
import java.awt.*

@Slf4j
class GUI extends JFrame {

    GUI() {
        super("CanPhony")
    }

    void start() {
        new LoggedOutWindow(readServerAddress()).create(this)
    }

    static ClientConfig readServerAddress() {
        File configFile = new File('clientConfig.json')
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
            gui.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource('/gui/icon.png')))
            gui.setSize(320, 200)
            gui.setLocationRelativeTo()
            log.info("starting gui client")
            gui.start()
        }
    }

}
