package com.navtasks.app

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

internal val NavSentenceKeyboard = KeyboardOptions(
    capitalization = KeyboardCapitalization.Sentences, autoCorrectEnabled = true)
internal val NavTitleKeyboard = KeyboardOptions(
    capitalization = KeyboardCapitalization.Sentences, autoCorrectEnabled = true)
internal val NavIdentifierKeyboard = KeyboardOptions(
    capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false)
internal const val NavRichHtmlMax = 24000

/** Full-page note body is larger; small work-comment fields retain 14/20sp. */
internal object NavRichNoteTypography {
    const val fullPageBodySp = 17
    const val fullPageLineSp = 25
    const val compactBodySp = 14
    const val compactLineSp = 20
}

internal fun navRichNoteAllowed(plain: String, html: String, max: Int): Boolean =
    plain.length <= max && html.length <= NavRichHtmlMax

internal fun navEscapeNoteHtml(plain: String): String =
    plain.split('\n').joinToString("") { part ->
        "<p>" + part.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;") + "</p>"
    }

@Composable
internal fun NavRichNoteEditor(
    initialPlain: String, initialHtml: String, entryKey: String,
    p: NavPalette, lang: UiLanguage, placeholder: String,
    maxCharacters: Int, onChange: (String, String) -> Unit,
    fullPage: Boolean = false,
) {
    val state = rememberRichTextState()
    var ready by remember(entryKey) { mutableStateOf(false) }
    var showColors by remember(entryKey) { mutableStateOf(false) }
    LaunchedEffect(entryKey) {
        ready = false
        state.setHtml(initialHtml.takeIf { it.isNotBlank() } ?: navEscapeNoteHtml(initialPlain))
        ready = true
    }
    LaunchedEffect(state.annotatedString, ready) {
        if (ready) onChange(state.annotatedString.text, state.toHtml())
    }
    Column(if (fullPage) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val formattingTools: @Composable () -> Unit = {
        Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            NavRichTool("B", p, selected = state.currentSpanStyle.fontWeight == FontWeight.Bold) { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
            NavRichTool("I", p, selected = state.currentSpanStyle.fontStyle == FontStyle.Italic) { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
            NavRichTool("U̲", p, selected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true) { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
            NavRichTool("S̶", p, selected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true) { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }
            NavRichTool("◉", p, selected = showColors) { showColors = !showColors }
            NavRichTool("▰", p, selected = state.currentSpanStyle.background != Color.Unspecified) {
                state.toggleSpanStyle(SpanStyle(background = Color(0xFFFFD86B).copy(alpha = .55f)))
            }
        }
        if (showColors) Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Color.Unspecified, p.blue, Color(0xFFC04949),
                Color(0xFF2B9069), Color(0xFFBE852D)).forEach { color ->
                NavRichTool("●", p, fg = if (color == Color.Unspecified) p.text else color) {
                    if (color == Color.Unspecified)
                        state.removeSpanStyle(SpanStyle(color = state.currentSpanStyle.color))
                    else state.addSpanStyle(SpanStyle(color = color))
                    showColors = false
                }
            }
        }
        }
        // Notes-style editor uses full height and toolbar above keyboard.
        if (!fullPage) formattingTools()
        val editorModifier = if (fullPage) Modifier.fillMaxWidth().weight(1f)
            else Modifier.fillMaxWidth().heightIn(min = 115.dp, max = 200.dp)
        Box(editorModifier.navGlass(p, radius = 12.dp, role = GlassRole.CONTROL)
                .padding(10.dp)) {
            if (ready && state.annotatedString.text.isEmpty())
                Text(placeholder, color = p.muted, fontSize = 12.sp)
            if (ready) RichTextEditor(
                state = state,
                modifier = if (fullPage) Modifier.fillMaxSize()
                    else Modifier.fillMaxWidth().heightIn(min = 95.dp),
                textStyle = TextStyle(
                    color = p.text,
                    fontSize = (if (fullPage) NavRichNoteTypography.fullPageBodySp
                        else NavRichNoteTypography.compactBodySp).sp,
                    lineHeight = (if (fullPage) NavRichNoteTypography.fullPageLineSp
                        else NavRichNoteTypography.compactLineSp).sp,
                ),
                keyboardOptions = NavSentenceKeyboard,
            )
        }
        if (fullPage) formattingTools()
        val size = state.annotatedString.text.length
        Text(
            if (navRichNoteAllowed(state.annotatedString.text, state.toHtml(), maxCharacters))
                "$size / $maxCharacters"
            else localized("Комментарий слишком длинный", "Note is too long", lang),
            color = if (navRichNoteAllowed(state.annotatedString.text, state.toHtml(), maxCharacters))
                p.muted else p.dangerText, fontSize = 11.sp)
    }
}
@Composable
private fun NavRichTool(label: String, p: NavPalette, fg: Color = p.text,
    selected: Boolean = false, onClick: () -> Unit) {
    Box(Modifier.size(42.dp).navGlass(p, radius = 11.dp,
            role = if (selected) GlassRole.SELECTED else GlassRole.CONTROL)
        .glassClickable(onClick), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) p.selectionContent else fg, fontSize = 20.sp,
            fontWeight = if (label == "B") FontWeight.Bold else FontWeight.Normal)
    }
}
@Composable
internal fun NavRichNoteText(plain: String, html: String, p: NavPalette,
    fontSize: TextUnit = 13.sp, modifier: Modifier = Modifier) {
    if (html.isBlank()) {
        Text(plain, color = p.muted, fontSize = fontSize, modifier = modifier)
    } else {
        val state = rememberRichTextState()
        var valid by remember(html) { mutableStateOf(false) }
        LaunchedEffect(html) { valid = runCatching { state.setHtml(html) }.isSuccess }
        if (valid) ProvideTextStyle(TextStyle(color = p.muted, fontSize = fontSize)) {
            RichText(state = state, modifier = modifier)
        }
        else Text(plain, color = p.muted, fontSize = fontSize, modifier = modifier)
    }
}
