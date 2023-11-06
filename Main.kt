package calculator
import java.math.BigInteger
import kotlin.math.pow

object Calculator {
    private val listVar = mutableMapOf<String, String>()
    private val operands = mapOf("-" to 0, "+" to 0, "*" to 1, "/" to 1, "(" to 2, ")" to 2, "^" to 3)
    private var list = mutableListOf<String>()
    fun input(input: String) {
        if (input.contains("=")) assignment(input)
        else calc(input)
    }
    private fun assignment(input: String) {
        if (!checkVar(input.substringBefore("="))) return
        if (!checkValue(input.substringAfter("="))) return
        val (variable, value) = input.filter { !it.isWhitespace() }.split("=")
        if (value.matches(Regex("""-?\d*"""))) listVar[variable] = value
        else if (checkVarInListVar(value)) listVar[variable] = listVar[value]!!
    }
    private fun calc(input: String) {
        if(!createInputListValue(input)) return
        val listCalc = mutableListOf<String>()
        var result: BigInteger
        val postfixList = createPostfixNotation()
        for (v in postfixList) {
            if (v.matches(Regex("""-?\d+"""))) listCalc.add(v)
            else {
                val l = listCalc.lastIndex
                result = transformStringOperandInMath(listCalc[l-1].toBigInteger(), listCalc[l].toBigInteger(), v).also { listCalc.removeAt(l); listCalc.removeAt(l-1) }
                listCalc.add(result.toString())
            }
        }
        println(listCalc.first())
    }
    private fun createPostfixNotation():MutableList<String> {
        val postfixList = mutableListOf<String>()
        val stack = mutableListOf<String>()
        for (i in list) {
            if (i.matches(Regex("""-?\d+"""))) postfixList.add(i)
            else {
                when {
                    stack.isEmpty()||stack.last() == "(" -> stack.add(i)
                    i == "(" -> stack.add(i)
                    operands[i]!! > operands[stack.last()]!! && i != ")" -> stack.add(i)
                    else -> {
                        if (i == ")") {
                            for(j in stack.size - 1 downTo  0) {
                                if (stack[j] != "(") { postfixList.add(stack[j]); stack.removeAt(j) }
                                else { stack.removeAt(j); break }
                            }
                        }
                        else {
                            for (j in stack.size - 1 downTo  0) {
                                if (operands[i]!! <= operands[stack[j]]!! && stack[j] != "(") { postfixList.add(stack[j]); stack.removeAt(j) }
                                else break
                            }
                            stack.add(i)
                        }
                    }
                }
            }
        }
        stack.reversed().forEach { postfixList.add(it) }
        return  postfixList
    }
    private fun createInputListValue(input: String):Boolean {
        if (input.count{ it == '('} != input.count{ it == ')'} || Regex("""[*/^]{2,}""").find(input) != null){ println("Invalid expression"); return false }
        val listWithoutUnary = input.replace("--", "+").replace(Regex("""[+]+"""), "+").replace("+-", "-")
        list = listWithoutUnary.replace("-", " - ").replace("+", " + ").replace("*", " * ").replace("/", " / ").replace("(", " ( ").replace(")", " ) ").replace("^", " ^ ").split(" ").filter { it.isNotBlank() }.toMutableList()
        for (i in list.size-2 downTo 1) {
            if (list[i] == "-" && list[i-1].matches(Regex("""[-+*/(^]"""))) { list[i + 1] = "-" + list[i + 1]; list.removeAt(i) }
        }
        for (str in list) {
            if (str.all { !it.isLetterOrDigit() }) continue
            if (!checkValue(str)) return false
            if (str.matches(Regex("""[a-zA-Z]+"""))) {
                if (!checkVarInListVar(str)) return false
                else list[list.indexOf(str)] = listVar[str]!!
            }
        }
        return true
    }
    private fun transformStringOperandInMath(var1:BigInteger, var2:BigInteger, operand:String):BigInteger {
       return when(operand) {
           "+" -> var1 + var2
           "*" -> var1 * var2
           "/" -> {
               if (var2 == BigInteger.ZERO) { println("You can't divide by zero!"); return BigInteger.ZERO }
               else var1 / var2
           }
           "-" -> var1 - var2
           "^" -> var1.pow(var2.toInt())
           else -> return BigInteger.ZERO
        }
    }
    private fun checkVar(input: String): Boolean {
        if (!input.matches(Regex("""\s*[a-zA-Z]+\s*"""))) { println("Invalid identifier"); return false }
        return true
    }
    private fun checkValue(input: String): Boolean {
        if (!input.matches(Regex("""\s*(-?\d+|[a-zA-Z]+)\s*"""))) { println("Invalid assignment"); return false }
        return true
    }
    private fun checkVarInListVar(variable: String): Boolean {
        if (!listVar.containsKey(variable)) { println("Unknown variable"); return false }
        return true
    }
}

fun main() {
    while(true) {
        val input = readln()
        when {
            input.isBlank()-> continue
            input == "/exit" -> { println("Bye!"); break }
            input == "/help" -> println("The calculator calculates the value of any expression, containing operands such as: +, -, *, :\nIt is also possible to use parentheses to perform priority operations. \nAll calculations are carried out according to mathematical rules.")
            input.startsWith("/") -> println("Unknown command")
            else -> Calculator.input(input)
        }
    }
}

