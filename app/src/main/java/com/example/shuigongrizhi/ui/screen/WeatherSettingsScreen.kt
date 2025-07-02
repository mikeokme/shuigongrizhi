package com.example.shuigongrizhi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuigongrizhi.ui.theme.AppCardDefaults
import com.example.shuigongrizhi.ui.viewmodel.WeatherSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeatherSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var apiToken by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var showVerificationCode by remember { mutableStateOf(false) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var isVerificationVisible by remember { mutableStateOf(false) }
    
    // 初始化时加载当前Token
    LaunchedEffect(Unit) {
        apiToken = viewModel.getCurrentToken()
    }
    
    // 监听保存结果
    LaunchedEffect(uiState.saveResult) {
        uiState.saveResult?.let { result ->
            if (result.isSuccess) {
                onNavigateBack()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("天气设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = AppCardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "天气API设置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "您可以选择以下三种模式：\n" +
                                "1. 无API模式：不需要Token，使用模拟天气数据（推荐新用户）\n" +
                                "2. 自定义Token：配置您自己的彩云天气API Token\n" +
                                "3. 开发者Token：使用开发者提供的Token（需要验证码）",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 无API模式说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = AppCardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 无API模式",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "如果您不想配置API Token，可以直接使用无API模式。此模式下应用会生成模拟的天气数据，包括温度、湿度、风速等信息，让您可以正常体验应用的所有功能。要启用无API模式，只需将API Token留空即可。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // API Token输入
            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it },
                label = { Text("API Token") },
                placeholder = { Text("请输入您的彩云天气API Token") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isTokenVisible) "隐藏Token" else "显示Token"
                        )
                    }
                },
                supportingText = {
                    Text("获取Token: https://dashboard.caiyunapp.com/")
                }
            )
            
            // 使用开发者Token选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showVerificationCode,
                    onCheckedChange = { 
                        showVerificationCode = it
                        if (!it) {
                            verificationCode = ""
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "使用开发者提供的Token（需要验证码）",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // 验证码输入（条件显示）
            if (showVerificationCode) {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("验证码") },
                    placeholder = { Text("请输入验证码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isVerificationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isVerificationVisible = !isVerificationVisible }) {
                            Icon(
                                imageVector = if (isVerificationVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isVerificationVisible) "隐藏验证码" else "显示验证码"
                            )
                        }
                    },
                    supportingText = {
                        Text("请联系开发者获取验证码")
                    }
                )
            }
            
            // 当前状态显示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = AppCardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "当前状态",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("当前模式:")
                        Text(
                            text = when {
                                uiState.currentToken.isEmpty() -> "无API模式（使用模拟数据）"
                                uiState.currentToken.isNotEmpty() && uiState.isTokenVerified -> "API模式（已验证）"
                                uiState.currentToken.isNotEmpty() -> "API模式（已配置）"
                                else -> "未配置"
                            },
                            color = when {
                                uiState.currentToken.isEmpty() -> MaterialTheme.colorScheme.tertiary
                                uiState.currentToken.isNotEmpty() && uiState.isTokenVerified -> MaterialTheme.colorScheme.primary
                                uiState.currentToken.isNotEmpty() -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    
                    if (uiState.currentToken.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("当前Token:")
                            Text(
                                text = "${uiState.currentToken.take(8)}...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // 错误信息显示
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppCardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 保存按钮
                Button(
                    onClick = {
                        if (showVerificationCode) {
                            viewModel.saveTokenWithVerification(verificationCode)
                        } else {
                            viewModel.saveCustomToken(apiToken)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading && (
                        (showVerificationCode && verificationCode.isNotEmpty()) ||
                        !showVerificationCode // 允许保存空token以启用无API模式
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (apiToken.isEmpty() && !showVerificationCode) "启用无API模式" else "保存")
                    }
                }
                
                // 测试按钮
                OutlinedButton(
                    onClick = {
                        val tokenToTest = if (showVerificationCode && verificationCode == "nzdwssm") {
                            "6MzHC6Wp0Fs5DhAz"
                        } else {
                            apiToken
                        }
                        viewModel.testToken(tokenToTest)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading && (
                        (showVerificationCode && verificationCode.isNotEmpty()) ||
                        !showVerificationCode // 允许测试空token以验证无API模式
                    )
                ) {
                    Text(if (apiToken.isEmpty() && !showVerificationCode) "测试无API模式" else "测试")
                }
            }
            
            // 重置按钮
            OutlinedButton(
                onClick = { viewModel.resetToken() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("切换到无API模式")
            }
        }
    }
}