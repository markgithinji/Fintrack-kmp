package com.fintrack.shared.feature.transaction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                "Fintrack",
                style = MaterialTheme.typography.titleMedium // smaller than default
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* TODO: Open menu */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Notifications */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = { /* TODO: Settings */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    )
}

data class BottomNavItem(val title: String, val icon: ImageVector)

@Composable
fun BottomBar() {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Stats", Icons.Default.BarChart),
        BottomNavItem("Budget", Icons.Default.Info),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    var selectedItem by remember { mutableStateOf(0) }

    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEachIndexed { index, item ->
                // Leave space in the middle for FAB
                if (index == 2) {
                    Spacer(modifier = Modifier.width(72.dp))
                }

                IconButton(
                    onClick = { selectedItem = index },
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selectedItem == index) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}