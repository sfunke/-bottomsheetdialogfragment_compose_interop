package de.sfunke.bottomsheetdialogfragmentcomposeinterop.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import de.sfunke.bottomsheetdialogfragmentcomposeinterop.BuildConfig

class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
inline fun LogCompositions(tag: String, msg: String = "") {
    if (BuildConfig.DEBUG) {
        val ref = remember { Ref(0) }
        SideEffect { ref.value++ }
        println("${tag} >> Compositions: $msg ${ref.value} @${Thread.currentThread().name}")
    }
}