package com.healthcare.hospitalapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(applicationContext)
        setContent {
            val dir = if (store.urdu.value) LayoutDirection.Rtl else LayoutDirection.Ltr
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF0B7A75))) {
                CompositionLocalProvider(LocalLayoutDirection provides dir) { App(store) }
            }
        }
    }
}

fun dial(c: Context, phone: String) {
    c.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(s: Store) {
    val u = s.urdu.value
    fun t(en: String, ur: String) = if (u) ur else en
    var tab by remember { mutableIntStateOf(0) }
    var admin by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("HealthCare", "ہیلتھ کیئر")) },
                actions = { TextButton(onClick = { s.setUrdu(!u) }) { Text(if (u) "English" else "اردو") } }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text(t("Hospitals", "ہسپتال")) })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Call, null) }, label = { Text(t("Doctors", "ڈاکٹر")) })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.ShoppingCart, null) }, label = { Text(t("Medicines", "ادویات")) })
                NavigationBarItem(selected = tab == 3, onClick = { tab = 3 }, icon = { Icon(Icons.Default.Lock, null) }, label = { Text(t("Admin", "ایڈمن")) })
            }
        },
        floatingActionButton = {
            if (admin && tab < 3) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> RecList(s.hospitals, 0, admin, u, s) { r -> listOf(r.b) }
                1 -> RecList(s.doctors, 1, admin, u, s) { r -> listOf(r.b, r.c) }
                2 -> RecList(s.medicines, 2, admin, u, s) { r ->
                    listOf(r.b, r.c, if (r.flag) t("Prescription required", "نسخہ ضروری ہے") else "")
                }
                else -> AdminTab(s, admin, u) { admin = it }
            }
        }
    }

    if (adding) {
        val labels = when (tab) {
            0 -> listOf(t("Hospital name", "ہسپتال کا نام"), t("City", "شہر"), "", t("Phone", "رابطہ نمبر"))
            1 -> listOf(t("Doctor name", "ڈاکٹر کا نام"), t("Speciality", "شعبہ"), t("Hospital", "ہسپتال"), t("Phone", "رابطہ نمبر"))
            else -> listOf(t("Medicine name", "دوا کا نام"), t("Price", "قیمت"), t("Seller / pharmacy", "فروخت کنندہ / فارمیسی"), t("Phone", "رابطہ نمبر"))
        }
        AddDialog(labels, tab == 2, u, { adding = false }) { r -> s.add(tab, r); adding = false }
    }
}

@Composable
fun RecList(list: List<Rec>, tab: Int, admin: Boolean, u: Boolean, s: Store, lines: (Rec) -> List<String>) {
    val ctx = LocalContext.current
    var q by remember { mutableStateOf("") }
    val shown = list.withIndex().filter { iv ->
        val r = iv.value
        q.isBlank() || (r.a + " " + r.b + " " + r.c).contains(q, ignoreCase = true)
    }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = q, onValueChange = { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            label = { Text(if (u) "تلاش" else "Search") }
        )
        Spacer(Modifier.height(8.dp))
        if (shown.isEmpty()) {
            Text(
                if (u) "ابھی کوئی ریکارڈ نہیں۔ ایڈمن تصدیق شدہ معلومات شامل کرے گا۔"
                else "No records yet. The admin adds verified information.",
                modifier = Modifier.padding(16.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown) { iv ->
                val r = iv.value
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.a, style = MaterialTheme.typography.titleMedium)
                            lines(r).filter { it.isNotBlank() }.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            Text(r.phone, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { dial(ctx, r.phone) }) { Icon(Icons.Default.Call, if (u) "کال" else "Call") }
                        if (admin) IconButton(onClick = { s.remove(tab, iv.index) }) { Icon(Icons.Default.Delete, if (u) "حذف" else "Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDialog(labels: List<String>, rx: Boolean, u: Boolean, onDismiss: () -> Unit, onSave: (Rec) -> Unit) {
    val v = remember { mutableStateListOf("", "", "", "") }
    var flag by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (u) "نیا اندراج" else "Add new") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                labels.forEachIndexed { i, l ->
                    if (l.isNotEmpty()) OutlinedTextField(
                        value = v[i],
                        onValueChange = { v[i] = if (i == 3) it.filter { ch -> ch.isDigit() || ch == '+' || ch == '-' } else it },
                        label = { Text(l) }, singleLine = true
                    )
                }
                if (rx) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = flag, onCheckedChange = { flag = it })
                    Text(if (u) "نسخہ ضروری ہے" else "Prescription required")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (v[0].isNotBlank() && v[3].isNotBlank()) onSave(Rec(v[0].trim(), v[1].trim(), v[2].trim(), v[3].trim(), flag))
            }) { Text(if (u) "محفوظ کریں" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(if (u) "منسوخ" else "Cancel") } }
    )
}

@Composable
fun AdminTab(s: Store, admin: Boolean, u: Boolean, setAdmin: (Boolean) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (admin) {
            Text(if (u) "ایڈمن موڈ فعال ہے۔ دوسرے ٹیبز میں + اور حذف کے بٹن دستیاب ہیں۔" else "Admin mode is on. Use the + and delete buttons in the other tabs.")
            Button(onClick = { setAdmin(false) }) { Text(if (u) "لاگ آؤٹ" else "Log out") }
        } else {
            Text(
                if (s.hasPin()) (if (u) "ایڈمن پن درج کریں" else "Enter admin PIN")
                else (if (u) "نیا ایڈمن پن بنائیں (کم از کم 4 ہندسے)" else "Create an admin PIN (min 4 digits)")
            )
            OutlinedTextField(
                value = pin, onValueChange = { pin = it.filter { c -> c.isDigit() }; err = false }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), isError = err, label = { Text("PIN") }
            )
            Button(onClick = {
                if (s.hasPin()) { if (s.checkPin(pin)) setAdmin(true) else err = true }
                else if (pin.length >= 4) { s.setPin(pin); setAdmin(true) } else err = true
            }) { Text(if (u) "داخل ہوں" else "Continue") }
        }
        HorizontalDivider()
        Text(
            if (u) "اہم: یہ ایپ طبی مشورہ نہیں دیتی۔ ڈاکٹر سے رجوع کریں۔ نسخے والی دوائیں صرف لائسنس یافتہ فارمیسی نسخے پر فروخت کر سکتی ہے۔ ادائیگی ایپ سے باہر براہِ راست فروخت کنندہ سے ہوتی ہے۔"
            else "Important: this app gives no medical advice. Consult a doctor. Prescription medicines may only be sold by licensed pharmacies with a valid prescription. Payment happens outside the app, directly with the seller.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
