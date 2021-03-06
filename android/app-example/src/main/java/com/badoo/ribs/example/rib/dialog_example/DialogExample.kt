package com.badoo.ribs.example.rib.dialog_example

import com.badoo.ribs.core.Rib
import com.badoo.ribs.core.directory.CanProvideRibCustomisation
import com.badoo.ribs.core.directory.inflateOnDemand
import com.badoo.ribs.core.view.ViewFactory
import com.badoo.ribs.dialog.CanProvideDialogLauncher
import com.badoo.ribs.example.R

interface DialogExample : Rib {

    interface Dependency : CanProvideDialogLauncher, CanProvideRibCustomisation

    class Customisation(
        val viewFactory: ViewFactory<DialogExampleView> = inflateOnDemand(
            R.layout.rib_dialog_example
        )
    )
}
