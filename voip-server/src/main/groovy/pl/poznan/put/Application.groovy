package pl.poznan.put

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pl.poznan.put.managers.DatabaseManager

@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args)
        DatabaseManager.createDatabaseIfNotExists()
    }

}
