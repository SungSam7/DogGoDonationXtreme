package ie.projects.doggo.tools

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.models.Constants


class PdfClass : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {


        //function to get pdf sixzes
        fun loadPdfSize(pdfUrl : String, pdfTitle: String, sizeTv: TextView){
            val TAG = "PDF_SIZE_TAG"

            //using rul we can get file and its medata from fb storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {
                        storageMetadata ->

                    val bytes = storageMetadata.sizeBytes.toDouble()
                    val kilob = bytes/1024
                    val megab = kilob/1024
                    if(megab>=1){
                        sizeTv.text = "${String.format("%.2f", megab)} MB"
                    }
                    else if (kilob>=1){
                        sizeTv.text = "${String.format("%.2f", kilob)} KB"
                    }
                    else{
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "Failed to load size of the pdf due to ${e.message}")
                }
        }

        fun incrementPDFInfoViewCount(infoId: String){
            //get current views count
            val ref = FirebaseDatabase.getInstance().getReference("Info")
            ref.child(infoId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get view count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if(viewsCount == "" || viewsCount == "null"){
                            viewsCount = "0";
                        }
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in the db
                        val hashMap  = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Info")
                        dbRef.child(infoId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


        fun loadPdfFromUrl(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {

            val TAG = "PDF_THUMBNAIL_TAG"

            //using url we can get file and its metadata from firebases storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //set to pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")

                        }.onLoad { numberOfPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $numberOfPages")
                            //pdf loaded, we can set the page count, pdf thumbnail
                            progressBar.visibility = View.INVISIBLE

                            //if pagesTV param is not null then set pages numbers
                            if (pagesTv != null) {
                                pagesTv.text = "$numberOfPages"
                            }
                        }
                        .load()

                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfSize: Failed due to ${e.message}")
                }
        }

    }
}