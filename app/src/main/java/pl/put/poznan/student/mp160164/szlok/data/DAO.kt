package pl.put.poznan.student.mp160164.szlok.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimeDao{
    @Query("SELECT * FROM TrailTime")
    fun getAllTimes(): LiveData<List<TrailTime>>
    @Insert
    suspend fun insertTime(time: TrailTime)
}

@Dao
interface StartDao{
    @Query("SELECT * FROM TimerStart")
    fun getAllStarts(): LiveData<List<TimerStart>>
    @Query("SELECT * FROM TimerStart")
    fun getInitStarts(): List<TimerStart>

    @Insert
    suspend fun insertStart(start: TimerStart)
    @Update
    suspend fun updateStart(start: TimerStart)
    @Delete
    suspend fun deleteStart(start: TimerStart)
}

@Dao
interface FavDao{
    @Query("SELECT * FROM Favourite")
    fun getAllFavs(): LiveData<List<Favourite>>
    @Query("SELECT * FROM Favourite")
    fun getInitFavs(): List<Favourite>

    @Insert
    suspend fun insertFav(fav: Favourite)
    @Delete
    suspend fun deleteFav(fav: Favourite)
}