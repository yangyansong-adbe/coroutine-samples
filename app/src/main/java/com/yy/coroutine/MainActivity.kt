package com.yy.coroutine

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.UserProfile
import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.optimize.Optimize
import com.yy.coroutine.ui.theme.CoroutineSampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutineSampleTheme {
                Surface(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(70.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Init()
                        SendEvent("Android")
                    }
                }
            }
        }
    }
}

@Composable
fun SendEvent(name: String, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {

        coroutineScope.launch(Dispatchers.IO) {
            val counter = AtomicInteger(0)
            val requestNumber = 10
            val startTime = System.currentTimeMillis()
            (1..requestNumber).forEach { _ ->
                val xdmData = mapOf(
                    "eventType" to "SampleXDMEvent",
                    "sample" to "data"
                )
                val event: ExperienceEvent =
                    ExperienceEvent.Builder().setXdmSchema(xdmData).build()
                Edge.sendEvent(event) { _ ->
                    if (counter.incrementAndGet() == requestNumber) {
                        val endTime = System.currentTimeMillis()
                        Log.e("--------------------", "total time: ${endTime - startTime}")
                    }
                }
            }
        }

    }) {
        Text(text = "sendEvent")
    }
}

@Composable
fun Init() {
    Button(onClick = {
        MobileCore.configureWithAppID("94f571f308d5/719a9846c6c5/launch-2db90676e962-development")
        val extensions = listOf(
            Edge.EXTENSION,
            Identity.EXTENSION,
            Consent.EXTENSION,
            Lifecycle.EXTENSION,
            UserProfile.EXTENSION,
            Assurance.EXTENSION,
            Optimize.EXTENSION,
            Messaging.EXTENSION
        )
        MobileCore.registerExtensions(extensions) {
            val collectConsents = mutableMapOf<String, Any>()
            collectConsents["collect"] = mutableMapOf("val" to "y")

            val consents = mutableMapOf<String, Any>()
            consents["consents"] = collectConsents

            Consent.update(consents)

        }
    }) {
        Text(text = "Initialize SDK")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CoroutineSampleTheme {
        Init()
        SendEvent("Android")
    }
}