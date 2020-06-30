package pl.poznan.put.windows


import pl.poznan.put.ClientConfig

@FunctionalInterface
interface SaveClientConfig {

    default void writeConfigToFile(ClientConfig config) {
        File configFile = new File("clientConfig.json")
        configFile.setText(config.toJSON().toString())
    }

}