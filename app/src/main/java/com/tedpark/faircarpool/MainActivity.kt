package com.tedpark.faircarpool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tedpark.faircarpool.ui.theme.FairCarpoolTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


// 앱 전역에서 사용하는 상수들
const val HEIGHT_INPUT_FIELD = 63
const val MINIMUM_PASSENGER = 2
const val MAXIMUM_PASSENGER = 6
const val INVALID_FLOAT = 0f
const val INVALID_INT = 0
const val DEFAULT_RESULT_TEXT = "입력 예)\n 총 운행거리: 260.5\n 연비: 19.7\n 연료가격: 1630\n 탑승인원: 3"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FairCarpoolTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DriveDataUI() // 메인 화면
                }
            }
        }
    }
}

// 탑승 인원을 드롭다운으로 선택하는 UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSelector(selectedValue: String, onValueChange: (String) -> Unit) {
    val options = (MINIMUM_PASSENGER..MAXIMUM_PASSENGER).map { it.toString() }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            isError = selectedValue.isBlank(),
            label = { Text("탑승인원", fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .height(HEIGHT_INPUT_FIELD.dp)
        )
        // 드롭다운 메뉴 구성
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$option 명") },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 입력 필드를 구성하는 컴포저블
@Composable
fun InputField(
    label: String,
    suffix: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    validateBlank: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        isError = validateBlank && value.isBlank(),
        modifier = Modifier
            .fillMaxWidth()
            .height(HEIGHT_INPUT_FIELD.dp),
        suffix = { if (suffix.isNotEmpty()) Text(suffix) }
    )
}

// 전체 UI 및 상태 처리
@Composable
fun DriveDataUI() {
    var totalDistance by remember { mutableStateOf("") }
    var fuelEconomy by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var tollFee by remember { mutableStateOf("") }
    var etcCost by remember { mutableStateOf("") }
    var totalPerson by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(DEFAULT_RESULT_TEXT) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // 스크롤 상태 추가
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        InputField("총 운행거리", "KM", totalDistance, { totalDistance = it })
        InputField("연비", "KM/L", fuelEconomy, { fuelEconomy = it })
        InputField("연료 가격", "원(리터당)", fuelCost, { fuelCost = it })
        PassengerSelector(totalPerson) { totalPerson = it }

        Spacer(modifier = Modifier.height(16.dp))

        Text("선택(입력 안해도 됨):", fontSize = 12.sp)
        InputField("톨게이트 비용", "원", tollFee, { tollFee = it }, validateBlank = false)
        InputField("추가 비용(식사, 음료 등)", "원", etcCost, { etcCost = it }, validateBlank = false)

        Spacer(modifier = Modifier.height(8.dp))

        val isInputValid = (totalDistance.toFloatOrNull() ?: INVALID_FLOAT) > 0f &&
                (fuelEconomy.toFloatOrNull() ?: INVALID_FLOAT) > 0f &&
                (fuelCost.toIntOrNull() ?: INVALID_INT) > 0 &&
                (totalPerson.toIntOrNull() ?: INVALID_INT) >= MINIMUM_PASSENGER

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                totalDistance = ""
                fuelEconomy = ""
                fuelCost = ""
                tollFee = ""
                etcCost = ""
                totalPerson = ""
                result = DEFAULT_RESULT_TEXT
                keyboardController?.hide()
            }) {
                Text("초기화")
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    val distance = totalDistance.toFloatOrNull() ?: INVALID_FLOAT
                    val economy = fuelEconomy.toFloatOrNull() ?: INVALID_FLOAT
                    val fuel = fuelCost.toIntOrNull() ?: INVALID_INT
                    val toll = tollFee.toIntOrNull() ?: INVALID_INT
                    val etc = etcCost.toIntOrNull() ?: INVALID_INT
                    val persons = totalPerson.toIntOrNull() ?: INVALID_INT

                    if (distance > 0 && economy > 0 && fuel > 0 && persons >= MINIMUM_PASSENGER) {
                        val totalFuel = distance / economy
                        val totalCost = (totalFuel * fuel) + toll + etc
                        val costPerPerson = totalCost / persons

                        result = buildString {
                            appendLine("총 사용 연료: ${"%.2f".format(totalFuel)} L")
                            appendLine("총 비용: %,d 원".format(totalCost.toInt()))
                            appendLine("1명당 비용: %,d 원".format(costPerPerson.toInt()))
                        }
                    } else {
                        result = "잘못 입력된 필드가 있습니다."
                    }
                },
                enabled = isInputValid
            ) {
                Text("계산")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("결과:", fontSize = 12.sp)
        Text(result, fontSize = 14.sp)
    }
}
