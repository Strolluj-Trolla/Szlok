package pl.put.poznan.student.mp160164.szlok.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.put.poznan.student.mp160164.szlok.data.Trail
import pl.put.poznan.student.mp160164.szlok.data.TrailRepository

class MainListViewModel : ViewModel() {
    private val _list= MutableStateFlow(listOf(emptyList<Trail>(), emptyList()))
    private val _pieszeActive= MutableStateFlow(true)
    private val _roweroweActive= MutableStateFlow(false)
    val trailRepository= TrailRepository()
    private var listenerRegistration: ListenerRegistration? = null
    private val _shouldAnimate = MutableStateFlow(true)
    private val _loaded = MutableStateFlow(false)
    private val _queries = MutableStateFlow(listOf("", ""))



    val list: StateFlow<List<List<Trail>>> = _list.asStateFlow()
    val pieszeActive: StateFlow<Boolean> = _pieszeActive.asStateFlow()
    val roweroweActive: StateFlow<Boolean> = _roweroweActive.asStateFlow()
    val shouldAnimate: StateFlow<Boolean> = _shouldAnimate.asStateFlow()
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()
    val queries: StateFlow<List<String>> = _queries.asStateFlow()

    fun toRower(){
        _pieszeActive.value=false
        _roweroweActive.value=!_pieszeActive.value
    }

    fun toStopa(){
        _pieszeActive.value=true
        _roweroweActive.value=!_pieszeActive.value
    }

    fun startSync() {
        listenerRegistration = trailRepository.observeTrails(
            onDataChanged = { trails ->
                run {
                    val new_piesze: MutableList<Trail> = mutableListOf()
                    val new_rowerowe: MutableList<Trail> = mutableListOf()
                    for (trail in trails.sortedBy{it.name}) {
                        if (trail.type=="piesza") new_piesze += trail
                        else if (trail.type=="rowerowa") new_rowerowe += trail
                    }
                    val newList=listOf(new_piesze.toList(), new_rowerowe.toList())
                    _list.value=newList
                    _loaded.value=true
                }
            },
            onError = { e ->e.printStackTrace()}
        )
    }

    fun doneAnimating(){
        _shouldAnimate.value=false
    }

    fun changeQuery(idx: Int, query: String){
        var piesze=_queries.value[0]
        var rowerowe=_queries.value[1]
        if(idx==0)piesze=query
        else rowerowe=query
        _queries.value=listOf(piesze, rowerowe)
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

}