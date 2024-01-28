package mx.dwtraining.droidbountyhunter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fugitivo(
    val id: Int = 0,
    var name: String = "",
    var status: Int = 0
): Parcelable
