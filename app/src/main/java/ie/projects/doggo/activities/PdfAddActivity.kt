package ie.projects.doggo.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.models.ModelCategory
import ie.projects.doggo.databinding.ActivityPdfAddBinding

class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPdfAddBinding


    private lateinit var firebaseAuth: FirebaseAuth


    private lateinit var progressDialog : ProgressDialog

    //arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfUri: Uri? = null

    //Tag
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        getPdfCategoriesFromDatabase()



        //handle click go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click show category pick dialogs
        binding.categoryTv.setOnClickListener {
            categoryPick()
        }

        // handle click pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //handle click, start uploading pdf
        binding.submitBtn.setOnClickListener {
            //1 validate data
            //2 upload pdf to fb db
            //3 get url of uploaded pdf
            //4 upload pdf info to

            savingPDFInfo()
        }
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun savingPDFInfo() {

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this, "Enter a title", Toast.LENGTH_LONG).show()
        }
        else if(description.isEmpty()){
            Toast.makeText(this, "Enter a description", Toast.LENGTH_LONG).show()
        }
        else if (category.isEmpty()){
            Toast.makeText(this, "Enter a category", Toast.LENGTH_LONG).show()
        }
        else if(pdfUri == null){
            Toast.makeText(this, "Pick a PDF", Toast.LENGTH_LONG).show()
        }
        else{
            // data validated begin upload
            uploadPdfToDatabase()
        }
    }

    private fun uploadPdfToDatabase() {
        //2. upload pdf to fb db

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Info/$timestamp"

        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url")

                //3. get url of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDatabase(uploadedPdfUrl, timestamp)

            }.addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    //TODO timestamp maybe needed to be String??, this occurred when I did it before
    private fun uploadPdfInfoToDatabase(uploadedPdfUrl: String, timestamp: Long) {
        //4. upload pdf info to fb db
        progressDialog.setMessage("Uploading pdf info")

        val uid = firebaseAuth.uid

        //set up data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0


        //db reference DB > Info > info id > info
        val reference = FirebaseDatabase.getInstance().getReference("Info")
        reference.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "PDF Uploaded Successfully", Toast.LENGTH_LONG).show()
                pdfUri = null

            }.addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_LONG).show()


            }
    }

    private fun getPdfCategoriesFromDatabase() {

        //init arraylist
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                categoryArrayList.clear()
                for (dataSnapshot in snapshot.children){
                    //get data
                    val model = dataSnapshot.getValue(ModelCategory::class.java)

                    //add to arraylist
                    categoryArrayList.add(model!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPick(){
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoriesArray.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){ dialog, which ->
                //handle item click, get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                //set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryTitle")

            }.show()
    }

    private fun pdfPickIntent(){

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == RESULT_OK){
                pdfUri = result.data!!.data
            }
            else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}