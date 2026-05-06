package pl.put.poznan.student.mp160164.szlok.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.put.poznan.student.mp160164.szlok.R
import pl.put.poznan.student.mp160164.szlok.data.Trail

class DetailListViewModel: ViewModel() {
    private val _trail=MutableStateFlow(Trail())

    val trail: StateFlow<Trail> = _trail.asStateFlow()

    fun changeTrail(newTrail: Trail){
        _trail.value=newTrail
    }
}