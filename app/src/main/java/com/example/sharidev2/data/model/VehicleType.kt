package com.example.sharidev2.data.model

enum class VehicleType {
    Sedans,
    SUVs,
    Minivans,
    Hatchbacks;

    companion object {
        fun fromString(value: String): VehicleType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: Sedans
        }
    }
}
