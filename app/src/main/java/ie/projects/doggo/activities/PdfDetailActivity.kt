package ie.projects.doggo.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.models.Constants
import ie.projects.doggo.databinding.ActivityPdfDetailBinding
import ie.projects.doggo.tools.CategoryClass
import ie.projects.doggo.tools.PdfClass
import ie.projects.doggo.tools.FormatTimeStampClass
import java.io.FileOutputStream
import java.lang.Exception

class PdfDetailActivity : AppCompatActivity() {

    lateinit var binding : ActivityPdfDetailBinding

    private companion object{const val TAG = "INFO_DETAILS_TAG"
    }

    private var infoId = ""

    private var infoTitle = ""
    private var infoUrl = ""

    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /// get infoId from intent
        infoId = intent.getStringExtra("infoId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)


        //increease info view count when the page starts
        PdfClass.incrementPDFInfoViewCount(infoId)


        loadInfoDetails()


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // open pdfviewactivity
        binding.readInfoBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("infoId", infoId) //passing infoId to the activity

            startActivity(intent)
        }

        //download info pdf
        binding.downloadInfoBtn.setOnClickListener {
            // must check the storage permission needs WRITE_EXTERNAL_STORAGE inside the manifest
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                android.util.Log.d(TAG, "onCreate: STORAGE PERMISSION GRANTED")
                downloadInfo()
            }
            else{
                Log.d(TAG, "onCreate: STORAGE PERMISSION DENIED, REQUEST IT")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean ->

        if(isGranted){
            downloadInfo()
        }
        else{
            Log.d(TAG, "onCreate: STORAGE PERMISSION DENIED")
            Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()

        }
    }

    private fun downloadInfo(){
        //progress bar

        progressDialog.setMessage("Downloading Info PDF")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(infoUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                saveToDownloadsFolder(bytes)
                progressDialog.dismiss()
                Log.d(TAG, "downloadInfo: Info Downloaded")
                Toast.makeText(this, "Info Downloaded", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadInfo: Failed to download ${e.message}")
                Toast.makeText(this, "Failed to download ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {


        Log.d(TAG, "saveToDownloadsFolder: SAVING DOWNLOAD PDF")

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //create folder it doesnt exist

            val filePath = downloadsFolder.path +"/"+nameWithExtension

            val out = FileOutputStream(filePath)

            out.write(bytes)
            out.close()

            Toast.makeText(this, "SAVED TO DOWNLOADS FOLDER", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: SAVED TO DOWNLOADS FOLDER")
            progressDialog.dismiss()
            incrementDownloadCount()



        }
        catch (e: Exception){
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: FAILED TO SAVE ${e.message}")
            Toast.makeText(this, "FAILED TO SAVE TO DOWNLOADS FOLDER", Toast.LENGTH_SHORT).show()

        }
    }

    private fun incrementDownloadCount() {

        // incerememnting the downloads count
        Log.d(TAG, "incrementDownloadCount: ")

        //1. get the current downloads count
        val reference = FirebaseDatabase.getInstance().getReference("Info")
        reference.child(infoId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: CURRENT DOWNLOAD COUNT : $downloadsCount")


                    if(downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }

                    val newDownloadCount : Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: NEW DOWNLOADS COUNT : $newDownloadCount")

                    val hashMap: HashMap<String, Any> = HashMap()

                    hashMap["downloadsCount"] = newDownloadCount


                    val dbRef = FirebaseDatabase.getInstance().getReference("Info")
                    dbRef.child(infoId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: DOWNLOADS INCREASE ")
                        }
                        .addOnFailureListener { e->
                            Log.d(TAG, "onDataChange: FAILED ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    private fun loadInfoDetails() {

        val reference = FirebaseDatabase.getInstance().getReference("Info")
        reference.child(infoId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    infoTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                     infoUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val date = FormatTimeStampClass.formatTimeStamp(timestamp.toLong())

                        //load pdf category
                    CategoryClass.loadCategoryFromDatabase(categoryId, binding.categoryTv)

                    //load pdf thumbnail, pages count
                    PdfClass.loadPdfFromUrl(
                        "$infoUrl",
                        "$infoTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    //load pdf size
                    PdfClass.loadPdfSize("$infoUrl", "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text = infoTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}