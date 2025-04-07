package com.tedpark.faircarpool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tedpark.faircarpool.ui.theme.FairCarpoolTheme
import androidx.compose.ui.platform.LocalSoftwareKeyboardController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FairCarpoolTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DriveDataUI()
                }
            }
        }
    }
}

@Composable
fun DriveDataUI() {
    var totalDistance by remember { mutableStateOf("") }
    var fuelEconomy by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var tollFee by remember { mutableStateOf("0") }
    var totalPerson by remember { mutableStateOf("1") }

    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("입력:")
        OutlinedTextField(value = totalDistance, onValueChange = { totalDistance = it }, label = { Text("총 운행거리 (KM)") })
        OutlinedTextField(value = fuelEconomy, onValueChange = { fuelEconomy = it }, label = { Text("연비 (Km/L)") })
        OutlinedTextField(value = fuelCost, onValueChange = { fuelCost = it }, label = { Text("연료 가격 (원)") })
        OutlinedTextField(value = tollFee, onValueChange = { tollFee = it }, label = { Text("톨게이트 비용 (원)") })
        OutlinedTextField(value = totalPerson, onValueChange = { totalPerson = it }, label = { Text("탑승인원") })

        Spacer(modifier = Modifier.height(16.dp))
        val keyboardController = LocalSoftwareKeyboardController.current
        Button(onClick = {
            keyboardController?.hide() // hide keyboard.
            val distance = totalDistance.toFloatOrNull() ?: 0f
            val economy = fuelEconomy.toFloatOrNull() ?: 0f
            val fuel = fuelCost.toIntOrNull() ?: 0
            val toll = tollFee.toIntOrNull() ?: 0
            val persons = totalPerson.toIntOrNull() ?: 0

            if (distance > 0 && economy > 0 && fuel > 0 && persons > 0) {
                val totalFuel = distance / economy
                val totalCost = (totalFuel * fuel) + toll
                val costPerPerson = totalCost / persons

                result = """
                    총 사용 연료: ${"%.2f".format(totalFuel)} 리터
                    총 비용: ${totalCost.toInt()} 원
                    1명당 비용: ${costPerPerson.toInt()} 원
                """.trimIndent()
            } else {
                result = "잘못 입력된 필드가 있습니다."
            }
        }) {
            Text("계산")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("결과:")
        Text(result)
    }
}