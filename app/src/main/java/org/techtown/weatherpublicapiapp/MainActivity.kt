package org.techtown.weatherpublicapiapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.techtown.weatherpublicapiapp.data.Weather
import org.techtown.weatherpublicapiapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var cancellationTokenSource : CancellationTokenSource? = null

    private lateinit var geocoder: Geocoder

    private var lon : Double = 0.0
    private var lat : Double = 0.0

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()
        bindViews()

        val window = this.window
        window.statusBarColor
    }

    @SuppressLint("NewApi")
    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            Log.d("testt refresh","refresh")
            binding.constraintlayout2.visibility = View.INVISIBLE
            binding.recyvlerView.visibility = View.INVISIBLE
            fetchLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun connectRetrofit() {

        val url2 = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&pageNo=1&numOfRows=1000&dataType=json&base_date=20220527&base_time=0500&nx=62&ny=125"

        val gson : Gson = GsonBuilder()
            .setLenient()
            .create()
        // 에러나서 수정한 부분 GsonConverterFactory.create() 여기에 gson 넣어줌
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://apis.data.go.kr/")
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//        val retrofitService = retrofit.create(RetrofitService::class.java)

        val convertXY2 = convertGRID_GPS(TO_GRID, lat, lon)

        val cal = Calendar.getInstance()
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"))
        var baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        var baseDateM = SimpleDateFormat("MM", Locale.getDefault()).format(cal.time) // 현재 날짜 m
        var baseDateD = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time) // 현재 날짜 d
        var baseDateE = SimpleDateFormat("E", Locale.getDefault()).format(cal.time) // 현재 날짜 요일
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시각
        val timeM = SimpleDateFormat("mm", Locale.getDefault()).format(cal.time) // 현재 분
        val baseTime = getBaseTime(timeH, timeM)

        if (timeH == "00" && baseTime == "2330") {
            cal.add(Calendar.DATE, -1).toString()
            baseDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        //binding.date.text = "${baseDateM}월 ${baseDateD}일 ${baseDateE}요일"

        Log.d("testt date","$baseDate")
        Log.d("testt time","$time")
        Log.d("testt hour", "$timeH")
        Log.d("testt minute", "$timeM")
        Log.d("testt baseTime", "$baseTime")

        //val time2 = cal.add(Calendar.MINUTE,-30)
        //Log.d("testt time2","${time2.toString()}")

        Log.d("testt lat lon", "${convertXY2.x.toInt()}, ${convertXY2.y.toInt()}")

        RetrofitObject.apiService.getWeather(convertXY2.x.toInt().toString(), convertXY2.y.toInt().toString(), baseDate, baseTime)
            .enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful) {

                    val item = response.body()
                    val itemList = item?.response?.body?.items?.item

                    Log.d("testt itemlist","${itemList}")
                    Log.d("testt totalcount","${response.body()?.response?.body?.totalCount}")

                    val weatherArr = arrayOf(ModelWeather(), ModelWeather(), ModelWeather(), ModelWeather(), ModelWeather(), ModelWeather())

                    var index = 0
                    val totalCount = response.body()?.response?.body?.totalCount!!.toInt()-1

                    for(i in 0..totalCount) {
                        index %= 6
                        when(itemList?.get(i)?.category) {
                            "PTY" -> weatherArr[index].rainType = itemList.get(i).fcstValue.toString()     // 강수 형태
                            "REH" -> weatherArr[index].humidity = itemList.get(i).fcstValue.toString()     // 습도
                            "SKY" -> weatherArr[index].sky = itemList.get(i).fcstValue.toString()          // 하늘 상태
                            "T1H" -> weatherArr[index].temp = itemList.get(i).fcstValue.toString()         // 기온
                            "VEC" -> weatherArr[index].windDirection = itemList.get(i).fcstValue.toString() // 풍향
                            "WSD" -> weatherArr[index].windSpeed = itemList.get(i).fcstValue.toString() // 풍속
                            else -> continue
                        }
                        index++
                    }

                    for(i in 0..5) {
                        weatherArr[i].fcstTime = itemList?.get(i)?.fcstTime.toString()
                    }

                    val mainSky = weatherArr.firstOrNull()?.sky
                    val mainRain = weatherArr.firstOrNull()?.rainType
                    val mainTemp = weatherArr.firstOrNull()?.temp
                    val mainTime = weatherArr.firstOrNull()?.fcstTime.toString()
                    Log.d("testt mainSky", "${mainSky}")
                    Log.d("testt mainRain", "${mainRain}")
                    Log.d("testt mainTemp", "${mainTemp}")
                    Log.d("testt mainTime", "${mainTime}")

                    when(mainSky) {
                        // 여기서 밤 낮 구분을 해줘야 함
                        "1" -> {
                            Log.d("testt mainSky", "맑음")
                            binding.mainImage.setImageResource(R.drawable.sunny)
                            binding.skyStatus.text = "맑음"

                            when(mainTime) {
                                "1900" -> {
                                    Log.d("mainTime 7시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "2000" -> {
                                    Log.d("mainTime 8시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "2100" -> {
                                    Log.d("mainTime 9시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "2200" -> {
                                    Log.d("mainTime 10시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "2300" -> {
                                    Log.d("mainTime 11시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0000" -> {
                                    Log.d("mainTime 00시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0100" -> {
                                    Log.d("mainTime 01시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0200" -> {
                                    Log.d("mainTime 02시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0300" -> {
                                    Log.d("mainTime 03시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0400" -> {
                                    Log.d("mainTime 04시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0500" -> {
                                    Log.d("mainTime 05시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }
                                "0600" -> {
                                    Log.d("mainTime 06시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.sunny_night)
                                }

                            }
                        }
                        "3" -> {
                            Log.d("testt mainSky", "구름많음")
                            binding.mainImage.setImageResource(R.drawable.cloudy)
                            binding.skyStatus.text = "구름많음"

                            when(mainTime) {
                                "1900" -> {
                                    Log.d("mainTime 7시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "2000" -> {
                                    Log.d("mainTime 8시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "2100" -> {
                                    Log.d("mainTime 9시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "2200" -> {
                                    Log.d("mainTime 10시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "2300" -> {
                                    Log.d("mainTime 11시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0000" -> {
                                    Log.d("mainTime 00시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0100" -> {
                                    Log.d("mainTime 01시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0200" -> {
                                    Log.d("mainTime 02시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0300" -> {
                                    Log.d("mainTime 03시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0400" -> {
                                    Log.d("mainTime 04시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0500" -> {
                                    Log.d("mainTime 05시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                                "0600" -> {
                                    Log.d("mainTime 06시", "${mainTime}")
                                    binding.mainImage.setImageResource(R.drawable.cloudy_night)
                                }
                            }

                            if (mainRain?.toInt() == 1) {
                                binding.skyStatus.text = "구름많음 / 비"
                                binding.mainImage.setImageResource(R.drawable.rain)
                            }
                        }
                        else -> {
                            Log.d("testt mainSky", "흐림")
                            binding.mainImage.setImageResource(R.drawable.cloud)
                            binding.skyStatus.text = "흐림"

                            if (mainRain?.toInt() == 1) {
                                binding.skyStatus.text = "흐림 / 비"
                                binding.mainImage.setImageResource(R.drawable.rain)
                            }
                        }
                    }


                    binding.temp.text = "${mainTemp}℃"

                    binding.recyvlerView.adapter = WeatherAdapter(weatherArr, LayoutInflater.from(this@MainActivity), this@MainActivity)
                    binding.recyvlerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

                    binding.progressBar.visibility = View.GONE
                    binding.constraintlayout2.visibility = View.VISIBLE

                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Log.d("testt fail","${t.message}")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        cancellationTokenSource = CancellationTokenSource()
        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location ->
            try {
                lat = location.latitude
                lon = location.longitude
                Log.d("testt location ", "latitude : ${location.latitude}, longitude : ${location.longitude}")

                geocoder = Geocoder(this, Locale.getDefault())
                val address = geocoder.getFromLocation(lat, lon, 1)
                Log.d("testt getAddressLine","${address[0].getAddressLine(0)}")
                binding.address.text = "${address[0].getAddressLine(0)}"

                val gu = address[0].subLocality
                var dong = address[0].thoroughfare

                if (dong == null) {
                    dong = ""
                }
                //binding.address.text = "${address[0].getAddressLine(0)}"
                binding.address.text = "${gu} ${dong}"

                val convertXY = convertGRID_GPS(TO_GRID, lat, lon)
                Log.d("testt xy convert", "x = ${convertXY.x}, y = ${convertXY.y}")
                connectRetrofit()

            } catch (e : Exception) {
                e.printStackTrace()
                Toast.makeText(this,"error 발생 다시 시도", Toast.LENGTH_SHORT).show()
            } finally {
                Log.d("testt finish","finish")
                binding.refresh.isRefreshing = false
                binding.constraintlayout2.visibility = View.VISIBLE
                binding.recyvlerView.visibility = View.VISIBLE
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d("testt", "승낙")

                fetchLocation()

            } else {
                Log.d("testt", "거부")
                finish()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    fun getBaseTime(h : String, m : String) : String {
        var result = ""

        // 45분 전이면
        if (m.toInt() < 45) {
            // 0시면 2330
            if (h == "00") result = "2330"
            // 아니면 1시간 전 날씨 정보 부르기
            else {
                var resultH = h.toInt() - 1
                // 1자리면 0 붙여서 2자리로 만들기
                if (resultH < 10) result = "0" + resultH + "30"
                // 2자리면 그대로
                else result = resultH.toString() + "30"
            }
        }
        // 45분 이후면 바로 정보 받아오기
        else result = h + "30"

        return result
    }

    private fun convertGRID_GPS(mode: Int, lat_X: Double, lng_Y: Double): LatXLngY {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0 // 격자 간격(km)
        val SLAT1 = 30.0 // 투영 위도1(degree)
        val SLAT2 = 60.0 // 투영 위도2(degree)
        val OLON = 126.0 // 기준점 경도(degree)
        val OLAT = 38.0 // 기준점 위도(degree)
        val XO = 43.0 // 기준점 X좌표(GRID)
        val YO = 136.0 // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //
        val DEGRAD = Math.PI / 180.0
        val RADDEG = 180.0 / Math.PI
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD
        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)
        val rs: LatXLngY = LatXLngY()
        if (mode == TO_GRID) {
            rs.lat = lat_X
            rs.lng = lng_Y
            var ra = Math.tan(Math.PI * 0.25 + lat_X * DEGRAD * 0.5)
            ra = re * sf / Math.pow(ra, sn)
            var theta = lng_Y * DEGRAD - olon
            if (theta > Math.PI) theta -= 2.0 * Math.PI
            if (theta < -Math.PI) theta += 2.0 * Math.PI
            theta *= sn
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5)
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5)
        } else {
            rs.x = lat_X
            rs.y = lng_Y
            val xn = lat_X - XO
            val yn = ro - lng_Y + YO
            var ra = Math.sqrt(xn * xn + yn * yn)
            if (sn < 0.0) {
                ra = -ra
            }
            var alat = Math.pow(re * sf / ra, 1.0 / sn)
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5
            var theta = 0.0
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0
            } else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5
                    if (xn < 0.0) {
                        theta = -theta
                    }
                } else theta = Math.atan2(xn, yn)
            }
            val alon = theta / sn + olon
            rs.lat = alat * RADDEG
            rs.lng = alon * RADDEG
        }
        return rs
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
    }

    internal inner class LatXLngY {
        var lat = 0.0
        var lng = 0.0
        var x = 0.0
        var y = 0.0
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 1000
        var TO_GRID = 0
        var TO_GPS = 1
    }
}





































