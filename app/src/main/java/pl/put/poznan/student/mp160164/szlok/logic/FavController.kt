package pl.put.poznan.student.mp160164.szlok.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.put.poznan.student.mp160164.szlok.data.AppDatabase
import pl.put.poznan.student.mp160164.szlok.data.Favourite

class FavController(application: Application): AndroidViewModel(application) {
    private val favDao = AppDatabase.getInstance(application).favDao()

    val favs: LiveData<List<Favourite>> = favDao.getAllFavs()

    fun toggleFav(trailId: String) {
        var add=false
        val data= favs.value
        var fav: Favourite?=null
        if(data!=null){
            fav=data.find{it.trailId==trailId}
            if(fav==null)
                add=true
        }

        viewModelScope.launch {
            if(add) favDao.insertFav(Favourite(trailId=trailId))
            else favDao.deleteFav(fav!!)
        }
    }
}