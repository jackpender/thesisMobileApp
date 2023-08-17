package com.jpender.mobile_app.data

import com.jpender.mobile_app.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface TemperatureAndHumidityReceiveManager {

    val data:MutableSharedFlow<Resource<TempHumidityResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}