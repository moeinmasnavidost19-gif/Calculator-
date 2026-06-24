package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CalculationHistory
import com.example.data.repository.HistoryRepository
import com.example.math.ConverterCategory
import com.example.math.ExpressionEvaluator
import com.example.math.UnitConverter
import com.example.math.UnitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(db.historyDao())

    // UI Language (True = Persian, False = English)
    private val _isPersian = MutableStateFlow(true)
    val isPersian: StateFlow<Boolean> = _isPersian.asStateFlow()

    // Calculator States
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _liveResult = MutableStateFlow("")
    val liveResult: StateFlow<String> = _liveResult.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    // History Flow
    val historyList: StateFlow<List<CalculationHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Unit Converter States
    private val _selectedCategory = MutableStateFlow<ConverterCategory>(ConverterCategory.Length)
    val selectedCategory: StateFlow<ConverterCategory> = _selectedCategory.asStateFlow()

    private val _fromUnit = MutableStateFlow<UnitType>(ConverterCategory.Length.units[0])
    val fromUnit: StateFlow<UnitType> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow<UnitType>(ConverterCategory.Length.units[1])
    val toUnit: StateFlow<UnitType> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _converterOutput = MutableStateFlow("1000")
    val converterOutput: StateFlow<String> = _converterOutput.asStateFlow()

    private val formatter = DecimalFormat("#.########", DecimalFormatSymbols(Locale.US))

    init {
        selectConverterCategory(ConverterCategory.Length)
    }

    fun toggleLanguage() {
        _isPersian.value = !_isPersian.value
    }

    fun toggleDegreeMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        evaluateLive()
    }

    // Append standard inputs or scientific functions
    fun appendToExpression(value: String) {
        val current = _expression.value
        if (value == "." && current.endsWith(".")) return
        _expression.value = current + value
        evaluateLive()
    }

    fun appendOperator(op: String) {
        val current = _expression.value
        if (current.isEmpty()) {
            if (op == "-") {
                _expression.value = "-"
            }
            return
        }
        val lastChar = current.last()
        if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '^' || lastChar == '*') {
            _expression.value = current.dropLast(1) + op
        } else {
            _expression.value = current + op
        }
        evaluateLive()
    }

    fun backspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val functions = listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan(", "log(", "ln(", "sqrt(", "cbrt(", "abs(")
            var deleted = false
            for (f in functions) {
                if (current.endsWith(f)) {
                    _expression.value = current.substring(0, current.length - f.length)
                    deleted = true
                    break
                }
            }
            if (!deleted) {
                _expression.value = current.dropLast(1)
            }
            evaluateLive()
        }
    }

    fun clear() {
        _expression.value = ""
        _result.value = ""
        _liveResult.value = ""
    }

    fun evaluateResult() {
        val expr = _expression.value
        if (expr.isEmpty()) return
        try {
            val evaluator = ExpressionEvaluator(_isDegreeMode.value)
            val res = evaluator.evaluate(expr)
            if (res.isNaN() || res.isInfinite()) {
                throw ArithmeticException("Invalid result")
            }
            val formattedResult = formatDouble(res)
            _result.value = formattedResult
            _liveResult.value = ""

            // Save to Room Database
            viewModelScope.launch {
                repository.insert(
                    CalculationHistory(
                        expression = expr,
                        result = formattedResult,
                        category = "basic"
                    )
                )
            }
        } catch (e: Exception) {
            _result.value = if (_isPersian.value) "خطا" else "Error"
            _liveResult.value = ""
        }
    }

    private fun evaluateLive() {
        val expr = _expression.value
        if (expr.isEmpty()) {
            _liveResult.value = ""
            return
        }
        if (expr == "-" || expr == "+") return
        try {
            var balancedExpr = expr
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }
            if (openCount > closeCount) {
                balancedExpr += ")".repeat(openCount - closeCount)
            }

            val evaluator = ExpressionEvaluator(_isDegreeMode.value)
            val res = evaluator.evaluate(balancedExpr)
            if (!res.isNaN() && !res.isInfinite()) {
                _liveResult.value = formatDouble(res)
            } else {
                _liveResult.value = ""
            }
        } catch (e: Exception) {
            _liveResult.value = ""
        }
    }

    private fun formatDouble(value: Double): String {
        return if (value % 1.0 == 0.0) {
            if (value > Long.MAX_VALUE.toDouble() || value < Long.MIN_VALUE.toDouble()) {
                formatter.format(value)
            } else {
                value.toLong().toString()
            }
        } else {
            formatter.format(value)
        }
    }

    // History Actions
    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun useHistoryItem(history: CalculationHistory, useAsExpression: Boolean) {
        if (useAsExpression) {
            _expression.value = history.expression
        } else {
            _expression.value = _expression.value + history.result
        }
        evaluateLive()
    }

    // Unit Converter Actions
    fun selectConverterCategory(category: ConverterCategory) {
        _selectedCategory.value = category
        _fromUnit.value = category.units[0]
        _toUnit.value = category.units[1]
        _converterInput.value = "1"
        performConversion()
    }

    fun setFromUnit(unit: UnitType) {
        _fromUnit.value = unit
        performConversion()
    }

    fun setToUnit(unit: UnitType) {
        _toUnit.value = unit
        performConversion()
    }

    fun appendToConverterInput(value: String) {
        val current = _converterInput.value
        if (current == "0" && value != ".") {
            _converterInput.value = value
        } else {
            if (value == "." && current.contains(".")) return
            _converterInput.value = current + value
        }
        performConversion()
    }

    fun backspaceConverterInput() {
        val current = _converterInput.value
        if (current.length <= 1) {
            _converterInput.value = "0"
        } else {
            _converterInput.value = current.dropLast(1)
        }
        performConversion()
    }

    fun clearConverterInput() {
        _converterInput.value = "0"
        _converterOutput.value = "0"
    }

    private fun performConversion() {
        val inputStr = _converterInput.value
        val value = inputStr.toDoubleOrNull() ?: 0.0
        val category = _selectedCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value
        val res = UnitConverter.convert(value, from, to, category)
        _converterOutput.value = formatDouble(res)
    }

    // Helper for formatting digits to Persian
    fun localizeString(str: String): String {
        return str.localizeDigits(_isPersian.value)
    }

    private fun String.localizeDigits(isPersian: Boolean): String {
        if (!isPersian) return this
        val englishDigits = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        var result = this
        for (i in 0..9) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        // Localize division and multiplication operators visually
        result = result.replace("*", "×").replace("/", "÷")
        return result
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CalculatorViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
