package com.example.sunnyweather.ui.place

import android.content.Intent
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filepersistencetest.databinding.FragmentPlaceBinding
import com.example.sunnyweather.MainActivity
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.ui.weather.WeatherActivity


class PlaceFragment : Fragment() {
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter
    lateinit var binding: FragmentPlaceBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    //获取历史地点列表里的第一个地点
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            if (activity is MainActivity && viewModel.isPlaceSaved()) {
                val places = viewModel.getSavedPlace()
                if (places.isNotEmpty()) {
                    val place = places[0] // 获取列表的第一个地点
                    val intent = Intent(context, WeatherActivity::class.java).apply {
                        putExtra("location_lng", place.location.lng)
                        putExtra("location_lat", place.location.lat)
                        putExtra("place_name", place.name)
                    }
                    startActivity(intent)
                    activity?.finish()
                    return
                }
            }

        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this,viewModel.placeList)
        binding.recyclerView.adapter = adapter
        binding.searchPlaceEdit.addTextChangedListener{editable ->
            val content = editable.toString()
            if (content.isNotEmpty()){
                viewModel.searchPlaces(content)
            }else{
                binding.recyclerView.visibility = View.GONE
                binding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()//通知 RecyclerView 的适配器数据发生了变化，需要重新绑定数据。这样，当 RecyclerView 再次可见时，它将显示最新的空数据状态。
            }
        }
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result: Result<List<Place>> ->
            // 在 Observer lambda 中的代码保持不变
            val places = result.getOrNull()
            if (places != null) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}