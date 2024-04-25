package com.example.sunnyweather.ui.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.filepersistencetest.R
import com.example.filepersistencetest.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //第一次初始化时，数据从intent获取
        if (viewModel.locationLat.isNotEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?:""
        }
        if (viewModel.locationLng.isNotEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng")?:""
        }
        if (viewModel.placeName.isNotEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name")?:""
        }
        viewModel.weatherLiveData.observe(this, Observer{ result ->
            val weather = result.getOrNull() //getOrNull()用于从 Result 实例中获取包含在 Result.success 中的数据
            if (weather != null){
                showWeatherInfo(weather)
            }else{
                Log.d("WeatherActivity", "Result is null: $result")
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        //第一次执行从空变成有数据，observe也会观察到，然后调用showWeatherInfo
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
    }

    private fun showWeatherInfo(weather: Weather) {
        binding.now.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml的数据
        val currentTempText = "${realtime.temperature.toInt()}^C"//为什么要这一行
        binding.now.currentTemp.text = currentTempText
        binding.now.currentSky.text = getSky(realtime.skycon).info //得到天气情况的代码值，通过Sky对象转化成文字图片
        val currenrPM25Text = "空气指数${realtime.airQuality.aqi.chn.toInt()}"
        binding.now.currentAQI.text = currenrPM25Text
        binding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg) //now的背景图片是天气情况的图片
        //填充forecast.xml的布局
        binding.forecast.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 1 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            //动态创建子视图，forecastLayout
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,binding.forecast.forecastLayout,false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            //子视图的数据挂载
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()}^C"
            temperatureInfo.text = tempText
            //子视图添加到父视图
            binding.forecast.forecastLayout.addView(view)
        }
        //填充life_index。xml的数据
        val lifeIndex = daily.lifeIndex
        binding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        binding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }
}