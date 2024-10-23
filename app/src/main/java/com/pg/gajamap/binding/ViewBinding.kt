package com.pg.gajamap.binding

import android.content.Intent
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load
import com.pg.gajamap.R
import com.pg.gajamap.data.model.response.client.Client
import com.pg.gajamap.util.Constants.imageUrlPrefix
import com.pg.gajamap.util.launchKakaoNavi
import com.pg.gajamap.util.launchNaverNavi
import com.pg.gajamap.util.showNavigationDialog


object ViewBinding {

    @JvmStatic
    @BindingAdapter("formattedPhoneNumber")
    fun setFormattedPhoneNumber(textView: TextView, phoneNumber: String?) {
        phoneNumber?.let {
            val formattedNumber = PhoneNumberUtils.formatNumber(it)
            textView.text = formattedNumber
        }
    }

    @JvmStatic
    @BindingAdapter("imageFilePath")
    fun loadImage(view: ImageView, imageFilePath: String?) {
        val file = imageUrlPrefix + imageFilePath
        view.load(file) {
            error(R.drawable.profile_img_origin)
        }
    }

    @JvmStatic
    @BindingAdapter("phoneNumber")
    fun dialPhoneNumber(view: View, phoneNumber: String?) {
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${phoneNumber}")
            view.context.startActivity(intent)
        }
    }

    @JvmStatic
    @BindingAdapter("navItems", "itemView")
    fun setNavigationDialog(
        view: View,
        items: Array<String>?,
        itemView: Client
    ) {
        view.setOnClickListener {
            if (items != null) {
                view.context.showNavigationDialog(null, items) { selectedItem ->
                    when (selectedItem) {
                        "카카오 내비" -> {
                            view.context.launchKakaoNavi(
                                itemView.clientName,
                                itemView.location.longitude!!,
                                itemView.location.latitude!!
                            )
                        }

                        "네이버 내비" -> {
                            view.context.launchNaverNavi(
                                itemView.clientName,
                                itemView.location.longitude!!,
                                itemView.location.latitude!!
                            )
                        }
                    }
                }
            }
        }
    }
}