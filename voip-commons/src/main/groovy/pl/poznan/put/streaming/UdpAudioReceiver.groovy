package pl.poznan.put.streaming

import groovy.util.logging.Slf4j
import pl.poznan.put.GlobalConstants
import pl.poznan.put.audio.AudioBuffer
import pl.poznan.put.security.EncryptionSuite

@Slf4j
class UdpAudioReceiver {

    String localAddress
    int streamerPort
    int receiverPort
    int sleepTime
    Thread receiverThread
    AudioBuffer audioBuffer
    private DatagramSocket socket
    private EncryptionSuite encryptionSuite
    private boolean stop

    void start() {
        log.info("starting receiving from address: ${localAddress}:${streamerPort}")
        if (encryptionSuite == null) {
            log.info("audio receiver encryption is disabled")
        }

        stop = false
        socket = new DatagramSocket(null as SocketAddress)
        socket.setReuseAddress(true)
        socket.setReceiveBufferSize(audioBuffer.size)
        socket.bind(new InetSocketAddress(receiverPort))
        receiverThread = createReceiverThread()
        receiverThread.start()
        log.info("started receiver")
    }

    @SuppressWarnings('GrReassignedInClosureLocalVar')
    private Thread createReceiverThread() {
        return new Thread({
            byte[] sampleBuffer = new byte[audioBuffer.size]
            try {
                byte[] data = null
                Thread bufferWriteThread = new Thread({
                    byte[] dataToWrite
                    while (!stop) {
                        if (data != null) {
                            dataToWrite = data.collect()
                            data = null
                        } else if (dataToWrite == null) {
                            sleep(1)
                            continue
                        }
                        if (GlobalConstants.ENCRYPT_AUDIO && encryptionSuite != null) {
                            dataToWrite = encryptionSuite.decryptAudio(dataToWrite)
                            if (dataToWrite == null) {
                                throw new RuntimeException("audio decryption failed")
                            }
                        }
                        audioBuffer.write(dataToWrite)
                        dataToWrite = null
                    }
                })
                bufferWriteThread.start()

                while (!stop) {
                    DatagramPacket packet = new DatagramPacket(sampleBuffer, 0, sampleBuffer.length)
                    socket.receive(packet)
                    data = packet.getData()
                    sleep(sleepTime)
                }
            } catch (SocketException ignored) {
            } catch (Exception e) {
                e.printStackTrace()
            }
        })
    }

    void stop() {
        stop = true
        if (socket != null) {
            try {
                socket.close()
            } catch (SocketTimeoutException ignored) {
                log.info("interrupted read while closing socket")
            }
        }
        if (receiverThread != null) {
            receiverThread.interrupt()
        }
        log.info("stopped receiver")
    }

}
