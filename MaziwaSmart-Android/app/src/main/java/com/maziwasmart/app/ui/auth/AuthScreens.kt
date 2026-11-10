
package com.maziwasmart.app.ui.auth

import androidx.compose.material3.ExperimentalMaterial3Api
import android.app.Activity
import androidx.compose.material3.OutlinedButton
import com.maziwasmart.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maziwasmart.app.data.UserProfile
import com.maziwasmart.app.ui.components.AppButton
import com.maziwasmart.app.ui.components.AppTextField
import com.maziwasmart.app.ui.components.ChipRow
import com.maziwasmart.app.ui.components.FieldLabel
import com.maziwasmart.app.ui.components.ScreenWithHeader
import com.maziwasmart.app.ui.components.toast
import com.maziwasmart.app.ui.theme.Brown
import com.maziwasmart.app.ui.theme.GreenDeep
import com.maziwasmart.app.ui.theme.Muted
import com.maziwasmart.app.ui.theme.Yellow

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(GreenDeep)
    ) {
        // Logo
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 36.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Yellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = Brown,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) {
                        append("Maziwa")
                    }
                    withStyle(SpanStyle(color = Yellow)) {
                        append("Smart")
                    }
                },
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Manage your dairy farm smarter.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }

        // Login form
        Column(
            Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    "Welcome back",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )

                Text(
                    "Log in to see your farm records",
                    fontSize = 12.sp,
                    color = Muted
                )
            }

            AppTextField(
                email,
                { email = it },
                "Email",
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Filled.Email
            )

            AppTextField(
                password,
                { password = it },
                "Password",
                isPassword = true,
                leadingIcon = Icons.Filled.Lock
            )

            Text(
                "Forgot password?",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenDeep,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        vm.resetPassword(email) {
                            context.toast(it)
                        }
                    }
            )

            vm.error?.let {
                Text(
                    it,
                    color = Color(0xFFC62828),
                    fontSize = 12.sp
                )
            }

            AppButton(
                "Log in",
                onClick = {
                    vm.login(email, password, onLoggedIn)
                },
                loading = vm.loading
            )

            OutlinedButton(
                onClick = {
                    val activity = context as? Activity

                    if (activity != null) {
                        val serverClientId = context.getString(
                            R.string.default_web_client_id
                        )

                        vm.signInWithGoogle(
                            activity = activity,
                            serverClientId = serverClientId,
                            onSuccess = onLoggedIn
                        )
                    } else {
                        context.toast("Unable to start Google Sign-In")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loading
            ) {
                Text(
                    "Continue with Google",
                    color = GreenDeep
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "New to MaziwaSmart? ",
                    fontSize = 12.sp,
                    color = Muted
                )

                Text(
                    "Create account",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenDeep,
                    modifier = Modifier.clickable {
                        vm.error = null
                        onRegister()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var farmName by rememberSaveable { mutableStateOf("") }
    var county by rememberSaveable { mutableStateOf("") }
    var subCounty by rememberSaveable { mutableStateOf("") }
    var herdSize by rememberSaveable { mutableStateOf("4-10") }

    var countyExpanded by rememberSaveable { mutableStateOf(false) }
    var subCountyExpanded by rememberSaveable { mutableStateOf(false) }

    val counties = listOf(
        "Nairobi",
        "Uasin Gishu",
        "Nakuru",
        "Kiambu",
        "Mombasa",
        "Kisumu",
        "Bungoma",
        "Trans Nzoia"
    )

    val subCounties = mapOf(
        "Uasin Gishu" to listOf(
            "Ainabkoi", "Kapseret", "Kesses",
            "Moiben", "Soy", "Turbo"
        ),
        "Nairobi" to listOf(
            "Westlands", "Dagoretti", "Embakasi",
            "Kasarani", "Langata"
        ),
        "Nakuru" to listOf(
            "Nakuru Town East", "Nakuru Town West",
            "Naivasha", "Gilgil"
        ),
        "Kiambu" to listOf(
            "Thika", "Ruiru", "Kiambu",
            "Limuru", "Kikuyu"
        ),
        "Mombasa" to listOf(
            "Changamwe", "Jomvu", "Kisauni",
            "Nyali", "Likoni"
        ),
        "Kisumu" to listOf(
            "Kisumu East", "Kisumu West",
            "Kisumu Central", "Seme"
        ),
        "Bungoma" to listOf(
            "Bungoma Central", "Kimilili",
            "Webuye East", "Webuye West"
        ),
        "Trans Nzoia" to listOf(
            "Kiminini", "Saboti", "Endebess",
            "Cherangany"
        )
    )

    ScreenWithHeader(
        title = "Create account",
        subtitle = "Tell us about you and your farm",
        onBack = {
            vm.error = null
            onBack()
        }
    ) {
        AppTextField(
            name,
            { name = it },
            "Full name",
            leadingIcon = Icons.Filled.Person
        )

        AppTextField(
            phone,
            { phone = it },
            "Phone number",
            keyboardType = KeyboardType.Phone,
            leadingIcon = Icons.Filled.Phone
        )

        AppTextField(
            email,
            { email = it },
            "Email",
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Filled.Email
        )

        AppTextField(
            password,
            { password = it },
            "Password (at least 6 characters)",
            isPassword = true,
            leadingIcon = Icons.Filled.Lock
        )

        AppTextField(
            confirmPassword,
            { confirmPassword = it },
            "Confirm Password",
            isPassword = true,
            leadingIcon = Icons.Filled.Lock
        )

        AppTextField(
            farmName,
            { farmName = it },
            "Farm name"
        )

        // County dropdown
        ExposedDropdownMenuBox(
            expanded = countyExpanded,
            onExpandedChange = {
                countyExpanded = !countyExpanded
            }
        ) {
            OutlinedTextField(
                value = county,
                onValueChange = {},
                readOnly = true,
                label = { Text("County") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = countyExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = countyExpanded,
                onDismissRequest = {
                    countyExpanded = false
                }
            ) {
                counties.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            county = item
                            subCounty = ""
                            countyExpanded = false
                        }
                    )
                }
            }
        }

        // Sub-county dropdown
        ExposedDropdownMenuBox(
            expanded = subCountyExpanded,
            onExpandedChange = {
                if (county.isNotBlank()) {
                    subCountyExpanded = !subCountyExpanded
                }
            }
        ) {
            OutlinedTextField(
                value = subCounty,
                onValueChange = {},
                readOnly = true,
                enabled = county.isNotBlank(),
                label = { Text("Sub-county") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = subCountyExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = subCountyExpanded,
                onDismissRequest = {
                    subCountyExpanded = false
                }
            ) {
                subCounties[county].orEmpty().forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            subCounty = item
                            subCountyExpanded = false
                        }
                    )
                }
            }
        }

        FieldLabel("Number of cows")

        ChipRow(
            listOf("1-3", "4-10", "10+"),
            herdSize
        ) {
            herdSize = it
        }

        vm.error?.let {
            Text(
                it,
                color = Color(0xFFC62828),
                fontSize = 12.sp
            )
        }

        AppButton(
            "Register",
            loading = vm.loading,
            onClick = {
                when {
                    name.isBlank() ->
                        vm.error = "Enter your full name"

                    phone.isBlank() ->
                        vm.error = "Enter your phone number"

                    email.isBlank() ->
                        vm.error = "Enter your email"

                    password.isBlank() || confirmPassword.isBlank() ->
                        vm.error = "Please fill in both password fields"

                    password.length < 6 ->
                        vm.error = "Password must be at least 6 characters"

                    password != confirmPassword ->
                        vm.error = "Passwords do not match"

                    farmName.isBlank() ->
                        vm.error = "Enter your farm name"

                    county.isBlank() ->
                        vm.error = "Select your county"

                    subCounty.isBlank() ->
                        vm.error = "Select your sub-county"

                    else -> {
                        vm.register(
                            UserProfile(
                                name = name.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                farmName = farmName.trim(),
                                location = "$county, $subCounty",
                                herdSize = herdSize
                            ),
                            password,
                            onRegistered
                        )
                    }
                }
            }
        )
    }
}
