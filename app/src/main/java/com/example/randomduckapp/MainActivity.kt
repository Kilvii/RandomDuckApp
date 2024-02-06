package com.example.randomduckapp

import android.annotation.SuppressLint
import android.net.http.HttpException
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.randomduckapp.ui.theme.RandomDuckAppTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val response = try {
                    RetrofitInstance.api.getDuck()
                } catch (e: IOException) {
                    Log.e(TAG, "IOException | You migth not have internet connection")
                    return@repeatOnLifecycle
                } catch (@SuppressLint("NewApi", "LocalSuppress") e: HttpException) {
                    Log.e(TAG, "HttpException | You migth have unexpected response")
                    return@repeatOnLifecycle
                }
                if(response.isSuccessful && response.body() != null){
                    setContent{
                        ScreenWithDuck(response = response)
                    }
                }
                else{
                    Log.e(TAG, "Response not successful")
                }
            }
        }
    }
}

@Composable
fun ScreenWithDuck(
    response: Response<Duck>
) {
    var imageSource by remember {mutableStateOf(response.body()?.url)}
    val isGif = imageSource?.endsWith(".gif", ignoreCase = true)
    var isLoading by remember { mutableStateOf(false)}
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if(isGif == true){
                val context = LocalContext.current
                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        if (SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }
                    }
                    .build()

                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageSource,
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(400.dp)
                )
            }
            else{
                AsyncImage(
                    model = imageSource,
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(400.dp)
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        imageSource = RetrofitInstance.api.getDuck().body()?.url
                        isLoading = false
                    }
                }
            ) {
                Text(
                    text = "Get duck"
                )
            }
        }
    }
}