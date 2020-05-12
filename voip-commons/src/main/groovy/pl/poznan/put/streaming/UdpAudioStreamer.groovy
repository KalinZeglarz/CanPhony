package pl.poznan.put.streaming

import groovy.util.logging.Slf4j
import pl.poznan.put.audio.AudioBuffer

@Slf4j
class UdpAudioStreamer {

    String remoteAddress
    int streamerPort
    int receiverPort
    int sleepTime
    boolean stop
    Thread streamer
    AudioBuffer audioBuffer

    void start() {
        log.info("starting streaming to address: ${remoteAddress}:${receiverPort}")
        stop = false
        DatagramSocket socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.bind(new InetSocketAddress(streamerPort))
        streamer = new Thread({
            while (!stop) {
                try {
                    byte[] data = audioBuffer.read()
                    if (data == null) {
                        break
                    }
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length,
                            InetAddress.getByName(remoteAddress), receiverPort)
                    socket.send(packet)
                    sleep(sleepTime)
                } catch (Exception e) {
                    e.printStackTrace()
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
