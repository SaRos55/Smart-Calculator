package calculator

import java.lang.Exception
import java.math.BigInteger

val variables = mutableMapOf<String, BigInteger>()

fun main() {
    do {
        val input = readln().replace(Regex("\\s+"), " ").trim()
        if (input == "/exit") break
        when {
            Regex("\\s*").matches(input) -> continue
            Regex("/.*").matches(input) -> action(input)
            Regex("[a-zA-Z]+\\s*(=.*)?").matches(input) -> variablesFun(input)
            else -> calculate(input)
        }
    } while (true)
    println("Bye!")
}

fun variablesFun(input: String) {
    val listAssignment = input.replace(Regex("\\s"), "").split('=')
    val variable = listAssignment[0]

    if (!Regex("[a-zA-Z]+").matches(variable)) {
        println("Invalid identifier")
        return
    }

    if (listAssignment.size == 1)
        if (variables.containsKey(variable)) {
            println(variables[variable])
            return
        } else {
            println("Unknown variable")
            return
        }

    if (listAssignment.size == 2) {
        val v = listAssignment[1]
        if (Regex("-?\\d+").matches(v)) {
            variables[variable] = v.toBigInteger()
            return
        }

        if (Regex("[a-zA-Z]+").matches(v)) {
            if (variables.containsKey(v)) {
                val newV = variables[v]
                if (newV != null) {
                    variables[variable] = newV
                    return
                }
            }
            println("Unknown variable")
            return
        }
    }

    println("Invalid assignment")
}

fun action(input: String) {
    if (input == "/help") println("The program calculates the sum of numbers")
    else if (Regex("/.*").matches(input)) println("Unknown command")
}

fun calculate(input: String) {
    if (input.isEmpty()) return
    val listExp = Regex("(\\w+|[*/()]|[+\\-]+)").findAll(input.replace(Regex("\\s"), ""))
        .map {
            transformOperators(it.value)
        }.toMutableList()
    for (i in 1..listExp.lastIndex) {
        if (Regex("-\\w+").matches(listExp[i])) {
            listExp[i] = listExp[i].replaceFirst("-", "")
            listExp[i - 1] = transformOperators(listExp[i - 1] + "-")
        }
    }
    val postfix = mutableListOf<String>()
    val stack = mutableListOf<String>()
    if (Regex("[+-]").matches(listExp.first())) postfix.add("0")

    try {
        listExp.forEach {
            if (Regex("(\\d+)").matches(it)) postfix.add(it)
            else if (it == "(") stack.add(it)
            else if (stack.isEmpty() || stack.last() == "(") stack.add(it)
            else if (it == ")") {
                do {
                    postfix.add(stack.removeLast())
                } while (stack.last() != "(")
                stack.removeLast()
            } else if (Regex("[*/]").matches(it) && Regex("[+-]").matches(stack.last())) stack.add(it)
            else {
                do {
                    postfix.add(stack.removeLast())
                } while (stack.isNotEmpty()
                    && !(Regex("[*/]").matches(it) && Regex("[+-]").matches(stack.last())
                            || stack.last() == "(")
                )
                stack.add(it)
            }
        }
    } catch (e: Exception) {
        println("Invalid expression")
        return
    }
    for (i in stack.lastIndex downTo 0) postfix.add(stack[i])
    calculatePostfix(postfix)
}

fun calculatePostfix(postfix: List<String>) {
    val stack = mutableListOf<BigInteger>()
    postfix.forEach {
        if (Regex("-?\\d+").matches(it)) stack.add(it.toBigInteger())
        else {
            val result: BigInteger
            val lastIndex = stack.lastIndex
            try {
                result = when (it) {
                    "+" -> stack[lastIndex - 1] + stack[lastIndex]
                    "-" -> stack[lastIndex - 1] - stack[lastIndex]
                    "*" -> stack[lastIndex - 1] * stack[lastIndex]
                    "/" -> stack[lastIndex - 1] / stack[lastIndex]
                    else -> BigInteger.ZERO
                }
                stack.removeLast()
                stack.removeLast()
            } catch (e: Exception) {
                println("Invalid expression")
                return
            }
            stack.add(result)
        }
    }
    println(stack.last())
}

fun transformOperators(value: String): String {
    if (Regex("(\\d+|[*/]|[()])").matches(value)) return value
    if (variables.containsKey(value)) return variables[value].toString()

    return if (Regex("[+-]+").matches(value)) {
        var string = value
        string = Regex("(\\+{2}|-{2})").replace(string, "+")
        string = Regex("(\\+-|-\\+)").replace(string, "-")
        return if (Regex("[+-]").matches(string)) string else transformOperators(string)
    } else ""
}