package com.jpender.mobile_app.util

//Helper class that can be wrapped around data to help distinguish between success, error or loading
sealed class Resource<out T:Any>{
    data class Success<out T:Any> (val data:T):Resource<T>()
    data class Error(val errorMessage:String):Resource<Nothing>()
    data class Loading<out T:Any> (val data:T? = null, val message:String? = null):Resource<T>()
}
