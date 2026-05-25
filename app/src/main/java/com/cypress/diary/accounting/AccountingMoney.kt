package com.cypress.diary.accounting

fun parseAmountCents(input: String): Long? {
    val normalized = input.trim()
    if (normalized.isBlank()) return null
    if (!normalized.matches(Regex("""\d+(\.\d{1,2})?"""))) return null
    val parts = normalized.split('.')
    val yuan = parts[0].toLongOrNull() ?: return null
    val centsText = parts.getOrNull(1).orEmpty().padEnd(2, '0')
    val cents = centsText.ifBlank { "00" }.toLongOrNull() ?: return null
    val total = yuan * 100 + cents
    return total.takeIf { it > 0 }
}

fun formatAmountCents(amountCents: Long): String {
    val absValue = kotlin.math.abs(amountCents)
    val sign = if (amountCents < 0) "-" else ""
    return "$sign${absValue / 100}.${(absValue % 100).toString().padStart(2, '0')}"
}
