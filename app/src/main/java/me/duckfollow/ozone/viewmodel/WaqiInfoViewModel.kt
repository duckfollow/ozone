package me.duckfollow.ozone.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.duckfollow.ozone.model.WaqiInfo
import me.duckfollow.ozone.service.WaqiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WaqiInfoViewModel() : ViewModel() {
    private var service: WaqiService? = null
    private val infoMutableLiveData: MutableLiveData<WaqiInfo> = MutableLiveData<WaqiInfo>()

    init {
        fetchDataInfo()
    }

    private fun fetchDataInfo(){
        service = WaqiService()
        service!!.createService()!!.getInfo().enqueue(object :Callback<WaqiInfo>{
            override fun onFailure(call: Call<WaqiInfo>, t: Throwable) {

            }

            override fun onResponse(call: Call<WaqiInfo>, response: Response<WaqiInfo>) {
                Observable.just(response.body())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscribeData())
            }
        })
    }

    private fun subscribeData(): Observer<WaqiInfo?> {
        return object :Observer<WaqiInfo?>{
            override fun onComplete() {

            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(t: WaqiInfo) {
                infoMutableLiveData.value = t
            }

            override fun onError(e: Throwable) {

            }
        }
    }

    fun getInfoList():MutableLiveData<WaqiInfo>{
        return infoMutableLiveData
    }
}

