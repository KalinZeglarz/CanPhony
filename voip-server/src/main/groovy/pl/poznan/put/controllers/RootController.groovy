package pl.poznan.put.controllers


import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @RequestMapping(value = "/")
    String test() {
        return "app is running"
    }

}
