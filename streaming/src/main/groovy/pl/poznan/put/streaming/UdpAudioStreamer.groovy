package pl.poznan.put.streaming

import pl.poznan.put.audio.AudioStream

class UdpAudioStreamer {

    int sourcePort
    int destinationPort
    int sleepTime
    boolean stop
    Thread streamer
    AudioStream audioStream

    void start() {
        stop = false
        DatagramSocket socket = new DatagramSocket(sourcePort, InetAddress.localHost)
        streamer = new Thread({
            while (!stop) {
                byte[] data = audioStream.read()
                DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.localHost, destinationPort)
                socket.send(packet)
                sleep(sleepTime)
            }
        })
        streamer.start()
    }

    void stop() {
        stop = true
        streamer.join()
    }

}
