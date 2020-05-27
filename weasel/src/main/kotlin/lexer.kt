sealed class Token {
    // Keywords
    object IF: Token()
    object THEN: Token()
    object ELSE: Token()

    // Symbols
    object LEFT_PAREN: Token()
    object RIGHT_PAREN: Token()
    object LAMBDA: Token()
    object RIGHT_ARROW: Token()
    data class OPERATOR(val operator: String): Token()

    // Idents
    data class IDENT(val ident: String): Token()

    // Literals
    data class BOOLEAN(val boolean: Boolean): Token()
    data class NUMBER(val number: Int): Token()

    // Types
    data class PRIMITIVE_TYPE(val type: Type): Token()

    // EOF
    object END_OF_FILE: Token()
}

class Peekable<T>(val iterator: Iterator<T>) {
    var lookahead: T? = null
    fun next(): T? = when {
        lookahead != null -> lookahead.also { lookahead = null }
        iterator.hasNext() -> iterator.next()
        else -> null
    }

    fun peek(): T? = next().also { lookahead = it }
}

fun Char.isNewLine() : Boolean {
    return this == '\r' || this == '\n'
}

class Lexer(input: String) {
    private val chars = Peekable(input.iterator())
    private var lookahead: Token? = null

    fun peek(): Token = next().also { lookahead = it }
    fun next(): Token {
        lookahead?.let { lookahead = null; return it }
        consumeWhitespace()
        val c = chars.next() ?: return Token.END_OF_FILE
        return when(c) {
            '(' -> Token.LEFT_PAREN
            ')' -> Token.RIGHT_PAREN
            '+' -> Token.OPERATOR("+")
            '*' -> Token.OPERATOR("*")
            '\\' -> Token.LAMBDA
            '/' -> if(chars.next()=='/') comment() else throw Exception("Expected seccond '/")
            '-' -> if(chars.next() == '>') Token.RIGHT_ARROW else Token.OPERATOR("-")
            '=' -> if(chars.next() == '=') Token.OPERATOR("==") else throw Exception("Unclosed equals-equals token")
            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() -> number(c)
                else -> throw Exception("Unexpected $c")
            }
        }
    }

    private fun number(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isDigit() == true) res += chars.next()
        return Token.NUMBER(res.toInt())
    }
    private fun comment():Token {
        var c = chars.peek()
        while (true){
            if (c == '\n') break
            c = chars.next()
        }
        return next()
    }

    private fun ident(c: Char): Token {
        var res = c.toString()
        while (chars.peek()?.isJavaIdentifierPart() == true) res += chars.next()
        return when(res) {
            "if" -> Token.IF
            "then" -> Token.THEN
            "else" -> Token.ELSE
            "true" -> Token.BOOLEAN(true)
            "false" -> Token.BOOLEAN(false)
            "Number" -> Token.PRIMITIVE_TYPE(Type.Number)
            "Boolean" -> Token.PRIMITIVE_TYPE(Type.Boolean)
            else -> Token.IDENT(res)
        }
    }

    private fun consumeWhitespace() {
        do {
            while (chars.peek()?.isWhitespace() == true) chars.next()
        } while (consumeComment())
    }

    private fun consumeComment() : Boolean {
        if (chars.peek() != '/') {
            return false
        }
        chars.next()
        if (chars.peek() != '/')
            throw Exception("unexpected /. Use // for comments.")
        chars.next()

        while (chars.peek()?.isNewLine() == false) chars.next()

        return true
    }
}

fun main() {
    val input = """
        if (\x1 -> equals 20 x1) 25 // Kommentar
        then true
        else add 3 (multiply 4 5)
    """.trimIndent()
    val lexer = Lexer(input)
    while(lexer.next().also(::println) !is Token.END_OF_FILE) {}

    /// Uebung: Kommentare als Whitespace lexen
    // Kommentar Syntax: // Hello\n
    // Tipp: / <- kann keine andere tokens starten
}