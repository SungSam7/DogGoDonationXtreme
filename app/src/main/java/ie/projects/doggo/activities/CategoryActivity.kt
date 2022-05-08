package ie.projects.doggo.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import ie.projects.doggo.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCategoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        binding.backButton.setOnClickListener{
            onBackPressed()
        }


        binding.submitButton.setOnClickListener {
            checkCategoryEntry()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private var category = ""
    private fun checkCategoryEntry() {

        //get data
        category = binding.categoryEt.text.toString().trim()

        //validate data
        if(category.isEmpty()){
            Toast.makeText(this, "Enter Category",Toast.LENGTH_LONG).show()
        }
        else{
            addCategoryToDatabase()
        }

    }

    private fun addCategoryToDatabase() {


        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = "$timestamp"
        hashMap["uid"] = "${firebaseAuth.uid}"


        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {

                Toast.makeText(this, "Added the Category to the database",Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }.addOnFailureListener { e ->

                Toast.makeText(this, "Error failed to add category because = ${e.message}",Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }

    }
}