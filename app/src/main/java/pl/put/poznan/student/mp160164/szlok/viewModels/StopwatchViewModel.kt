package pl.put.poznan.student.mp160164.szlok.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pl.put.poznan.student.mp160164.szlok.data.AppDatabase
import pl.put.poznan.student.mp160164.szlok.data.StartDao
import pl.put.poznan.student.mp160164.szlok.data.TimeDao
import pl.put.poznan.student.mp160164.szlok.data.TimerStart
import pl.put.poznan.student.mp160164.szlok.data.Trail
import pl.put.poznan.student.mp160164.szlok.data.TrailRepository
import pl.put.poznan.student.mp160164.szlok.data.TrailTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.toDuration

class StopwatchViewModel(application: Application): AndroidViewModel(application) {
    enum class State {STARTED, STOPPED}
    private val _time = MutableStateFlow(0)
    private val _startTime = MutableStateFlow(TimeSource.Monotonic.markNow())
    private var _state= MutableStateFlow(State.STOPPED)
    private val _trail= MutableStateFlow("")
    private val _trailName= MutableStateFlow("")

    val state: StateFlow<State> = _state.asStateFlow()
    val time: StateFlow<Int> = _time.asStateFlow()
    val timerTrail: StateFlow<String> = _trail.asStateFlow()
    val timerTrailName: StateFlow<String> = _trailName.asStateFlow()


    private val timeDao: TimeDao = AppDatabase.getInstance(application).timeDao()
    private val startDao: StartDao = AppDatabase.getInstance(application).startDao()
    val trailTimes: LiveData<List<TrailTime>> = timeDao.getAllTimes()
    private val starts: LiveData<List<TimerStart>> = startDao.getAllStarts()
    private var startId: Int? = null

    init{
        viewModelScope.launch {
            var rec_starts=withContext(Dispatchers.IO){
                startDao.getInitStarts()
            }

            if(rec_starts.isNotEmpty()){
                Log.d("Stopwatch", "Restoring timer session:")
                if(rec_starts.size>1){
                    Log.d("Stopwatch", "Too many entries, culling")
                    withContext(Dispatchers.IO){
                        for (start in rec_starts){
                            startDao.deleteStart(start)
                        }
                    }
                }
                else{
                    val start=rec_starts.first()
                    Log.d("Stopwatch", "Restoring for trail ${start.trailId}")
                    startId=start.uid
                    val startTime=start.startTime
                    val trailId=start.trailId
                    val time=start.time
                    Log.d("Stopwatch", "Started at second $startTime with state ${start.state}")

                    _trail.value=trailId
                    updateTimerTrailName()

                    if(start.state=="STARTED"){
                        _startTime.value=TimeSource.Monotonic.markNow()
                            .minus((System.currentTimeMillis().div(1000)-startTime).toDuration(DurationUnit.SECONDS))
                        _state.value=State.STARTED
                        _time.value=_startTime.value.elapsedNow().toInt(DurationUnit.SECONDS)
                    }
                    else{
                        _startTime.value=TimeSource.Monotonic.markNow()
                            .minus(time.toDuration(DurationUnit.SECONDS))
                        _state.value=State.STOPPED
                        _time.value=time
                    }


                    viewModelScope.launch{
                        incrementTimer()
                    }

                    Log.d("Stopwatch", "Start at ${_startTime.value}")
                }
            }
            else Log.d("Stopwatch", "Nothing to restore")
        }

    }

    fun updateCurrentStart(){
        val startTime=System.currentTimeMillis().div(1000)-_time.value
        var state="STOPPED"
        if(_state.value==State.STARTED) state="STARTED"

        if(startId==null){
            runBlocking{
                startDao.insertStart(TimerStart(trailId = _trail.value, startTime = startTime, state=state, time=_time.value))
                val rec_starts=withContext(Dispatchers.IO){
                    startDao.getInitStarts()
                }
                val start=rec_starts.first()
                startId=start.uid
            }
        }
        else{
            val start= TimerStart(uid= startId!!, startTime,_trail.value, state=state, time=_time.value)
            updateStart(start, _trail.value, startTime, _state.value)
        }
    }

    fun addTime(trailId: String, time: Int, date: String) {
        viewModelScope.launch {
            timeDao.insertTime(TrailTime(trailId=trailId, time=time, date=date))
        }
    }

    fun addStart(trailId: String, startTime: Long, state: State) {
        var state="STOPPED"
        if(_state.value==State.STARTED) state="STARTED"
        viewModelScope.launch {
            startDao.insertStart(TimerStart(trailId=trailId, startTime=startTime, state=state, time=_time.value))
        }
    }

    fun updateStart(start: TimerStart, trailId: String, startTime: Long, state: State) {
        var state="STOPPED"
        if(_state.value==State.STARTED) state="STARTED"
        viewModelScope.launch {
            startDao.updateStart(TimerStart(uid=start.uid, trailId=trailId, startTime=startTime, state=state, time=_time.value))
        }
    }

    fun deleteStart(start: TimerStart) {
        viewModelScope.launch {
            startDao.deleteStart(start)
        }
    }

    private suspend fun incrementTimer(){
        while (_state.value==State.STARTED){
            _time.value=_startTime.value.elapsedNow().toInt(DurationUnit.SECONDS)
            delay(1000)
        }
        return
    }

    fun startTimer(trail: String){
        if(_state.value==State.STOPPED && (_trail.value==trail || _trail.value=="")) {
            if(_time.value > 0){
                _startTime.value =
                    TimeSource.Monotonic.markNow().minus(_time.value.toDuration(DurationUnit.SECONDS))
            }
            else{
                _startTime.value = TimeSource.Monotonic.markNow()
                _trail.value = trail
            }
            _state.value = State.STARTED
            viewModelScope.launch {
                incrementTimer()
            }
            updateCurrentStart()
            updateTimerTrailName()
        }
    }

    fun pauseTimer(trail: String){
        if(_state.value==State.STARTED  && _trail.value==trail){
            _state.value=State.STOPPED
            updateCurrentStart()
        }
    }

    fun resetTimer(){
        _state.value=State.STOPPED
        _time.value=0
        _trail.value=""
        _trailName.value=""
        viewModelScope.launch{
            var rec_starts=withContext(Dispatchers.IO){
                startDao.getInitStarts()
            }
            val start=rec_starts.find{it.uid==startId}
            if(start!=null) deleteStart(start)
        }

        if(starts.value!=null){
            deleteStart(starts.value!!.first())
        }

    }

    fun saveTime(){
        viewModelScope.launch{
            val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            addTime(trailId = _trail.value, time=_time.value, date=date)
        }
    }

    override fun onCleared(){
        updateCurrentStart()
    }

    fun updateTimerTrailName(){
        var name:String= ""
        if(_trail.value!=""){
            var trail: Trail?=null
            viewModelScope.launch{
                trail=withContext(Dispatchers.IO){ TrailRepository().getTrails().find{it.id==_trail.value} }
                trail?.let { name= it.name }
                _trailName.value=name
            }
        }

    }

}