package com.esport.logo.maker.unlimited.main.edit_create_logo.features.effects

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.esport.logo.maker.unlimited.databinding.FragmentEffectsBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.effects.adapters.EffectsListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.EffectsFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EffectsFragment(var effectsFragmentInterface: EffectsFragmentInterface) : Fragment(),
    EffectsListAdapter.ItemClickedImages, EffectsListAdapter.CloseButtonEffectsClicked {

    private val effectsViewModel: ViewModelMain by activityViewModels()
    private lateinit var binding: FragmentEffectsBinding
    private lateinit var effectsAdapter: EffectsListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        //initialization of the binding
        binding = FragmentEffectsBinding.inflate(inflater, container, false)

        effectsAdapter = EffectsListAdapter(this,this)

        //set the background of the logo
        if (effectsViewModel.selectedImageShapeApplied.isNotEmpty()) {

            effectsViewModel.background = Utils.stringToBitmap(effectsViewModel.selectedImageShapeApplied)
        } else if (effectsViewModel.selectedImageSimple.isNotEmpty()) {

            effectsViewModel.background = Utils.stringToBitmap(effectsViewModel.selectedImageSimple)
        }

        //setting the recyclerview for effects
        binding.listEffects.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.listEffects.setHasFixedSize(false)
        binding.listEffects.adapter = effectsAdapter

        binding.progressBarAnim.visibility = View.VISIBLE
        binding.progressBarAnim.playAnimation()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //display the list of images to the recyclerview
        effectsViewModel.getEffectsLiveData().observe(viewLifecycleOwner) { effectsList ->

            if (effectsList != null) {

                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //effects list
                effectsAdapter.setEffectsList(effectsList)
                effectsAdapter.notifyDataSetChanged()
            }
            else {

                //replace the view with empty views
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()
            }
        }

        //seekbar opacity handling
        binding.opacityProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            //Callback for the value change in progress
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                //get the value of alpha and set it to the card view
                Log.i("onProgressChanged: ", progress.toString())
                //calculate percentage of progress value
                //maximum color value is 255 and according to this the progress is divided by the maximum value of the seekbar
                // to make alpha effective according to the progress percentage.
                // Calculate alpha apply opacity progress to the background
                effectsFragmentInterface.effectItemClickOpacityProgressEffectsFragment(Utils.stringToBitmap(effectsViewModel.selectedEffect!!.logoImage)!!, effectsViewModel.background, progress)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun itemClicked(item: Image, position: Int) {

        //clicked image from the background 'graphics' list
        effectsAdapter.changeSelectionStatus(position)
        binding.listEffects.scrollToPosition(position)

        //assigning value to the variable
        effectsViewModel.selectedEffect = item
        //apply the effect on the background of the stickers
        val texture = Utils.stringToBitmap(item.logoImage)

        //apply the texture effect on the background created by the user before
        effectsFragmentInterface.effectItemClickApplyBackgroundEffectsFragment(texture!!, effectsViewModel.background, binding.opacityProgress.progress)
    }

    override fun closeEffectButtonClicked() {

        //remove effect from the object
        effectsFragmentInterface.effectCleanButtonClickedEffectsFragment()
    }

}