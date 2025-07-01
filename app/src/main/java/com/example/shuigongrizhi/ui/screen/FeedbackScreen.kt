package com.example.shuigongrizhi.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val authorEmail = "yb89725@hotmail.com"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF231942))
    ) {
        // 顶部栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color(0xFF8D6EFF)
                )
            }
            Text(
                text = "反馈",
                color = Color(0xFF8D6EFF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 反馈内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "邮箱",
                tint = Color(0xFF8D6EFF),
                modifier = Modifier.size(80.dp)
            )
            
            Text(
                text = "有任何问题或建议，请联系开发者",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = authorEmail,
                color = Color(0xFF8D6EFF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(authorEmail))
                        putExtra(Intent.EXTRA_SUBJECT, "淮工集团施工日志系统反馈")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "发送邮件",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
        
        // 添加底部空间，确保内容不被底部导航栏遮挡
        Spacer(modifier = Modifier.weight(1f))
    }
}