package org.techtown.weatherpublicapiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(val itemList : Array<ModelWeather>,
                     val layoutInflater: LayoutInflater,
                     val activity: MainActivity
) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val temp : TextView
        val rain : TextView
        val sky : TextView
        val time : TextView
        val humidity : TextView
        val image : ImageView
        val windSpeed : TextView

        init {
            temp = itemView.findViewById(R.id.temp)
            rain = itemView.findViewById(R.id.rain)
            sky = itemView.findViewById(R.id.sky)
            time = itemView.findViewById(R.id.time)
            humidity = itemView.findViewById(R.id.humidity)
            image = itemView.findViewById(R.id.image)
            windSpeed = itemView.findViewById(R.id.windSpeed)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherAdapter.ViewHolder {
        val view = layoutInflater.inflate(R.layout.recyclerview_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherAdapter.ViewHolder, position: Int) {
        var AmPm = ""
        if(itemList.get(position).fcstTime.toInt() < 1200) {
            AmPm = "am"
        } else
            AmPm = "pm"

        holder.temp.text = "기온 : ${itemList.get(position).temp}℃"
        when(itemList.get(position).sky) {
            "1" -> {
                holder.sky.text = "하늘상태 : 맑음"
                if (itemList.get(position).fcstTime.toInt() > 1800 ||
                    itemList.get(position).fcstTime == "0000" ||
                    itemList.get(position).fcstTime == "0100" ||
                    itemList.get(position).fcstTime == "0200" ||
                    itemList.get(position).fcstTime == "0300" ||
                    itemList.get(position).fcstTime == "0400" ||
                    itemList.get(position).fcstTime == "0500" ||
                    itemList.get(position).fcstTime == "0600") {
                    holder.image.setImageResource(R.drawable.sunny_night)

                } else
                    holder.image.setImageResource(R.drawable.sunny)
            }
            "3" -> {
                holder.sky.text = "하늘상태 : 구름많음"
                if (itemList.get(position).fcstTime.toInt() > 1800 ||
                    itemList.get(position).fcstTime == "0000" ||
                    itemList.get(position).fcstTime == "0100" ||
                    itemList.get(position).fcstTime == "0200" ||
                    itemList.get(position).fcstTime == "0300" ||
                    itemList.get(position).fcstTime == "0400" ||
                    itemList.get(position).fcstTime == "0500" ||
                    itemList.get(position).fcstTime == "0600") {
                    holder.image.setImageResource(R.drawable.cloudy_night)

                } else
                    holder.image.setImageResource(R.drawable.cloudy)

            }
            "4" -> {
                holder.sky.text = "하늘상태 : 흐림"
                holder.image.setImageResource(R.drawable.cloud)
            }
            else -> holder.sky.text = "하늘상태 : 몰?루"
        }
        //holder.sky.text = itemList.get(position).sky
        when(itemList.get(position).rainType) {
            "1" -> {
                holder.rain.text = "강수형태 : 비"
                holder.image.setImageResource(R.drawable.rain)
            }
            else -> holder.rain.text = "강수형태 : 없음"
        }
        holder.humidity.text = "습도 : ${itemList.get(position).humidity}%"
        holder.time.text = "$AmPm ${itemList.get(position).fcstTime}"
        holder.windSpeed.text = "풍속 : ${itemList.get(position).windSpeed}m/s"

        //holder.rain.text = itemList.get(position).rainType


    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}