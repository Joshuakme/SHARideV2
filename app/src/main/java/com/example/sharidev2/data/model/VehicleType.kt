package com.example.sharidev2.data.model

enum class VehicleType {
    Sedans,
    SUVs,
    Minivans,
    Hatchbacks;

    companion object {
        fun fromString(value: String): VehicleType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: Sedans
        }
    }
}
