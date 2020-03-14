package pl.poznan.put.streaming

import pl.poznan.put.audio.AudioStream

class UdpAudioReceiver {

    int sourcePort
    int destinationPort
    int sleepTime
    boolean stop
    Thread receiver
    AudioStream audioStream

    void start() {
        stop = false
        DatagramSocket socket = new DatagramSocket(sourcePort, InetAddress.localHost)
        byte[] buf = new byte[audioStream.bufferSize]
        Thread receiver = new Thread({
            while (!stop) {
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length)
                socket.receive(packet)
                byte[] data = packet.getData()
                audioStream.write(data)
                sleep(sleepTime)
            }
        })
        receiver.start()
    }

    void stop() {
        stop = true
        receiver.join()
    }

}
