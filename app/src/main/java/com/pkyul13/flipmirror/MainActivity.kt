package com.pkyul13.flipmirror

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePreferences.getLanguage(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FlipMirrorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlipMirrorMainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ShizukuManager.checkStatus()
        DisplayResolutionManager.refreshState(this)
    }
}

@Composable
fun FlipMirrorTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = Color(0xFF38BDF8),       // Sky blue
        onPrimary = Color(0xFF0F172A),
        primaryContainer = Color(0xFF0369A1),
        onPrimaryContainer = Color(0xFFE0F2FE),
        secondary = Color(0xFF34D399),     // Mint green
        onSecondary = Color(0xFF064E3B),
        background = Color(0xFF0F172A),    // Deep Navy
        surface = Color(0xFF1E293B),       // Slate Dark
        surfaceVariant = Color(0xFF334155),
        onSurface = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFFCBD5E1),
        error = Color(0xFFF87171)
    )

    MaterialTheme(
        colorScheme = darkColors,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlipMirrorMainScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isShizukuAvailable by ShizukuManager.isShizukuAvailable.collectAsState()
    val hasPermission by ShizukuManager.hasPermission.collectAsState()
    val displayState by DisplayResolutionManager.displayState.collectAsState()

    var showShizukuHelp by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    var preferredDpi by remember { mutableIntStateOf(DisplayResolutionManager.getPreferredDpi(context)) }
    var currentLanguage by remember { mutableStateOf(LanguagePreferences.getLanguage(context)) }

    var isExternalConnected by remember { mutableStateOf(false) }
    var externalDisplayName by remember { mutableStateOf<String?>(null) }

    // Periodic refresh for display connection & state
    LaunchedEffect(Unit) {
        while (true) {
            val ext = DisplayHelper.getExternalDisplay(context)
            isExternalConnected = ext != null
            externalDisplayName = ext?.name
            DisplayResolutionManager.refreshState(context)
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher Button
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.lang_dialog_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh Button
                    IconButton(onClick = {
                        ShizukuManager.checkStatus()
                        DisplayResolutionManager.refreshState(context)
                        Toast.makeText(context, context.getString(R.string.toast_status_refreshed), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.toast_status_refreshed),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Shizuku Status Banner
            ShizukuStatusCard(
                isAvailable = isShizukuAvailable,
                hasPermission = hasPermission,
                onRequestPermission = { ShizukuManager.requestPermission() },
                onShowHelp = { showShizukuHelp = true }
            )

            // 2. External Monitor Connection Status Card
            ExternalMonitorCard(
                isConnected = isExternalConnected,
                monitorName = externalDisplayName
            )

            // 3. Current Display State & Visual Ratio Card
            CurrentDisplayStateCard(displayState = displayState)

            // 4. DPI Settings Card (100 ~ 400, default 240)
            DpiSettingsCard(
                currentDpi = preferredDpi,
                isAppliedOnDevice = displayState.overrideDensity == preferredDpi,
                enabled = hasPermission,
                onDpiChange = { newDpi ->
                    preferredDpi = newDpi
                    DisplayResolutionManager.setPreferredDpi(context, newDpi)
                },
                onApplyNow = { dpiToApply ->
                    DisplayResolutionManager.applyDensityOnly(context, dpiToApply) {
                        Toast.makeText(context, context.getString(R.string.toast_dpi_applied, dpiToApply), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // 5. One-Tap Quick Presets
            Text(
                text = stringResource(R.string.presets_title),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            PresetButton(
                title = stringResource(R.string.preset_portrait_title),
                description = stringResource(R.string.preset_portrait_desc, preferredDpi),
                icon = Icons.Default.AspectRatio,
                isActive = displayState.overrideWidth == 1080 && displayState.overrideHeight == 1920,
                enabled = hasPermission,
                onClick = {
                    DisplayResolutionManager.apply16x9Portrait(context, 1080, 1920, preferredDpi) {
                        Toast.makeText(context, context.getString(R.string.toast_portrait_applied, preferredDpi), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            PresetButton(
                title = stringResource(R.string.preset_landscape_title),
                description = stringResource(R.string.preset_landscape_desc, preferredDpi),
                icon = Icons.Default.StayCurrentLandscape,
                isActive = displayState.overrideWidth == 1920 && displayState.overrideHeight == 1080,
                enabled = hasPermission,
                onClick = {
                    DisplayResolutionManager.apply16x9Landscape(context, 1920, 1080, preferredDpi) {
                        Toast.makeText(context, context.getString(R.string.toast_landscape_applied, preferredDpi), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            PresetButton(
                title = stringResource(R.string.preset_qhd_title),
                description = stringResource(R.string.preset_qhd_desc, preferredDpi),
                icon = Icons.Default.HighQuality,
                isActive = displayState.overrideWidth == 1440 && displayState.overrideHeight == 2560,
                enabled = hasPermission,
                onClick = {
                    DisplayResolutionManager.apply16x9Portrait(context, 1440, 2560, preferredDpi) {
                        Toast.makeText(context, context.getString(R.string.toast_qhd_applied, preferredDpi), Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Reset Button
            Button(
                onClick = {
                    DisplayResolutionManager.resetToDefault(context) {
                        Toast.makeText(context, context.getString(R.string.toast_restored), Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = hasPermission && displayState.isOverrideApplied,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_restore),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // 6. Custom Resolution Action Button
            OutlinedButton(
                onClick = { showCustomDialog = true },
                enabled = hasPermission,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.btn_custom_res))
            }

            // 7. Quick Settings Tile Guide Card
            QuickSettingsTileGuideCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { selectedLang ->
                showLanguageDialog = false
                if (selectedLang != currentLanguage) {
                    LanguagePreferences.setLanguage(context, selectedLang)
                    currentLanguage = selectedLang
                    (context as? Activity)?.recreate()
                }
            }
        )
    }

    // Help Dialog for Shizuku
    if (showShizukuHelp) {
        ShizukuHelpDialog(onDismiss = { showShizukuHelp = false })
    }

    // Custom Resolution Dialog
    if (showCustomDialog) {
        CustomResolutionDialog(
            currentState = displayState,
            initialDpi = preferredDpi,
            onDismiss = { showCustomDialog = false },
            onApply = { w, h, dpi ->
                preferredDpi = dpi
                DisplayResolutionManager.applyCustom(context, w, h, dpi) {
                    Toast.makeText(context, context.getString(R.string.toast_custom_applied, w, h, dpi), Toast.LENGTH_SHORT).show()
                }
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val languages = listOf(
        LanguagePreferences.LANG_EN to stringResource(R.string.lang_en),
        LanguagePreferences.LANG_KO to stringResource(R.string.lang_ko)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.lang_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                languages.forEach { (code, label) ->
                    val isSelected = currentLanguage == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelectLanguage(code) }
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectLanguage(code) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun ShizukuStatusCard(
    isAvailable: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onShowHelp: () -> Unit
) {
    val (bgColor, borderColor, icon, title, subtitle) = when {
        isAvailable && hasPermission -> Tuple5(
            Color(0xFF064E3B).copy(alpha = 0.5f),
            Color(0xFF10B981),
            Icons.Default.CheckCircle,
            stringResource(R.string.shizuku_connected),
            stringResource(R.string.shizuku_connected_desc)
        )
        isAvailable && !hasPermission -> Tuple5(
            Color(0xFF78350F).copy(alpha = 0.5f),
            Color(0xFFF59E0B),
            Icons.Default.Warning,
            stringResource(R.string.shizuku_perm_required),
            stringResource(R.string.shizuku_perm_desc)
        )
        else -> Tuple5(
            Color(0xFF7F1D1D).copy(alpha = 0.5f),
            Color(0xFFEF4444),
            Icons.Default.Error,
            stringResource(R.string.shizuku_not_running),
            stringResource(R.string.shizuku_not_running_desc)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable {
                if (isAvailable && !hasPermission) {
                    onRequestPermission()
                } else if (!isAvailable) {
                    onShowHelp()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
            if (isAvailable && !hasPermission) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = borderColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(R.string.btn_grant), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (!isAvailable) {
                OutlinedButton(
                    onClick = onShowHelp,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(R.string.btn_help), color = borderColor, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ExternalMonitorCard(isConnected: Boolean, monitorName: String?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFF1E3A8A).copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isConnected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF10B981) else Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isConnected) stringResource(R.string.monitor_connected) else stringResource(R.string.monitor_waiting),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isConnected) (monitorName ?: stringResource(R.string.monitor_connected_desc)) else stringResource(R.string.monitor_waiting_desc),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CurrentDisplayStateCard(displayState: DisplayState) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.current_display_info),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (displayState.isOverrideApplied) Color(0xFF0284C7) else Color(0xFF475569)
                ) {
                    Text(
                        text = if (displayState.isOverrideApplied) stringResource(R.string.badge_16_9) else stringResource(R.string.badge_native),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(
                    label = stringResource(R.string.label_applied_res),
                    value = "${displayState.currentWidth} × ${displayState.currentHeight}",
                    highlight = displayState.isOverrideApplied
                )
                InfoColumn(
                    label = stringResource(R.string.label_density),
                    value = "${displayState.currentDensity} DPI",
                    highlight = displayState.overrideDensity != null
                )
                InfoColumn(
                    label = stringResource(R.string.label_native_res),
                    value = "${displayState.physicalWidth} × ${displayState.physicalHeight}",
                    highlight = false
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aspect Ratio Illustration Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 22:9 Representation
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(40.dp)
                                .border(1.5.dp, if (!displayState.isOverrideApplied) Color(0xFF38BDF8) else Color(0xFF64748B), RoundedCornerShape(3.dp))
                                .background(if (!displayState.isOverrideApplied) Color(0xFF0369A1).copy(alpha = 0.3f) else Color.Transparent)
                        )
                        Text(stringResource(R.string.preview_native), fontSize = 9.sp, color = Color(0xFF94A3B8))
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )

                    // 16:9 Representation
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(40.dp)
                                .border(1.5.dp, if (displayState.isOverrideApplied) Color(0xFF10B981) else Color(0xFF64748B), RoundedCornerShape(3.dp))
                                .background(if (displayState.isOverrideApplied) Color(0xFF059669).copy(alpha = 0.3f) else Color.Transparent)
                        )
                        Text(
                            stringResource(R.string.preview_fit),
                            fontSize = 9.sp,
                            color = if (displayState.isOverrideApplied) Color(0xFF34D399) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, highlight: Boolean) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DpiSettingsCard(
    currentDpi: Int,
    isAppliedOnDevice: Boolean,
    enabled: Boolean,
    onDpiChange: (Int) -> Unit,
    onApplyNow: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dpi_card_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$currentDpi DPI" + if (currentDpi == 240) stringResource(R.string.dpi_default_tag) else "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider & Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDpiChange((currentDpi - 10).coerceIn(100, 400)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-10", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Slider(
                    value = currentDpi.toFloat(),
                    onValueChange = { onDpiChange(it.toInt()) },
                    valueRange = 100f..400f,
                    steps = 29,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onDpiChange((currentDpi + 10).coerceIn(100, 400)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "+10", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Quick Preset Chips
            val chipValues = listOf(120, 160, 200, 240, 280, 320, 360, 400)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chipValues.forEach { chipDpi ->
                    val isSelected = currentDpi == chipDpi
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDpiChange(chipDpi) }
                    ) {
                        Text(
                            text = if (chipDpi == 240) "240★" else "$chipDpi",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onApplyNow(currentDpi) },
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_apply_dpi_now, currentDpi),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PresetButton(
    title: String,
    description: String,
    icon: ImageVector,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun QuickSettingsTileGuideCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.tile_guide_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tile_guide_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ShizukuHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.shizuku_help_title), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.shizuku_help_1), fontSize = 13.sp)
                Text(stringResource(R.string.shizuku_help_2), fontSize = 12.sp)
                Text(stringResource(R.string.shizuku_help_3), fontSize = 12.sp)
                Text(stringResource(R.string.shizuku_help_4), fontSize = 12.sp)
                Text(stringResource(R.string.shizuku_help_5), fontSize = 12.sp)
                Text(stringResource(R.string.shizuku_help_6), fontSize = 12.sp)
                Text(stringResource(R.string.shizuku_help_note), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_ok))
            }
        }
    )
}

@Composable
fun CustomResolutionDialog(
    currentState: DisplayState,
    initialDpi: Int,
    onDismiss: () -> Unit,
    onApply: (width: Int, height: Int, density: Int) -> Unit
) {
    var widthText by remember { mutableStateOf(currentState.currentWidth.toString()) }
    var heightText by remember { mutableStateOf(currentState.currentHeight.toString()) }
    var densityValue by remember { mutableFloatStateOf(initialDpi.toFloat().coerceIn(100f, 400f)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_custom_title), fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = it },
                    label = { Text(stringResource(R.string.label_width)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text(stringResource(R.string.label_height)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "${stringResource(R.string.label_density)}: ${densityValue.toInt()}" + if (densityValue.toInt() == 240) stringResource(R.string.dpi_default_tag) else "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = densityValue,
                        onValueChange = { densityValue = it },
                        valueRange = 100f..400f,
                        steps = 29
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val w = widthText.toIntOrNull() ?: 1080
                val h = heightText.toIntOrNull() ?: 1920
                val d = densityValue.toInt()
                onApply(w, h, d)
            }) {
                Text(stringResource(R.string.btn_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
