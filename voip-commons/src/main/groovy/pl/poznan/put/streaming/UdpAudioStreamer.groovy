package pl.poznan.put.streaming

import groovy.util.logging.Log
import pl.poznan.put.audio.AudioBuffer

@Log
class UdpAudioStreamer {

    int streamerPort
    int receiverPort
    int sleepTime
    boolean stop
    Thread streamer
    AudioBuffer audioBuffer

    void start() {
        stop = false
        DatagramSocket socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.bind(new InetSocketAddress(streamerPort))
        streamer = new Thread({
            while (!stop) {
                try {
                    byte[] data = audioBuffer.read()
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.localHost, receiverPort)
                    socket.send(packet)
                    sleep(sleepTime)
                } catch (Exception ignored) {
                    socket.close()
                }
            }
            socket.close()
        })
        streamer.start()
        log.info("started streamer")
    }

    void stop() {
        stop = true
        if (streamer != null) {
            streamer.interrupt()
        }
        log.info("stopped streamer")
    }

}
