package ie.projects.doggo.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ie.projects.doggo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        firebaseAuth = FirebaseAuth.getInstance()




        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }


        binding.loginBtn.setOnClickListener {

            checkEmailAndPassword()
        }



        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private var email = ""
    private var password = ""

    private fun checkEmailAndPassword() {

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_LONG).show()
        }

        else if(password.isEmpty()){
            Toast.makeText(this, "Please enter a password at least 6 characters long", Toast.LENGTH_LONG).show()
        }
        else{
            loginUserToDatabase()
        }
    }

    private fun loginUserToDatabase() {
        progressDialog.setMessage("Logging in to your account")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener {
                checkUserInDatabase()
            }.addOnFailureListener { e->

                Toast.makeText(this, "Error has occurred due to = ${e.message}", Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }
    }

    private fun checkUserInDatabase() {

        val firebaseUser = firebaseAuth.currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseUser.uid)
        startActivity(Intent(this@LoginActivity, RegisteredUserActivity::class.java))
        finish()



    }
}