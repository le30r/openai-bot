fun String.escapedMsg() = this
        .replace("_", "\\_")
        .replace("*", "\\*")
        .replace("[", "\\[")
        .replace("-", "\\-")
        .replace("`", "\\`")
        .replace(">", "\\>")
        .replace("<", "\\<")
        .replace("{", "\\{")
        .replace("}", "\\}")
        .replace("=", "\\=")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace(".", "\\.")
        .replace(",", "\\,")
        .replace("!", "\\!")
        .replace("?", "\\?")
        .replace("+", "\\+")

