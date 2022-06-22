package org.techtown.weatherpublicapiapp.data

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("response")
    val response: Response?
)