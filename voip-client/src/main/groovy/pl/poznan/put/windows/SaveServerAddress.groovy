package pl.poznan.put.windows

import org.json.JSONObject

@FunctionalInterface
interface SaveServerAddress {

    default String[] saveServerAddress(String[] configs) {
        File configFile = new File('clientConfig.json')
        if (!configFile.exists()) {
            configFile.setText('{}')
        }
        JSONObject configJson = new JSONObject(configFile.getText())
        configJson.put('serverAddress', configs[0])
        configJson.put('serverPort', configs[1])
        configFile.setText(configJson.toString())
        return configs
    }

}