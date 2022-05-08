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
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.projects.doggo.models.ModelCategory
import ie.projects.doggo.adapters.AdapterCategory
import ie.projects.doggo.databinding.ActivityRegisteredUserBinding
import java.lang.Exception

class RegisteredUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisteredUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //both are weather
    lateinit var mfusedlocation: FusedLocationProviderClient
    private var myResquestCode=1010

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityRegisteredUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase authen
        firebaseAuth = FirebaseAuth.getInstance()
        checkForUserInDatabase()
        getCategoriesFromDatabase()

        //search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user types anything
                try{
                    adapterCategory.filter.filter(s)
                }
                catch (e:Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        //handle click start add category page
        binding.addCategoryButton.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        //starting add pdf page
        binding.addPdfButton.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }

        //handle logout click
        binding.logoutButton.setOnClickListener{
            firebaseAuth.signOut()
            checkForUserInDatabase()

        }

        binding.weatherBtn.setOnClickListener(){
            getLastLocation()

        }

        binding.mapBtn.setOnClickListener {
            startActivity(Intent(this, GoogleMapsActivity::class.java))
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



    private fun getCategoriesFromDatabase() {
        //init arraylist
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for(ds in snapshot.children)
                {
                    //get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to arrayList
                    categoryArrayList.add(model!!)
                }

                //setup adapter
                adapterCategory = AdapterCategory(this@RegisteredUserActivity, categoryArrayList)

                //set adapter to recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }

    private fun checkForUserInDatabase() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            val email = firebaseUser.email

            //show user email
            binding.subTitleTv.text = email


        }
    }
}