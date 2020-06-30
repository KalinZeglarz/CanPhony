package pl.poznan.put.windows

import pl.poznan.put.ClientConfig

import javax.swing.*

abstract class Window {

    protected ClientConfig config
    protected JFrame frame = null

    Window(ClientConfig config) {
        this.config = config
    }


    void create(JFrame frame) {
        this.frame = frame
    }

}
