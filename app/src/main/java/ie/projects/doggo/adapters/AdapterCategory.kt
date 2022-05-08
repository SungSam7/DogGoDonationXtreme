package ie.projects.doggo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import ie.projects.doggo.filters.FilterCategory
import ie.projects.doggo.models.ModelCategory
import ie.projects.doggo.activities.PdfListActivity
import ie.projects.doggo.databinding.RowCategoryBinding


class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable{


    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null

    private lateinit var binding : RowCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        /*Get the data, set the data and handle clicks*/

        //get the data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.categoryTv.text = category

        //handle click delete category
        holder.deleteBtn.setOnClickListener{
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this?")
                .setPositiveButton("Confirm"){a, d->
                    Toast.makeText(context, "Deleting", Toast.LENGTH_LONG).show()
                    deleteCategory(model, holder)

                }.setNegativeButton("Cancel"){a, d->
                    a.dismiss()

                }
                .show()
        }

        //nadling start pdf list admin activity which also passes ddf id, title
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfListActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {

        //get id of category to delete
        val id = model.id
        //Firebase DB > Categories > categoryId
        val reference = FirebaseDatabase.getInstance().getReference("Categories")
        reference.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { e->
                Toast.makeText(context, "Unable to delete ${e.message}", Toast.LENGTH_LONG).show()
            }

    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }




    //viewholder class to hold/init UI views for row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        //init ui views
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}