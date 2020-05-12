package pl.poznan.put.streaming

import groovy.util.logging.Slf4j
import pl.poznan.put.audio.AudioBuffer

@Slf4j
class UdpAudioReceiver {

    String localAddress
    int streamerPort
    int receiverPort
    int sleepTime
    boolean stop
    Thread receiverThread
    AudioBuffer audioBuffer
    private DatagramSocket socket

    void start() {
        log.info("starting receiving from address: ${localAddress}:${streamerPort}")
        stop = false
        socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.setReceiveBufferSize(audioBuffer.size)
        socket.bind(new InetSocketAddress(/*localAddress, */ receiverPort))
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
            } catch (SocketException ignored) {
            } catch (Exception e) {
                e.printStackTrace()
            }
        })
        receiverThread.start()
        log.info("started receiver")
    }

    void stop() {
        stop = true
        if (socket != null) {
            try {
                socket.close()
            } catch (SocketTimeoutException ignored) {
                log.info('interrupted read while closing socket')
            }
        }
        if (receiverThread != null) {
            receiverThread.interrupt()
        }
        log.info('stopped receiver')
    }

}
