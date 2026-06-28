package com.kb

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class IMEService : InputMethodService() {

    private var keyboard: KeyboardPanel? = null

    override fun onCreateInputView(): View {
        keyboard = KeyboardPanel(this)
        return keyboard!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        keyboard?.reset()
    }

    // ── Called from KeyboardPanel ──────────────────────────────────

    fun sendChar(c: Char) {
        currentInputConnection?.commitText(c.toString(), 1)
    }

    fun sendText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun delete() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    fun deleteWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val trimmed = before.trimEnd()
        val lastSpace = trimmed.lastIndexOf(' ')
        val count = if (lastSpace < 0) trimmed.length else trimmed.length - lastSpace
        ic.deleteSurroundingText(count, 0)
    }

    fun sendEnter() {
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo ?: run {
            ic.commitText("\n", 1)
            return
        }
        val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
        when (action) {
            EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
            EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
            EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
            EditorInfo.IME_ACTION_NEXT -> ic.performEditorAction(EditorInfo.IME_ACTION_NEXT)
            else -> ic.commitText("\n", 1)
        }
    }

    fun shouldCapitalize(): Boolean {
        val before = currentInputConnection?.getTextBeforeCursor(3, 0)?.toString() ?: return true
        if (before.isEmpty()) return true
        val trimmed = before.trimEnd()
        return trimmed.isEmpty() || trimmed.endsWith('.') || trimmed.endsWith('!') || trimmed.endsWith('?')
    }

    fun applyAutoConvert(shortcut: String): String? {
        return AutoConvertManager.get(shortcut)
    }

    fun filterBannedWords(text: String): String {
        return BannedWordsFilter.filter(text)
    }
}
