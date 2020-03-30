package pl.poznan.put.examples

import groovy.transform.PackageScope
import groovy.util.logging.Log
import pl.poznan.put.audio.AudioBuffer

import pl.poznan.put.audio.Microphone
import pl.poznan.put.audio.Speakers

@Log
@PackageScope
class MicrophoneToSpeakersExample {

    static void main(String[] args) {
        final AudioBuffer audioBuffer = new AudioBuffer(4096)
        final Speakers speakers = new Speakers(audioBuffer)
        final Microphone microphone = new Microphone(audioBuffer)

        if(!speakers.start() || !microphone.start()){
            log.severe('microphone or speaker inaccessible')
            System.exit(0)
        }

        log.info("enter anything to stop ")
        System.in.newReader().readLine()

        microphone.stop()
        speakers.stop()
    }

}
