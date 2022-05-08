package ie.projects.doggo.filters

import android.widget.Filter
import ie.projects.doggo.adapters.AdapterPdf
import ie.projects.doggo.models.ModelPdf


//user to filter data from rV | search pdf from pdf list in Rv
class FilterPdf : Filter {


    //arraylist in which we want to search
    var filterList : ArrayList<ModelPdf>

    //adapter in which filter needs to be implemented
    var adapterPdf: AdapterPdf


    constructor(filterList: ArrayList<ModelPdf>, adapterPdf: AdapterPdf) {
        this.filterList = filterList
        this.adapterPdf = adapterPdf
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint : CharSequence? = constraint //value to be searched
        val results = FilterResults()

        //search value should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()){
            //change to upercase or lower to avoid sensitivty
            constraint = constraint.toString().toLowerCase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate if matched
                if (filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list add to the filtered list
                        filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //value is either null or empty reutrn all data
            results.count = filterList.size
            results.values = filterList

        }

        return results

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes

        adapterPdf.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdf.notifyDataSetChanged()
    }
}