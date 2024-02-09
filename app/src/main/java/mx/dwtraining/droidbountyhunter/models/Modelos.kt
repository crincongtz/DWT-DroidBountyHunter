package mx.dwtraining.droidbountyhunter.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fugitivo(
    val id: Int = 0,
    @SerializedName("name")
    var name: String,
    var status: Int = 0
): Parcelable

/**
 * API POST Objects
 */
data class FugitivoRequest(
    @SerializedName("UDIDString")
    var udidString: String
)

data class FugitivoResponse(
    @SerializedName("mensaje")
    val mensaje: String
)