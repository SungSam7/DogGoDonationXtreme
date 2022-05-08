package ie.projects.doggo.tools

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.models.Constants

import java.util.*
import kotlin.collections.HashMap

class CategoryClass : Application(){

    override fun onCreate(){
        super.onCreate()
    }

    companion object{


        fun loadCategoryFromDatabase(categoryId : String, categoryTv: TextView)
        {
            //loading category using category id from fb
            val reference = FirebaseDatabase.getInstance().getReference("Categories")
            reference.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category: String = "${snapshot.child("category").value}"

                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }





    }


}