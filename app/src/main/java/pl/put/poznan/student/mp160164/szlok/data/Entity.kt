package pl.put.poznan.student.mp160164.szlok.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrailTime(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val trailId: String,
    val time: Int,
    val date: String
)

@Entity
data class TimerStart(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val startTime: Long,
    val trailId: String,
    val time: Int,
    val state: String
)

@Entity
data class Favourite(
    @PrimaryKey(autoGenerate = true) val uid: Int=0,
    val trailId: String
)