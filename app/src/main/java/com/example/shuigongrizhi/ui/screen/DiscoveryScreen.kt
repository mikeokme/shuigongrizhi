package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shuigongrizhi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onDesktopClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFF8D6EFF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "发现",
                    color = Color(0xFF8D6EFF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color(0xFF231942)
    ) { paddingValues ->
        // 功能卡片区
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "香槟",
                    iconRes = R.drawable.ic_champagne,
                    gradient = Brush.linearGradient(listOf(Color(0xFF7F53AC), Color(0xFF647DEE))),
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "起泡酒",
                    iconRes = R.drawable.ic_sparkaling,
                    gradient = Brush.linearGradient(listOf(Color(0xFF56CCF2), Color(0xFF2F80ED))),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "红酒",
                    iconRes = R.drawable.ic_red_wine,
                    gradient = Brush.linearGradient(listOf(Color(0xFFCB2D3E), Color(0xFFEF473A))),
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "玫瑰酒",
                    iconRes = R.drawable.ic_rose_wine,
                    gradient = Brush.linearGradient(listOf(Color(0xFF8E54E9), Color(0xFF4776E6))),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "甜酒",
                    iconRes = R.drawable.ic_sweet_wine,
                    gradient = Brush.linearGradient(listOf(Color(0xFF43CEA2), Color(0xFF185A9D))),
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "干邑",
                    iconRes = R.drawable.ic_cognac,
                    gradient = Brush.linearGradient(listOf(Color(0xFFB06AB3), Color(0xFF4568DC))),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}