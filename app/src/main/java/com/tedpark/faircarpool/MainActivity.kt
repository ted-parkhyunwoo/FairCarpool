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
import androidx.compose.ui.unit.sp

import androidx.compose.material3.MenuAnchorType


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSelector(
    selectedValue: String,
    onValueChange: (String) -> Unit
) {
    val options = (1..6).map { it.toString() }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { }, // 직접 입력 막음
            readOnly = true,
            isError = selectedValue.isBlank(),
            label = { Text("탑승인원", fontSize = 12.sp) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .height(60.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option + "명") },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun DriveDataUI() {
    var totalDistance by remember { mutableStateOf("") }
    var fuelEconomy by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var tollFee by remember { mutableStateOf("") }
    var etcCost by remember { mutableStateOf("") }
    var totalPerson by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("없음") }

    // OutlinedTextField 필드를 생성하는 함수.
    //원형: OutlinedTextField(value = totalDistance, onValueChange = { totalDistance = it }, modifier = Modifier.fillMaxWidth(), suffix = { Text("KM") }, label = { Text("총 운행거리", ) })
    @Composable
    fun InputField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        suffix: String = "",
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        validateBlank: Boolean = true
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            isError = validateBlank && value.isBlank(), // 조건부 에러 적용
            modifier = modifier,
            suffix = { if (suffix.isNotEmpty()) Text(suffix) }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("필수:", fontSize = 12.sp)
        InputField("총 운행거리", totalDistance, { totalDistance = it }, "KM")
        InputField("연비", fuelEconomy, { fuelEconomy = it }, "KM/L")
        InputField("연료 가격", fuelCost, { fuelCost = it }, "원")
//        InputField("탑승인원", totalPerson, { totalPerson = it }, "명")
        PassengerSelector(
            selectedValue = totalPerson,
            onValueChange = { totalPerson = it }
        )

        Text("선택:", fontSize = 12.sp)
        InputField("톨게이트 비용", tollFee, { tollFee = it }, "원", validateBlank = false)
        InputField("추가 비용(식사, 음료 등)", etcCost, { etcCost = it }, "원", validateBlank = false)

        val keyboardController = LocalSoftwareKeyboardController.current

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(onClick = {
                // 초기화 버튼 동작
                totalDistance = ""
                fuelEconomy = ""
                fuelCost = ""
                tollFee = ""
                etcCost = ""
                totalPerson = ""
                result = "None"
                keyboardController?.hide() // hide keyboard.
            }) { Text("초기화") }

            Button(onClick = {
                keyboardController?.hide() // hide keyboard.
                val distance = totalDistance.toFloatOrNull() ?: 0f
                val economy = fuelEconomy.toFloatOrNull() ?: 0f
                val fuel = fuelCost.toIntOrNull() ?: 0
                val toll = tollFee.toIntOrNull() ?: 0
                val etc = etcCost.toIntOrNull() ?: 0
                val persons = totalPerson.toIntOrNull() ?: 0

                if (distance > 0 && economy > 0 && fuel > 0 && persons > 0) {
                    val totalFuel = distance / economy
                    val totalCost = (totalFuel * fuel) + toll + etc
                    val costPerPerson = totalCost / persons

                    result = """
                총 사용 연료: ${"%.2f".format(totalFuel)} 리터
                총 비용: ${totalCost.toInt()} 원
                1명당 비용: ${costPerPerson.toInt()} 원
            """.trimIndent()
                } else {
                    result = "잘못 입력된 필드가 있습니다."
                }
            }) { Text("계산") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("결과:", fontSize = 12.sp)
        Text(result)
    }
}