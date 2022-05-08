package ie.projects.doggo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.models.Constants
import ie.projects.doggo.databinding.ActivityPdfViewBinding

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding


    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }
    var infoId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)



        infoId = intent.getStringExtra("infoId")!!

        getInfoDetailsFromDatabase()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }



    private fun getPDFInfoFromUrl(pdfUrl: String) {

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false) // set to false to scroll vertical or true to scroll horizontal
                    .onPageChange{page, pageCount ->
                       val currentPage = page+1 // page starts from zero so we add plus 1 to start from 1
                       binding.toolbarSubtitleTv.text = "$currentPage/$pageCount" //ex. 2/43
                        Log.d(TAG, "loadInfoFromUrl: $currentPage/$pageCount")
                        
                    }
                    .onError {t->
                        Log.d(TAG, "${t.message}")
                    }
                    .onPageError{page, t->
                        Log.d(TAG, "${t.message}")
                        
                    }
                    .load()

                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                Log.d(TAG, "${e.message}")
                binding.progressBar.visibility = View.GONE

            }
    }

    private fun getInfoDetailsFromDatabase() {

        val reference = FirebaseDatabase.getInstance().getReference("Info")
        reference.child(infoId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get info url
                    val pdfUrl = snapshot.child("url").value

                    //step 2 load pdf using url from firebase storage
                    getPDFInfoFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}