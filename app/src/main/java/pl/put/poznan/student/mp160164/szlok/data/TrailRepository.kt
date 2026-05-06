package pl.put.poznan.student.mp160164.szlok.data
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList


class TrailRepository {
    private val db = FirebaseFirestore.getInstance()
    private val trailCollection = db.collection("trails")

    fun observeTrails(
        onDataChanged: (List<Trail>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return trailCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            val trails = snapshot?.documents?.mapNotNull { document -> document.toObject(Trail::class.java)} ?: emptyList()
            onDataChanged(trails)
        }
    }

    suspend fun getTrails(): List<Trail> {
        val snapshot = trailCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Trail::class.java) }
    }
}