package pl.poznan.put

import groovy.util.logging.Slf4j
import org.json.JSONObject
import org.pushingpixels.substance.api.skin.SubstanceNightShadeLookAndFeel
import pl.poznan.put.windows.LoginWindow

import javax.swing.*

@Slf4j
class GUI extends JFrame {

    GUI() {
        super("CanPhony")
    }

    void start() {
        new LoginWindow(readServerAddress()).create(this)
    }

    static String[] readServerAddress() {
        File configFile = new File('clientConfig.json')
        if (!configFile.exists()) {
            String [] blank = ['','']
            return blank
        }
        JSONObject configJson = new JSONObject(configFile.getText())
        String[] configs = [configJson.getString('serverAddress'), configJson.getString('serverPort')]
        System.out.println(configs)
        return configs
    }

    static void main(String[] args) throws InterruptedException {
        setDefaultLookAndFeelDecorated(true)
        SwingUtilities.invokeLater {
            try {
                UIManager.setLookAndFeel(SubstanceNightShadeLookAndFeel.class.getCanonicalName())
            } catch (Exception ignored) {
                log.error("Substance Graphite failed to initialize")
                System.exit(1)
            }
            GUI gui = new GUI()
            gui.setSize(320, 200)
            gui.setLocationRelativeTo()
            log.info("starting gui client")
            gui.start()
        }
    }

}
