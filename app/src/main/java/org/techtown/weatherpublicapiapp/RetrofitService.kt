package org.techtown.weatherpublicapiapp

import org.techtown.weatherpublicapiapp.data.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&pageNo=1&numOfRows=1000&dataType=json")
    fun getWeather(
        @Query("nx") nx : String,
        @Query("ny") ny : String,
        @Query("base_date") base_date : String,
        @Query("base_time") base_time : String
    ) : Call<Weather>

}