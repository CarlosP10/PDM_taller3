package com.naldana.ejemplo10.model

import android.os.Parcel
import android.os.Parcelable

data class infoCoins(
    val name: String = "N/A",
    val country:String = "N/A",
    val value: Double = 0.0,
    val value_us: Double = 0.0,
    val year: Int = 0,
    val review: String = "N/A",
    val isAvailable: Int=0,
    val img: String = "N/A"
):Parcelable{
    constructor(parcel: Parcel) : this(
        name = parcel.readString(),
        country =parcel.readString(),
        value = parcel.readDouble(),
        value_us = parcel.readDouble(),
        year= parcel.readInt(),
        review = parcel.readString(),
        isAvailable =  parcel.readInt(),
        img = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(country)
        parcel.writeDouble(value)
        parcel.writeDouble(value_us)
        parcel.writeInt(year)
        parcel.writeString(review)
        parcel.writeInt(isAvailable)
        parcel.writeString(img)
    }


    override fun describeContents(): Int =0


    companion object CREATOR : Parcelable.Creator<infoCoins> {
        override fun createFromParcel(parcel: Parcel): infoCoins = infoCoins(parcel)
        override fun newArray(size: Int): Array<infoCoins?> = arrayOfNulls(size)
    }
}
