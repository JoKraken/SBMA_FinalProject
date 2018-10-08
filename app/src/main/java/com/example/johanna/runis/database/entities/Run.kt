package com.example.johanna.runis.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Run(
        @PrimaryKey val runid: Int,
        val time: String, //time in minutes
        val km: String,
        val date: String,
        val date_base: Long,
        val date_milisecound: Long
) {
    //constructor, getter and setter are implicit :)
    override fun toString(): String{
        return "$runid:  $date, $time, $km, base: $date_base"
    }
}