package ie.projects.doggo.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.projects.doggo.databinding.ActivityPdfEditBinding

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPdfEditBinding

    private companion object{
        private val TAG = "PDF_EDIT_TAG"
    }

    //book id get from inten start from adapterPdfAdmin
    private var infoId = ""

    private lateinit var progressDialog: ProgressDialog

    //array list to hold category titles
    private lateinit var categoryTitleArrayList : ArrayList<String>

    //array list to hold category ids
    private lateinit var categoryIdArrayList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        infoId = intent.getStringExtra("infoId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadInfo()


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }



    }

    private fun loadInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Info")
        ref.child(infoId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()


                    //set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load info category info using categoryId
                    Log.d(TAG, "onDataChange: Loading Info Category")
                    val refInfoCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refInfoCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to textview
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }




    private var title = ""
    private var description = ""

    private fun validateData() {

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()


        if (title.isEmpty()){
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
        }
        else if (selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Selected Category", Toast.LENGTH_SHORT).show()
        }
        else{
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Updating pdf info")

        progressDialog.setMessage("Update the pdf info")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"


        val ref = FirebaseDatabase.getInstance().getReference("Info")
        ref.child(infoId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: updated successfully")

                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()


            }
            .addOnFailureListener { e->
                Log.d(TAG, "updatePdf: update failed ${e.message}")
                progressDialog.dismiss()

                Toast.makeText(this, "Failed to update ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {

        //make string array from array list of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)

        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){
                dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //set to textview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show() //show dialog
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}