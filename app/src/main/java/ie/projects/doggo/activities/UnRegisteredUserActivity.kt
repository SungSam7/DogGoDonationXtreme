package ie.projects.doggo.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import ie.projects.doggo.databinding.ActivityUnregisteredUserBinding


class UnRegisteredUserActivity : AppCompatActivity() {



    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var binding: ActivityUnregisteredUserBinding

    lateinit var mfusedlocation: FusedLocationProviderClient
    private var myResquestCode=1010


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnregisteredUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUserInDataBase()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut() // firebase function
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }


        binding.weatherBtn.setOnClickListener(){
            getLastLocation()
        }

        binding.mapBtn.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
        }

        binding.stepCounter.setOnClickListener{
            startActivity(Intent(this, StepCounterActivity::class.java))
        }
    }

    private fun checkUserInDataBase() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {

            binding.subTitleTv.text = "No Account Login"
        } else {
            val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if(CheckPermission()) {
            if(LocationEnable()){
                mfusedlocation.lastLocation.addOnCompleteListener{
                        task->
                    var location: Location?=task.result
                    if(location==null)
                    {
                        NewLocation()
                    }else{
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent= Intent(this, WeatherActivity::class.java)
                            intent.putExtra("lat",location.latitude.toString())
                            intent.putExtra("long",location.longitude.toString())
                            startActivity(intent)
//                            finish()
                        },0)
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        }else{
            RequestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun NewLocation() {
        var locationRequest= LocationRequest()
        locationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=0
        locationRequest.fastestInterval=0
        locationRequest.numUpdates=1
        mfusedlocation= LocationServices.getFusedLocationProviderClient(this)

        //TODO DONT KNOW WHYLOOPER WANTS !!
        mfusedlocation.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper()!!)
    }
    private val locationCallback=object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation: Location =p0.lastLocation
        }
    }

    private fun LocationEnable(): Boolean {
        var locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun RequestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION),myResquestCode)
    }

    private fun CheckPermission(): Boolean {
        if(
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==myResquestCode)
        {
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                getLastLocation()
            }
        }
    }





}