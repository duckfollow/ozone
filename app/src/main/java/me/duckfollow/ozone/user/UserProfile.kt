package me.duckfollow.ozone.user

import android.content.Context
import android.content.SharedPreferences

class UserProfile {
    var User: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val USER_PREFS = "USER_PREFS"

    constructor(context: Context) {
        User = context!!.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
    }

    fun setUserId(userId:String){
        this.setUserData("userId", userId)
    }

    fun getUserId():String{
        return User.getString("userId","")
    }

    fun setImageBase64(base64:String){
        this.setUserData("base64", base64)
    }

    fun getImageBase64():String{
        return User.getString("base64","")
    }

    fun setMapTutorial(isShow:String){
        this.setUserData("MapTutorial", isShow)
    }

    fun getMapTutorial():String {
        return User.getString("MapTutorial","true")
    }

    fun setMainDetailsTutorial(isShow:String){
        this.setUserData("MainDetailsTutorial", isShow)
    }

    fun getMainDetailsTutorial():String {
        return User.getString("MainDetailsTutorial","true")
    }

    fun setProfileTutorial(isShow:String){
        this.setUserData("ProfileTutorial", isShow)
    }

    fun getProfileTutorial():String {
        return User.getString("ProfileTutorial","true")
    }

    fun setCountReview(count: String){
        this.setUserData("CountReview", count)
    }

    fun getCountReview():String {
        return User.getString("CountReview","0")
    }

    fun setExpandViewCorona(isExpend:String) {
        this.setUserData("ExpandViewCorona", isExpend)
    }

    fun getExpandViewCorona():String {
        return User.getString("ExpandViewCorona","true")
    }

    fun setUserData(key:String,value:String){
        editor = User.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getUserData(key:String):String{
        return User.getString(key,"")
    }
}
