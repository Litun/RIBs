package com.badoo.ribs.core.routing.backstack

import android.os.Bundle
import android.os.Parcelable
import com.badoo.ribs.core.Node
import com.badoo.ribs.core.Node.ViewAttachMode.PARENT
import com.badoo.ribs.core.routing.NodeConnector
import com.badoo.ribs.core.routing.action.RoutingAction
import com.badoo.ribs.core.routing.backstack.BackStackRibConnector.DetachStrategy.DESTROY
import com.badoo.ribs.core.routing.backstack.BackStackRibConnector.DetachStrategy.DETACH_VIEW

internal class BackStackRibConnector<C : Parcelable>(
    private val permanentParts: List<Node<*>>,
    private val resolver: (C) -> RoutingAction<*>,
    private val connector: NodeConnector
) {
    enum class DetachStrategy {
        DESTROY, DETACH_VIEW
    }

    init {
        permanentParts.forEach {
            connector.attachChildNode(it)
        }
    }

    fun leave(backStackElement: BackStackElement<C>, detachStrategy: DetachStrategy): BackStackElement<C> {
        with(backStackElement) {
            routingAction?.cleanup()

            when (detachStrategy) {
                DESTROY -> destroyChildren()
                DETACH_VIEW -> saveAndDetachChildViews()
            }
        }

        return backStackElement
    }

    private fun BackStackElement<C>.destroyChildren() {
        builtNodes?.forEach {
            connector.detachChildView(it.node)
            connector.detachChildNode(it.node)
        }
        builtNodes = null
    }

    private fun BackStackElement<C>.saveAndDetachChildViews() {
        builtNodes?.forEach {
            it.node.saveViewState()

            if (it.viewAttachMode == PARENT) {
                connector.detachChildView(it.node)
            }
        }
    }

    fun goTo(backStackElement: BackStackElement<C>): BackStackElement<C> {
        with(backStackElement) {
            if (routingAction == null) {
                routingAction = resolver.invoke(configuration)
            }

            if (builtNodes == null) {
                buildNodes()
                attachBuiltNodes()
            }

            reAttachViewsIfNeeded()
            routingAction!!.execute()
        }

        return backStackElement
    }

    private fun BackStackElement<C>.buildNodes() {
        builtNodes = routingAction!!.buildNodes()
    }

    private fun BackStackElement<C>.attachBuiltNodes() {
        builtNodes!!.forEachIndexed { index, nodeDescriptor ->
            connector.attachChildNode(nodeDescriptor.node, bundleAt(index))
        }
    }

    private fun BackStackElement<C>.reAttachViewsIfNeeded() {
        builtNodes!!
            .forEach {
                if (it.viewAttachMode == PARENT && !it.node.isViewAttached) {
                    connector.attachChildView(it.node)
                }
            }
    }

    private fun BackStackElement<C>.bundleAt(index: Int): Bundle? =
        bundles.elementAtOrNull(index)?.also {
            it.classLoader = BackStackManager.State::class.java.classLoader
        }

    fun shrinkToBundles(backStack: List<BackStackElement<C>>): List<BackStackElement<C>> =
        saveInstanceState(backStack).apply {
            dropLast(1).forEach {
                it.builtNodes?.forEach {
                    connector.detachChildView(it.node)
                    connector.detachChildNode(it.node)
                }
                it.builtNodes = null
            }
        }

    fun saveInstanceState(backStack: List<BackStackElement<C>>): List<BackStackElement<C>> {
        backStack.forEach {
            it.bundles = it.builtNodes?.map { nodeDescriptor ->
                Bundle().also {
                    nodeDescriptor.node.onSaveInstanceState(it)
                }
            } ?: emptyList()
        }

        return backStack
    }

    fun detachFromView(backStack: List<BackStackElement<C>>) {
        permanentParts.forEach { connector.detachChildView(it) }

        backStack.lastOrNull()?.let {
            leave(it, DETACH_VIEW)
        }
    }

    fun attachToView(backStack: List<BackStackElement<C>>) {
        permanentParts.forEach { connector.attachChildView(it) }

        backStack.lastOrNull()?.let {
            goTo(it)
        }
    }
}
