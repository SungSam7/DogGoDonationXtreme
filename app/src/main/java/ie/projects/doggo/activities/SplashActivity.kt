package ie.projects.doggo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import ie.projects.doggo.R

class SplashActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //TODO Change to login/Sign up
        // splash screen delay,
        Handler().postDelayed(Runnable {

            startActivity(Intent(this, MainActivity::class.java))
            //finish() stops you from pressing back into the splashscreen
            finish()

        },2000)
    }


 }

