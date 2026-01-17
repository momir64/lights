package rs.moma.lights.ui.screens

import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import rs.moma.lights.ui.components.LightGroupCard
import rs.moma.lights.ui.dialogs.LightGroupDialog
import rs.moma.lights.viewmodels.MainViewModel
import rs.moma.lights.ui.dialogs.ConfirmDialog
import androidx.compose.foundation.lazy.items
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.layout.*
import rs.moma.lights.data.models.Group
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun LightsScreen() {
    val vm: MainViewModel = viewModel()
    val config by vm.config.collectAsState()
    val refreshing by vm.isRefreshing.collectAsState()
    val deleteGroupId = remember { mutableIntStateOf(-1) }
    val dialogGroup = remember { mutableStateOf<Group?>(null) }

    if (dialogGroup.value != null)
        LightGroupDialog(dialogGroup)

    if (deleteGroupId.intValue > -1) ConfirmDialog({ deleteGroupId.intValue = -1 }) {
        vm.delete(deleteGroupId.intValue)
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        vm.moveGroup(from.index, to.index)
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { vm.refresh() },
        modifier = Modifier.fillMaxSize(),
        state = rememberPullToRefreshState(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(config.groups, key = { it.id }) { group ->
                ReorderableItem(reorderableState, key = group.id) {
                    LightGroupCard(
                        group = group,
                        modifier = Modifier.longPressDraggableHandle(),
                        deleteGroupId = deleteGroupId,
                        dialogGroup = dialogGroup,
                        vm = vm
                    )
                }
            }
        }
    }
}