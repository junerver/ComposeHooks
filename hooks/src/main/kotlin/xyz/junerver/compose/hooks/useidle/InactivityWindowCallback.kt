package xyz.junerver.compose.hooks.useidle

import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SearchEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import xyz.junerver.compose.hooks.utils.currentTime

/*
  Description:
  Author: Junerver
  Date: 2024/7/9-9:10
  Email: junerver@gmail.com
  Version: v1.0
*/

internal class InactivityWindowCallback(
    private val originalCallback: Window.Callback,
    scope: CoroutineScope,
    private val timeout: Duration,
    private val interval: Duration = 500.milliseconds,
    private val isTimeoutCallback: (Boolean, Instant) -> Unit,
) : Window.Callback {

    private var lastActiveTime = currentTime

    init {
        scope.launch {
            while (isActive) {
                isTimeoutCallback(currentTime - lastActiveTime > timeout, lastActiveTime)
                delay(interval)
            }
        }
    }

    private fun updateLastInteractionTime() {
        lastActiveTime = currentTime
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        updateLastInteractionTime()
        return originalCallback.dispatchTouchEvent(event)
    }

    // Implement other methods by delegating to originalCallback
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return originalCallback.dispatchKeyEvent(event)
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        return originalCallback.dispatchKeyShortcutEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent?): Boolean {
        return originalCallback.dispatchTrackballEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        return originalCallback.dispatchGenericMotionEvent(event)
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        return originalCallback.dispatchPopulateAccessibilityEvent(event)
    }

    override fun onCreatePanelView(featureId: Int): View? {
        return originalCallback.onCreatePanelView(featureId)
    }

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        return originalCallback.onCreatePanelMenu(featureId, menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        return originalCallback.onPreparePanel(featureId, view, menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        return originalCallback.onMenuOpened(featureId, menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        return originalCallback.onMenuItemSelected(featureId, item)
    }

    override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams) {
        originalCallback.onWindowAttributesChanged(attrs)
    }

    override fun onContentChanged() {
        originalCallback.onContentChanged()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        originalCallback.onWindowFocusChanged(hasFocus)
    }

    override fun onAttachedToWindow() {
        originalCallback.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        originalCallback.onDetachedFromWindow()
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        originalCallback.onPanelClosed(featureId, menu)
    }

    override fun onSearchRequested(): Boolean {
        return originalCallback.onSearchRequested()
    }

    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        return originalCallback.onSearchRequested(searchEvent)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
        return originalCallback.onWindowStartingActionMode(callback)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        return originalCallback.onWindowStartingActionMode(callback, type)
    }

    override fun onActionModeStarted(mode: ActionMode) {
        originalCallback.onActionModeStarted(mode)
    }

    override fun onActionModeFinished(mode: ActionMode) {
        originalCallback.onActionModeFinished(mode)
    }
}
