package com.sagealarm.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val ttsMessage: String,
    val repeatDays: String, // Comma-separated Calendar day values (e.g. "2,3,4")
    val musicUri: String?,
    val isEnabled: Boolean,
)
