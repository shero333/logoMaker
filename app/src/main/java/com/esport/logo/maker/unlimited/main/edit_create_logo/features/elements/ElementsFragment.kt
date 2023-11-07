package com.esport.logo.maker.unlimited.main.edit_create_logo.features.elements

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.esport.logo.maker.unlimited.databinding.FragmentElementsBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.elements.adapters.ElementsListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.ElementsFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.utils.Utils
import com.xiaopo.flying.sticker.stickers.ElementSticker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElementsFragment(private var elementsFragmentInterface: ElementsFragmentInterface) : Fragment(), ElementsListAdapter.ItemClickedImages {

    private val elementsViewModel: ViewModelMain by activityViewModels()
    private lateinit var binding: FragmentElementsBinding
    private lateinit var elementsAdapter: ElementsListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentElementsBinding.inflate(inflater, container, false)

        elementsAdapter = ElementsListAdapter(this)

        //setting the recyclerview for effects
        binding.listElements.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.listElements.setHasFixedSize(false)
        binding.listElements.adapter = elementsAdapter

        binding.progressBarAnim.visibility = View.VISIBLE
        binding.progressBarAnim.playAnimation()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //display the list of images to the recyclerview
        elementsViewModel.getElementsLiveData().observe(viewLifecycleOwner) { elementsList ->

            if (elementsList != null) {

                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //effects list
                elementsAdapter.setElementsList(elementsList)
                elementsAdapter.notifyDataSetChanged()
            } else {

                //replace the view with empty views
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()
            }
        }
    }

    override fun itemClicked(item: Image, position: Int) {

        //clicked image from the background 'graphics' list
        elementsAdapter.changeSelectionStatus(position)
        binding.listElements.scrollToPosition(position)


        //add the stickers on click keeping in mind the list of stickers {stickers will be replaced over here!}
        //add the sticker to the view
        //create the sticker
        val sticker = ElementSticker(
            BitmapDrawable(
                requireContext().resources,
                Utils.stringToBitmap(item.logoImage)
            )
        )

        //onclick
        elementsFragmentInterface.clickedElementFromListApplyElementsFragment(sticker)

        Log.i("itemClickedElements: ",sticker.centerPoint.toString())
    }
}