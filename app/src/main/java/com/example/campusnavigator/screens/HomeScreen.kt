package com.example.campusnavigator.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.campusnavigator.ui.theme.NavyPrimary
import com.example.campusnavigator.ui.theme.TextSecondary

data class HomeSectionItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

val homeSections = listOf<HomeSectionItem>(
    HomeSectionItem(
        route = "mainMap",
        title = "Карта кампуса",
        subtitle = "Навигация и режимы карты",
        icon = Icons.Default.LocationOn
    ),
    HomeSectionItem(
        route = "decisionTree",
        title = "Выбор места для обеда",
        subtitle = "Подбор заведения по параметрам",
        icon = Icons.Default.Restaurant
    ),
    HomeSectionItem(
        route = "neural",
        title = "Оценка",
        subtitle = "Распознавание цифр от 0 до 9",
        icon = Icons.Default.Star
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Навигатор ТГУ", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NavyPrimary
                )
            )
        }) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        "Томский государтсвенный университет",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Добро пожаловать",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Выберите раздел",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(4.dp)
                )

                homeSections.forEach { item ->
                    HomeCard(
                        item = item,
                        onClick = { navController.navigate(item.route) }
                    )
                }
            }
        }
    }
}


@Composable
fun HomeCard(item: HomeSectionItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = NavyPrimary, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = NavyPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
