package ie.projects.doggo.adapters

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import ie.projects.doggo.activities.*
import ie.projects.doggo.databinding.RowPdfBinding
import ie.projects.doggo.filters.FilterPdf
import ie.projects.doggo.models.ModelPdf
import ie.projects.doggo.tools.CategoryClass
import ie.projects.doggo.tools.PdfClass
import ie.projects.doggo.tools.FormatTimeStampClass

class AdapterPdf : RecyclerView.Adapter<AdapterPdf.HolderPdf>, Filterable{

    //context
    private var context: Context

    //array list to hold the pdfs
    public var pdfArrayList: ArrayList<ModelPdf>
    private val filterList : ArrayList<ModelPdf>

    private lateinit var binding:RowPdfBinding

    //filter object
    private var filter: FilterPdf? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }







    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdf {
        //bidning/inflatelayout row_pdf_admin.xml
        binding = RowPdfBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderPdf(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdf, position: Int) {
        //GET DATA,SET DATA. Hanlde clicks etyc.

        //GET DATA
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //converting timestamp to dd/MM/yyyy format
        val formattedDate = FormatTimeStampClass.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load further details like category, pff from url, pdf size

        //category id
        CategoryClass.loadCategoryFromDatabase(categoryId, holder.categoryTv)

        //we dont need page number herenull for page no. || load pdf thumnbail
        PdfClass.loadPdfFromUrl(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
      //  MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //show dialig with options edit info, delete
        holder.moreBtn.setOnClickListener {
            moreOptions(model, holder)
        }

        // open pdfDetail Activity
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("infoId", pdfId)
            context.startActivity(intent)
        }
    }

    private fun moreOptions(model: ModelPdf, holder: HolderPdf) {
        //get id, url, title of the info
        val infoId = model.id
        val infoUrl = model.url
        val infoTitle = model.title

        //options to show indialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){
                dialog, position ->

                if(position == 0){
                    // edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("infoId", infoId) //passed the info id, going to be used to edit the info
                    context.startActivity(intent)
                }
                else if (position == 1)
                {
                    //delete is clicked

                   deleteInfo(context, infoId, infoUrl, infoTitle)


                }
            }
            .show()

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //items count
    }



    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdf(filterList, this)
        }

        return filter as FilterPdf
    }

    // view holder class for row_pdf_admin.xml
    inner class HolderPdf(itemView : View) : RecyclerView.ViewHolder(itemView){

        //ui views of row pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }

    private fun deleteInfo(context : Context, infoId: String, infoUrl : String, infoTitle: String){

        val TAG = "DELETE_INFO_TAG"

        Log.d(TAG, "deleteInfo: deleting")

        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Delete $infoTitle")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        Log.d(TAG, "deleteInfo: Deleting Info from Storage")
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(infoUrl)
        storageReference.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteInfo: Deleting from storage")
                Log.d(TAG, "deleteInfo: Deleting from db")

                val ref = FirebaseDatabase.getInstance().getReference("Info")
                ref.child(infoId)
                    .removeValue()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "deleteInfo: Deleted from db")
                    }
                    .addOnFailureListener { e->
                        Log.d(TAG, "deleteInfo: failed to delete from db ${e.message}")
                        Toast.makeText(context, "Failed to delete from db ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {e->
                Log.d(TAG, "deleteInfo: failed to delete from storage ${e.message}")
                Toast.makeText(context, "Failed to delete from storage ${e.message}", Toast.LENGTH_SHORT).show()
            }


    }
}