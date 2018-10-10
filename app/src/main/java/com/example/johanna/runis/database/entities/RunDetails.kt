package com.example.johanna.runis.database.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(foreignKeys = [(ForeignKey(
        entity = Run::class,
        parentColumns = ["runid"],
        childColumns = ["run"]))])
data class RunDetails(
        val run: Int,
        val type: Int, //e.g. 1 = position, 2 = heartbeat
        @PrimaryKey
        val value: String
){
    //constructor, getter and setter are implicit :)
    override fun toString(): String = "$run:   $type:   $value"
}