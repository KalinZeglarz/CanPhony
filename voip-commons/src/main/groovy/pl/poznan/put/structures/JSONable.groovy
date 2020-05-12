package pl.poznan.put.structures

import org.json.JSONObject

@FunctionalInterface
interface JSONable {

    JSONObject toJSON()

}