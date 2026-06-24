package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.CalculationHistory
import com.example.math.ConverterCategory
import com.example.math.UnitType
import com.example.ui.components.CalcButton
import com.example.ui.theme.*
import com.example.viewmodel.CalculatorViewModel
import kotlin.math.abs

enum class CalcMode {
    BASIC, SCIENTIFIC, CONVERTER, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isPersian by viewModel.isPersian.collectAsStateWithLifecycle()
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val liveResult by viewModel.liveResult.collectAsStateWithLifecycle()
    val isDegreeMode by viewModel.isDegreeMode.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    var activeMode by remember { mutableStateOf(CalcMode.BASIC) }

    // State for drag gestures (swipe to delete)
    var dragOffsetX by remember { mutableStateOf(0f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Container for Header & Screen Displays (Spacious and padded)
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP HEADER ROW (Mode selection + DEG/RAD & En/Fa quick controls)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mode Selector Tabs
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                CalcMode.BASIC to if (isPersian) "ساده" else "Basic",
                                CalcMode.SCIENTIFIC to if (isPersian) "علمی" else "Sci",
                                CalcMode.CONVERTER to if (isPersian) "تبدیل" else "Unit",
                                CalcMode.HISTORY to if (isPersian) "تاریخچه" else "Log"
                            ).forEach { (mode, label) ->
                                val isSelected = activeMode == mode
                                val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                val tc = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .clickable { activeMode = mode }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = tc,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // DEG/RAD & Persian/English Toggle Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DEG / RAD Toggle (Only relevant for basic & scientific calculators)
                            if (activeMode == CalcMode.BASIC || activeMode == CalcMode.SCIENTIFIC) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { viewModel.toggleDegreeMode() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isDegreeMode) "DEG" else "RAD",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Persian / English Translation Toggle
                            Box(
                                modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { viewModel.toggleLanguage() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isPersian) "En" else "Fa",
                                    color = AccentOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )
                }

                // 2. MIDDLE DISPLAY OR UNIT CONVERTER PANE
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AnimatedContent(
                        targetState = activeMode,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "mode_screen_transition"
                    ) { mode ->
                        when (mode) {
                            CalcMode.BASIC, CalcMode.SCIENTIFIC -> {
                                // Calculator Screen (Formula, Live result, Final Result)
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetX += dragAmount.x
                                                },
                                                onDragEnd = {
                                                    if (abs(dragOffsetX) > 100f) {
                                                        viewModel.backspace()
                                                    }
                                                    dragOffsetX = 0f
                                                }
                                            )
                                        },
                                    verticalArrangement = Arrangement.Bottom,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Scrollable calculation expression
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState(), reverseScrolling = true),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = viewModel.localizeString(expression.ifEmpty { "0" }),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            fontSize = 32.sp,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Live evaluation / preview
                                    AnimatedVisibility(visible = liveResult.isNotEmpty()) {
                                        Text(
                                            text = viewModel.localizeString("= $liveResult"),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Final output expression
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        // Copy result icon
                                        if (result.isNotEmpty() && result != "Error" && result != "خطا") {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(result))
                                                    Toast.makeText(
                                                        context,
                                                        if (isPersian) "نتیجه کپی شد" else "Copied to clipboard",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = ContentCopyIcon,
                                                    contentDescription = "Copy Result",
                                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.width(1.dp))
                                        }

                                        // Very large formatted result
                                        Text(
                                            text = viewModel.localizeString(result),
                                            style = MaterialTheme.typography.displayMedium,
                                            color = if (result == "Error" || result == "خطا") ErrorRed else MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (result.length > 10) 36.sp else 48.sp,
                                            textAlign = TextAlign.End,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            CalcMode.CONVERTER -> {
                                UnitConverterPane(viewModel, isPersian)
                            }
                            CalcMode.HISTORY -> {
                                HistoryPane(viewModel, isPersian)
                            }
                        }
                    }
                }
            }

            // 3. KEYPAD SECTION (Distinct elegant panel at the bottom)
            Box(
                modifier = Modifier
                    .weight(2.0f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                AnimatedContent(
                    targetState = activeMode,
                    transitionSpec = {
                        slideInHorizontally { width -> if (activeMode == CalcMode.SCIENTIFIC) width else -width } togetherWith
                                slideOutHorizontally { width -> if (activeMode == CalcMode.SCIENTIFIC) -width else width }
                    },
                    label = "keypad_transition"
                ) { mode ->
                    when (mode) {
                        CalcMode.BASIC -> BasicKeypad(viewModel, isPersian)
                        CalcMode.SCIENTIFIC -> ScientificKeypad(viewModel, isPersian)
                        CalcMode.CONVERTER -> ConverterKeypad(viewModel, isPersian)
                        CalcMode.HISTORY -> HistoryEmptyOrPrompt(historyList.isEmpty(), isPersian)
                    }
                }
            }
        }
    }
}

@Composable
fun BasicKeypad(viewModel: CalculatorViewModel, isPersian: Boolean) {
    val isDark = isSystemInDarkTheme()
    val numBg = if (isDark) ButtonNumberDark else ButtonNumberLight
    val numText = if (isDark) TextPrimaryDark else TextPrimaryLight
    val numBorder = BorderStroke(1.dp, if (isDark) ButtonNumberBorderDark else ButtonNumberBorderLight)

    val opBg = if (isDark) ButtonOperatorDark else ButtonOperatorLight
    val opText = if (isDark) TextOperatorDark else TextOperatorLight

    val parenBg = if (isDark) ButtonParenthesesDark else ButtonParenthesesLight
    val parenText = if (isDark) TextParenthesesDark else TextParenthesesLight

    val acBg = if (isDark) ButtonAcDark else ButtonAcLight
    val acText = if (isDark) TextAcDark else TextAcLight

    val eqBg = MaterialTheme.colorScheme.primary
    val eqText = MaterialTheme.colorScheme.onPrimary

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton("AC", { viewModel.clear() }, Modifier.weight(1f), acBg, acText)
            CalcButton("()", { viewModel.appendToExpression("(") }, Modifier.weight(1f), parenBg, parenText)
            CalcButton("%", { viewModel.appendToExpression("%") }, Modifier.weight(1f), parenBg, parenText)
            CalcButton("÷", { viewModel.appendOperator("/") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۷" else "7", { viewModel.appendToExpression("7") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۸" else "8", { viewModel.appendToExpression("8") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۹" else "9", { viewModel.appendToExpression("9") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("×", { viewModel.appendOperator("*") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۴" else "4", { viewModel.appendToExpression("4") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۵" else "5", { viewModel.appendToExpression("5") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۶" else "6", { viewModel.appendToExpression("6") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("-", { viewModel.appendOperator("-") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۱" else "1", { viewModel.appendToExpression("1") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۲" else "2", { viewModel.appendToExpression("2") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۳" else "3", { viewModel.appendToExpression("3") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("+", { viewModel.appendOperator("+") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۰" else "0", { viewModel.appendToExpression("0") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(".", { viewModel.appendToExpression(".") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("⌫", { viewModel.backspace() }, Modifier.weight(1f), parenBg, parenText)
            CalcButton("=", { viewModel.evaluateResult() }, Modifier.weight(1f), eqBg, eqText, isEqualButton = true)
        }
    }
}

@Composable
fun ScientificKeypad(viewModel: CalculatorViewModel, isPersian: Boolean) {
    val isDark = isSystemInDarkTheme()
    val numBg = if (isDark) ButtonNumberDark else ButtonNumberLight
    val numText = if (isDark) TextPrimaryDark else TextPrimaryLight
    val numBorder = BorderStroke(1.dp, if (isDark) ButtonNumberBorderDark else ButtonNumberBorderLight)

    val opBg = if (isDark) ButtonOperatorDark else ButtonOperatorLight
    val opText = if (isDark) TextOperatorDark else TextOperatorLight

    val parenBg = if (isDark) ButtonParenthesesDark else ButtonParenthesesLight
    val parenText = if (isDark) TextParenthesesDark else TextParenthesesLight

    val acBg = if (isDark) ButtonAcDark else ButtonAcLight
    val acText = if (isDark) TextAcDark else TextAcLight

    val eqBg = MaterialTheme.colorScheme.primary
    val eqText = MaterialTheme.colorScheme.onPrimary

    val sciFuncBg = if (isDark) ButtonParenthesesDark else ButtonParenthesesLight
    val sciFuncText = if (isDark) TextParenthesesDark else ScientificTextPurple

    // Grid representing scientific functions + basic keypad merged
    val rows = listOf(
        listOf("sin", "cos", "tan", "sqrt"),
        listOf("log", "ln", "!", "^"),
        listOf("π", "e", "(", ")"),
        listOf("AC", "÷", "⌫", "=")
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { btn ->
                    val config = when (btn) {
                        "sin" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("sin(") })
                        "cos" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("cos(") })
                        "tan" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("tan(") })
                        "sqrt" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("sqrt(") })
                        "log" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("log(") })
                        "ln" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("ln(") })
                        "!" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendToExpression("!") })
                        "^" -> Triple(sciFuncBg, sciFuncText, { viewModel.appendOperator("^") })
                        "π" -> Triple(numBg, numText, { viewModel.appendToExpression("π") })
                        "e" -> Triple(numBg, numText, { viewModel.appendToExpression("e") })
                        "(" -> Triple(parenBg, parenText, { viewModel.appendToExpression("(") })
                        ")" -> Triple(parenBg, parenText, { viewModel.appendToExpression(")") })
                        "AC" -> Triple(acBg, acText, { viewModel.clear() })
                        "÷" -> Triple(opBg, opText, { viewModel.appendOperator("/") })
                        "⌫" -> Triple(parenBg, parenText, { viewModel.backspace() })
                        "=" -> Triple(eqBg, eqText, { viewModel.evaluateResult() })
                        else -> Triple(numBg, numText, {})
                    }
                    val hasB = btn in listOf("π", "e")
                    CalcButton(
                        text = btn,
                        onClick = config.third,
                        modifier = Modifier.weight(1f),
                        backgroundColor = config.first,
                        contentColor = config.second,
                        isEqualButton = btn == "=",
                        border = if (hasB) numBorder else null
                    )
                }
            }
        }

        // Add standard digits row for scientific mode to keep it self-contained
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۷" else "7", { viewModel.appendToExpression("7") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۸" else "8", { viewModel.appendToExpression("8") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۹" else "9", { viewModel.appendToExpression("9") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("×", { viewModel.appendOperator("*") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۴" else "4", { viewModel.appendToExpression("4") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۵" else "5", { viewModel.appendToExpression("5") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۶" else "6", { viewModel.appendToExpression("6") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("-", { viewModel.appendOperator("-") }, Modifier.weight(1f), opBg, opText)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۱" else "1", { viewModel.appendToExpression("1") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۲" else "2", { viewModel.appendToExpression("2") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۳" else "3", { viewModel.appendToExpression("3") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("+", { viewModel.appendOperator("+") }, Modifier.weight(1f), opBg, opText)
        }
    }
}

@Composable
fun ConverterKeypad(viewModel: CalculatorViewModel, isPersian: Boolean) {
    val isDark = isSystemInDarkTheme()
    val numBg = if (isDark) ButtonNumberDark else ButtonNumberLight
    val numText = if (isDark) TextPrimaryDark else TextPrimaryLight
    val numBorder = BorderStroke(1.dp, if (isDark) ButtonNumberBorderDark else ButtonNumberBorderLight)

    val parenBg = if (isDark) ButtonParenthesesDark else ButtonParenthesesLight
    val parenText = if (isDark) TextParenthesesDark else TextParenthesesLight

    val acBg = if (isDark) ButtonAcDark else ButtonAcLight
    val acText = if (isDark) TextAcDark else TextAcLight

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۷" else "7", { viewModel.appendToConverterInput("7") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۸" else "8", { viewModel.appendToConverterInput("8") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۹" else "9", { viewModel.appendToConverterInput("9") }, Modifier.weight(1f), numBg, numText, border = numBorder)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۴" else "4", { viewModel.appendToConverterInput("4") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۵" else "5", { viewModel.appendToConverterInput("5") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۶" else "6", { viewModel.appendToConverterInput("6") }, Modifier.weight(1f), numBg, numText, border = numBorder)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton(if (isPersian) "۱" else "1", { viewModel.appendToConverterInput("1") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۲" else "2", { viewModel.appendToConverterInput("2") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(if (isPersian) "۳" else "3", { viewModel.appendToConverterInput("3") }, Modifier.weight(1f), numBg, numText, border = numBorder)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CalcButton("C", { viewModel.clearConverterInput() }, Modifier.weight(1f), acBg, acText)
            CalcButton(if (isPersian) "۰" else "0", { viewModel.appendToConverterInput("0") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton(".", { viewModel.appendToConverterInput(".") }, Modifier.weight(1f), numBg, numText, border = numBorder)
            CalcButton("⌫", { viewModel.backspaceConverterInput() }, Modifier.weight(1f), parenBg, parenText)
        }
    }
}

@Composable
fun UnitConverterPane(viewModel: CalculatorViewModel, isPersian: Boolean) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val fromUnit by viewModel.fromUnit.collectAsStateWithLifecycle()
    val toUnit by viewModel.toUnit.collectAsStateWithLifecycle()
    val converterInput by viewModel.converterInput.collectAsStateWithLifecycle()
    val converterOutput by viewModel.converterOutput.collectAsStateWithLifecycle()

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Category Pills Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConverterCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectConverterCategory(category) },
                    label = { Text(if (isPersian) category.nameFa else category.nameEn) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conversion Cards
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "FROM" Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Unit selector dropdown trigger
                    Box {
                        Text(
                            text = "${if (isPersian) fromUnit.nameFa else fromUnit.nameEn} (${fromUnit.symbol}) ▾",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showFromDropdown = true }
                                .padding(4.dp)
                        )
                        DropdownMenu(
                            expanded = showFromDropdown,
                            onDismissRequest = { showFromDropdown = false }
                        ) {
                            selectedCategory.units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${if (isPersian) unit.nameFa else unit.nameEn} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setFromUnit(unit)
                                        showFromDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Right: Value input display
                    Text(
                        text = viewModel.localizeString(converterInput),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // "TO" Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Unit selector dropdown trigger
                    Box {
                        Text(
                            text = "${if (isPersian) toUnit.nameFa else toUnit.nameEn} (${toUnit.symbol}) ▾",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange,
                            modifier = Modifier
                                .clickable { showToDropdown = true }
                                .padding(4.dp)
                        )
                        DropdownMenu(
                            expanded = showToDropdown,
                            onDismissRequest = { showToDropdown = false }
                        ) {
                            selectedCategory.units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${if (isPersian) unit.nameFa else unit.nameEn} (${unit.symbol})") },
                                    onClick = {
                                        viewModel.setToUnit(unit)
                                        showToDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Right: Output display
                    Text(
                        text = viewModel.localizeString(converterOutput),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryPane(viewModel: CalculatorViewModel, isPersian: Boolean) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPersian) "تاریخچه محاسبات اخیر" else "Recent Calculations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (historyList.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear All History",
                        tint = ErrorRed
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPersian) "تاریخچه خالی است" else "History is empty",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Double Click copies or lets use simple text clicks
                                Text(
                                    text = viewModel.localizeString(item.expression),
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.useHistoryItem(item, true)
                                            Toast.makeText(context, if (isPersian) "فرمول کپی شد به بالا" else "Formula pasted", Toast.LENGTH_SHORT).show()
                                        }
                                )

                                IconButton(
                                    onClick = { viewModel.deleteHistoryItem(item.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Item",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = viewModel.localizeString("= ${item.result}"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.useHistoryItem(item, false)
                                        Toast.makeText(context, if (isPersian) "نتیجه چسبانده شد" else "Result appended", Toast.LENGTH_SHORT).show()
                                    },
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryEmptyOrPrompt(isEmpty: Boolean, isPersian: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty) {
            Text(
                text = if (isPersian) "آیتم تاریخچه وجود ندارد" else "No history recorded yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = if (isPersian) "برای استفاده مجدد، روی فرمول یا نتیجه در بالا ضربه بزنید" else "Tap expression or result in history cards to reuse",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

val ContentCopyIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomContentCopy",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color(0xFF94A3B8)),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 9f)
            lineTo(17f, 9f)
            lineTo(17f, 17f)
            lineTo(9f, 17f)
            close()
        }
        path(
            stroke = SolidColor(Color(0xFF94A3B8)),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 13f)
            lineTo(5f, 5f)
            lineTo(13f, 5f)
        }
    }.build()
