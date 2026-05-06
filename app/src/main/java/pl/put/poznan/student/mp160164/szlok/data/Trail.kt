package pl.put.poznan.student.mp160164.szlok.data

import com.google.firebase.firestore.DocumentId

data class Trail(
    @DocumentId
    val id: String="",
    val name: String="",
    val type: String="",
    val difficulty: Int=1,
    val description: String="",
    val image: String="placeholder"
)

