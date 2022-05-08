package ie.projects.doggo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.projects.doggo.models.ModelPdf
import ie.projects.doggo.adapters.AdapterPdf
import ie.projects.doggo.databinding.ActivityPdfListBinding
import java.lang.Exception

class PdfListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListBinding

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    private var categoryId = ""
    private var category = ""

    //array list to hold the info
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdf : AdapterPdf


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //get from intent, that was passed from the adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.subTitleTv.text = category

        //load pdfs
        getPdfListFromDatabase()

        //handle click going back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }


        //search pdf
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
                //filter data
                try{
                    adapterPdf.filter!!.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, " ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                
            }
        })

    }

    private fun getPdfListFromDatabase() {
        //init arraylist

        pdfArrayList = ArrayList()


        val reference = FirebaseDatabase.getInstance().getReference("Info")
        //TODO Not sure if i labeled the path right somewhere else if an error occurs
        reference.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before start adding data into it
                    pdfArrayList.clear()
                    for(dataSnapshot in snapshot.children){
                        //get data
                        val model = dataSnapshot.getValue(ModelPdf::class.java)

                        //add to list
                        if (model != null) {
                            pdfArrayList.add(model)
                        }

                    }

                    //setup adapter
                    adapterPdf = AdapterPdf(this@PdfListActivity, pdfArrayList)
                    binding.infoRv.adapter = adapterPdf
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}