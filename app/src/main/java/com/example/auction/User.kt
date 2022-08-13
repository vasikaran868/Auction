package com.example.auction

data class User(
    var username:String,
    var password:String,
    var email:String,
    var xp:Int,
    var match_played:Int,
    var match_won:Int,
    var avg_points:Float
)
