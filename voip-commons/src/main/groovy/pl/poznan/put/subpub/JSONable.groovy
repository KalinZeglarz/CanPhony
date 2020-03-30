package pl.poznan.put.subpub

import org.json.JSONObject

@FunctionalInterface
interface JSONable {

    abstract JSONObject toJSON()

}