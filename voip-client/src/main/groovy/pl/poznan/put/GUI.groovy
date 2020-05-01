package pl.poznan.put

import java.util.concurrent.TimeUnit

class GUI {
    static void main(String[] args) throws InterruptedException {
        Window gui = new Window()
        gui.login()
        TimeUnit.SECONDS.sleep(5)
        gui.connection()
    }
}
