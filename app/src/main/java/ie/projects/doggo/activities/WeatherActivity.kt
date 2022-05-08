package ie.projects.doggo.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import ie.projects.doggo.R
import ie.projects.doggo.databinding.ActivityWeatherBinding
import org.json.JSONObject
import kotlin.math.ceil


class WeatherActivity : AppCompatActivity() {

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 0


    lateinit var binding : ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()





        val lat=intent.getStringExtra("lat")
        var long=intent.getStringExtra("long")




        //TODO CHANGE COLOR
        window.statusBarColor= Color.parseColor("#C8BBDF")
        getJsonData(lat,long)

        //handle click going back
        binding.backBtn.setOnClickListener{
            onBackPressed()



            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WARNING!")
                .setContentText("It is getting HOT!")
                .setSmallIcon(R.drawable.logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val notificationManager = NotificationManagerCompat.from(this)

            notificationManager.notify(NOTIFICATION_ID, notification)
        }



    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lightColor = Color.RED
                enableLights(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }





    private fun notificationsHigh()
    {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WARNING!")
            .setContentText("It is getting HOT!")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun notificationsLow()
    {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WARNING!")
            .setContentText("It is getting COLD!")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    private fun getJsonData(lat:String?,long:String?)
    {

        val API_KEY="d782c3641dcc9cbcb8528dfb0245b3e4"
        val queue = Volley.newRequestQueue(this)

        val url ="https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${long}&appid=${API_KEY}"
        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url,null,
            Response.Listener { response ->
                setValues(response)
            },
            Response.ErrorListener { Toast.makeText(this,"ERROR HERE",Toast.LENGTH_LONG).show() })


        queue.add(jsonRequest)
    }

    private fun setValues(response:JSONObject){
        binding.city.text=response.getString("name")
        var lat = response.getJSONObject("coord").getString("lat")
        var long=response.getJSONObject("coord").getString("lon")
        binding.coordinates.text="${lat} , ${long}"
        binding.weather.text=response.getJSONArray("weather").getJSONObject(0).getString("main")
        var tempr=response.getJSONObject("main").getString("temp")
        tempr=((((tempr).toFloat()-273.15)).toInt()).toString()
        binding.temp.text="${tempr}°C"
        var mintemp=response.getJSONObject("main").getString("temp_min")
        mintemp=((((mintemp).toFloat()-273.15)).toInt()).toString()
        binding.minTemp1.text=mintemp+"°C"
        var maxtemp=response.getJSONObject("main").getString("temp_max")
        maxtemp=((ceil((maxtemp).toFloat()-273.15)).toInt()).toString()
        binding.maxTemp1.text=maxtemp+"°C"

        binding.pressure.text=response.getJSONObject("main").getString("pressure")
        binding.humidity.text=response.getJSONObject("main").getString("humidity")+"%"
        binding.wind.text=response.getJSONObject("wind").getString("speed")
        binding.degree.text="Degree : "+response.getJSONObject("wind").getString("deg")+"°"



        val imageView = findViewById<ImageView>(R.id.iconIv)
        if(tempr.toFloat() >= 13){
            notificationsHigh()
            imageView.setImageResource(R.drawable.background)

        }


        else if(tempr.toFloat() <= 12){
            notificationsLow()

        }
        else if(tempr.toFloat() <= 9){
            notificationsLow()

        }
        else if(tempr.toFloat() <= 8){
            notificationsLow()

        }
        else if(tempr.toFloat() <= 7){
            notificationsLow()

        }
        else if(tempr.toFloat() <= 6){
            notificationsLow()

        }
        else if(tempr.toFloat() <= 5){
            notificationsLow()

        }

        if(binding.weather.text == "Rain")
        {
            imageView.setImageResource(R.drawable.rain)
        }
        else if(binding.weather.text == "Clouds"){
            imageView.setImageResource(R.drawable.clouds)
        }
        else if(binding.weather.text == "Clear"){
            imageView.setImageResource(R.drawable.sunny)
        }
    }
}