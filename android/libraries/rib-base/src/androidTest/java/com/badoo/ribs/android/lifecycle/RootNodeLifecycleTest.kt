package com.badoo.ribs.android.lifecycle

import android.arch.lifecycle.Lifecycle
import com.badoo.common.ribs.RibsRule
import com.badoo.ribs.test.util.ribs.root.TestRoot
import com.badoo.ribs.test.util.waitForActivityFinish
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class RootNodeLifecycleTest {
    private val provider = TestRoot.Provider()
    val node get() = provider.rootNode!!

    @get:Rule
    val ribsRule = RibsRule { provider() }

    @Test
    fun whenActivityResumed_nodeIsAttached() {
        assertThat(node.isAttached).isTrue()
    }

    @Test
    fun whenActivityResumed_viewIsAttached() {
        assertThat(node.isViewAttached).isTrue()
    }

    @Test
    fun whenActivityResumed_lifecycleEventsAreDispatched() {
        provider.viewLifecycleObserver.assertValues(
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_RESUME
        )
    }

    @Test
    fun whenActivityDestroyed_nodeIsDetached() {
        ribsRule.waitForActivityFinish()

        assertThat(node.isAttached).isFalse()
    }

    @Test
    fun whenActivityDestroyed_viewIsDetached() {
        ribsRule.waitForActivityFinish()

        assertThat(node.isViewAttached).isFalse()
    }

    @Test
    fun whenActivityDestroyed_lifecycleEventsAreDispatched() {
        val viewLifecycleObserver = provider.viewLifecycleObserver
        viewLifecycleObserver.clear()

        ribsRule.waitForActivityFinish()

        viewLifecycleObserver.assertValues(
            Lifecycle.Event.ON_PAUSE,
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY
        )
    }
}
