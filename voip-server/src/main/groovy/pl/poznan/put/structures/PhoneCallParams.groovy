package pl.poznan.put.structures

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import pl.poznan.put.audio.AudioQuality

class PhoneCallParams {

    int sessionId = -1
    String clientAddress1 = ""
    String clientAddress2 = ""
    AudioQuality audioQuality = AudioQuality.LOW_MONO
    int bufferSize = 4096

    @PackageScope
    PhoneCallParams() {}

}
