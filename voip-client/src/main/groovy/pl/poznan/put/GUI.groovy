package pl.poznan.put


import groovy.util.logging.Slf4j
import org.json.JSONObject
import pl.poznan.put.windows.LoggedOutWindow
import org.pushingpixels.substance.api.skin.SubstanceNightShadeLookAndFeel

import javax.swing.*
import javax.swing.plaf.FontUIResource

@Slf4j
class GUI extends JFrame   {

    GUI(){
        super("CanPhony")
    }

    void start() {
        new LoggedOutWindow(readServerAddress()).create(this)
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

    static void setUIFont (FontUIResource f){
        Enumeration keys = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement()
            Object value = UIManager.get (key)
            if (value instanceof FontUIResource)
                UIManager.put (key, f)
        }
    }

    static void main(String[] args) throws InterruptedException {
        setDefaultLookAndFeelDecorated(true)
        SwingUtilities.invokeLater {
            try {
                UIManager.setLookAndFeel(SubstanceNightShadeLookAndFeel.class.getCanonicalName())
                //setUIFont (new FontUIResource("Helvetica", Font.BOLD,12))
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
