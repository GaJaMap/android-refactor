package com.pg.gajamap.util

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kakao.sdk.navi.Constants
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.CoordType
import com.kakao.sdk.navi.model.Location
import com.kakao.sdk.navi.model.NaviOption
import com.pg.gajamap.databinding.GroupDialogBinding
import com.pg.gajamap.databinding.MemoDialogBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun <T> Fragment.collectLatestStateFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun <T> Fragment.collectLatestStateFlowWithDrop(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.drop(1).collectLatest(collect)
        }
    }
}

fun View.hide(activity: Activity) {
    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    visibility = View.GONE
}

fun View.show(activity: Activity) {
    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    visibility = View.VISIBLE
}

fun Context.toast(message: String, time: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, time).show()
}

fun Fragment.showGroupDialog(
    layoutInflater: LayoutInflater,
    title: String
): Pair<AlertDialog, GroupDialogBinding> {
    val binding = GroupDialogBinding.inflate(layoutInflater).apply {
        tvTitle.text = title
    }

    val dialog = createDialog(binding.root)
    dialog.show()

    return dialog to binding
}

fun Fragment.showMemoDialog(
    layoutInflater: LayoutInflater,
    title: String
): Pair<AlertDialog, MemoDialogBinding> {
    val binding = MemoDialogBinding.inflate(layoutInflater).apply {
        tvTitle.text = title
    }

    val dialog = createDialog(binding.root)
    dialog.show()

    return dialog to binding
}

fun Fragment.deleteCheckDialog(
    title: String,
    message: String,
    onDeleteConfirmed: () -> Unit
): AlertDialog {
    return AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("확인") { _, _ -> onDeleteConfirmed.invoke() }
        .setNegativeButton("취소", null)
        .create()
        .apply { show() }
}

fun Context.showNavigationDialog(
    title: String?,
    items: Array<String>,
    onItemClick: (String) -> Unit
) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    title?.let { builder.setTitle(it) }

    if (title != null) {
        builder.setItems(items) { _, item ->
            onItemClick(items[item])
        }
    } else {
        val adapter = CustomerDialogAdapter(this, items)
        builder.setAdapter(adapter) { _, which ->
            val selectedItem = items[which]
            onItemClick(selectedItem)
        }
    }

    builder.show()
}

private fun Fragment.createDialog(view: View): AlertDialog {
    return AlertDialog.Builder(requireContext()).apply {
        setView(view)
    }.create()
}

fun Context.launchKakaoNavi(clientName: String, longitude: Double?, latitude: Double?) {
    if (NaviClient.instance.isKakaoNaviInstalled(this)) {
        startActivity(
            NaviClient.instance.navigateIntent(
                Location(
                    clientName,
                    longitude.toString(),
                    latitude.toString()
                ),
                NaviOption(coordType = CoordType.WGS84)
            )
        )
    } else {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.WEB_NAVI_INSTALL)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}

fun Context.launchNaverNavi(clientName: String, longitude: Double?, latitude: Double?) {
    try {
        val url =
            "nmap://navigation?dlat=$latitude&dlng=$longitude&dname=$clientName&appname=com.pg.gajamap"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.nhn.android.nmap")
            )
        )
    }
}

fun EditText.getTextChangeStateFlow(): StateFlow<String> {
    val textStateFlow = MutableStateFlow("")

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            textStateFlow.value = editable.toString()
        }
    })

    return textStateFlow
}

fun getStaticMapUrl(latitude: Double?, longitude: Double?, apiKey: String): String {
    return "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=$latitude,$longitude" +
            "&zoom=15" +
            "&size=400x200" +
            "&markers=color:red%7Clabel:S%7C$latitude,$longitude" +
            "&language=ko" +
            "&key=$apiKey"
}

fun String.toTextRequestBody() =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

fun Boolean.toTextRequestBody() =
    this.toString().toRequestBody("text/plain".toMediaTypeOrNull())

fun Uri.getRealPathFromURI(context: Context): String {
    val buildName = Build.MANUFACTURER
    if (buildName.equals("Xiaomi")) {
        return this.path!!
    }
    var columnIndex = 0
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(this, proj, null, null, null)
    if (cursor!!.moveToFirst()) {
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    }
    val result = cursor.getString(columnIndex)
    cursor.close()
    return result
}

fun Context.getAssetsTextString(fileName: String): String {
    val termsString = StringBuilder()
    val reader: BufferedReader

    try {
        reader = BufferedReader(InputStreamReader(this.resources.assets.open("$fileName.txt")))

        var str: String?
        while (reader.readLine().also { str = it } != null) {
            termsString.append(str)
            termsString.append('\n')
        }
        reader.close()
        return termsString.toString()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return "fail"
}