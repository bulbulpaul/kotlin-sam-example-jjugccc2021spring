package com.merrylab.example.sam

import java.sql.Connection
import java.sql.DriverManager

val RDS_URL = System.getenv("RDS_URL") ?: "localhost:3306"

fun getConnection(): Connection {
    return DriverManager.getConnection(RDS_URL)
}