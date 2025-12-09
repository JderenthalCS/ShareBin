package com.example.csc371_sharebin.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.csc371_sharebin.R
import com.example.csc371_sharebin.data.UserSession

/**
 * Main entry landing page for the app. Shows background art and transitions
 * into login or registration overlays. If a saved user exists, the button
 * displays “CONTINUE” instead of “GET STARTED”.
 *
 */

@Composable
fun LandingScreen(
    onContinueToApp: (String?) -> Unit
) {
    val context = LocalContext.current


    var savedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        savedName = UserSession.getUserName(context)
    }

    var showLogin by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with logo / title / text
        Image(
            painter = painterResource(id = R.drawable.a_base2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Get Started / Continue button at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            GradientPillButton(
                text = if (savedName != null) "CONTINUE" else "GET STARTED",
                modifier = Modifier.width(260.dp)
            ) {

                showLogin = true
            }
        }

        if (showLogin) {
            LoginOverlayUI(
                context = context,
                savedName = savedName,
                onDismiss = { showLogin = false },
                onLoginSuccess = { loggedInName ->

                    val finalName = loggedInName.ifBlank { null }
                    savedName = finalName
                    showLogin = false
                    onContinueToApp(finalName)
                },
                onSignUpClick = {
                    showLogin = false
                    showRegister = true
                }
            )
        }

        if (showRegister) {
            RegisterOverlayUI(
                onDismiss = { showRegister = false },
                onRegister = { first, last, dob, email, pass ->

                    val displayName = (first + " " + last).trim()
                        .ifBlank { first.ifBlank { email } }
                    val emailOrNull = email.ifBlank { null }

                    if (!displayName.isNullOrBlank()) {
                        // now also saving password
                        UserSession.saveUser(context, displayName, emailOrNull, pass)
                        savedName = displayName
                    }

                    // after register, go back to login
                    showRegister = false
                    showLogin = true
                }
            )
        }
    }
}

/**
 * Full-screen login overlay that allows the user to enter an email/username
 * and password. Validates input against stored UserSession data.
 *
 */

@Composable
fun LoginOverlayUI(
    context: Context,
    savedName: String?,
    onDismiss: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Inner box consumes clicks so taps on the card don't dismiss
        Box(
            modifier = Modifier
                .clickable(
                    onClick = { /* consume */ },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            SignInCard(
                modifier = Modifier
            ) {
                PillTextField(
                    value = username,
                    placeholder = "Email",
                    onValueChange = { username = it }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PillTextField(
                    value = password,
                    placeholder = "Password",
                    onValueChange = { password = it },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                GradientPillButton(
                    text = "LOG IN",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (username.isBlank() || password.isBlank()) {
                        loginError = "Please enter both username and password."
                    } else {
                        val storedName = UserSession.getUserName(context)
                        val storedEmail = UserSession.getUserEmail(context)
                        val storedPassword = UserSession.getUserPassword(context)

                        if (storedName == null || storedPassword == null) {
                            loginError = "No account found. Please register first."
                        } else if ((username != storedName && username != storedEmail) ||
                            password != storedPassword
                        ) {
                            loginError = "Incorrect username/email or password."
                        } else {
                            loginError = null
                            onLoginSuccess(storedName)
                        }
                    }
                }

                if (loginError != null) {
                    Text(
                        text = loginError!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Need an account? Sign up",
                    color = Color.Gray.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        onSignUpClick()
                    }
                )
            }
        }
    }
}

/**
 * Reusable UI card that wraps sign-in fields and buttons in a rounded
 * white container.
 *
 */

@Composable
fun SignInCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .width(360.dp)
            .background(
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SIGN IN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

/**
 * Rounded pill-style text input used across login/register screens.
 * Supports optional password masking.
 *
 */

@Composable
fun PillTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFFF5F5F5),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword)
                KeyboardOptions(keyboardType = KeyboardType.Password)
            else
                KeyboardOptions.Default,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                inner()
            }
        )
    }
}


/**
 * Animated gradient pill-shaped button used throughout authentication screens.
 * Slightly scales when pressed for a softer touch effect.
 *
 */

@Composable
fun GradientPillButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "pillButtonScale"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFE91E63),
                        Color(0xFF9C27B0)
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Full-screen registration form overlay for creating a new ShareBin account.
 * Validates all fields including email format, DOB, and password confirmation.
 *
 */

@Composable
fun RegisterOverlayUI(
    onDismiss: () -> Unit,
    onRegister: (
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        password: String
    ) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .clickable(
                    onClick = { /* consume */ },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            RegisterCard {
                PillTextField(
                    value = firstName,
                    placeholder = "First name",
                    onValueChange = {
                        firstName = it
                        errorMessage = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                PillTextField(
                    value = lastName,
                    placeholder = "Last name",
                    onValueChange = {
                        lastName = it
                        errorMessage = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                PillTextField(
                    value = dob,
                    placeholder = "Date of birth (MM/DD/YYYY)",
                    onValueChange = {
                        dob = it
                        errorMessage = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                PillTextField(
                    value = email,
                    placeholder = "Email",
                    onValueChange = {
                        email = it
                        errorMessage = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                PillTextField(
                    value = password,
                    placeholder = "Password",
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                PillTextField(
                    value = confirmPassword,
                    placeholder = "Confirm password",
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(18.dp))

                GradientPillButton(
                    text = "CREATE ACCOUNT",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Validation
                    val dobRegex = Regex("""\d{2}/\d{2}/\d{4}""")
                    val emailRegex = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")

                    val error = when {
                        firstName.isBlank() -> "Please enter your first name."
                        lastName.isBlank() -> "Please enter your last name."
                        dob.isBlank() -> "Please enter your date of birth."
                        !dobRegex.matches(dob) ->
                            "Date of birth must be in MM/DD/YYYY format."
                        email.isBlank() -> "Please enter your email."
                        !emailRegex.matches(email) ->
                            "Please enter a valid email address."
                        password.isBlank() -> "Please enter a password."
                        password.length < 6 ->
                            "Password must be at least 6 characters."
                        confirmPassword.isBlank() ->
                            "Please confirm your password."
                        password != confirmPassword ->
                            "Passwords do not match."
                        else -> null
                    }

                    if (error != null) {
                        errorMessage = error
                    } else {
                        errorMessage = null
                        onRegister(firstName, lastName, dob, email, password)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


/**
 * Reusable rounded card container for the registration form fields.
 */

@Composable
fun RegisterCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .width(360.dp)
            .background(
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CREATE ACCOUNT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
