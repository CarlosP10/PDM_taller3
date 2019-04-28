package com.naldana.ejemplo10

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.naldana.ejemplo10.model.infoCoins
import kotlinx.android.synthetic.main.activity_detail_coin.*

class DetailCoin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_coin)
        val reciever: infoCoins = intent?.extras?.getParcelable("COIN") ?: infoCoins()
        init(reciever)
    }

    fun init(coin: infoCoins){
        Glide.with(this)
            .load(coin.img)
            .placeholder(R.drawable.ic_launcher_background)
            .into(app_bar_image_viewer)
        tv_name.text=coin.name
        collapsingtoolbarviewer.title = coin.country
        tv_value.text=coin.value.toString()
        tv_us_value.text=coin.value_us.toString()
        tv_year.text=coin.year.toString()
        tv_review.text=coin.review
        tv_is_available.text=coin.isAvailable.toString()


    }

}
