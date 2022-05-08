package ie.projects.doggo.filters

import android.widget.Filter
import ie.projects.doggo.adapters.AdapterCategory
import ie.projects.doggo.models.ModelCategory

class FilterCategory: Filter {

    //arraylist in which we want to searh category
    private var filterList: ArrayList<ModelCategory>

    //adapter in which we filter need to be implemented
    private var adapterCategory: AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //value should not be null and not be empty
        if(constraint != null && constraint.isNotEmpty()){
            //searched vlaue is nor null not empty

            //change to uppoercase, or lowercase to avoid sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelCategory> = ArrayList()

            for(i in 0 until filterList.size){
                //validate
                if(filterList[i].category.uppercase().contains(constraint)) {
                    //add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else
        {
           //search value either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results //dont miss it
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filtered
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //notify changes
        adapterCategory.notifyDataSetChanged()
    }
}