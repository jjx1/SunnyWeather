package com.example.sunnyweather.ui.weather

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filepersistencetest.R
import com.example.filepersistencetest.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.PlaceWeatherData
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.ui.place.PlaceFragment
import com.sunnyweather.android.logic.model.RealtimeResponse
import com.sunnyweather.android.logic.model.getSky
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherBinding
    private lateinit var adapter: WeatherAdapter
    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }
//    val viewModelnav by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        Log.d(
//            "WeatherActivity",
//            "第一次跳转,为空 ${viewModel.locationLat.isEmpty()}+${viewModel.locationLat.toString()}"
//        )
//        第一次初始化时，数据从intent获取
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
//            Log.d("WeatherActivity", viewModel.locationLat)

        }
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
//            Log.d("WeatherActivity", viewModel.locationLng)

        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""

//            Log.d("WeatherActivity", viewModel.locationLng)

        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull() //getOrNull()用于从 Result 实例中获取包含在 Result.success 中的数据
            if (weather != null) {
//                Log.d("WeatherActivity", "Result is not null: $result")
                showWeatherInfo(weather)

            } else {
                Log.d("WeatherActivity", "Result is null: $result")
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeResources(R.color.black)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        //第一次执行从空变成有数据，observe也会观察到，然后调用showWeatherInfo
//        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        //侧边栏
        binding.now.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })

        val headerView = binding.navigationView.getHeaderView(0)
        val editText = headerView.findViewById<EditText>(R.id.serch_jump)
        editText.setOnClickListener {
            // 在这里处理点击事件的逻辑
            val placeFragment = PlaceFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.entireNavigation, placeFragment)
                .commit()
        }

        viewModel.searchLocalWeather()
// 观察 localWeatherLiveDataArray
        viewModel.getLocalWeatherLiveDataArray().observe(this, Observer { liveDataArray ->
            // 遍历 LiveData 数组
            for (liveData in liveDataArray) {
                viewModel.weatherList.clear()
                // 观察每个 LiveData 对象
                liveData.observe(this, Observer { realtime ->
                    val weather = realtime
                    if (weather != null) {
                        viewModel.weatherList.add(weather)
                    }
                    // 打印每个 LiveData 对象的具体值
                    Log.d("WeatherViewModel", "getLocalWeatherLiveDataArray 每个LiveData的 value: $realtime")
                })
                adapter.notifyDataSetChanged()
            }
        })

        binding.recyclerviewLocalweather.layoutManager = LinearLayoutManager(this)
        val places = viewModel.getSavedPlace()
        val weathers = getLocalWeatherDataList()
        // 创建适配器实例并设置给 RecyclerView
        adapter = WeatherAdapter(this, places, weathers)
        binding.recyclerviewLocalweather.adapter = adapter
////        viewModel.getLocalWeatherLiveDataArray().observe(this, Observer { dataList ->
////            // 更新 UI，显示数据列表
////        })

    }
    fun getLocalWeatherDataList(): List<RealtimeResponse.Realtime> {
        val liveDataArray = viewModel.getLocalWeatherLiveDataArray().value ?: arrayOf()
        Log.d("WeatherActivity","本地天气数组获取iveDataArray${liveDataArray.size}")
        for (liveData in liveDataArray) {
            Log.d("WeatherActivity", "元素 $liveData: ${liveData.value}")
            liveData.value?.let { realtime ->
                viewModel.weatherList.add(realtime)
                Log.d("WeatherActivity","本地天气数组获取1${realtime}")
            }
        }
        val weatherList1 = viewModel.weatherList
        Log.d("WeatherViewModel","本地天气数组获取2${weatherList1.toString()}")
        return weatherList1
    }



    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
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
        for (i in 1 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            //动态创建子视图，forecastLayout
            val view = LayoutInflater.from(this)
                .inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false)
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
//把历史地点动态查询挂载
//    fun getPlaceWeatherData(): PlaceWeatherData {
//        val savedPlaces = viewModel.getSavedPlace()
//        val weathers = mutableListOf<RealtimeResponse.Realtime>()
//
//        for (place in savedPlaces) {
//            val lat = place.location.lat
//            val lng = place.location.lng
//            viewModel.refreshlocalWeather(lng, lat)
//            viewModel.localWeatherLiveData.observe(this, Observer { result ->
//                val weather = result.getOrNull()
//                weather?.let {
//                    weathers.add(it.realtime)
//                }
//            })
//        }
//        //我想知道给recycleview传值，是像下面这样一起传还是分开比较好
//        return PlaceWeatherData(savedPlaces, weathers)
//    }
