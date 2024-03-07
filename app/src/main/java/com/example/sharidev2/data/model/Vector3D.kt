package com.example.sharidev2.data.model

data class Vector3D(val x: Double, val y: Double, val z: Double) {
    // Function to add two vectors
    fun add(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }

    // Function to subtract two vectors
    fun subtract(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    // Function to calculate dot product with another vector
    fun dot(other: Vector3D): Double {
        return x * other.x + y * other.y + z * other.z
    }
}