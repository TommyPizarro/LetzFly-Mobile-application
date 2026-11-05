package com.gamecodeschool.letzfly.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecodeschool.letzfly.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OpeningScreen(
    onNavigateToLogin: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "LetzFly",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.4f
                    )
            ),
            shape = RoundedCornerShape(24.dp)
        ) {

            RealisticGlobeFlightAnimation()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Explore The World",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {

                Text(
                    text = "Let's Fly!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}


@Composable
fun RealisticGlobeFlightAnimation() {

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "realistic_globe_animation"
        )

    val flightProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 9000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flight_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {

        // ---------------------------------------------------------
        // 1. REALISTIC GLOBE VECTOR
        // ---------------------------------------------------------

        Image(
            painter = painterResource(
                id = R.drawable.world_map
            ),
            contentDescription = "World globe",
            modifier = Modifier.size(215.dp),
            contentScale = ContentScale.Fit
        )

        // ---------------------------------------------------------
        // 2. AIRPLANE + TRAIL CANVAS
        // ---------------------------------------------------------

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val center = Offset(
                size.width / 2f,
                size.height / 2f
            )

            /*
             * This radius controls the relationship between the
             * globe and the airplane's orbit.
             *
             * The vector globe above is approximately 215dp.
             * This keeps the flight path positioned around it.
             */

            val radius =
                size.minDimension / 2.3f

            // -----------------------------------------------------
            // FLIGHT ORBIT
            // -----------------------------------------------------

            val orbitRadiusX =
                radius * 1.15f

            val orbitRadiusY =
                radius * 0.42f

            val fullRotation =
                2f * Math.PI.toFloat()

            fun pointOnOrbit(
                progress: Float
            ): Offset {

                val normalizedProgress =
                    ((progress % 1f) + 1f) % 1f

                val angle =
                    normalizedProgress * fullRotation

                return Offset(
                    x = center.x +
                            orbitRadiusX * cos(angle),

                    y = center.y +
                            orbitRadiusY * sin(angle)
                )
            }

            // -----------------------------------------------------
            // DOTTED TRAIL
            // -----------------------------------------------------

            val trailPath = Path()

            val trailLength = 0.25f

            val trailSegments = 70

            for (
            index in 0..trailSegments
            ) {

                val amountBehindPlane =
                    trailLength *
                            (
                                    1f -
                                            index /
                                            trailSegments.toFloat()
                                    )

                val trailProgress =
                    flightProgress -
                            amountBehindPlane

                val trailPoint =
                    pointOnOrbit(
                        trailProgress
                    )

                if (index == 0) {

                    trailPath.moveTo(
                        trailPoint.x,
                        trailPoint.y
                    )

                } else {

                    trailPath.lineTo(
                        trailPoint.x,
                        trailPoint.y
                    )
                }
            }

            drawPath(
                path = trailPath,
                color = Color.Black.copy(
                    alpha = 0.85f
                ),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect =
                        PathEffect.dashPathEffect(
                            intervals =
                                floatArrayOf(
                                    8f,
                                    8f
                                ),
                            phase = 0f
                        )
                )
            )

            // -----------------------------------------------------
            // AIRPLANE
            // -----------------------------------------------------

            val currentAngle =
                flightProgress *
                        fullRotation

            val planePosition =
                pointOnOrbit(
                    flightProgress
                )

            /*
             * Actual tangent of the elliptical orbit.
             * This keeps the nose pointed in the direction
             * the airplane is traveling.
             */

            val velocityX =
                -orbitRadiusX *
                        sin(currentAngle)

            val velocityY =
                orbitRadiusY *
                        cos(currentAngle)

            val directionDegrees =
                Math.toDegrees(
                    atan2(
                        velocityY.toDouble(),
                        velocityX.toDouble()
                    )
                ).toFloat()

            val depthScale =
                0.82f +
                        0.18f *
                        (
                                (sin(currentAngle) + 1f) /
                                        2f
                                )

            // -----------------------------------------------------
            // DRAW AIRPLANE LAST
            //
            // Drawing the airplane after the trail keeps the
            // airplane visually in front of the dotted path.
            // -----------------------------------------------------

            translate(
                left = planePosition.x,
                top = planePosition.y
            ) {

                rotate(
                    degrees =
                        directionDegrees + 90f,
                    pivot = Offset.Zero
                ) {

                    scale(
                        scaleX = depthScale,
                        scaleY = depthScale,
                        pivot = Offset.Zero
                    ) {

                        drawBlackPlaneIcon()
                    }
                }
            }
        }
    }
}


// -------------------------------------------------------------
// AIRPLANE ICON
// -------------------------------------------------------------

private fun DrawScope.drawBlackPlaneIcon() {

    val planePath =
        Path().apply {

            moveTo(
                0f,
                -22f
            )

            lineTo(
                3f,
                -5f
            )

            lineTo(
                14f,
                2f
            )

            lineTo(
                14f,
                7f
            )

            lineTo(
                3f,
                3f
            )

            lineTo(
                2f,
                18f
            )

            lineTo(
                7f,
                22f
            )

            lineTo(
                7f,
                25f
            )

            lineTo(
                0f,
                23f
            )

            lineTo(
                -7f,
                25f
            )

            lineTo(
                -7f,
                22f
            )

            lineTo(
                -2f,
                18f
            )

            lineTo(
                -3f,
                3f
            )

            lineTo(
                -14f,
                7f
            )

            lineTo(
                -14f,
                2f
            )

            lineTo(
                -3f,
                -5f
            )

            close()
        }

    drawPath(
        path = planePath,
        color = Color.Black
    )
}