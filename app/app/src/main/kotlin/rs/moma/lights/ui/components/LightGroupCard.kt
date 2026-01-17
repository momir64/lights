package rs.moma.lights.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.res.painterResource
import rs.moma.lights.viewmodels.MainViewModel
import androidx.compose.foundation.layout.Row
import rs.moma.lights.data.models.LightMode.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Composable
import rs.moma.lights.data.models.Group
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.moma.lights.ui.theme.*
import rs.moma.lights.R

@Composable
fun LightGroupCard(group: Group, modifier: Modifier, vm: MainViewModel, dialogGroup: MutableState<Group?>, deleteGroupId: MutableState<Int>) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.name)
                Row {
                    IconButton({ deleteGroupId.value = group.id }, Modifier.offset(x = 6.dp)) {
                        Icon(painterResource(R.drawable.ic_delete), "Delete light group", tint = OutlineColor)
                    }
                    IconButton({ dialogGroup.value = group }) {
                        Icon(painterResource(R.drawable.ic_edit), "Edit light group", tint = OutlineColor)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp)
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                ToggleButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp),
                    active = group.day.on == true,
                    icon = R.drawable.ic_day
                ) { if (group.day.on == true) vm.off(group.id, Day) else vm.on(group.id, Day) }

                Spacer(Modifier.width(8.dp))

                ToggleButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp),
                    active = group.night.on == true,
                    icon = R.drawable.ic_night
                ) { if (group.night.on == true) vm.off(group.id, Night) else vm.on(group.id, Night) }

                Slider(
                    group.night.brightness?.toFloat(),
                    modifier = Modifier.offset(x = (-3).dp)
                ) {
                    vm.setGroupBrightness(group.id, it.toInt())
                }
            }
        }
    }
}