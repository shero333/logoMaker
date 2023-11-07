package com.esport.logo.maker.unlimited.main.edit_create_logo.features.logo

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.esport.logo.maker.unlimited.databinding.FragmentLogoBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.logo.adapters.LogosListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.LogosFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.utils.Utils
import com.xiaopo.flying.sticker.stickers.LogoSticker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogoFragment(private var logosFragmentInterface: LogosFragmentInterface) : Fragment(), LogosListAdapter.ItemClickedImages {

    private val logosViewModel: ViewModelMain by activityViewModels()
    private lateinit var binding: FragmentLogoBinding
    private lateinit var logosAdapter: LogosListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentLogoBinding.inflate(inflater, container, false)

        logosAdapter = LogosListAdapter(this)

        //setting the recyclerview for effects
        binding.listLogosImages.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.listLogosImages.setHasFixedSize(false)
        binding.listLogosImages.adapter = logosAdapter

        binding.progressBarAnim.visibility = View.VISIBLE
        binding.progressBarAnim.playAnimation()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //display the list of images to the recyclerview
        logosViewModel.getLogosLiveData().observe(viewLifecycleOwner) { logosList ->

            if (logosList != null) {

                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //effects list
                logosAdapter.setLogosList(logosList)
                logosAdapter.notifyDataSetChanged()
            } else {

                //replace the view with empty views
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun itemClicked(item: Image, position: Int) {

        //clicked image from the background 'graphics' list
        logosAdapter.changeSelectionStatus(position)
        binding.listLogosImages.scrollToPosition(position)

        //add the sticker to the view
        //create the sticker
        val sticker = LogoSticker(BitmapDrawable(requireContext().resources, Utils.stringToBitmap(item.logoImage)))

        logosFragmentInterface.clickedLogoFromListApplyLogosFragment(sticker)
    }
}