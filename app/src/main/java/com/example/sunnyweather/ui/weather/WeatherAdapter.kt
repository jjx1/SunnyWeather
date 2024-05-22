package com.example.sunnyweather.ui.weather

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.filepersistencetest.R
import com.example.sunnyweather.logic.model.Place
import com.sunnyweather.android.logic.model.RealtimeResponse
import com.sunnyweather.android.logic.model.getSky

class WeatherAdapter(private val activity: WeatherActivity ,val placeList: List<Place>, private val weatherList: List<RealtimeResponse.Realtime>):
    RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {
    private var newWeatherList = mutableListOf<RealtimeResponse.Realtime>()
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val localPlaceName: TextView = view.findViewById(R.id.local_name)
        val localPlaceTemp: TextView = view.findViewById(R.id.local_temp)
        val localPlaceAirQuality: TextView = view.findViewById(R.id.local_airQuality)
        val localPlaceRealTemp: TextView = view.findViewById(R.id.local_realTemp)

    }
    fun submitList(newList: List<RealtimeResponse.Realtime>) {
        newWeatherList.clear()
        newWeatherList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.localweather_item, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        val weather = weatherList[position]
        holder.localPlaceName.text = place.name
        val currenrPM25Text = "空气指数${weather.airQuality?.aqi?.chn?.toInt()}"
        holder.localPlaceAirQuality.text = currenrPM25Text
        holder.localPlaceRealTemp.text = weather.temperature.toString()
        holder.localPlaceTemp.text = getSky(weather.skycon).info

        //动态加载背景
        val containerWidth = holder.itemView.width
        val containerHeight = holder.itemView.height
        // 获取天气情况的背景图片资源ID
        val bgResourceId = getSky(weather.skycon).bg

        // 设置背景图片
        holder.itemView.setBackgroundResource(bgResourceId)

        // 获取容器的 LayoutParams
        val layoutParams = holder.itemView.layoutParams

        // 根据容器的尺寸动态调整背景图片的大小
        layoutParams.width = containerWidth
        layoutParams.height = containerHeight

        // 将 LayoutParams 设置回容器中
        holder.itemView.layoutParams = layoutParams
        //item点击事件
        if (placeList.isNotEmpty()) {
            Log.d("localWeatherActivity", "adapterResult is null: $holder")
            holder.itemView.setOnClickListener {
                activity.binding.drawerLayout.closeDrawers()
                activity.viewModel.locationLng = place.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                activity.refreshWeather()
                //刷新并关闭侧边栏
            }
        }

    }
    override fun getItemCount() = placeList.size
}