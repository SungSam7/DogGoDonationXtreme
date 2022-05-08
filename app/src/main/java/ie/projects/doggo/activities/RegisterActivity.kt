package ie.projects.doggo.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ie.projects.doggo.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth


    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()


        //handle back button
        binding.backBtn.setOnClickListener{
            onBackPressed() //already a method
        }

        //handle click, begin the register phase
        binding.registBtn.setOnClickListener {

            //steps
            //1. input data
            //2. validate data
            //3. create account - firebase authentication
            //4. save user information - firebase realtime databases
            checkUserInput()
        }


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setCanceledOnTouchOutside(false)
    }
    private var name = ""
    private var email = ""
    private var password = ""

    private fun checkUserInput() {
        //1. input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        //2. validate data
        if(name.isEmpty()){
            //empty name
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show()
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email
            Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_LONG).show()
        }

        else if(password.isEmpty()){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show()
        }

        else if(cPassword.isEmpty()){
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_LONG).show()
        }

        else if (password != cPassword){
            Toast.makeText(this, "Passwords don't match, please try again", Toast.LENGTH_LONG).show()
        }
        else{
            createUser()
        }
    }

    private fun createUser() {
        //3.Create account
        //show progress

        //create user in fire base
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account creeted, now to add user info into firebase db
                saveUser()
            }.addOnFailureListener{ e->
                //failed making the new account
                progressDialog.dismiss()
                Toast.makeText(this, "Error has occurred due to ${e.message}", Toast.LENGTH_LONG).show()

            }
    }

    private fun saveUser() {
        //4.Save user info
        progressDialog.setMessage("Saving your user information")

        //timestamping
        val timestamp = System.currentTimeMillis()

        //get current user uid, now that the user is registered to the app, we can get this now
        val uid = firebaseAuth.uid

        //setup the date to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" //TODO I want to eventually add images for the user
        hashMap["userType"] = "Registered"
        hashMap["timestamp"] = timestamp


        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Account Created", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@RegisterActivity, RegisteredUserActivity::class.java))
                finish()

            }.addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error has occurred due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


}