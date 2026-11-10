
package com.maziwasmart.app.ui.settings

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.ui.FarmViewModel
import com.maziwasmart.app.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: FarmViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val profile by vm.profile.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    var feedUnit by remember { mutableStateOf("kg") }
    var missingRecordReminders by remember { mutableStateOf(true) }
    var milkingReminders by remember { mutableStateOf(true) }

    val green = Color(0xFF285C45)
    val greenSoft = Color(0xFFEAF3ED)
    val cream = Color(0xFFF8F7F1)
    val brown = Color(0xFF594B3C)
    val muted = Color(0xFF858585)
    val orange = Color(0xFFB86B36)

    val initials = profile?.name
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?.ifBlank { "F" }
        ?: "F"

    Scaffold(
        containerColor = cream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontWeight = FontWeight.Bold,
                        color = green
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = green
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = green
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .background(
                                    color = Color(0xFFF3D98B),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = green,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = profile?.name
                                ?.ifBlank { "Farmer profile" }
                                ?: "Farmer profile",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = profile?.farmName
                                ?.ifBlank { "Add your farm name" }
                                ?: "Add your farm name",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                message = null
                                showEditDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit profile")
                        }
                    }
                }
            }

            item {
                if (message != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (messageIsError) {
                            Color(0xFFFFE8E5)
                        } else {
                            greenSoft
                        }
                    ) {
                        Text(
                            text = message!!,
                            modifier = Modifier.padding(12.dp),
                            color = if (messageIsError) {
                                Color(0xFFB3261E)
                            } else {
                                green
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                SectionHeading("PERSONAL INFORMATION")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Full name",
                            value = profile?.name?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )

                        Divider(color = Color(0xFFEEEEEE))

                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone number",
                            value = profile?.phone?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )

                        Divider(color = Color(0xFFEEEEEE))

                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email address",
                            value = profile?.email?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )
                    }
                }
            }

            item {
                SectionHeading("FARM INFORMATION")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ProfileInfoRow(
                            icon = Icons.Default.Home,
                            label = "Farm name",
                            value = profile?.farmName?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )

                        Divider(color = Color(0xFFEEEEEE))

                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Farm location",
                            value = profile?.location?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )

                        Divider(color = Color(0xFFEEEEEE))

                        ProfileInfoRow(
                            icon = Icons.Default.Groups,
                            label = "Herd size",
                            value = profile?.herdSize?.ifBlank { "Not provided" }
                                ?: "Not provided",
                            green = green
                        )
                    }
                }
            }

            item {
                SectionHeading("PREFERENCES")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Feed measurement unit",
                                    color = brown,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Unit used for feeding records",
                                    color = muted,
                                    fontSize = 12.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                UnitChoice(
                                    label = "kg",
                                    selected = feedUnit == "kg",
                                    green = green
                                ) {
                                    feedUnit = "kg"
                                }

                                UnitChoice(
                                    label = "g",
                                    selected = feedUnit == "g",
                                    green = green
                                ) {
                                    feedUnit = "g"
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        PreferenceSwitch(
                            title = "Missing record reminders",
                            subtitle = "Remind me when records are missing",
                            checked = missingRecordReminders,
                            green = green,
                            onCheckedChange = {
                                missingRecordReminders = it
                            }
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        PreferenceSwitch(
                            title = "Milking reminders",
                            subtitle = "Reminders for daily milking",
                            checked = milkingReminders,
                            green = green,
                            onCheckedChange = {
                                milkingReminders = it
                            }
                        )
                    }
                }
            }

            item {
                SectionHeading("ACCOUNT & SUPPORT")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SupportRow(
                            icon = Icons.Default.Lock,
                            title = "Password & security",
                            subtitle = "Reset your password from the login screen",
                            green = green
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        SupportRow(
                            icon = Icons.Default.Info,
                            title = "Privacy",
                            subtitle = "Your farm data is stored under your account",
                            green = green
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFEEEEEE)
                        )

                        SupportRow(
                            icon = Icons.Default.Notifications,
                            title = "Help & support",
                            subtitle = "MaziwaSmart support information",
                            green = green
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val activity = context as? Activity

                        if (activity != null) {
                            vm.logout(activity) {
                                onLoggedOut()
                            }
                        } else {
                            message = "Could not access the current activity."
                            messageIsError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = orange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log out",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "MaziwaSmart  •  Version 1.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 24.dp),
                    color = muted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile ?: UserProfile(),
            saving = saving,
            onDismiss = {
                if (!saving) showEditDialog = false
            },
            onSave = { updatedProfile ->
                saving = true

                vm.updateProfile(updatedProfile) { error ->
                    saving = false

                    if (error == null) {
                        message = "Your profile was updated successfully."
                        messageIsError = false
                        showEditDialog = false
                    } else {
                        message = "Could not update profile: $error"
                        messageIsError = true
                    }
                }
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var farmName by remember(profile) { mutableStateOf(profile.farmName) }
    var location by remember(profile) { mutableStateOf(profile.location) }
    var herdSize by remember(profile) { mutableStateOf(profile.herdSize) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit your profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update your personal and farm information.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = profile.email,
                    onValueChange = {},
                    label = { Text("Email address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true
                )

                OutlinedTextField(
                    value = farmName,
                    onValueChange = { farmName = it },
                    label = { Text("Farm name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Farm location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = herdSize,
                    onValueChange = { herdSize = it },
                    label = { Text("Number of cows") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Please enter your full name."
                    } else {
                        validationError = null

                        onSave(
                            UserProfile(
                                name = name.trim(),
                                phone = phone.trim(),
                                email = profile.email,
                                farmName = farmName.trim(),
                                location = location.trim(),
                                herdSize = herdSize.trim()
                            )
                        )
                    }
                },
                enabled = !saving
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Save changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !saving
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    green: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xFFEAF3ED),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = green,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                text = value,
                color = Color(0xFF594B3C),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        color = Color(0xFF285C45),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun UnitChoice(
    label: String,
    selected: Boolean,
    green: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) green else Color(0xFFF1F1F1)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 8.dp
            ),
            color = if (selected) Color.White else Color.DarkGray,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    green: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF594B3C),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SupportRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    green: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = green,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = Color(0xFF594B3C),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
