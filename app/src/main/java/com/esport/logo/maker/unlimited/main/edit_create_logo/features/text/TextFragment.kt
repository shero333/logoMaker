package com.esport.logo.maker.unlimited.main.edit_create_logo.features.text

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Layout.Alignment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.databinding.FragmentTextBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.FontItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.TabItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.UserColors
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.text.adapters.ColorListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.text.adapters.FontsListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.text.adapters.TabsAdapterBackground
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.TextFragmentInterface
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TextFragment(var textFragmentInterface: TextFragmentInterface) : Fragment(),
    TabsAdapterBackground.ItemClickedTabs, ColorListAdapter.ColorClickedEvent, FontsListAdapter.FontItemClickEvent {

    private lateinit var binding: FragmentTextBinding
    private val textViewModel: ViewModelMain by activityViewModels()
    private lateinit var tabsAdapter: TabsAdapterBackground
    private lateinit var colorsAdapter: ColorListAdapter
    private lateinit var tabs: ArrayList<TabItem>
    private lateinit var fontsListAdapter: FontsListAdapter
    private var alpha = 0
    private var colorSelected = false

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentTextBinding.inflate(inflater, container, false)

        tabsAdapter = TabsAdapterBackground(requireContext(), this)
        colorsAdapter = ColorListAdapter(this)
        fontsListAdapter = FontsListAdapter(this)

        binding.progressBarAnim.visibility = View.VISIBLE
        binding.progressBarAnim.playAnimation()

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setting the tabs list adapter
        tabs = ArrayList()
        tabs.add(TabItem("Fonts", isSelected = true))
        tabs.add(TabItem("Color"))
        tabs.add(TabItem("3D"))
        tabs.add(TabItem("Controls"))
        tabs.add(TabItem("Shadow"))
        tabs.add(TabItem("Brush"))
        tabsAdapter.setTabsNames(tabs)

        //setting the recyclerview for tabs
        binding.listTabs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.listTabs.setHasFixedSize(false)
        binding.listTabs.adapter = tabsAdapter

        //default selection of tab 'fonts'
        fontsClick()

//        //rotation of the sticker text {x and y axis}
//        binding.dimensionsLayout.d1Progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//
//                //apply rotations on the sticker
//                val rotation = progress.toFloat() * 1.8f
//
////                textViewModel.getCurrentStickerLiveData().observe(viewLifecycleOwner) { currentSticker ->
////
////                        if (currentSticker != null) {
//////                            if (currentSticker is TextSticker) {
//////
//////                                currentSticker =
//////                                    .setText(stickerText)
////////                    .setTextColor(selectedColor)
////////                    .setTypeface(selectedFontItem.typeface)
////////                    .setTextAlign(alignment!!)
//////                                    .setMaxTextSize(30f)
//////                                    .resizeText()
//////
//////                                binding.stickerViewText.replace(currentSticker, false)
//////
//////                                //sending stickers list to liveData
//////                                val location = IntArray(2)
//////                                binding.stickerViewText.getLocationInWindow(location)
//////
//////                                //setting the current sticker which is being working on
//////                                textViewModel.setCurrentSticker(currentSticker)
//////                            }
////                        }
////                    }
//
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//
//        })
//
//        //rotation of the sticker text {x axis}
//        binding.dimensionsLayout.d2Progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//
////                //apply rotations on the sticker
////                val rotation = progress.toFloat() * 1.8f
////
////                val sticker = TextSticker(
////                    requireContext(),
////                    true,
////                    false,
////                    rotation
////                )
////
////                    .setText(stickerText)
//////                    .setTextColor(selectedColor)
//////                    .setTypeface(selectedFontItem.typeface)
//////                    .setTextAlign(alignment!!)
////                    .setMaxTextSize(30f)
////                    .resizeText()
////
////                activityBinding.stickerView.replace(sticker, false)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//        })
//
//        //rotation of the sticker text {y axis}
//        binding.dimensionsLayout.d3Progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//
////                //apply rotations on the sticker
////                val rotation = progress.toFloat() * 1.8f
////
////                val sticker = TextSticker(
////                    requireContext(),
////                    false,
////                    true,
////                    rotation
////                )
////
////                    .setText(stickerText)
//////                    .setTextColor(selectedColor)
//////                    .setTypeface(selectedFontItem.typeface)
//////                    .setTextAlign(alignment!!)
////                    .setMaxTextSize(30f)
////                    .resizeText()
////
////                activityBinding.stickerView.replace(sticker, false)
//
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//        })

        //seekbar of opacity
        binding.opacityProgress.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView") object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                //color opacity
                alpha = (progress.toFloat() / 0.35f).toInt()

                //shadow intensity
                val intensity = (progress.toFloat() / 3.33f).toInt()

                if (textViewModel.shadowClicked){
                    textViewModel.shadowRadius = intensity
                    textFragmentInterface.shadowColorAndIntensityForTheStickerTextTextFragment(textViewModel.shadowColor,textViewModel.shadowRadius)

                }
                else{
                    textFragmentInterface.opacityOfTextOfStickerTextFragment(alpha)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {


            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {


            }

        })

        //layout control
        binding.controlLayout.upControlButton.setOnClickListener {
            //move the sticker up
            textFragmentInterface.controllerViewMoveStickerButtonsClickEventsTextFragment("Up")
        }
        binding.controlLayout.leftControlButton.setOnClickListener {

            //move the sticker left
            textFragmentInterface.controllerViewMoveStickerButtonsClickEventsTextFragment("Left")
        }
        binding.controlLayout.rightControlButton.setOnClickListener {

            //move the sticker right
            textFragmentInterface.controllerViewMoveStickerButtonsClickEventsTextFragment("Right")
        }
        binding.controlLayout.downControlButton.setOnClickListener {

            //move the sticker down
            textFragmentInterface.controllerViewMoveStickerButtonsClickEventsTextFragment("Down")
        }

        //text alignment
        binding.controlLayout.txCenterButton.setOnClickListener {

            textViewModel.alignment = Alignment.ALIGN_CENTER
            textFragmentInterface.controllerViewAlignStickerTextButtonsClickEventsTextFragment(Alignment.ALIGN_CENTER)
        }
        binding.controlLayout.txRightButton.setOnClickListener {

            textViewModel.alignment = Alignment.ALIGN_OPPOSITE
            textFragmentInterface.controllerViewAlignStickerTextButtonsClickEventsTextFragment(Alignment.ALIGN_OPPOSITE)
        }
        binding.controlLayout.txLeftButton.setOnClickListener {

            textViewModel.alignment = Alignment.ALIGN_NORMAL
            textFragmentInterface.controllerViewAlignStickerTextButtonsClickEventsTextFragment(Alignment.ALIGN_NORMAL)
        }

        //edit the text sticker controlling layout
        binding.controlLayout.editTextButton.setOnClickListener {

            textFragmentInterface.controllerViewEditStickerTextButtonsClickEventTextFragment()
        }
        //copy the text sticker controlling layout
        binding.controlLayout.copyTextButton.setOnClickListener {
            textFragmentInterface.controllerViewCopyStickerButtonClickEventTextFragment()
        }

        //observing fonts list and setting to the recyclerview
        textViewModel.getFontsLiveData().observe(viewLifecycleOwner) { fontsList ->

            if (fontsList != null) {

                //set the data and disable and enable the views
                binding.listFonts.visibility = View.VISIBLE
                binding.noDataAvailable.visibility = View.GONE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //set the list to the adapter
                fontsListAdapter.setFontsList(fontsList)
                fontsListAdapter.notifyDataSetChanged()
            } else {
                binding.noDataAvailable.visibility = View.VISIBLE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()
            }
        }

    }

    //functions to handle the views on runtime
    @SuppressLint("NotifyDataSetChanged")
    private fun fontsClick() {

        //setting visibility of views
        binding.listFonts.visibility = View.VISIBLE
        binding.colorListRecyclerview.visibility = View.GONE
        binding.dimensionsLayout.visibility = View.GONE
        binding.controlLayout.controlLayout.visibility = View.GONE
        binding.opacityProgress.visibility = View.GONE

        //setting recyclerview of colors
        binding.listFonts.layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        binding.listFonts.setHasFixedSize(false)
        binding.listFonts.adapter = fontsListAdapter

    }

    private fun colorClick() {

        //setting visibility of views
        binding.listFonts.visibility = View.INVISIBLE
        binding.colorListRecyclerview.visibility = View.VISIBLE
        binding.dimensionsLayout.visibility = View.GONE
        binding.controlLayout.controlLayout.visibility = View.GONE
        binding.opacityProgress.visibility = View.VISIBLE
        binding.opacityProgress.progress = 100
        textViewModel.shadowClicked = false

        //setting recyclerview of colors
        binding.colorListRecyclerview.layoutManager =
            GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false)
        binding.colorListRecyclerview.setHasFixedSize(false)
        binding.colorListRecyclerview.adapter = colorsAdapter

        //get the colors list to the colorsAdapter
        val listColors = ArrayList<UserColors>()
        for (color in requireActivity().resources.getIntArray(R.array.colorsList)) {
            listColors.add(UserColors(color))
        }
        colorsAdapter.setListColors(listColors)
        Log.i(
            "listColors",
            requireActivity().resources.getIntArray(R.array.colorsList).size.toString()
        )
    }

    private fun d3Click() {

        //setting visibility of views
        binding.listFonts.visibility = View.INVISIBLE
        binding.colorListRecyclerview.visibility = View.GONE
        binding.dimensionsLayout.visibility = View.VISIBLE
        binding.controlLayout.controlLayout.visibility = View.GONE
        binding.opacityProgress.visibility = View.GONE

    }

    private fun controlsClick() {

        //setting visibility of views
        binding.listFonts.visibility = View.INVISIBLE
        binding.colorListRecyclerview.visibility = View.GONE
        binding.dimensionsLayout.visibility = View.GONE
        binding.controlLayout.controlLayout.visibility = View.VISIBLE
        binding.opacityProgress.visibility = View.GONE

    }

    private fun shadowClick() {

        //setting visibility of views
        binding.listFonts.visibility = View.INVISIBLE
        binding.colorListRecyclerview.visibility = View.VISIBLE
        binding.dimensionsLayout.visibility = View.GONE
        binding.controlLayout.controlLayout.visibility = View.GONE
        binding.opacityProgress.visibility = View.VISIBLE
        textViewModel.shadowClicked = true
        binding.opacityProgress.progress = 30

        //setting recyclerview of colors
        binding.colorListRecyclerview.layoutManager =
            GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false)
        binding.colorListRecyclerview.setHasFixedSize(false)
        binding.colorListRecyclerview.adapter = colorsAdapter

        //get the colors list to the colorsAdapter
        val listColors = ArrayList<UserColors>()
        for (color in requireActivity().resources.getIntArray(R.array.colorsList)) {
            listColors.add(UserColors(color))
        }
        colorsAdapter.setListColors(listColors)
        Log.i(
            "listColors",
            requireActivity().resources.getIntArray(R.array.colorsList).size.toString()
        )
    }

    private fun brushClick() {

        //setting visibility of views
        binding.listFonts.visibility = View.INVISIBLE
        binding.colorListRecyclerview.visibility = View.VISIBLE
        binding.dimensionsLayout.visibility = View.GONE
        binding.controlLayout.controlLayout.visibility = View.GONE
        binding.opacityProgress.visibility = View.VISIBLE
        textViewModel.shadowClicked = false

        //setting recyclerview of colors
        binding.colorListRecyclerview.layoutManager = GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false)
        binding.colorListRecyclerview.setHasFixedSize(false)
        binding.colorListRecyclerview.adapter = colorsAdapter

        //get the colors list to the colorsAdapter
        val listColors = ArrayList<UserColors>()
        for (color in requireActivity().resources.getIntArray(R.array.colorsList)) {
            listColors.add(UserColors(color))
        }
        colorsAdapter.setListColors(listColors)
        Log.i(
            "listColors",
            requireActivity().resources.getIntArray(R.array.colorsList).size.toString()
        )
    }

    override fun tabClicked(tab: TabItem, position: Int) {

        //clicked image from the background 'graphics' list
        tabsAdapter.changeSelectionStatus(position)
        binding.listTabs.scrollToPosition(position)

        //after getting the clicked item check which item is it! and show the data accordingly
        when (tab.tabName) {
            "Fonts" -> {

                fontsClick()
            }

            "Color" -> {

                colorClick()
            }

            "3D" -> {
                d3Click()
            }

            "Controls" -> {

                controlsClick()
            }

            "Shadow" -> {

                shadowClick()
            }

            "Brush" -> {

                brushClick()
            }
        }
    }

    override fun colorClicked(color: Int, position: Int) {

        colorsAdapter.changeSelectionStatus(position)
        binding.colorListRecyclerview.scrollToPosition(position)

        if (textViewModel.shadowClicked) {
            textViewModel.shadowColor = color
            textViewModel.shadowRadius = binding.opacityProgress.progress

            textFragmentInterface.shadowColorAndIntensityForTheStickerTextTextFragment(textViewModel.shadowColor, binding.opacityProgress.progress)

        }
        else {
            textViewModel.selectedColor = color
            textFragmentInterface.colorListItemClickApplyOnTextOfStickerTextFragment(textViewModel.selectedColor)
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun fontItemClicked(item: FontItem, position: Int) {

        //got the selected font and apply to the TextSticker
        textViewModel.selectedFontItem = item

        textFragmentInterface.fontListItemClickApplyOnTextOfStickerTextFragment(item)
    }
}