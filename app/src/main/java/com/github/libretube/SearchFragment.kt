package com.github.libretube

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.github.libretube.adapters.SearchAdapter
import java.io.IOException


class SearchFragment : Fragment() {

    private val TAG = "SearchFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.search_recycler)
        recyclerView.layoutManager = GridLayoutManager(view.context, 1)
        val autoTextView = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
            autoTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(s!! != ""){
                        GlobalScope.launch {
                            fetchSuggestions(s.toString(), autoTextView)
                            delay(2000)
                            fetchSearch(s.toString(),recyclerView)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
    }

    private fun fetchSuggestions(query: String, autoTextView: AutoCompleteTextView){
        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.api.getSuggestions(query)
            } catch (e: IOException) {
                println(e)
                Log.e(TAG, "IOException, you might not have internet connection")
                return@launchWhenCreated
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException, unexpected response")
                return@launchWhenCreated
            }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, response)
            autoTextView.setAdapter(adapter)
        }
    }
    private fun fetchSearch(query: String, recyclerView: RecyclerView){
        lifecycleScope.launchWhenCreated {
            val response = try {
                RetrofitInstance.api.getSearchResults(query, "all")
            } catch (e: IOException) {
                println(e)
                Log.e(TAG, "IOException, you might not have internet connection "+e.toString())
                return@launchWhenCreated
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException, unexpected response")
                return@launchWhenCreated
            }
            if(response.items!!.isNotEmpty()){
               runOnUiThread {
                   recyclerView.adapter = SearchAdapter(response.items)
               }
            }

        }
    }


    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
}