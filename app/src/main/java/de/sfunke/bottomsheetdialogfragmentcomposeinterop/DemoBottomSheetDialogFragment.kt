package de.sfunke.bottomsheetdialogfragmentcomposeinterop

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.sfunke.bottomsheetdialogfragmentcomposeinterop.utils.LogCompositions
import kotlinx.coroutines.delay


class DemoBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) {
            val sheetFragment = DemoBottomSheetDialogFragment()
            sheetFragment.show(fragmentManager, sheetFragment.tag)
        }
    }

    private lateinit var behavior: BottomSheetBehavior<ViewGroup>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)!!

            behavior = BottomSheetBehavior.from(bottomSheet).apply {
                isHideable = true
                state = BottomSheetBehavior.STATE_EXPANDED
                peekHeight = requireView().height - requireContext().resources.getDimensionPixelSize(R.dimen.bottom_sheet_top_margin)
                halfExpandedRatio = 0.8f
                skipCollapsed = true
                isDraggable = true
            }
        }
        return dialog
    }

    private fun allowSheetDrag(allowSheetDrag: Boolean) {
        if (!::behavior.isInitialized) return

        Log.w("XXX", "🟡 allowSheetDrag: ${allowSheetDrag}")
        behavior.isDraggable = allowSheetDrag
    }

    @ExperimentalComposeUiApi
    @ExperimentalFoundationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply composeView@{

        // test for few items, so list is not scrollable and sheet is always dragged
//        val items = (0..5).map { "Item $it" }

        // test for items just about filling the screen (adjust per screen size)
//        val items = (0..11).map { "Item $it" }

        // test for large amount of items
        val items = (0..100).map { "Item $it" }

        // Dispose the Composition when the view's LifecycleOwner is destroyed
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {

            val listState = rememberLazyListState()
            var listTouched by remember { mutableStateOf(false) }

            // whenever scroll happens, allowSheetDrag is recalculated.
            // Probably not the most efficient way, but does the job
            LaunchedEffect(key1 = listState.firstVisibleItemScrollOffset, key2 = listTouched) {
                if (listTouched) {
                    val isTop = listState.isTop
                    allowSheetDrag(isTop)
                    delay(100)
                }
                allowSheetDrag(true)
            }

            LogCompositions(tag = "XXX")

            Column(
                modifier = Modifier
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                // important!
                                listTouched = false
                                println("XXX\t >> Container ACTION_DOWN >> listTouched: ${listTouched}")
                            }
                        }
                        false
                    }
                    .padding(all = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Some Top Area which should always drag the Sheet.\nAlso dragging in the white padding area should be possible.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Green.copy(0.3f))
                        .fillMaxWidth()
                        .padding(20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))



                LazyColumn(
                    modifier = Modifier
                        .pointerInteropFilter(onTouchEvent = {
                            when (it.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    // important!
                                    listTouched = true

                                    val canScroll = listState.canScroll
                                    val isTop = listState.isTop
                                    println("XXX\t >> LazyColumn ACTION_DOWN >> listTouched: ${listTouched} >> canScroll: ${canScroll}")
                                    if (!isTop && canScroll) {
                                        // We tap the List, are not at the top, and can scroll.
                                        // So we forbid sheet drag.
                                        allowSheetDrag(false)
                                    }
                                }
                            }
                            false
                        })
                        .background(Color.Red.copy(0.3f))
                        .fillMaxWidth(),
                    state = listState
                ) {

                    stickyHeader {
                        Text(
                            "Sticky header", modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(16.dp)
                        )
                    }

                    itemsIndexed(items) { index, item ->
                        Text(
                            item, modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Divider()
                    }
                }


                Spacer(modifier = Modifier.weight(1f))
            }
        }

    }
}

//----------------------------------
//  LazyListState Helpers
//----------------------------------
val LazyListState.canScroll: Boolean
    get() {
        val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
        var itemsHeight = 0
        for (item in layoutInfo.visibleItemsInfo) {
            itemsHeight += item.size
            if (itemsHeight > viewportHeight) {
                return true
            }
        }
        return false
    }

val LazyListState.isTop: Boolean
    get() = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0