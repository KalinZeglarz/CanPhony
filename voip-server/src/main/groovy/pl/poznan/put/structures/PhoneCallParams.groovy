package pl.poznan.put.structures


import groovy.transform.PackageScope
import pl.poznan.put.GlobalConstants
import pl.poznan.put.audio.AudioQuality
import pl.poznan.put.security.EncryptionSuite

class PhoneCallParams {

    int sessionId = -1
    String sourceUsername = ""
    String targetUsername = ""
    EncryptionSuite sourceEncryptionSuite
    EncryptionSuite targetEncryptionSuite
    AudioQuality audioQuality = AudioQuality.LOW_MONO
    int bufferSize = GlobalConstants.AUDIO_BUFFER_SIZE

    @PackageScope
    PhoneCallParams() {}

}
