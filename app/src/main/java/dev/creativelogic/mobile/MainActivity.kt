package dev.creativelogic.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.creativelogic.mobile.blueprint.BlueprintAccess
import dev.creativelogic.mobile.ui.CreativeLogicApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as CreativeLogicApplication).repository
        val displayPreferences = dev.creativelogic.mobile.ui.DisplayPreferences(this)
        setContent {
            Box(Modifier.fillMaxSize()) {
                CreativeLogicApp(repository, displayPreferences)
                BlueprintAccess(repository, Modifier.align(Alignment.BottomStart)
                    .navigationBarsPadding().padding(start = 12.dp, bottom = 84.dp))
            }
        }
    }
}
