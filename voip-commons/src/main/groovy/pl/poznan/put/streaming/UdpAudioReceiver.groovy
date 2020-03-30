package pl.poznan.put.streaming

import groovy.util.logging.Log
import pl.poznan.put.audio.AudioBuffer

@Log
class UdpAudioReceiver {

    int streamerPort
    int receiverPort
    int sleepTime
    boolean stop
    Thread receiverThread
    AudioBuffer audioBuffer
    private DatagramSocket socket

    void start() {
        stop = false
        socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.setReceiveBufferSize(audioBuffer.size)
        socket.bind(new InetSocketAddress(receiverPort))
        byte[] buf = new byte[audioBuffer.size]
        receiverThread = new Thread({
            try {
                while (!stop) {
                    DatagramPacket packet = new DatagramPacket(buf, 0, buf.length)
                    socket.receive(packet)
                    byte[] data = packet.getData()
                    audioBuffer.write(data)
                    sleep(sleepTime)
                }
            } catch (Exception ignored) {
            }
        })
        receiverThread.start()
        log.info("started receiver")
    }

    void stop() {
        stop = true
        if(socket != null) {
            socket.close()
        }
        if (receiverThread != null) {
            receiverThread.join()
        }
    }

}
