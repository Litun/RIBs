package com.badoo.ribs.template.rib_with_view.foo_bar.builder

import com.badoo.ribs.core.Node
import com.badoo.ribs.core.view.ViewFactory
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBar
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBar.Input
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBar.Output
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBarInteractor
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBarRouter
import com.badoo.ribs.template.rib_with_view.foo_bar.FooBarView
import com.badoo.ribs.template.rib_with_view.foo_bar.feature.FooBarFeature
import dagger.Provides
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

@dagger.Module
internal object FooBarModule {

    @FooBarScope
    @Provides
    @JvmStatic
    internal fun router(
        // pass component to child rib builders, or remove if there are none
        component: FooBarComponent
    ): FooBarRouter =
        FooBarRouter()

    @FooBarScope
    @Provides
    @JvmStatic
    internal fun feature(): FooBarFeature =
        FooBarFeature()

    @FooBarScope
    @Provides
    @JvmStatic
    internal fun interactor(
        router: FooBarRouter,
        input: ObservableSource<Input>,
        output: Consumer<Output>,
        feature: FooBarFeature
    ): FooBarInteractor =
        FooBarInteractor(
            router = router,
            input = input,
            output = output,
            feature = feature
        )

    @FooBarScope
    @Provides
    @JvmStatic
    internal fun node(
        viewFactory: ViewFactory<FooBarView>,
        router: FooBarRouter,
        interactor: FooBarInteractor
    ) : Node<FooBarView> = Node(
        identifier = object : FooBar {},
        viewFactory = viewFactory,
        router = router,
        interactor = interactor
    )
}
