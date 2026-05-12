package ir.moha.persiantodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceId
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.*
import ir.moha.persiantodo.MainActivity

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = Color(0xFF6650A4), night = Color(0xFF4A3780)))
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📋 یادآور فارسی",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "برای مشاهده وظایف لمس کنید",
                    style = TextStyle(
                        color = ColorProvider(
                            day   = Color.White.copy(alpha = 0.8f),
                            night = Color.White.copy(alpha = 0.8f)
                        ),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
