package com.example.math

import kotlin.math.*

class ExpressionEvaluator(private val isDegreeMode: Boolean = false) {

    fun evaluate(expressionString: String): Double {
        // Clean up the string to ensure mathematical uniformity
        val cleaned = expressionString
            .replace(" ", "")
            .replace("mod", "%")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")

        if (cleaned.isEmpty()) return 0.0

        return object {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < cleaned.length) cleaned[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < cleaned.length) throw RuntimeException("Unexpected character: " + ch.toChar())
                return x
            }

            // expression = term | expression `+` term | expression `-` term
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else break
                }
                return x
            }

            // term = factor | term `*` factor | term `/` factor
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor // division
                    } else break
                }
                return x
            }

            // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor
            fun parseFactor(): Double {
                if (eat('+'.code)) return +parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis")
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = cleaned.substring(startPos, this.pos).toDouble()
                } else if ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'p'.code) { // functions or constants
                    while ((ch >= 'a'.code && ch <= 'z'.code)) nextChar()
                    val name = cleaned.substring(startPos, this.pos)
                    if (name == "pi") {
                        x = PI
                    } else if (name == "e") {
                        x = E
                    } else {
                        // It's a function
                        val hasParentheses = eat('('.code)
                        x = parseExpression()
                        if (hasParentheses && !eat(')'.code)) {
                            throw RuntimeException("Missing closing parenthesis for function $name")
                        }
                        x = when (name) {
                            "sin" -> if (isDegreeMode) sin(Math.toRadians(x)) else sin(x)
                            "cos" -> if (isDegreeMode) cos(Math.toRadians(x)) else cos(x)
                            "tan" -> if (isDegreeMode) tan(Math.toRadians(x)) else tan(x)
                            "asin" -> {
                                val res = asin(x)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "acos" -> {
                                val res = acos(x)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "atan" -> {
                                val res = atan(x)
                                if (isDegreeMode) Math.toDegrees(res) else res
                            }
                            "log" -> log10(x)
                            "ln" -> ln(x)
                            "sqrt" -> {
                                if (x < 0) throw ArithmeticException("Square root of negative number")
                                sqrt(x)
                            }
                            "cbrt" -> Math.cbrt(x)
                            "abs" -> abs(x)
                            else -> throw RuntimeException("Unknown function: $name")
                        }
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }

                // Suffix percentage check
                if (eat('%'.code)) {
                    x /= 100.0
                }

                // Factorial check
                while (eat('!'.code)) {
                    x = calculateFactorial(x)
                }

                // Exponentiation check
                if (eat('^'.code)) {
                    x = x.pow(parseFactor())
                }

                return x
            }

            private fun calculateFactorial(n: Double): Double {
                if (n < 0.0) throw ArithmeticException("Factorial of negative number is undefined")
                val integerPart = n.toInt()
                if (integerPart.toDouble() != n) throw ArithmeticException("Factorial is only defined for integers")
                if (integerPart > 170) return Double.POSITIVE_INFINITY // overflow for Double limit
                var result = 1.0
                for (i in 1..integerPart) {
                    result *= i
                }
                return result
            }
        }.parse()
    }
}
