package pl.poznan.put.streaming


import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.security.EncryptionSuite

@Slf4j
class UdpAudioStreamer {

    String remoteAddress
    int streamerPort
    int receiverPort
    int sleepTime
    Thread streamerThread
    AudioBuffer audioBuffer
    private DatagramSocket socket
    private EncryptionSuite encryptionSuite
    private boolean stop

    void start() {
        log.info("starting streaming to address: ${remoteAddress}:${receiverPort}")
        if (encryptionSuite == null) {
            log.info("audio streamer encryption is disabled")
        }

        stop = false
        socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.bind(new InetSocketAddress(streamerPort))
        streamerThread = createStreamerThread()
        streamerThread.start()
        log.info("started streamer")
    }

    @SuppressWarnings('GrReassignedInClosureLocalVar')
    private Thread createStreamerThread() {
        return new Thread({
            byte[] data = null
            Thread udpSendThread = new Thread({
                while (!stop) {
                    if (data == null) {
                        sleep(1)
                        continue
                    }
                    byte[] dataToSend = data.collect()
                    data = null
                    if (GlobalConstants.ENCRYPT_AUDIO && encryptionSuite != null) {
                        dataToSend = encryptionSuite.encryptAudio(dataToSend)
                        if (dataToSend == null) {
                            throw new RuntimeException("audio encryption failed")
                        }
                    }

                    DatagramPacket packet = new DatagramPacket(dataToSend, 0, dataToSend.length,
                            InetAddress.getByName(remoteAddress), receiverPort)
                    socket.send(packet)
                }
            })
            udpSendThread.start()
            while (!stop) {
                try {
                    data = audioBuffer.read()
                    if (data == null) {
                        break
                    }
                    sleep(sleepTime)
                } catch (Exception e) {
                    e.printStackTrace()
                    socket.close()
                }
            }
            socket.close()
        })
    }

    void stop() {
        stop = true
        if (streamerThread != null) {
            streamerThread.interrupt()
        }
        log.info("stopped streamer")
    }

}
