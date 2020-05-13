package pl.poznan.put.windows

import org.json.JSONObject

@FunctionalInterface
interface SaveServerAddress {

    default String[] saveServerAddress(String serverAddress, String serverPort) {
        File configFile = new File('clientConfig.json')
        if (!configFile.exists()) {
            configFile.setText('{}')
        }
        JSONObject configJson = new JSONObject(configFile.getText())
        configJson.put('serverAddress', serverAddress)
        configJson.put('serverPort', serverPort)
        configFile.setText(configJson.toString())
        return serverAddress
    }

}