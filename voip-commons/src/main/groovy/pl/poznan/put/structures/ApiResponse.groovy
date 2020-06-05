package pl.poznan.put.structures

import pl.poznan.put.structures.JSONable

abstract class ApiResponse implements JSONable {
    @Override
    String toString() {
        return this.toJSON().toString()
    }
}
