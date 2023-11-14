package com.esport.logo.maker.unlimited.main.edit_create_logo.features.background

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.databinding.FragmentBackgroundBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.ViewModelMain
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.TabItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.UserColors
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters.BackgroundListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters.ColorListAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters.GradientListOneAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters.GradientListTwoAdapter
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.adapters.TabsAdapterBackground
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.BackgroundFragmentInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackgroundFragment(var backgroundFragmentInterface: BackgroundFragmentInterface) : Fragment(),
    BackgroundListAdapter.ItemClickedImages,
    TabsAdapterBackground.ItemClickedTabs,
    ColorListAdapter.ColorClickedEvent,
    GradientListOneAdapter.GradientColor1ClickedEvent,
    GradientListTwoAdapter.GradientColor2ClickedEvent,
    BackgroundListAdapter.CloseButtonClicked{

    private lateinit var binding: FragmentBackgroundBinding
    private lateinit var adapter: BackgroundListAdapter
    private lateinit var tabsAdapter: TabsAdapterBackground
    private lateinit var colorsAdapter: ColorListAdapter
    private lateinit var gradientColorAdapter1: GradientListOneAdapter
    private lateinit var gradientColorAdapter2: GradientListTwoAdapter
    private val viewModelMain: ViewModelMain by activityViewModels()
    private lateinit var tabs: ArrayList<TabItem>

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentBackgroundBinding.inflate(inflater, container, false)

        //initialization
        adapter = BackgroundListAdapter(this,this)
        tabsAdapter = TabsAdapterBackground(requireContext(), this)
        colorsAdapter = ColorListAdapter(this)
        gradientColorAdapter1 = GradientListOneAdapter(this)
        gradientColorAdapter2 = GradientListTwoAdapter(this)

        adapter.changeSelectionStatus(adapter.selectedItemPosition)
        binding.listLogos.smoothScrollToPosition(adapter.selectedItemPosition)

        //setting the tabs list adapter
        tabs = ArrayList()
        tabs.add(TabItem("Graphics", isSelected = true))
        tabs.add(TabItem("Texture"))
        tabs.add(TabItem("Color"))
        tabs.add(TabItem("Shapes"))
        tabs.add(TabItem("Gradient"))
        tabsAdapter.setTabsNames(tabs)

        //setting the recyclerview for tabs
        binding.listTabs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.listTabs.setHasFixedSize(false)
        binding.listTabs.adapter = tabsAdapter

        binding.progressBarAnim.visibility = View.VISIBLE
        binding.progressBarAnim.playAnimation()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //default selection of items as tabs
        graphicsClick()

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
                // Calculate alpha
                val alpha = progress.toFloat() / 100.0f
                backgroundFragmentInterface.opacityLogoBackgroundFragment(alpha)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //gradient radio buttons
        binding.gradientBottomview.linearButton.setOnClickListener {

            binding.gradientBottomview.linearButton.isChecked = true
            binding.gradientBottomview.radicalButton.isChecked = false
            binding.gradientBottomview.angularButton.isChecked = false

            //apply linear gradient to the card
            //remember radical choice
            viewModelMain.selectedGradientType = "linear"
            viewModelMain.gradientSelected = true
            viewModelMain.colorWasSelected = false
            viewModelMain.selectedGradientType = "linear"
        }

        binding.gradientBottomview.radicalButton.setOnClickListener {

            binding.gradientBottomview.linearButton.isChecked = false
            binding.gradientBottomview.radicalButton.isChecked = true
            binding.gradientBottomview.angularButton.isChecked = false

            //remember radical choice
            viewModelMain.selectedGradientType = "radical"
            viewModelMain.gradientSelected = true
            viewModelMain.colorWasSelected = false
            viewModelMain.selectedGradientType = "radical"
        }

        binding.gradientBottomview.angularButton.setOnClickListener {

            binding.gradientBottomview.linearButton.isChecked = false
            binding.gradientBottomview.radicalButton.isChecked = false
            binding.gradientBottomview.angularButton.isChecked = true

            //apply angular gradient to the card
            //remember radical choice
            viewModelMain.selectedGradientType = "angular"
            viewModelMain.gradientSelected = true
            viewModelMain.colorWasSelected = false
            viewModelMain.selectedGradientType = "angular"
        }

    }

    //graphics button clicked (done)
    @SuppressLint("NotifyDataSetChanged")
    private fun graphicsClick() {

        //setting visibility of views
        binding.listLogos.visibility = View.VISIBLE
        binding.opacityProgress.visibility = View.GONE
        binding.opacityIcon.visibility = View.GONE
        binding.colorListRecyclerview.visibility = View.GONE
        binding.gradientBottomview.gradientBottomview.visibility = View.GONE
        binding.colorOtherLayout.visibility = View.GONE


        ///graphics is selected
        viewModelMain.graphicsTabSelected = true
        viewModelMain.textureTabSelected = false
        viewModelMain.colorTabSelected = false
        viewModelMain.shapesTabSelected = false
        viewModelMain.gradientTabSelected = false

        //setting the recyclerview for logo and templates
        binding.listLogos.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.listLogos.setHasFixedSize(false)
        binding.listLogos.adapter = adapter

        //setting data to the adapter of logos list {after checking that user has clicked template or create logo button}

        if (viewModelMain.templateEnabled){

            viewModelMain.templateListLiveData().observe(viewLifecycleOwner){ templateList ->

                if (templateList != null) {

                    binding.listLogos.visibility = View.VISIBLE
                    binding.noDataAvailable.visibility = View.GONE
                    binding.progressBarAnim.visibility = View.GONE
                    binding.progressBarAnim.pauseAnimation()

                    //set the list to the adapter
                    adapter.setImagesList("templates", templateList)
                    adapter.notifyDataSetChanged()
                } else {
                    //empty views
                    binding.noDataAvailable.visibility = View.VISIBLE
                    binding.progressBarAnim.visibility = View.GONE
                    binding.progressBarAnim.pauseAnimation()

                }
            }
        }
        else{

            viewModelMain.graphicListLiveData().observe(viewLifecycleOwner) { graphicsImages ->

                if (graphicsImages != null) {

                    binding.listLogos.visibility = View.VISIBLE
                    binding.noDataAvailable.visibility = View.GONE
                    binding.progressBarAnim.visibility = View.GONE
                    binding.progressBarAnim.pauseAnimation()

                    //set the list to the adapter
                    adapter.setImagesList("graphics", graphicsImages)
                    adapter.notifyDataSetChanged()
                } else {
                    //empty views
                    binding.noDataAvailable.visibility = View.VISIBLE
                    binding.progressBarAnim.visibility = View.GONE
                    binding.progressBarAnim.pauseAnimation()

                }
            }
        }
        //for view in activity to be handled
        backgroundFragmentInterface.clickOnGraphicsItemBackgroundFragment()
    }

    //texture button clicked (done)
    @SuppressLint("NotifyDataSetChanged")
    private fun textureClick() {

        //setting visibility of views
        binding.listLogos.visibility = View.VISIBLE
        binding.opacityProgress.visibility = View.GONE
        binding.opacityIcon.visibility = View.GONE
        binding.colorListRecyclerview.visibility = View.GONE

        binding.gradientBottomview.gradientBottomview.visibility = View.GONE
        binding.colorOtherLayout.visibility = View.GONE


        ///texture is selected
        viewModelMain.graphicsTabSelected = false
        viewModelMain.textureTabSelected = true
        viewModelMain.colorTabSelected = false
        viewModelMain.shapesTabSelected = false
        viewModelMain.gradientTabSelected = false

        //setting the recyclerview for logo and templates
        binding.listLogos.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)

        binding.listLogos.setHasFixedSize(false)
        binding.listLogos.adapter = adapter

        //setting data to the adapter of logos list
        viewModelMain.textureListLiveData().observe(viewLifecycleOwner) { texturesImages ->

            if (texturesImages != null) {

                binding.listLogos.visibility = View.VISIBLE
                binding.noDataAvailable.visibility = View.GONE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //set the list to the adapter
                adapter.setImagesList("texture", texturesImages)
                adapter.notifyDataSetChanged()
            } else {
                //empty views
                binding.noDataAvailable.visibility = View.VISIBLE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

            }

        }

        //for texture click event
        backgroundFragmentInterface.clickOnTextureItemBackgroundFragment()
    }

    //color button clicked (in progress)
    private fun colorClick() {

        //making progressbar for opacity and color pallet visible
        binding.listLogos.visibility = View.INVISIBLE
        binding.opacityProgress.visibility = View.VISIBLE
        binding.opacityIcon.visibility = View.VISIBLE
        binding.colorListRecyclerview.visibility = View.VISIBLE
        binding.gradientBottomview.gradientBottomview.visibility = View.GONE
        binding.colorOtherLayout.visibility = View.VISIBLE

        binding.colorOtherLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.Black))


        ///color is selected
        viewModelMain.graphicsTabSelected = false
        viewModelMain.textureTabSelected = false
        viewModelMain.colorTabSelected = true
        viewModelMain.shapesTabSelected = false
        viewModelMain.gradientTabSelected = false

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

        //views in activity
        backgroundFragmentInterface.clickOnColorsItemBackgroundFragment()
    }

    //shapes button clicked (done)
    @SuppressLint("NotifyDataSetChanged")
    private fun shapesClick() {

        //setting visibility of views
        binding.listLogos.visibility = View.INVISIBLE
        binding.gradientBottomview.gradientBottomview.visibility = View.GONE
        binding.colorOtherLayout.visibility = View.GONE

        ///shapes is selected
        viewModelMain.graphicsTabSelected = false
        viewModelMain.textureTabSelected = false
        viewModelMain.colorTabSelected = false
        viewModelMain.shapesTabSelected = true
        viewModelMain.gradientTabSelected = false

        //setting the recyclerview for logo and templates
        binding.listLogos.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.listLogos.setHasFixedSize(false)
        binding.listLogos.adapter = adapter

        //setting data to the adapter of logos list
        viewModelMain.shapeListLiveData().observe(viewLifecycleOwner) { shapesImages ->

            if (shapesImages != null) {

                binding.listLogos.visibility = View.VISIBLE
                binding.noDataAvailable.visibility = View.GONE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

                //set the list to the adapter
                adapter.setImagesList("shapes", shapesImages)
                adapter.notifyDataSetChanged()
            } else {
                //empty views
                binding.noDataAvailable.visibility = View.VISIBLE
                binding.progressBarAnim.visibility = View.GONE
                binding.progressBarAnim.pauseAnimation()

            }

        }

        //activity views
        backgroundFragmentInterface.clickOnShapesItemBackgroundFragment()
    }

    //gradient button clicked
    private fun gradientClick() {

        //make gradient view visible
        binding.listLogos.visibility = View.INVISIBLE
        binding.opacityProgress.visibility = View.VISIBLE
        binding.opacityIcon.visibility = View.VISIBLE
        binding.colorListRecyclerview.visibility = View.INVISIBLE
        binding.colorOtherLayout.visibility = View.VISIBLE
        binding.gradientBottomview.gradientBottomview.visibility = View.VISIBLE

        binding.colorOtherLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.Black))

        ///graphics is selected
        viewModelMain.graphicsTabSelected = false
        viewModelMain.textureTabSelected = false
        viewModelMain.colorTabSelected = false
        viewModelMain.shapesTabSelected = false
        viewModelMain.gradientTabSelected = true

        //default selection
        binding.gradientBottomview.radicalButton.isChecked = true
        binding.gradientBottomview.linearButton.isChecked = false
        binding.gradientBottomview.radicalButton.isChecked = true
        binding.gradientBottomview.angularButton.isChecked = false

        //remember radical choice
        viewModelMain.selectedGradientType = "radical"
        viewModelMain.gradientSelected = true


        //get the colors list to the colorsAdapter
        val listColors = ArrayList<UserColors>()
        for (color in requireActivity().resources.getIntArray(R.array.colorsList)) {
            listColors.add(UserColors(color))
        }

        Log.i("listColors", requireActivity().resources.getIntArray(R.array.colorsList).size.toString())

        //splitting the array into 2 from mid
        val firstHalfList = listColors.subList(0, listColors.size / 2)
        val secondHalfList = listColors.subList(listColors.size / 2, listColors.size)

        //setting gradient recyclerview visible and setting data of colors to it (Gradient color list 1)
        binding.gradientBottomview.color1GradientListRecyclerview.layoutManager =
            GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false)

        binding.gradientBottomview.color1GradientListRecyclerview.setHasFixedSize(false)
        binding.gradientBottomview.color1GradientListRecyclerview.adapter = gradientColorAdapter1

        gradientColorAdapter1.setListColors(firstHalfList)
        Log.i(
            "listColors",
            requireActivity().resources.getIntArray(R.array.colorsList).size.toString()
        )

        //setting gradient recyclerview visible and setting data of colors to it (Gradient color list 2)
        binding.gradientBottomview.color2GradientListRecyclerview.layoutManager =
            GridLayoutManager(requireContext(), 5, GridLayoutManager.HORIZONTAL, false)

        binding.gradientBottomview.color2GradientListRecyclerview.setHasFixedSize(false)
        binding.gradientBottomview.color2GradientListRecyclerview.adapter = gradientColorAdapter2

        gradientColorAdapter2.setListColors(secondHalfList)

        //activity views
        backgroundFragmentInterface.clickOnGradientButtonBackgroundFragment()
    }

    //image item click
    override fun itemClicked(item: Image, position: Int, listName: String) {

        //clicked image from the background 'graphics' list
        adapter.changeSelectionStatus(position)
        binding.listLogos.scrollToPosition(position)

        //list type
        viewModelMain.listName = listName
        //item id of clicked item
        viewModelMain.imageItem = item

        backgroundFragmentInterface.clickOnImageItemForBackgroundLogoBackgroundFragment(item, listName)
    }

    //tab item clicked
    override fun tabClicked(tab: TabItem, position: Int) {

        //clicked image from the background 'graphics' list
        tabsAdapter.changeSelectionStatus(position)
        binding.listTabs.scrollToPosition(position)

        //after getting the clicked item check which item is it! and show the data accordingly
        when (tab.tabName) {
            "Graphics" -> {

                graphicsClick()
            }

            "Texture" -> {

                textureClick()
            }

            "Color" -> {
                colorClick()
            }

            "Shapes" -> {

                shapesClick()
            }

            "Gradient" -> {

                gradientClick()
            }
        }
    }

    //color item clicked
    override fun colorClicked(color: Int, position: Int) {

        colorsAdapter.changeSelectionStatus(position)
        binding.colorListRecyclerview.scrollToPosition(position)

        viewModelMain.colorItem = color
        backgroundFragmentInterface.clickOnColorItemForBackgroundLogoBackgroundFragment(color)
        viewModelMain.gradientSelected = false
    }

    override fun gradientColor1Clicked(gradient1Color: Int, position: Int) {

        gradientColorAdapter1.changeSelectionStatus(position)
        binding.gradientBottomview.color1GradientListRecyclerview.scrollToPosition(position)

        viewModelMain.gradientColor1 = gradient1Color
        backgroundFragmentInterface.clickOnGradient1ItemForBackgroundLogoBackgroundFragment(
            gradient1Color
        )
    }

    override fun gradientColor2Clicked(gradient2Color: Int, position: Int) {

        gradientColorAdapter2.changeSelectionStatus(position)
        binding.gradientBottomview.color2GradientListRecyclerview.scrollToPosition(position)

        viewModelMain.gradientColor2 = gradient2Color
        backgroundFragmentInterface.clickOnGradient2ItemForBackgroundLogoBackgroundFragment(gradient2Color)
    }

    override fun closeButtonClicked(listName: String) {
        //clicked image from the background 'graphics' list
        adapter.changeSelectionStatus(-1)
        binding.listLogos.scrollToPosition(0)

        backgroundFragmentInterface.clickOnRemoveButtonBackgroundItemListBackgroundFragment(listName)
    }

}