package com.healthcare.hospitalapp

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** Generic record. Hospitals: a=name b=city. Doctors: a=name b=speciality c=hospital. Medicines: a=name b=price c=seller flag=prescription. */
data class Rec(val a: String, val b: String, val c: String, val phone: String, val flag: Boolean = false)

class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("hc", Context.MODE_PRIVATE)
    val hospitals = mutableStateListOf<Rec>()
    val doctors = mutableStateListOf<Rec>()
    val medicines = mutableStateListOf<Rec>()
    val urdu = mutableStateOf(p.getBoolean("urdu", true))

    init {
        load("h", hospitals); load("d", doctors); load("m", medicines)
    }

    private fun load(key: String, out: MutableList<Rec>) {
        try {
            val arr = JSONArray(p.getString(key, "[]"))
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Rec(o.optString("a"), o.optString("b"), o.optString("c"), o.optString("p"), o.optBoolean("f")))
            }
        } catch (_: Exception) { }
    }

    private fun save(key: String, list: List<Rec>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("a", it.a).put("b", it.b).put("c", it.c).put("p", it.phone).put("f", it.flag))
        }
        p.edit().putString(key, arr.toString()).apply()
    }

    fun add(tab: Int, r: Rec) {
        when (tab) {
            0 -> { hospitals.add(r); save("h", hospitals) }
            1 -> { doctors.add(r); save("d", doctors) }
            2 -> { medicines.add(r); save("m", medicines) }
        }
    }

    fun remove(tab: Int, index: Int) {
        when (tab) {
            0 -> { hospitals.removeAt(index); save("h", hospitals) }
            1 -> { doctors.removeAt(index); save("d", doctors) }
            2 -> { medicines.removeAt(index); save("m", medicines) }
        }
    }

    fun setUrdu(v: Boolean) { urdu.value = v; p.edit().putBoolean("urdu", v).apply() }

    private fun hash(s: String) =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    fun hasPin() = p.getString("pin", null) != null
    fun setPin(pin: String) { p.edit().putString("pin", hash(pin)).apply() }
    fun checkPin(pin: String) = p.getString("pin", null) == hash(pin)
}
