package pl.poznan.put

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pl.poznan.put.managers.DatabaseManager

@SpringBootApplication
class Application {

    static void setupEnvironmentalVariables() {
        Map<String, String> config = [
                'security.require-ssl'         : 'true',
                'server.ssl.key-alias'         : 'selfsigned',
                'server.ssl.key-password'      : 'password',
                'server.ssl.key-store'         : 'voip-server/src/main/resources/keystore.jks',
                'server.ssl.key-store-provider': 'SUN',
                'server.ssl.key-store-password': 'password',
                'server.ssl.key-store-type'    : 'JKS'
        ]

        for (Map.Entry<String, String> param in config) {
            if (System.getProperty(param.key) == null) {
                System.setProperty(param.key, param.value)
            }
        }


    }

    static void main(String[] args) {
        setupEnvironmentalVariables()
        SpringApplication.run(Application.class, args)
        DatabaseManager.createDatabaseIfNotExists()
    }

}
