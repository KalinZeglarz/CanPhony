package pl.poznan.put


import groovy.util.logging.Slf4j
import org.json.JSONObject
import pl.poznan.put.windows.LoginWindow

import javax.swing.*

@Slf4j
class GUI extends JFrame   {

    GUI(){
        super("CanPhony")
    }

    void start() {
        new LoginWindow(readServerAddress()).create(this)
    }

    static String readServerAddress() {
        File configFile = new File('clientConfig.json')
        if (!configFile.exists()) {
            return ''
        }
        JSONObject configJson = new JSONObject(configFile.getText())
        return configJson.getString('serverAddress')
    }

    static void main(String[] args) throws InterruptedException {
        GUI gui = new GUI()
        gui.setSize(320, 200)
        gui.setLocationRelativeTo()
        log.info("starting gui client")
        gui.start()
    }

}
