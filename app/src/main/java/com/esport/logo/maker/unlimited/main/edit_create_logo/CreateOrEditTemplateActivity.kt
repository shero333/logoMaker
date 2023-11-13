package com.esport.logo.maker.unlimited.main.edit_create_logo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinSdk
import com.esport.logo.maker.unlimited.BuildConfig
import com.esport.logo.maker.unlimited.R
import com.esport.logo.maker.unlimited.application.LogoMakerApp
import com.esport.logo.maker.unlimited.databinding.ActivityCreateOrEditTemplateBinding
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.BottomNavItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.FontItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.SavedLogo
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.StickerBackgroundState
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.helper.UndoRedoStack
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.background.BackgroundFragment
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.effects.EffectsFragment
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.elements.ElementsFragment
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.logo.LogoFragment
import com.esport.logo.maker.unlimited.main.edit_create_logo.features.text.TextFragment
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.BackgroundFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.EffectsFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.ElementsFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.LogosFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.interface_click_events.TextFragmentInterface
import com.esport.logo.maker.unlimited.main.edit_create_logo.utils.Utils
import com.esport.logo.maker.unlimited.main.recent_work.fragments.PreviewFragment
import com.esport.logo.maker.unlimited.utils.MainUtils
import com.esport.logo.maker.unlimited.utils.OnShowAdCompleteListener
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.card.MaterialCardView
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.xiaopo.flying.sticker.BitmapStickerIcon
import com.xiaopo.flying.sticker.DeleteIconEvent
import com.xiaopo.flying.sticker.RotateIconEvent
import com.xiaopo.flying.sticker.Sticker
import com.xiaopo.flying.sticker.Sticker.Position
import com.xiaopo.flying.sticker.StickerView
import com.xiaopo.flying.sticker.ZoomIconEvent
import com.xiaopo.flying.sticker.stickers.ElementSticker
import com.xiaopo.flying.sticker.stickers.ImageSticker
import com.xiaopo.flying.sticker.stickers.LogoSticker
import com.xiaopo.flying.sticker.stickers.TextSticker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CreateOrEditTemplateActivity : AppCompatActivity(),
    BackgroundFragmentInterface, EffectsFragmentInterface,
    ElementsFragmentInterface, LogosFragmentInterface,
    TextFragmentInterface, PreviewFragment.DeleteItemInPreview,
    MaxAdListener, MaxAdViewAdListener {

    private var downloadClicked = false
    private var exitClicked = false
    lateinit var binding: ActivityCreateOrEditTemplateBinding
    private lateinit var viewModelMain: ViewModelMain
    private lateinit var bottomNavList: ArrayList<BottomNavItem>
    private lateinit var dialog: Dialog
    private lateinit var dialogExit: Dialog
    private var REQUEST_CODE_STORAGE = 3
    private var stickerText = ""
    private lateinit var backgroundFragment: BackgroundFragment
    private lateinit var textFragment: TextFragment
    private lateinit var effectsFragment: EffectsFragment
    private lateinit var logoFragment: LogoFragment
    private lateinit var elementsFragment: ElementsFragment
    private var editTheTextSticker = false

    //tabs which are selected
    private var backgroundTab: Boolean = false
    private var textTab: Boolean = false
    private var effectsTab: Boolean = false
    private var logoTab: Boolean = false
    private var elementsTab: Boolean = false
    private var backFragment: Fragment? = null
    private var backgroundItemSelected = false

    //Ads
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAd: MaxInterstitialAd? = null
    private lateinit var adViewTop: MaxAdView
    private lateinit var adViewBottom: MaxAdView
    private var textureSelected = false
    private var graphicsSelected = false

    //undo Redo
    private var undoRedoTraversal = 0

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initializing the viewModel and setting the status bar color
        viewModelMain = ViewModelProvider(this)[ViewModelMain::class.java]
        binding = ActivityCreateOrEditTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the custom ActionBar as the support ActionBar
        setSupportActionBar(binding.fragmentToolbar)

        if (supportActionBar != null)
            supportActionBar!!.title = ""

        //Initializing Google Ads
        MobileAds.initialize(this)
        AppLovinSdk.initializeSdk(this)

        //Initializing AppLovin Ads
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.initializeSdk(this)

        adRequest = AdRequest.Builder().build()

        //AppLovin
        interstitialAd = MaxInterstitialAd(resources.getString(R.string.interstitialAd), this)
        interstitialAd!!.setListener(this)

        //Creating banner for AppLovin
        BannerAdAppLovinTop()
        BannerAdAppLovinBottom()

        //top Ad
        when (LogoMakerApp.CREATE_LOGO_SCREEN_BANNER_TOP) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner2.visibility = View.GONE
                binding.applovinAdView2.visibility = View.GONE

            }
            "1" -> {

                //Admob Ad will be loaded
                binding.adaptiveBanner2.visibility = View.VISIBLE
                binding.applovinAdView2.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner2.visibility = View.INVISIBLE
                binding.applovinAdView2.visibility = View.VISIBLE

                adViewTop.loadAd()
            }
        }

        //bottom Ad
        when (LogoMakerApp.CREATE_LOGO_SCREEN_BANNER_BOTTOM) {
            "0" -> {

                //no ad will be loaded
                binding.adaptiveBanner1.visibility = View.GONE
                binding.applovinAdView1.visibility = View.GONE

            }
            "1" -> {

                //Admob Ad will be loaded
                binding.adaptiveBanner1.visibility = View.VISIBLE
                binding.applovinAdView1.visibility = View.INVISIBLE

                //google banner ad load
                //bottom
                Banner1Ads()
                //top
                Banner2Ads()

            }
            "2" -> {

                //load AppLovin Banner Ads
                binding.adaptiveBanner1.visibility = View.INVISIBLE
                binding.applovinAdView1.visibility = View.VISIBLE

                adViewBottom.loadAd()
            }
        }

        //loading interstitial ads
        setAd()
        interstitialAd!!.loadAd()

        //Exit dialog
        dialogExit = Dialog(this)
        dialogExit.setContentView(R.layout.exit_dialog_layout)
        dialogExit.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogExit.setCancelable(false)

        //fragments objects
        backgroundFragment = BackgroundFragment(this)
        textFragment = TextFragment(this)
        effectsFragment = EffectsFragment(this)
        logoFragment = LogoFragment(this)
        elementsFragment = ElementsFragment(this)

        //redo button click
        binding.redoButton.setOnClickListener {


            if (undoRedoTraversal < viewModelMain.undoRedoArray.lastIndex){

                //updating index
                undoRedoTraversal++

                //restore the previous state of the stickers
                if(viewModelMain.undoRedoArray[undoRedoTraversal].background != null){

                    binding.imageFromBackground.setImageBitmap(viewModelMain.undoRedoArray[undoRedoTraversal].background)
                }
                else if (viewModelMain.undoRedoArray[undoRedoTraversal].sticker != null) {

                    viewModelMain.undoRedoArray[undoRedoTraversal].sticker?.let { sticker ->

                        //add sticker according to the position matrix

                        when (sticker) {
                            is LogoSticker -> {
                                //setting the value to the image
                                binding.stickerView.addSticker(LogoSticker(sticker.drawable))
                                binding.stickerView.matrix = sticker.matrix
                            }

                            is ElementSticker -> {
                                //setting the value to the image
                                binding.stickerView.addSticker(ElementSticker(sticker.drawable))
                                binding.stickerView.matrix = sticker.matrix
                            }

                            is TextSticker -> {
                                //setting the value to the image
                                val textSticker = TextSticker(this)
                                if (viewModelMain.selectedColor != 0) {

                                    if (viewModelMain.selectedFontItem.typeface != null) {

                                        textSticker.text = sticker.text
                                        textSticker.textColor = sticker.textColor
                                        textSticker.setTypeface(viewModelMain.selectedFontItem.typeface)
                                        textSticker.setTextAlign(viewModelMain.alignment)
                                    } else {
                                        textSticker.text = sticker.text
                                        textSticker.textColor = sticker.textColor
                                        textSticker.setTextAlign(viewModelMain.alignment)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(this,"Nothing to redo!",Toast.LENGTH_SHORT).show()
            }
        }
        //undo button click
        binding.undoButton.setOnClickListener {

            //updating index


            if (undoRedoTraversal > 0){
                undoRedoTraversal--
                //restore the previous state of the stickers
                if(viewModelMain.undoRedoArray[undoRedoTraversal].background != null){

                    binding.imageFromBackground.setImageBitmap(viewModelMain.undoRedoArray[undoRedoTraversal].background)
                }
                else if (viewModelMain.undoRedoArray[undoRedoTraversal].sticker != null){

                    binding.stickerView.remove(viewModelMain.undoRedoArray[undoRedoTraversal].sticker)
                }
            }
            else{
                Toast.makeText(this,"Nothing to undo!",Toast.LENGTH_SHORT).show()
            }
        }

        setupStickerView()

        //setting status bar
        MainUtils.statusBarColor(this@CreateOrEditTemplateActivity)
        bottomNavList = ArrayList()

        //loading working image
        if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

            val bitmapShaped = Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied)
            binding.imageFromBackground.setImageBitmap(bitmapShaped)

            //setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapShaped,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        //finishing the screen
        binding.backButton.setOnClickListener {
            //finish the activity
            binding.colorPickerButton.visibility = View.INVISIBLE

            if (binding.stickerTextEdt.isVisible) {
                binding.stickerTextEdt.visibility = View.GONE
                binding.saveTextButton.visibility = View.GONE

                // Hide the keyboard
                val inputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.stickerTextEdt.windowToken, 0)
            } else {
                if (backFragment is BackgroundFragment) {
                    exitDialog()
                } else {
                    //replace {background} fragment
                    //setting view of selected item
                    binding.backgroundIcon.setImageResource(R.drawable.background_selected)
                    binding.backgroundName.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.theme_color
                        )
                    )

                    //normalizing views
                    normalizeOtherViews("background")
                    MainUtils.replaceFragment(
                        backgroundFragment,
                        supportFragmentManager,
                        R.id.container_fragment_activity_create_or_edit_template
                    )
                    backFragment = backgroundFragment
                }
            }
        }

        //preparing list for bottom navigation
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.background), "Background"))
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.text), "Text"))
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.image), "Image"))
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.effects), "Effects"))
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.logo), "Logo"))
        bottomNavList.add(BottomNavItem(ContextCompat.getDrawable(this, R.drawable.elements), "Elements"))

        //initializing the dialog
        dialog = Dialog(this)
        dialog.setContentView(R.layout.image_upload_dialog_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        //getting data according to the intent value
        if (intent != null) {

            val calledFrom = intent.getStringExtra("logosList")
            if (calledFrom == "createLogo") {

                viewModelMain.templateEnabled = false
                //get data for create logo flow
                //loading data for create logo activity
                lifecycleScope.launch(Dispatchers.Default) {

                    //fetching data for the logo making fragments {create logo portion}
                    viewModelMain.graphicsList()
                    viewModelMain.texturesList()
                    viewModelMain.shapesList()
                    viewModelMain.fontsList()
                    viewModelMain.effectsList()
                    viewModelMain.logosList()
                    viewModelMain.elementsList()
                }
            } else if (calledFrom == "editTemplate") {

                viewModelMain.templateEnabled = true
                //get data from template flow
                //loading data for template logo activity
                lifecycleScope.launch(Dispatchers.Default) {

                    //fetching data for the logo making fragments {edit template portion}
                    viewModelMain.graphicsList()
                    viewModelMain.texturesList()
                    viewModelMain.shapesList()
                    viewModelMain.fontsList()
                    viewModelMain.effectsList()
                    viewModelMain.logosList()
                    viewModelMain.elementsList()
                    viewModelMain.templatesList()
                }
            }
        }

        //default selection {by default }
        defaultSelectionOfItem()

        //handling the bottom nav items manually
        //background
        binding.backgroundIcon.setOnClickListener {

            //setting view of selected item
            binding.backgroundIcon.setImageResource(R.drawable.background_selected)
            binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("background")

            //replace {background} fragment
            MainUtils.replaceFragment(
                backgroundFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )
            backFragment = backgroundFragment

            //making edittext invisible if it is visible
            if (binding.stickerTextEdt.isVisible) {
                binding.stickerTextEdt.visibility = View.GONE
                binding.saveTextButton.visibility = View.GONE
            }
        }
        //text
        binding.textIcon.setOnClickListener {

            //setting view
            binding.textIcon.setImageResource(R.drawable.text_selected)
            binding.textName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))

            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("text")

            //setting the view of the activity
            binding.saveTextButton.visibility = View.VISIBLE
            binding.stickerTextEdt.visibility = View.VISIBLE
            //making download button invisible
            binding.downloadButton.visibility = View.INVISIBLE
            //making undo and redo buttons invisible
            binding.forwardBackwardLayout.visibility = View.INVISIBLE

            //removing the text st previously
            binding.stickerTextEdt.setText("")

            //replace {text} fragment
            textFragment = TextFragment(this)
            MainUtils.replaceFragment(
                textFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )
            backFragment = textFragment

            binding.colorPickerButton.visibility = View.INVISIBLE

            //showing the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(
                binding.stickerTextEdt,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
        //image
        binding.imageIcon.setOnClickListener {

            //setting view
            binding.imageIcon.setImageResource(R.drawable.image_selected)
            binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("image")

            //dialog to upload the image as sticker or background!
            dialogToUploadAnImageAndContinue()

            binding.colorPickerButton.visibility = View.INVISIBLE
            //making undo and redo buttons invisible
            binding.forwardBackwardLayout.visibility = View.VISIBLE
        }
        //effects
        binding.effectsIcon.setOnClickListener {

            //setting view
            binding.effectsIcon.setImageResource(R.drawable.effects_selected)
            binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))

            //setting background if the background is applied or not

            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("effects")

            //replace {effects} fragment
            effectsFragment = EffectsFragment(this)
            MainUtils.replaceFragment(
                effectsFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )
            backFragment = effectsFragment

            //making edittext invisible if it is visible
            if (binding.stickerTextEdt.isVisible) {
                binding.stickerTextEdt.visibility = View.GONE
                binding.saveTextButton.visibility = View.GONE
            }

            binding.colorPickerButton.visibility = View.INVISIBLE
            //making undo and redo buttons invisible
            binding.forwardBackwardLayout.visibility = View.VISIBLE
        }
        //logo
        binding.logoIcon.setOnClickListener {

            //setting view
            binding.logoIcon.setImageResource(R.drawable.logo_selected)
            binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))

            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("logo")

            //replace {logo} fragment
            logoFragment = LogoFragment(this)
            MainUtils.replaceFragment(
                logoFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )
            backFragment = logoFragment

            //making edittext invisible if it is visible
            if (binding.stickerTextEdt.isVisible) {
                binding.stickerTextEdt.visibility = View.GONE
                binding.saveTextButton.visibility = View.GONE
            }

            binding.colorPickerButton.visibility = View.INVISIBLE
            //making undo and redo buttons invisible
            binding.forwardBackwardLayout.visibility = View.VISIBLE
        }
        //elements
        binding.elementsIcon.setOnClickListener {

            //setting view
            binding.elementsIcon.setImageResource(R.drawable.element_selected)
            binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))

            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("elements")

            //replace {element} fragment
            elementsFragment = ElementsFragment(this)
            MainUtils.replaceFragment(
                elementsFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )
            backFragment = elementsFragment

            //making edittext invisible if it is visible
            if (binding.stickerTextEdt.isVisible) {
                binding.stickerTextEdt.visibility = View.GONE
                binding.saveTextButton.visibility = View.GONE
            }

            binding.colorPickerButton.visibility = View.INVISIBLE
            //making undo and redo buttons invisible
            binding.forwardBackwardLayout.visibility = View.VISIBLE
        }

        //de-selection of the layout
        binding.imageFromBackground.setOnClickListener {
            //deselected
            if (binding.stickerView.isSelected) {
                binding.stickerView.isSelected = false
                binding.stickerView.showBorder(false)
                binding.stickerView.showIcons(false)
            }
        }

        binding.stickerView.setOnClickListener {
            //deselected
            if (binding.stickerView.isSelected) {
                binding.stickerView.isSelected = false
                binding.stickerView.showBorder(false)
                binding.stickerView.showIcons(false)
            }
        }
        //handling the selection of the stickerView
        binding.stickerView.onStickerOperationListener = object : StickerView.OnStickerOperationListener {

                override fun onStickerAdded(sticker: Sticker) {}

                override fun onStickerClicked(sticker: Sticker) {

                    if (binding.stickerView.isSelected) {

                        //type of selected sticker
                        when (sticker) {
                            is TextSticker -> {
                                //setting view
                                binding.textIcon.setImageResource(R.drawable.text_selected)
                                binding.textName.setTextColor(
                                    ContextCompat.getColor(
                                        this@CreateOrEditTemplateActivity,
                                        R.color.theme_color
                                    )
                                )
                                //redefining view for other items so that if there is other item selected then the previous one should be unselected
                                normalizeOtherViews("text")

                                //setting text of the clicked sticker
                                stickerText = sticker.text!!

                                //replace {text} fragment
                                MainUtils.replaceFragment(
                                    textFragment,
                                    supportFragmentManager,
                                    R.id.container_fragment_activity_create_or_edit_template
                                )
                            }

                            is LogoSticker -> {

                                //setting view
                                binding.logoIcon.setImageResource(R.drawable.logo_selected)
                                binding.logoName.setTextColor(
                                    ContextCompat.getColor(
                                        this@CreateOrEditTemplateActivity,
                                        R.color.theme_color
                                    )
                                )
                                //redefining view for other items so that if there is other item selected then the previous one should be unselected
                                normalizeOtherViews("logo")

                                //replace {logo} fragment
                                MainUtils.replaceFragment(
                                    logoFragment,
                                    supportFragmentManager,
                                    R.id.container_fragment_activity_create_or_edit_template
                                )
                            }

                            is ElementSticker -> {

                                //setting view
                                binding.elementsIcon.setImageResource(R.drawable.element_selected)
                                binding.elementsName.setTextColor(
                                    ContextCompat.getColor(
                                        this@CreateOrEditTemplateActivity,
                                        R.color.theme_color
                                    )
                                )
                                //redefining view for other items so that if there is other item selected then the previous one should be unselected
                                normalizeOtherViews("elements")

                                //replace {element} fragment
                                MainUtils.replaceFragment(
                                    elementsFragment,
                                    supportFragmentManager,
                                    R.id.container_fragment_activity_create_or_edit_template
                                )
                            }
                        }
                    } else {
                        binding.stickerView.isSelected = true
                        binding.stickerView.showBorder(true)
                        binding.stickerView.showIcons(true)
                    }
                }

                override fun onStickerDeleted(sticker: Sticker) {}

                override fun onStickerDragFinished(sticker: Sticker) {}

                override fun onStickerTouchedDown(sticker: Sticker) {}

                override fun onStickerZoomFinished(sticker: Sticker) {}

                override fun onStickerFlipped(sticker: Sticker) {}

                override fun onStickerDoubleTapped(sticker: Sticker) {}
            }

        //color picker button click
        binding.colorPickerButton.setOnClickListener {
            //showing user the colorPicker
            ColorPickerDialogBuilder
                .with(this, R.style.ColorPickerDialogStyle)
                .setTitle("Select desired color")
                .initialColor(Color.YELLOW)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showColorPreview(true)
                .setPositiveButton("Select") { dialog: DialogInterface?, selectedColor: Int, c: Array<Int?>? ->

                    //apply the selected color to the image
                    //checking if the user has selected the gradient color or not

                    //applying gradient
                    viewModelMain.gradientColor1 = selectedColor

                    binding.stickerView.visibility = View.VISIBLE
                    //making download button visible
                    binding.downloadButton.visibility = View.VISIBLE
                    //making undo and redo buttons invisible
                    binding.forwardBackwardLayout.visibility = View.VISIBLE
                    binding.undoButton.visibility = View.VISIBLE
                    binding.redoButton.visibility = View.VISIBLE

                    //saving values
                    viewModelMain.selectedColor = selectedColor
                    viewModelMain.colorWasSelected = true
                    viewModelMain.gradientApplied = false

                    //on color item clicked is applied on the color card
                    if (viewModelMain.selectedImageShapeApplied.isNotEmpty())
                        applyColorToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                            viewModelMain.selectedColor,
                            viewModelMain.selectedShapeToAdd!!
                        )
                    else
                        applyColorToTheShapedBitmap(
                            Utils.stringToBitmap(
                                Utils.drawableToString(
                                    ContextCompat.getDrawable(
                                        this,
                                        R.drawable.bg1
                                    )
                                )
                            ),
                            viewModelMain.selectedColor, viewModelMain.selectedShapeToAdd!!)

                    dialog!!.dismiss()
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface?, w: Int ->
                    dialog!!.dismiss()
                }
                .build()
                .show()
        }
        binding.downloadButton.setOnClickListener {

            downloadClicked = true

            if (LogoMakerApp.CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL == "0") {

                //save the current sticker to the folder in the hard drive storage
                Utils.convertBitmapToPNGAndSave(
                    this@CreateOrEditTemplateActivity,
                    binding.stickerView
                )
            }
            else if (LogoMakerApp.CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL == "1") {

                //AdMob load
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()

                                //save the current sticker to the folder in the hard drive storage
                                Utils.convertBitmapToPNGAndSave(
                                    this@CreateOrEditTemplateActivity,
                                    binding.stickerView
                                )
                            }
                        }
                } else {
                    //save the current sticker to the folder in the hard drive storage
                    Utils.convertBitmapToPNGAndSave(
                        this@CreateOrEditTemplateActivity,
                        binding.stickerView
                    )
                }
            }
            else if (LogoMakerApp.CREATE_LOGO_SCREEN_DOWNLOAD_BUTTON_PRESS_INTERSTITIAL == "2") {

                //AppLovin load
                if (interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else {
                    //save the current sticker to the folder in the hard drive storage
                    Utils.convertBitmapToPNGAndSave(
                        this@CreateOrEditTemplateActivity,
                        binding.stickerView
                    )
                }
            }
            else {
                //save the current sticker to the folder in the hard drive storage
                Utils.convertBitmapToPNGAndSave(this@CreateOrEditTemplateActivity, binding.stickerView
                )
            }

        }

        //create the sticker
        binding.saveTextButton.setOnClickListener {

            binding.stickerTextEdt.visibility = View.GONE
            binding.saveTextButton.visibility = View.GONE
            binding.colorPickerButton.visibility = View.INVISIBLE

            if (editTheTextSticker) {

                //disabling edit button
                editTheTextSticker = false

                if (binding.stickerTextEdt.text.toString().isNotEmpty()) {
                    stickerText = binding.stickerTextEdt.text.toString()

                    if (viewModelMain.selectedColor != 0) {

                        val sticker = TextSticker(this)
                            .setText(stickerText)
                            .setTextColor(viewModelMain.selectedColor)
                            .setTextAlign(viewModelMain.alignment)
                            .setMaxTextSize(30f)
                            .resizeText()
                        binding.stickerView.replace(sticker)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size

                    } else {

                        val sticker = TextSticker(this)
                            .setText(stickerText)
                            .setTextAlign(viewModelMain.alignment)
                            .setMaxTextSize(30f)
                            .resizeText()
                        binding.stickerView.replace(sticker)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                } else
                    Toast.makeText(
                        this@CreateOrEditTemplateActivity,
                        "No Sticker has added!",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            else {

                if (binding.stickerTextEdt.text.toString().isNotEmpty()) {
                    stickerText = binding.stickerTextEdt.text.toString()

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTextColor(Color.BLACK)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.addSticker(sticker, Position.CENTER)

                    //selecting the added sticker
                    if (!binding.stickerView.isSelected) {
                        binding.stickerView.isSelected = true
                        binding.stickerView.showBorder(true)
                        binding.stickerView.showIcons(true)
                    }

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size

                } else
                    Toast.makeText(
                        this@CreateOrEditTemplateActivity,
                        "No Sticker has added!",
                        Toast.LENGTH_SHORT
                    ).show()
            }

            // Hide the keyboard
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.stickerTextEdt.windowToken, 0)
        }

        //handling back press {default function}
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                //finish the activity
                if (binding.stickerTextEdt.isVisible) {
                    binding.stickerTextEdt.visibility = View.GONE
                    binding.saveTextButton.visibility = View.GONE
                } else {
                    if (backFragment is BackgroundFragment) {
                        exitDialog()
                    } else {
                        //replace {background} fragment
                        //setting view of selected item
                        binding.backgroundIcon.setImageResource(R.drawable.background_selected)
                        binding.backgroundName.setTextColor(
                            ContextCompat.getColor(
                                this@CreateOrEditTemplateActivity,
                                R.color.theme_color
                            )
                        )

                        //normalizing views
                        normalizeOtherViews("background")
                        MainUtils.replaceFragment(
                            backgroundFragment,
                            supportFragmentManager,
                            R.id.container_fragment_activity_create_or_edit_template
                        )
                        backFragment = backgroundFragment
                    }
                }
            }
        })
    }

    private fun setupStickerView() {

        val deleteIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(this, R.drawable.deleting_icon), BitmapStickerIcon.RIGHT_TOP)
        deleteIcon.iconEvent = DeleteIconEvent()

        val zoomIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(this, R.drawable.zooming_icon),
            BitmapStickerIcon.RIGHT_BOTOM
        )
        zoomIcon.iconEvent = ZoomIconEvent()

        val rotationIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this, R.drawable.rotating_icon
            ), BitmapStickerIcon.LEFT_BOTTOM
        )
        rotationIcon.iconEvent = RotateIconEvent()

        binding.stickerView.icons = listOf(deleteIcon, zoomIcon, rotationIcon)
    }

    private fun defaultSelectionOfItem() {
        binding.backgroundIcon.setImageResource(R.drawable.background_selected)
        binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
        //redefining view for other items so that if there is other item selected then the previous one should be unselected
        normalizeOtherViews("background")

        //replace {background} fragment
        backgroundFragment = BackgroundFragment(this)

        MainUtils.replaceFragment(
            backgroundFragment,
            supportFragmentManager,
            R.id.container_fragment_activity_create_or_edit_template
        )
        backFragment = backgroundFragment
    }

    private fun normalizeOtherViews(itemSelected: String) {

        when (itemSelected) {
            "background" -> {
                //text
                binding.textIcon.setImageResource(R.drawable.text_unselected)
                binding.textName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //image
                binding.imageIcon.setImageResource(R.drawable.image_unselected)
                binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //effects
                binding.effectsIcon.setImageResource(R.drawable.effects_unselected)
                binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //logo
                binding.logoIcon.setImageResource(R.drawable.logo_unselected)
                binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //elements
                binding.elementsIcon.setImageResource(R.drawable.element_unselected)
                binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.white))

                //select the background tab and de-select other tabs
                backgroundTab = true
                textTab = false
                effectsTab = false
                logoTab = false
                elementsTab = false
            }
            "text" -> {

                //background
                binding.backgroundIcon.setImageResource(R.drawable.background_unselected)
                binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //image
                binding.imageIcon.setImageResource(R.drawable.image_unselected)
                binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //effects
                binding.effectsIcon.setImageResource(R.drawable.effects_unselected)
                binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //logo
                binding.logoIcon.setImageResource(R.drawable.logo_unselected)
                binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //elements
                binding.elementsIcon.setImageResource(R.drawable.element_unselected)
                binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.white))

                //select the text tab and de-select other tabs
                backgroundTab = false
                textTab = true
                effectsTab = false
                logoTab = false
                elementsTab = false
            }
            "image" -> {
                //background
                binding.backgroundIcon.setImageResource(R.drawable.background_unselected)
                binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //text
                binding.textIcon.setImageResource(R.drawable.text_unselected)
                binding.textName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //effects
                binding.effectsIcon.setImageResource(R.drawable.effects_unselected)
                binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //logo
                binding.logoIcon.setImageResource(R.drawable.logo_unselected)
                binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //elements
                binding.elementsIcon.setImageResource(R.drawable.element_unselected)
                binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            "effects" -> {
                //background
                binding.backgroundIcon.setImageResource(R.drawable.background_unselected)
                binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //text
                binding.textIcon.setImageResource(R.drawable.text_unselected)
                binding.textName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //image
                binding.imageIcon.setImageResource(R.drawable.image_unselected)
                binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //logo
                binding.logoIcon.setImageResource(R.drawable.logo_unselected)
                binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //elements
                binding.elementsIcon.setImageResource(R.drawable.element_unselected)
                binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.white))

                //select the effects tab and de-select other tabs
                backgroundTab = false
                textTab = false
                effectsTab = true
                logoTab = false
                elementsTab = false
            }
            "logo" -> {
                //background
                binding.backgroundIcon.setImageResource(R.drawable.background_unselected)
                binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //text
                binding.textIcon.setImageResource(R.drawable.text_unselected)
                binding.textName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //image
                binding.imageIcon.setImageResource(R.drawable.image_unselected)
                binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //effects
                binding.effectsIcon.setImageResource(R.drawable.effects_unselected)
                binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //elements
                binding.elementsIcon.setImageResource(R.drawable.element_unselected)
                binding.elementsName.setTextColor(ContextCompat.getColor(this, R.color.white))

                //select the logo tab and de-select other tabs
                backgroundTab = false
                textTab = false
                effectsTab = false
                logoTab = true
                elementsTab = false
            }
            "elements" -> {
                //background
                binding.backgroundIcon.setImageResource(R.drawable.background_unselected)
                binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //text
                binding.textIcon.setImageResource(R.drawable.text_unselected)
                binding.textName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //image
                binding.imageIcon.setImageResource(R.drawable.image_unselected)
                binding.imageName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //effects
                binding.effectsIcon.setImageResource(R.drawable.effects_unselected)
                binding.effectsName.setTextColor(ContextCompat.getColor(this, R.color.white))
                //logo
                binding.logoIcon.setImageResource(R.drawable.logo_unselected)
                binding.logoName.setTextColor(ContextCompat.getColor(this, R.color.white))

                //select the elements tab and de-select other tabs
                backgroundTab = true
                textTab = false
                effectsTab = false
                logoTab = false
                elementsTab = true
            }
        }
    }

    private fun dialogToUploadAnImageAndContinue() {

        val background = dialog.findViewById<MaterialCardView>(R.id.image_background_button)
        val sticker = dialog.findViewById<MaterialCardView>(R.id.image_sticker_button)
        dialog.show()

        background.setOnClickListener {

            viewModelMain.imageAsBackgroundIsClicked = true
            dialog.dismiss()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //opening the gallery
                    //pass the image to the cropper
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)
                        //set cropping frame here
                        .setCropShape(CropImageView.CropShape.RECTANGLE, 0)
                        .start(this@CreateOrEditTemplateActivity)


                }
                else {
                    // Request the permissions
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_CODE_STORAGE
                    )
                }
            }
            else {

                if (Utils.hasStoragePermission(this)) {
                    //opening the gallery
                    viewModelMain.imageAsBackgroundIsClicked = true
                    //pass the image to the cropper
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)
                        //set cropping frame here
                        .setCropShape(CropImageView.CropShape.RECTANGLE, 0)
                        .start(this@CreateOrEditTemplateActivity)

                }
                else {
                    //requesting the Gallery permission
                    Utils.getStoragePermission(this)
                }
            }
        }

        sticker.setOnClickListener {

            viewModelMain.imageAsBackgroundIsClicked = false
            dialog.dismiss()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //opening the gallery

                    //pass the image to the cropper
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)
                        //set cropping frame here
                        .setCropShape(CropImageView.CropShape.RECTANGLE, 0)
                        .start(this@CreateOrEditTemplateActivity)

                    dialog.dismiss()
                } else {
                    // Request the permissions
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)

                }
            }
            else {

                if (Utils.hasStoragePermission(this)) {
                    //opening the gallery
                    viewModelMain.imageAsBackgroundIsClicked = false
                    //pass the image to the cropper
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)
                        //set cropping frame here
                        .setCropShape(CropImageView.CropShape.RECTANGLE, 0)
                        .start(this@CreateOrEditTemplateActivity)

                    dialog.dismiss()

                } else {
                    //requesting the Gallery permission
                    Utils.getStoragePermission(this)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_STORAGE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i("TAG", "gallery permission allowed")
                //opening the gallery
//                openGallery()
                //pass the image to the cropper
                CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)
                    //set cropping frame here
                    .setCropShape(CropImageView.CropShape.RECTANGLE, 0)
                    .start(this@CreateOrEditTemplateActivity)

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "Gallery Permission Denied!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", "gallery permission denied")

            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Log.i("TAG", "gallery denied and don't show again")
                //navigate user to app settings page
                Utils.cameraAndGalleryPermissionDialog(this)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                // Finish the parent Activity (Activity1)
                finish();
            }
        } else {

            val result = CropImage.getActivityResult(data) ?: return

            val picUri = result.uri ?: return
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, picUri)

            //setting the liveData of imageSelected from gallery
            addImageAsSticker(bitmap)

            MainUtils.replaceFragment(
                backgroundFragment,
                supportFragmentManager,
                R.id.container_fragment_activity_create_or_edit_template
            )

            binding.backgroundIcon.setImageResource(R.drawable.background_selected)
            binding.backgroundName.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
            //redefining view for other items so that if there is other item selected then the previous one should be unselected
            normalizeOtherViews("background")
        }
    }

    private fun applyColorToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int) {

        val bitmap = Bitmap.createBitmap(shapedBitmapToApplyColor!!.width, shapedBitmapToApplyColor.height, Bitmap.Config.ARGB_8888)
        val coloredBitmap = bitmap.copy(bitmap.config, true)
        // Create a Canvas to draw the solid color
        // Draw the solid color onto the canvas

        // Apply the desired color to the copy
        val canvas = Canvas(coloredBitmap)
        canvas.drawColor(colorToApply)

        //setting margins to the view
        if (shapeId != 0) {

            val shapedBitmap = Utils.toCustomShapeBitmap(viewModelMain, coloredBitmap, shapeId, this)
            binding.imageFromBackground.setImageBitmap(shapedBitmap)

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

        }
        else {

            binding.imageFromBackground.setImageBitmap(coloredBitmap)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(coloredBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(coloredBitmap)!!
    }
    //applying color with alpha
    private fun applyAlphaColorToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int, alpha: Float) {

        val bitmap = Bitmap.createBitmap(
            shapedBitmapToApplyColor!!.width,
            shapedBitmapToApplyColor.height,
            Bitmap.Config.ARGB_8888
        )
        val coloredBitmap = bitmap.copy(bitmap.config, true)
        // Create a Canvas to draw the solid color
        // Draw the solid color onto the canvas

        // Apply the desired color to the copy
        val canvas = Canvas(coloredBitmap)
        canvas.drawColor(colorToApply)

        //saving values
        viewModelMain.selectedColor = colorToApply
        viewModelMain.colorWasSelected = true

        //load it to the imageview
        if (shapeId != 0) {

            val bitmapFinal = Utils.toCustomShapeBitmap(viewModelMain, coloredBitmap, shapeId, this)

            //setting the background bitmap to the background logo
            binding.imageFromBackground.setImageBitmap(bitmapFinal)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapFinal,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //setting and saving the alpha values
            binding.imageFromBackground.alpha = alpha
            //saving alpha value
            viewModelMain.selectedAlphaColorValue = alpha

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(bitmapFinal)!!
        }
        else {

            binding.imageFromBackground.setImageBitmap(coloredBitmap)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(coloredBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            binding.imageFromBackground.alpha = alpha
            //saving alpha value
            viewModelMain.selectedAlphaColorValue = alpha
        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(coloredBitmap)!!
    }
    //applying gradient with alpha
    private fun applyAlphaGradientToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int, color1: Int, color2: Int, alpha: Float) {

        //this value is to keep track that whether the user has applied and selected the gradient just to add the alpha value to the gradient
        viewModelMain.gradientApplied = true

        val bitmap = Bitmap.createBitmap(
            shapedBitmapToApplyColor!!.width,
            shapedBitmapToApplyColor.height,
            Bitmap.Config.ARGB_8888
        )
        val gradientBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // Draw the solid color onto the canvas

        // Apply the desired color to the copy
        val canvas = Canvas(gradientBitmap)
        // Create a radial gradient shader
        val centerX = bitmap.width / 2f
        val centerY = bitmap.height / 2f
        val radius = centerX.coerceAtMost(centerY)

        //setting values to the int array
        val gradientColors = if (color1 != 0) {
            if (color2 != 0) {
                intArrayOf(color2, color1)
            } else {
                intArrayOf(viewModelMain.gradientColor2, color1)
            }
        }
        else {
            if (color2 != 0) {
                intArrayOf(color2, viewModelMain.gradientColor1)
            } else {
                intArrayOf(viewModelMain.gradientColor2, viewModelMain.gradientColor1)
            }
        }


        val gradient = RadialGradient(
            centerX,
            centerY,
            radius,
            gradientColors,
            null,
            Shader.TileMode.CLAMP
        )

        // Create a paint object and set its shader to the radial gradient
        val paint = Paint()
        paint.shader = gradient

        // Draw the original bitmap onto the canvas with the radial gradient
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

        //saving values
        viewModelMain.selectedColor = colorToApply
        viewModelMain.gradientColor1 = color1
        viewModelMain.gradientColor2 = color2
        viewModelMain.gradientSelected = true

        //load it to the imageview
        if (shapeId != 0) {

            val shapedBitmap =
                Utils.toCustomShapeBitmap(viewModelMain, gradientBitmap, shapeId, this)
            binding.imageFromBackground.setImageBitmap(shapedBitmap)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //setting opacity
            binding.imageFromBackground.alpha = alpha

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

        }
        else {

            binding.imageFromBackground.setImageBitmap(gradientBitmap)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(gradientBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //setting opacity
            binding.imageFromBackground.alpha = alpha
        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(gradientBitmap)!!
    }
    //applying radical gradient to image
    @SuppressLint("LongLogTag")
    fun applyRadicalGradientColorToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int, color1: Int, color2: Int) {

        //this value is to keep track that whether the user has applied and selected the gradient just to add the alpha value to the gradient
        viewModelMain.gradientApplied = true

        val bitmap = Bitmap.createBitmap(
            shapedBitmapToApplyColor!!.width,
            shapedBitmapToApplyColor.height,
            Bitmap.Config.ARGB_8888)

        val gradientBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // Draw the solid color onto the canvas

        lifecycleScope.launch(Dispatchers.Default) {

            // Apply the desired color to the copy
            val canvas = Canvas(gradientBitmap)
            // Create a radial gradient shader
            val centerX = bitmap.width / 2f
            val centerY = bitmap.height / 2f
            val radius = centerX.coerceAtMost(centerY)

            Log.i(
                "applyRadicalGradientColorToTheShapedBitmap: ",
                "${viewModelMain.gradientColor1} ${viewModelMain.gradientColor2}"
            )

            //setting values to the int array
            val gradientColors = if (color1 != 0) {
                if (color2 != 0) {
                    intArrayOf(color2, color1)
                } else {
                    intArrayOf(viewModelMain.gradientColor2, color1)
                }
            } else {
                if (color2 != 0) {
                    intArrayOf(color2, viewModelMain.gradientColor1)
                } else {
                    intArrayOf(viewModelMain.gradientColor2, viewModelMain.gradientColor1)
                }
            }


            val gradient = RadialGradient(
                centerX,
                centerY,
                radius,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )

            // Create a paint object and set its shader to the radial gradient
            val paint = Paint()
            paint.shader = gradient

            // Draw the original bitmap onto the canvas with the radial gradient
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

            //saving values
            viewModelMain.selectedColor = colorToApply
            viewModelMain.colorWasSelected = false
        }

        //load it to the imageview
        if (shapeId != 0) {

            val shapedBitmap =
                Utils.toCustomShapeBitmap(viewModelMain, gradientBitmap, shapeId, this)

            //check whether the effect is selected or not and then apply the effect on the gradient
            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    shapedBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            } else {
                binding.imageFromBackground.setImageBitmap(shapedBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

        }
        else {

            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    gradientBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            }
            else {
                binding.imageFromBackground.setImageBitmap(gradientBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(gradientBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(gradientBitmap)!!
    }
    @SuppressLint("LongLogTag")
    fun applyLinearGradientColorToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int, color1: Int, color2: Int) {

        //this value is to keep track that whether the user has applied and selected the gradient just to add the alpha value to the gradient
        viewModelMain.gradientApplied = true

        val bitmap = Bitmap.createBitmap(
            shapedBitmapToApplyColor!!.width,
            shapedBitmapToApplyColor.height,
            Bitmap.Config.ARGB_8888
        )
        val gradientBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // Draw the solid color onto the canvas

        lifecycleScope.launch(Dispatchers.Default) {

            // Apply the desired color to the copy
            val canvas = Canvas(gradientBitmap)

            // Create a radial gradient shader
            val centerX = bitmap.width / 2f
            val centerY = bitmap.height / 2f
            val radius = centerX.coerceAtMost(centerY)

            Log.i(
                "applyRadicalGradientColorToTheShapedBitmap: ",
                "${viewModelMain.gradientColor1} ${viewModelMain.gradientColor2}"
            )

            //setting values to the int array
            val gradientColors = if (color1 != 0) {
                if (color2 != 0) {
                    intArrayOf(color1, color2)
                } else {
                    intArrayOf(color1, viewModelMain.gradientColor2)
                }
            } else {
                if (color2 != 0) {
                    intArrayOf(viewModelMain.gradientColor1, color2)
                } else {
                    intArrayOf(viewModelMain.gradientColor1, viewModelMain.gradientColor2)
                }
            }

            val gradient = RadialGradient(
                centerX,
                centerY,
                radius,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )

            // Create a paint object and set its shader to the radial gradient
            val paint = Paint()
            paint.shader = gradient

            // Draw the original bitmap onto the canvas with the radial gradient
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

            //saving values
            viewModelMain.selectedColor = colorToApply
            viewModelMain.colorWasSelected = false
        }

        //load it to the imageview
        if (shapeId != 0) {

            val finalBitmap =
                Utils.toCustomShapeBitmap(viewModelMain, gradientBitmap, shapeId, this)

            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    finalBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            } else {
                binding.imageFromBackground.setImageBitmap(finalBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(finalBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(finalBitmap)!!
        }
        else {

            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    gradientBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            } else {
                binding.imageFromBackground.setImageBitmap(gradientBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(gradientBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(gradientBitmap)!!
    }
    @SuppressLint("LongLogTag")
    fun applyAngularGradientColorToTheShapedBitmap(shapedBitmapToApplyColor: Bitmap?, colorToApply: Int, shapeId: Int, color1: Int, color2: Int) {

        //this value is to keep track that whether the user has applied and selected the gradient just to add the alpha value to the gradient
        viewModelMain.gradientApplied = true

        val bitmap = Bitmap.createBitmap(
            shapedBitmapToApplyColor!!.width,
            shapedBitmapToApplyColor.height,
            Bitmap.Config.ARGB_8888
        )
        val gradientBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // Draw the solid color onto the canvas

        lifecycleScope.launch(Dispatchers.Default) {

            // Apply the desired color to the copy
            val canvas = Canvas(gradientBitmap)
            // Create a radial gradient shader
            val centerX = bitmap.width / 2f
            val centerY = bitmap.height / 2f
            val radius = centerX.coerceAtMost(centerY)

            Log.i(
                "applyRadicalGradientColorToTheShapedBitmap: ",
                "${viewModelMain.gradientColor1} ${viewModelMain.gradientColor2}"
            )

            //setting values to the int array
            val gradientColors = if (color1 != 0) {
                if (color2 != 0) {
                    intArrayOf(color2, color1)
                } else {
                    intArrayOf(viewModelMain.gradientColor2, color1)
                }
            } else {
                if (color2 != 0) {
                    intArrayOf(color2, viewModelMain.gradientColor1)
                } else {
                    intArrayOf(viewModelMain.gradientColor2, viewModelMain.gradientColor1)
                }
            }


            val gradient = RadialGradient(
                centerX,
                centerY,
                radius,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )

            // Create a paint object and set its shader to the radial gradient
            val paint = Paint()
            paint.shader = gradient

            // Draw the original bitmap onto the canvas with the radial gradient
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

            //saving values
            viewModelMain.selectedColor = colorToApply
            viewModelMain.colorWasSelected = false
        }

        //load it to the imageview
        if (shapeId != 0) {

            val shapedBitmap =
                Utils.toCustomShapeBitmap(viewModelMain, gradientBitmap, shapeId, this)

            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    shapedBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            } else {
                binding.imageFromBackground.setImageBitmap(shapedBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

        }
        else {

            //applying effect to the bitmap
            if (viewModelMain.backgroundEffect.isNotEmpty()) {

                val effectAppliedBitmap = applyEffectToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                    gradientBitmap,
                    viewModelMain.selectedAlphaEffectValue
                )
                binding.imageFromBackground.setImageBitmap(effectAppliedBitmap)
            } else {
                binding.imageFromBackground.setImageBitmap(gradientBitmap)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(gradientBitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        viewModelMain.selectedImageSimple = Utils.bitmapToString(gradientBitmap)!!
    }
    private fun applyEffectToTheShapedBitmap(effectSelected: Bitmap, background: Bitmap?, alphaProgress: Int): Bitmap {

        val textureBitmap: Bitmap = effectSelected.copy(Bitmap.Config.ARGB_8888, true)

        if (background != null) {
            // Load the target and texture images as Bitmaps
            val targetBitmap = background.copy(Bitmap.Config.ARGB_8888, true)
            // Scale the texture to match the size of the background
            val scaledTexture = Bitmap.createScaledBitmap(textureBitmap, background.width, background.height, true)

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(targetBitmap!!)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = alphaProgress // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(scaledTexture, 0f, 0f, paint)

            //save the bitmap to the sharedPreference
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                viewModelMain.selectedImageShapeAppliedWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size

            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                viewModelMain.selectedImageSimpleWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }

            // Display the result in an ImageView or save it as needed
            viewModelMain.backgroundEffect = Utils.bitmapToString(scaledTexture)!!
            binding.imageFromBackground.setImageBitmap(targetBitmap)

            return targetBitmap
        }
        else {
            // Load the target and texture images as Bitmaps
            val transparentBitmap = Bitmap.createBitmap(
                textureBitmap.width,
                textureBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(transparentBitmap)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = alphaProgress // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(textureBitmap, 0f, 0f, paint)

            // Display the result in an ImageView or save it as needed {background is null so due to which only effect is drawn}
            viewModelMain.backgroundEffect = Utils.bitmapToString(transparentBitmap)!!

            val bitmapResult = Utils.stringToBitmap(viewModelMain.backgroundEffect)
            binding.imageFromBackground.setImageBitmap(bitmapResult)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapResult,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            return transparentBitmap
        }
    }
    //image Sticker
    private fun addImageAsSticker(imageChosenCropped: Bitmap) {

        if (viewModelMain.imageAsBackgroundIsClicked) {

            normalizeOtherViews("background")
            viewModelMain.gradientSelected = false
            viewModelMain.selectedImageSimple = ""
            viewModelMain.backgroundImageFromGallery = Utils.bitmapToString(imageChosenCropped)!!
            //background button clicked
            Log.i(
                "backgroundClicked: ",
                "${viewModelMain.imageAsBackgroundIsClicked} $imageChosenCropped"
            )

            //setting the image from previous fragment to the {PhotoView} the view is updated here
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                val image = CropImage.toCustomBitmap(
                    imageChosenCropped,
                    viewModelMain.selectedShapeToAdd!!,
                    this
                )
                binding.imageFromBackground.setImageBitmap(image)

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(image,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size

                viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(image)!!

            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                binding.imageFromBackground.setImageBitmap(imageChosenCropped)

                viewModelMain.backgroundImageFromGallery = Utils.bitmapToString(imageChosenCropped)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(imageChosenCropped,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }
        } else {

            //for sticker button
            //background button clicked
            Log.i(
                "sticker: ",
                "$viewModelMain.imageAsBackgroundIsClicked $imageChosenCropped.toString()"
            )

            //setting the image from previous fragment to the {StickerView} the view is updated here
            // set here the image as a sticker

            Log.i(
                "Background: ",
                "$viewModelMain.imageAsBackgroundIsClicked $imageChosenCropped.toString()"
            )

            //setting the image from previous fragment to the {StickerView} the view is updated here
            // set here the image as a sticker
            val stickerImage = ImageSticker(BitmapDrawable(resources, imageChosenCropped))
            binding.stickerView.addSticker(stickerImage, Position.CENTER)

            //selecting the added sticker
            if (!binding.stickerView.isSelected) {
                binding.stickerView.isSelected = true
                binding.stickerView.showBorder(true)
                binding.stickerView.showIcons(true)
            }

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,stickerImage))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //saving the image sticker to the preference
            if (!imageChosenCropped.isRecycled)
                viewModelMain.backgroundImageFromGallery = Utils.bitmapToString(imageChosenCropped)!!

        }
    }
    //Background fragment interface functions to listen the clicks on items of the lists in the background function
    //Tabs clicks
    override fun opacityLogoBackgroundFragment(opacity: Float) {

        if (viewModelMain.gradientApplied) {
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                applyAlphaGradientToTheShapedBitmap(
                    Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                    viewModelMain.selectedColor,
                    viewModelMain.selectedShapeToAdd!!,
                    viewModelMain.gradientColor1,
                    viewModelMain.gradientColor2,
                    opacity
                )
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                    applyAlphaGradientToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        viewModelMain.gradientColor1,
                        viewModelMain.gradientColor2,
                        opacity
                    )
                }
                else if (viewModelMain.backgroundImageFromGallery.isNotEmpty()){

                    applyAlphaGradientToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        viewModelMain.gradientColor1,
                        viewModelMain.gradientColor2,
                        opacity
                    )
                }
                else if (viewModelMain.selectedImageShapeApplied.isNotEmpty()){
                    applyAlphaGradientToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        viewModelMain.gradientColor1,
                        viewModelMain.gradientColor2,
                        opacity
                    )
                }
                else {
                    applyAlphaGradientToTheShapedBitmap(
                        Utils.stringToBitmap(Utils.drawableToString(ContextCompat.getDrawable(this,R.drawable.bg1))),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        viewModelMain.gradientColor1,
                        viewModelMain.gradientColor2,
                        opacity
                    )
                }
            }
        }
        else {

            //apply alpha to the image
            binding.imageFromBackground.alpha = opacity
        }
    }
    override fun clickOnGraphicsItemBackgroundFragment() {

        //activity
        binding.colorPickerButton.visibility = View.INVISIBLE
    }
    override fun clickOnTextureItemBackgroundFragment() {

        binding.colorPickerButton.visibility = View.INVISIBLE
    }
    override fun clickOnColorsItemBackgroundFragment() {
        binding.colorPickerButton.visibility = View.VISIBLE

        //visibility handling when the color is clicked
        if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {
            binding.stickerView.visibility = View.VISIBLE
        }
    }
    override fun clickOnShapesItemBackgroundFragment() {

        binding.colorPickerButton.visibility = View.INVISIBLE
    }
    //item clicks
    override fun clickOnImageItemForBackgroundLogoBackgroundFragment(item: Image,listName: String) {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        backgroundItemSelected = true

        //setting image to the image view
        if (viewModelMain.listName == "shapes") {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(10, 10, 10, 10)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            //checking image is loaded to the shape or not
            if (viewModelMain.colorWasSelected) {

                //if the color was selected then the selected shape is applied to the previously saved Image and then
                // result image bitmap is sent to paint the color which was selected.
                if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery)!!,
                        viewModelMain.imageItem!!.id,
                        this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        applyColorToTheShapedBitmap(
                            applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                shapedBitmap,
                                viewModelMain.selectedAlphaEffectValue
                            ),
                            viewModelMain.selectedColor, viewModelMain.imageItem!!.id
                        )
                    } else {
                        applyColorToTheShapedBitmap(
                            shapedBitmap,
                            viewModelMain.selectedColor,
                            viewModelMain.imageItem!!.id
                        )
                    }

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id
                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!
                    viewModelMain.selectedImageSimple = ""

                }
                else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.selectedImageSimple)!!,
                        viewModelMain.imageItem!!.id,
                        this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        applyColorToTheShapedBitmap(
                            applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                shapedBitmap,
                                viewModelMain.selectedAlphaEffectValue
                            ),
                            viewModelMain.selectedColor, viewModelMain.imageItem!!.id
                        )
                    }
                    else {
                        applyColorToTheShapedBitmap(
                            shapedBitmap,
                            viewModelMain.selectedColor,
                            viewModelMain.imageItem!!.id
                        )
                    }

                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!
                    binding.imageFromBackground.setImageBitmap(shapedBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id
                }
                else if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied)!!,
                        viewModelMain.imageItem!!.id,
                        this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        applyColorToTheShapedBitmap(
                            applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                shapedBitmap,
                                viewModelMain.selectedAlphaEffectValue
                            ),
                            viewModelMain.selectedColor, viewModelMain.imageItem!!.id
                        )
                    }
                    else {
                        applyColorToTheShapedBitmap(
                            shapedBitmap,
                            viewModelMain.selectedColor,
                            viewModelMain.imageItem!!.id
                        )
                    }

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id
                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

                }
                else {

                    //when there will be no image selected by the user, then the default image will be given to the shaper and then resultant
                    //will be painted 'bg1' is a default drawable just give to the shaper and then that shapedBitmap will be painted
                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.imageItem!!.id, this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        applyColorToTheShapedBitmap(
                            applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                shapedBitmap,
                                viewModelMain.selectedAlphaEffectValue
                            ),
                            viewModelMain.selectedColor, viewModelMain.imageItem!!.id
                        )
                    }
                    else {
                        applyColorToTheShapedBitmap(
                            shapedBitmap,
                            viewModelMain.selectedColor,
                            viewModelMain.imageItem!!.id
                        )
                    }

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id
                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!
                }
            }
            else if (viewModelMain.gradientSelected) {

                if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                    //apply radical gradient to the card
                    when (viewModelMain.selectedGradientType) {
                        "radical" -> {
                            applyRadicalGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.backgroundImageFromGallery
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "linear" -> {

                            applyLinearGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.backgroundImageFromGallery
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "angular" -> {
                            applyAngularGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.backgroundImageFromGallery
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }
                    }
                }
                else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                    //apply radical gradient to the card
                    when (viewModelMain.selectedGradientType) {
                        "radical" -> {
                            applyRadicalGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageSimple
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "linear" -> {

                            applyLinearGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageSimple
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "angular" -> {
                            applyAngularGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageSimple
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }
                    }

                }
                else if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                    //apply radical gradient to the card
                    when (viewModelMain.selectedGradientType) {
                        "radical" -> {
                            applyRadicalGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageShapeApplied
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "linear" -> {

                            applyLinearGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageShapeApplied
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }

                        "angular" -> {
                            applyAngularGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    viewModelMain.selectedImageShapeApplied
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }
                    }
                }
                else {

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                    //when there will be no image selected by the user, then the default image will be given to the shaper and then resultant
                    //apply radical gradient to the card
                    when (viewModelMain.selectedGradientType) {
                        "radical" -> {
                            applyRadicalGradientColorToTheShapedBitmap(Utils.stringToBitmap(
                                Utils.drawableToString(ContextCompat.getDrawable(this,R.drawable.bg1))
                            ),
                                viewModelMain.gradientColor1, viewModelMain.selectedShapeToAdd!!, 0, viewModelMain.gradientColor2)
                        }
                        "linear" -> {

                            applyLinearGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    Utils.drawableToString(ContextCompat.getDrawable(this,R.drawable.bg1))
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }
                        "angular" -> {
                            applyAngularGradientColorToTheShapedBitmap(
                                Utils.stringToBitmap(
                                    Utils.drawableToString(ContextCompat.getDrawable(this,R.drawable.bg1))
                                ),
                                viewModelMain.gradientColor1,
                                viewModelMain.selectedShapeToAdd!!,
                                0,
                                viewModelMain.gradientColor2
                            )
                        }
                    }
                }
            }
            else {

                if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery)!!,
                        viewModelMain.imageItem!!.id,
                        this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        val bitmapResult = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            shapedBitmap,
                            viewModelMain.selectedAlphaEffectValue
                        )
                        binding.imageFromBackground.setImageBitmap(bitmapResult)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapResult,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }

                    else {
                        binding.imageFromBackground.setImageBitmap(shapedBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

                }
                else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.selectedImageSimple)!!,
                        viewModelMain.imageItem!!.id,
                        this
                    )

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            shapedBitmap,
                            viewModelMain.selectedAlphaEffectValue
                        )
                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    } else {
                        binding.imageFromBackground.setImageBitmap(shapedBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }

                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied = Utils.bitmapToString(shapedBitmap)!!

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                }
                else if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                            viewModelMain.selectedAlphaEffectValue
                        )
                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    } else {
                        binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied))

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }

                    //save the applied shape image
                    viewModelMain.selectedImageShapeApplied =
                        Utils.bitmapToString(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied)!!)!!

                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id

                }
                else {

                    //applying effect to the bitmap
                    if (viewModelMain.backgroundEffect.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            Utils.stringToBitmap(item.logoImage),
                            viewModelMain.selectedAlphaEffectValue
                        )
                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else {
                        binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(item.logoImage))

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(item.logoImage),null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }

                    //first time user is coming directly to the shapes tab
                    //saving the shape which was selected
                    viewModelMain.selectedShapeToAdd = viewModelMain.imageItem!!.id
                }
            }
        }
        else {

            viewModelMain.colorWasSelected = false
            viewModelMain.gradientSelected = false
            viewModelMain.selectedImageSimple = viewModelMain.imageItem!!.logoImage
            //so that next time only the selected image should be visible
            viewModelMain.backgroundImageFromGallery = ""

            if (viewModelMain.listName == "texture"){
                textureSelected = true
                graphicsSelected = false
            }
            else{
                textureSelected = false
                graphicsSelected = true
            }

            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                val shapedBitmap = Utils.toCustomShapeBitmap(
                    viewModelMain, Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                    viewModelMain.selectedShapeToAdd!!, this
                )

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        shapedBitmap,
                        viewModelMain.selectedAlphaEffectValue
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
                else {
                    binding.imageFromBackground.setImageBitmap(shapedBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                val bitmapToApplyEffect = Utils.stringToBitmap(viewModelMain.selectedImageSimple)!!

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        bitmapToApplyEffect,
                        viewModelMain.selectedAlphaEffectValue
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
                else {
                    binding.imageFromBackground.setImageBitmap(bitmapToApplyEffect)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapToApplyEffect,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
        }
    }
    override fun clickOnColorItemForBackgroundLogoBackgroundFragment(colorClicked: Int) {

        binding.stickerView.visibility = View.VISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        //saving values
        viewModelMain.selectedColor = colorClicked
        viewModelMain.colorWasSelected = true
        viewModelMain.gradientApplied = false

        //on color item clicked is applied on the color card
        if (viewModelMain.selectedImageShapeApplied.isNotEmpty())
            applyColorToTheShapedBitmap(
                Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                viewModelMain.selectedColor,
                viewModelMain.selectedShapeToAdd!!
            )
        else
            applyColorToTheShapedBitmap(
                Utils.stringToBitmap(
                    Utils.drawableToString(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.bg1
                        )
                    )
                ),
                viewModelMain.selectedColor, viewModelMain.selectedShapeToAdd!!)
    }
    override fun clickOnGradientButtonBackgroundFragment() {

        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {
            binding.stickerView.visibility = View.VISIBLE
        }
    }
    override fun clickOnGradient1ItemForBackgroundLogoBackgroundFragment(gradient1ColorClicked: Int) {

        //making download button visible
        binding.stickerView.visibility = View.VISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        viewModelMain.colorWasSelected = false
        viewModelMain.gradientSelected = true

        //on color item clicked is applied on the color card
        if (viewModelMain.selectedImageShapeApplied.isNotEmpty() || viewModelMain.selectedGradientType.isNotEmpty()) {

            //apply radical gradient to the card
            when (viewModelMain.selectedGradientType) {
                "radical" -> {
                    applyRadicalGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient1ColorClicked
                    )
                }

                "linear" -> {

                    applyLinearGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient1ColorClicked
                    )
                }

                "angular" -> {
                    applyAngularGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient1ColorClicked
                    )
                }
            }
        }

    }
    override fun clickOnGradient2ItemForBackgroundLogoBackgroundFragment(gradient2ColorClicked: Int) {

        //making download button visible
        binding.stickerView.visibility = View.VISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        viewModelMain.colorWasSelected = false
        viewModelMain.gradientSelected = true

        //on color item clicked is applied on the color card
        if (viewModelMain.selectedImageShapeApplied.isNotEmpty() || viewModelMain.selectedGradientType.isNotEmpty()) {

            //applying color to the saved shaped bitmap
            //apply radical gradient to the card
            when (viewModelMain.selectedGradientType) {
                "radical" -> {

                    applyRadicalGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient2ColorClicked
                    )
                }

                "linear" -> {
                    applyLinearGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient2ColorClicked
                    )

                }

                "angular" -> {
                    applyAngularGradientColorToTheShapedBitmap(
                        Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    R.drawable.bg1
                                )
                            )
                        ),
                        viewModelMain.selectedColor,
                        viewModelMain.selectedShapeToAdd!!,
                        0,
                        gradient2ColorClicked
                    )

                }
            }
        }

    }
    override fun clickOnRemoveButtonBackgroundItemListBackgroundFragment(listName: String) {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

            if (listName == "shapes") {

                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.selectedShapeToAdd = 0

            }
            else if (listName == "graphics" && graphicsSelected) {

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
            }
            else if (listName == "textrure" && textureSelected){

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
                textureSelected = false
            }
        }
        else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

            if (listName == "shapes") {

                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.selectedShapeToAdd = 0

            }
            else if (listName == "graphics" && graphicsSelected) {

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
            }
            else if (listName == "texture" && textureSelected){

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
                textureSelected = false
            }
        }
        else if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

            if (listName == "shapes") {

                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.selectedShapeToAdd = 0

            }
            else if (listName == "graphics" && graphicsSelected) {

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
            }
            else if (listName == "textrure" && textureSelected){

                viewModelMain.selectedImageSimple = ""
                viewModelMain.selectedImageShapeApplied = ""
                viewModelMain.backgroundImageFromGallery = ""
                textureSelected = false
            }
        }
        else {
            viewModelMain.selectedImageShapeApplied = ""
            viewModelMain.selectedImageSimple = ""
            viewModelMain.background = null
            viewModelMain.selectedShapeToAdd = 0
        }

        backgroundItemSelected = false

        //this is used to remove the shape from the background image
        if (viewModelMain.colorWasSelected) {

            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //setting simple shape to the view
                val bitmapForEffect = Utils.stringToBitmap(viewModelMain.selectedImageSimple)
                //set bitmap for the color {This will apply color to the image and then will save it to the 'applied' variable}
                applyColorToTheShapedBitmap(bitmapForEffect, viewModelMain.selectedColor, 0)

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                        150
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {
                    binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied))

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                        150
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size

                }
                else {
                    binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.selectedImageSimple))

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageSimple),null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
        }
        else if (viewModelMain.gradientSelected) {

            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //setting simple shape to the view
                val bitmapForEffect = Utils.stringToBitmap(viewModelMain.selectedImageSimple)

                //apply radical gradient to the card
                when (viewModelMain.selectedGradientType) {
                    "radical" -> {
                        applyRadicalGradientColorToTheShapedBitmap(
                            bitmapForEffect,
                            viewModelMain.selectedColor,
                            0,
                            viewModelMain.gradientColor1,
                            viewModelMain.gradientColor2
                        )
                    }
                    "linear" -> {

                        applyLinearGradientColorToTheShapedBitmap(
                            bitmapForEffect,
                            viewModelMain.selectedColor,
                            0,
                            viewModelMain.gradientColor1,
                            viewModelMain.gradientColor2
                        )
                    }
                    "angular" -> {
                        applyAngularGradientColorToTheShapedBitmap(
                            bitmapForEffect,
                            viewModelMain.gradientColor1,
                            0,
                            0,
                            viewModelMain.gradientColor2
                        )
                    }
                }

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                        150
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
                else {
                    binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied))

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //apply radical gradient to the card
                when (viewModelMain.selectedGradientType) {
                    "radical" -> {
                        applyRadicalGradientColorToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                            viewModelMain.selectedColor,
                            0,
                            viewModelMain.gradientColor1,
                            viewModelMain.gradientColor2
                        )
                    }
                    "linear" -> {

                        applyLinearGradientColorToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                            viewModelMain.selectedColor,
                            0,
                            viewModelMain.gradientColor1,
                            viewModelMain.gradientColor2
                        )
                    }
                    "angular" -> {
                        applyAngularGradientColorToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                            viewModelMain.gradientColor1,
                            0,
                            0,
                            viewModelMain.gradientColor2
                        )
                    }
                }

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    val resultBitmap = applyEffectToTheShapedBitmap(
                        Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                        Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                        150
                    )
                    binding.imageFromBackground.setImageBitmap(resultBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size

                }
                else {
                    binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.selectedImageSimple))

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageSimple),null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
        }
        else {
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {
                        //setting simple shape to the view
                        val bitmapShapedForEffect = Utils.toCustomShapeBitmap(
                            viewModelMain,
                            Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery),
                            viewModelMain.selectedShapeToAdd!!,
                            this
                        )

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            bitmapShapedForEffect, 150
                        )

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                            150
                        )

                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else {

                        //setting simple shape to the view
                        val bitmapForEffect = Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    viewModelMain.selectedShapeToAdd!!
                                )
                            )
                        )

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            bitmapForEffect,
                            150
                        )

                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                }
                else {

                    if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {
                        //setting simple shape to the view
                        val bitmapShapedForEffect = Utils.toCustomShapeBitmap(
                            viewModelMain,
                            Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery),
                            viewModelMain.selectedShapeToAdd!!,
                            this
                        )

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            bitmapShapedForEffect, 150
                        )

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                        //setting simple shape to the view
                        val bitmapForEffect = Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    viewModelMain.selectedShapeToAdd!!
                                )
                            )
                        )

                        binding.imageFromBackground.setImageBitmap(bitmapForEffect)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapForEffect,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else {

                        val bitmapShape = Utils.stringToBitmap(
                            Utils.drawableToString(
                                ContextCompat.getDrawable(
                                    this,
                                    viewModelMain.selectedShapeToAdd!!
                                )
                            )
                        )
                        binding.imageFromBackground.setImageBitmap(bitmapShape)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(bitmapShape,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                }
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //applying effect to the bitmap
                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery),
                            150
                        )
                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                        val resultBitmap = applyEffectToTheShapedBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                            150
                        )
                        binding.imageFromBackground.setImageBitmap(resultBitmap)

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else {

                        if (viewModelMain.selectedShapeToAdd != 0){

                            //give the margin
                            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                            params.setMargins(10, 10, 10, 10)
                            // Apply the LayoutParams to the View
                            binding.imageFromBackground.layoutParams = params

                            val resultBitmap = applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                Utils.stringToBitmap(
                                    Utils.drawableToString(ContextCompat.getDrawable(this, viewModelMain.selectedShapeToAdd!!))), 150)

                            binding.imageFromBackground.setImageBitmap(resultBitmap)

                            // setting list in undo redo
                            viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                            undoRedoTraversal = viewModelMain.undoRedoArray.size
                        }
                        else{

                            //give the margin
                            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                            params.setMargins(0, 0, 0, 0)
                            // Apply the LayoutParams to the View
                            binding.imageFromBackground.layoutParams = params

                            val resultBitmap = applyEffectToTheShapedBitmap(
                                Utils.stringToBitmap(viewModelMain.backgroundEffect)!!,
                                Utils.stringToBitmap(""), 150)

                            binding.imageFromBackground.setImageBitmap(resultBitmap)

                            // setting list in undo redo
                            viewModelMain.undoRedoArray.add(UndoRedoStack(resultBitmap,null))
                            undoRedoTraversal = viewModelMain.undoRedoArray.size
                        }
                    }
                }
                else {

                    if (viewModelMain.backgroundImageFromGallery.isNotEmpty()) {

                        binding.imageFromBackground.setImageBitmap(
                            Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery)
                        )

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.backgroundImageFromGallery),null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else if (viewModelMain.selectedImageSimple.isNotEmpty()) {

                        binding.imageFromBackground.setImageBitmap(
                            Utils.stringToBitmap(viewModelMain.selectedImageSimple))

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.selectedImageSimple),null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                    else {
                        //because shape was already none and user removed the image it added to the background of logo
                        binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(""))

                        // setting list in undo redo
                        viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(""),null))
                        undoRedoTraversal = viewModelMain.undoRedoArray.size
                    }
                }
            }
        }
    }
    //Effects fragment interface functions to listen the clicks on items of the lists in the background function
    override fun effectItemClickOpacityProgressEffectsFragment(effectSelected: Bitmap, background: Bitmap?, alphaProgress: Int) {

        val textureBitmap: Bitmap = effectSelected.copy(Bitmap.Config.ARGB_8888, true)

        //saving the alpha value to the viewModel
        viewModelMain.selectedAlphaEffectValue = alphaProgress

        if (background != null) {
            // Load the target and texture images as Bitmaps
            val targetBitmap = background.copy(Bitmap.Config.ARGB_8888, true)
            // Scale the texture to match the size of the background
            val scaledTexture =
                Bitmap.createScaledBitmap(textureBitmap, background.width, background.height, true)

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(targetBitmap!!)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = alphaProgress // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(scaledTexture, 0f, 0f, paint)

            //save the bitmap to the sharedPreference
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //setting the effect applied image
                viewModelMain.selectedImageShapeAppliedWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                viewModelMain.selectedImageSimpleWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }

            // Display the result in an ImageView or save it as needed
            viewModelMain.backgroundEffect = Utils.bitmapToString(scaledTexture)!!
            binding.imageFromBackground.setImageBitmap(targetBitmap)

        }
        else if (viewModelMain.selectedShapeToAdd != 0) {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(10, 10, 10, 10)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            applyEffectToTheShapedBitmap(
                effectSelected,
                Utils.stringToBitmap(
                    Utils.drawableToString(
                        ContextCompat.getDrawable(
                            this,
                            viewModelMain.selectedShapeToAdd!!
                        )
                    )
                ),
                alphaProgress
            )
        }
        else {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, 0)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            // Load the target and texture images as Bitmaps
            val transparentBitmap = Bitmap.createBitmap(
                textureBitmap.width,
                textureBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(transparentBitmap)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = alphaProgress // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(textureBitmap, 0f, 0f, paint)

            // Display the result in an ImageView or save it as needed {background is null so due to which only effect is drawn}
            viewModelMain.backgroundEffect = Utils.bitmapToString(transparentBitmap)!!
            binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.backgroundEffect))

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.backgroundEffect),null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE
    }
    override fun effectItemClickApplyBackgroundEffectsFragment(effectSelected: Bitmap, background: Bitmap?, opacity: Int) {

        val textureBitmap: Bitmap = effectSelected.copy(Bitmap.Config.ARGB_8888, true)

        //saving the alpha value to the viewModel
        viewModelMain.selectedAlphaEffectValue = opacity

        if (background != null) {
            // Load the target and texture images as Bitmaps
            val targetBitmap = background.copy(Bitmap.Config.ARGB_8888, true)
            // Scale the texture to match the size of the background
            val scaledTexture = Bitmap.createScaledBitmap(textureBitmap, background.width, background.height, true)

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(targetBitmap!!)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = opacity // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(scaledTexture, 0f, 0f, paint)

            //save the bitmap to the sharedPreference
            if (viewModelMain.selectedShapeToAdd != 0) {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(10, 10, 10, 10)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                //setting the effect applied image
                viewModelMain.selectedImageShapeAppliedWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }
            else {

                //give the margin
                val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, 0)
                // Apply the LayoutParams to the View
                binding.imageFromBackground.layoutParams = params

                viewModelMain.selectedImageSimpleWithEffect = Utils.bitmapToString(targetBitmap)!!

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(targetBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }

            // Display the result in an ImageView or save it as needed
            viewModelMain.backgroundEffect = Utils.bitmapToString(scaledTexture)!!
            binding.imageFromBackground.setImageBitmap(targetBitmap)
        }
        else if (viewModelMain.selectedShapeToAdd != 0) {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(10, 10, 10, 10)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            applyEffectToTheShapedBitmap(
                effectSelected,
                Utils.stringToBitmap(
                    Utils.drawableToString(
                        ContextCompat.getDrawable(
                            this,
                            viewModelMain.selectedShapeToAdd!!
                        )
                    )
                ),
                opacity
            )
        }
        else {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, 0)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            // Load the target and texture images as Bitmaps
            val transparentBitmap = Bitmap.createBitmap(
                textureBitmap.width,
                textureBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            // Create a Canvas object using the target Bitmap
            val canvas = Canvas(transparentBitmap)

            // Create a Paint object for blending (you can customize this as needed)
            val paint = Paint()
            paint.alpha = opacity // Adjust the transparency of the texture

            // Draw the texture image onto the target image
            canvas.drawBitmap(textureBitmap, 0f, 0f, paint)

            // Display the result in an ImageView or save it as needed {background is null so due to which only effect is drawn}
            viewModelMain.backgroundEffect = Utils.bitmapToString(transparentBitmap)!!
            binding.imageFromBackground.setImageBitmap(Utils.stringToBitmap(viewModelMain.backgroundEffect))

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(Utils.stringToBitmap(viewModelMain.backgroundEffect),null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE
    }
    override fun effectCleanButtonClickedEffectsFragment() {

        //clean the effect
        viewModelMain.backgroundEffect = ""
        //saving the alpha value to the viewModel
        viewModelMain.selectedAlphaEffectValue = 150

        //drawing image again on removing the effect
        if (viewModelMain.selectedShapeToAdd != 0) {

            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(10, 10, 10, 10)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            if (viewModelMain.selectedImageShapeApplied.isNotEmpty()) {

                if (viewModelMain.backgroundEffect.isNotEmpty()) {

                    //effect is applied to the shaped image {again that image without effect}
                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain, Utils.stringToBitmap(viewModelMain.selectedImageSimple),
                        viewModelMain.selectedShapeToAdd!!, this
                    )

                    binding.imageFromBackground.setImageBitmap(shapedBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {

                    val shapedBitmap = Utils.toCustomShapeBitmap(
                        viewModelMain,
                        Utils.stringToBitmap(viewModelMain.selectedImageShapeApplied),
                        viewModelMain.selectedShapeToAdd!!,
                        this
                    )

                    binding.imageFromBackground.setImageBitmap(shapedBitmap)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
            else {

                //shape without image will be executed
                val shapedBitmap = Utils.stringToBitmap(
                    Utils.drawableToString(
                        ContextCompat.getDrawable(this, viewModelMain.selectedShapeToAdd!!)
                    )
                )

                binding.imageFromBackground.setImageBitmap(shapedBitmap)

                // setting list in undo redo
                viewModelMain.undoRedoArray.add(UndoRedoStack(shapedBitmap,null))
                undoRedoTraversal = viewModelMain.undoRedoArray.size
            }
        }
        else {
            //give the margin
            val params = binding.imageFromBackground.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, 0)
            // Apply the LayoutParams to the View
            binding.imageFromBackground.layoutParams = params

            //set the bitmap if the color was not selected but the shape was applied
            val bitmap = Utils.stringToBitmap(viewModelMain.selectedImageSimple)
            binding.imageFromBackground.setImageBitmap(bitmap)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(bitmap,null))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE
    }
    //Elements fragment interface functions to listen the clicks on items of the lists in the background function
    override fun clickedElementFromListApplyElementsFragment(sticker: Sticker) {

        binding.stickerView.addSticker(sticker, Position.CENTER)

        // setting list in undo redo
        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
        undoRedoTraversal = viewModelMain.undoRedoArray.size

        //selecting the added sticker
        if (!binding.stickerView.isSelected) {
            binding.stickerView.isSelected = true
            binding.stickerView.showBorder(true)
            binding.stickerView.showIcons(true)
        }

        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE
    }
    //Logos fragment interface functions to listen the clicks on items of the lists in the background function
    override fun clickedLogoFromListApplyLogosFragment(sticker: Sticker) {

        binding.stickerView.addSticker(sticker, Position.CENTER)

        //adding stickers
        // setting list in undo redo
        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
        undoRedoTraversal = viewModelMain.undoRedoArray.size

        //selecting the added sticker
        if (!binding.stickerView.isSelected) {
            binding.stickerView.isSelected = true
            binding.stickerView.showBorder(true)
            binding.stickerView.showIcons(true)
        }

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

    }
    //Text fragment interface functions to listen the clicks on items of the lists in the background function
    override fun opacityOfTextOfStickerTextFragment(alpha: Int) {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        val sticker = TextSticker(this)
            .setText(stickerText)
            .setShadowRadius(viewModelMain.shadowRadius)
            .setShadowColor(viewModelMain.shadowColor)
            .setTypeface(viewModelMain.selectedFontItem.typeface)
            .setTextColor(viewModelMain.selectedColor)
            .setAlpha(alpha)
            .setMaxTextSize(30f)
            .resizeText()

        binding.stickerView.replace(sticker)

        // setting list in undo redo
        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
        undoRedoTraversal = viewModelMain.undoRedoArray.size
    }
    override fun controllerViewMoveStickerButtonsClickEventsTextFragment(direction: String) {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        when (direction) {
            "Up" -> {
                if (viewModelMain.selectedColor != 0) {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTextColor(viewModelMain.selectedColor)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerUp(-5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerUp(-5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }

            "Left" -> {
                if (viewModelMain.selectedColor != 0) {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTextColor(viewModelMain.selectedColor)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerLeft(-5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerLeft(-5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }

            "Right" -> {
                if (viewModelMain.selectedColor != 0) {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTextColor(viewModelMain.selectedColor)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerRight(5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerRight(5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }

            "Down" -> {
                if (viewModelMain.selectedColor != 0) {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTextColor(viewModelMain.selectedColor)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerDown(5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                } else {

                    val sticker = TextSticker(this)
                        .setText(stickerText)
                        .setTypeface(viewModelMain.selectedFontItem.typeface)
                        .setTextAlign(viewModelMain.alignment)
                        .setMaxTextSize(30f)
                        .resizeText()
                    binding.stickerView.replace(sticker)
                    binding.stickerView.moveStickerDown(5.0f)

                    // setting list in undo redo
                    viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
                    undoRedoTraversal = viewModelMain.undoRedoArray.size
                }
            }
        }
    }
    override fun controllerViewAlignStickerTextButtonsClickEventsTextFragment(alignment: Layout.Alignment) {

        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.selectedColor != 0) {

            val sticker = TextSticker(this@CreateOrEditTemplateActivity)
                .setText(stickerText)
                .setTextColor(viewModelMain.selectedColor)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }
        else {

            val sticker = TextSticker(this@CreateOrEditTemplateActivity)
                .setText(stickerText)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(alignment)
                .setMaxTextSize(30f)
                .resizeText()

            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }
    }
    override fun controllerViewEditStickerTextButtonsClickEventTextFragment() {
        binding.stickerTextEdt.visibility = View.VISIBLE
        binding.saveTextButton.visibility = View.VISIBLE
        binding.colorPickerButton.visibility = View.INVISIBLE
        //making download button visible
        binding.downloadButton.visibility = View.INVISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        binding.stickerTextEdt.setText(stickerText)

        //for save button so that the text should be edited of the sticker
        editTheTextSticker = true
    }
    override fun controllerViewCopyStickerButtonClickEventTextFragment() {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.selectedColor != 0) {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTextColor(viewModelMain.selectedColor)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.addSticker(sticker, Position.CENTER)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //selecting the added sticker
            if (!binding.stickerView.isSelected) {
                binding.stickerView.isSelected = true
                binding.stickerView.showBorder(true)
                binding.stickerView.showIcons(true)
            }

        } else {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.addSticker(sticker, Position.CENTER)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size

            //selecting the added sticker
            if (!binding.stickerView.isSelected) {
                binding.stickerView.isSelected = true
                binding.stickerView.showBorder(true)
                binding.stickerView.showIcons(true)
            }
        }
    }
    override fun colorListItemClickApplyOnTextOfStickerTextFragment(color: Int) {

        //on color item clicked is applied on the text of the sticker visible on the made image previously
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.shadowColor != 0) {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTextColor(color)
                .setShadowRadius(viewModelMain.shadowRadius)
                .setShadowColor(viewModelMain.shadowColor)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        } else {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTextColor(color)
                .setTypeface(viewModelMain.selectedFontItem.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }
    }
    override fun fontListItemClickApplyOnTextOfStickerTextFragment(font: FontItem) {

        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        if (viewModelMain.selectedColor != 0) {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTextColor(viewModelMain.selectedColor)
                .setTypeface(font.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()
            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        } else {

            val sticker = TextSticker(this)
                .setText(stickerText)
                .setTypeface(font.typeface)
                .setTextAlign(viewModelMain.alignment)
                .setMaxTextSize(30f)
                .resizeText()

            binding.stickerView.replace(sticker)

            // setting list in undo redo
            viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
            undoRedoTraversal = viewModelMain.undoRedoArray.size
        }
    }
    override fun shadowColorAndIntensityForTheStickerTextTextFragment(color: Int, radius: Int) {

        //on color item clicked is applied on the text of the sticker visible on the made image previously
        //making download button visible
        binding.downloadButton.visibility = View.VISIBLE
        //making undo and redo buttons invisible
        binding.forwardBackwardLayout.visibility = View.VISIBLE
        binding.undoButton.visibility = View.VISIBLE
        binding.redoButton.visibility = View.VISIBLE

        val sticker = TextSticker(this)
            .setText(stickerText)
            .setShadowColor(color)
            .setShadowRadius(viewModelMain.shadowRadius)
            .setTextColor(viewModelMain.selectedColor)
            .setTypeface(viewModelMain.selectedFontItem.typeface)
            .setTextAlign(viewModelMain.alignment)
            .setMaxTextSize(30f)
            .resizeText()
        binding.stickerView.replace(sticker)

        // setting list in undo redo
        viewModelMain.undoRedoArray.add(UndoRedoStack(null,sticker))
        undoRedoTraversal = viewModelMain.undoRedoArray.size
    }

    //exit dialog function
    private fun exitDialog() {

        val exit = dialogExit.findViewById<AppCompatButton>(R.id.exit_button)
        val cancel = dialogExit.findViewById<AppCompatButton>(R.id.cancel_button)

        exit.setOnClickListener {

            exitClicked = true

            if (LogoMakerApp.CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL == "0") {
                finish()
                dialogExit.dismiss()
            } else if (LogoMakerApp.CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL == "1") {

                //load Ad mob
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(this)

                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()

                                setAd()

                                finish()
                                dialogExit.dismiss()
                            }
                        }
                } else {
                    setAd()

                    finish()
                    dialogExit.dismiss()
                }
            } else if (LogoMakerApp.CREATE_LOGO_SCREEN_BACK_BUTTON_PRESS_INTERSTITIAL == "2") {

                //load AppLovin
                if (interstitialAd!!.isReady)
                    interstitialAd!!.showAd()
                else {
                    interstitialAd!!.loadAd()
                    finish()
                    dialogExit.dismiss()
                }
            } else {
                finish()
                dialogExit.dismiss()
            }

        }

        cancel.setOnClickListener {
            dialogExit.dismiss()
        }

        dialogExit.show()

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()

        //finish the activity
        if (binding.stickerTextEdt.isVisible) {
            binding.stickerTextEdt.visibility = View.GONE
            binding.saveTextButton.visibility = View.GONE
        } else {
            if (backFragment is BackgroundFragment) {
                exitDialog()
            } else {
                //replace {background} fragment
                //setting view of selected item
                binding.backgroundIcon.setImageResource(R.drawable.background_selected)
                binding.backgroundName.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.theme_color
                    )
                )
                //normalizing views
                normalizeOtherViews("background")
                MainUtils.replaceFragment(
                    backgroundFragment,
                    supportFragmentManager,
                    R.id.container_fragment_activity_create_or_edit_template
                )
                backFragment = backgroundFragment
            }
        }
    }

    override fun deletePreviewItemClicked(clickedLogo: SavedLogo, typeLogo: String) {

        //nothing for
    }

    override fun onAdLoaded(p0: MaxAd?) {}
    override fun onAdDisplayed(p0: MaxAd?) {}
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAdHidden(p0: MaxAd?) {

        interstitialAd!!.loadAd()

        if (exitClicked) {

            exitClicked = false
            finish()
            dialogExit.dismiss()
        } else if (downloadClicked) {

            downloadClicked = false
            //save the current sticker to the folder in the hard drive storage
            Utils.convertBitmapToPNGAndSave(this@CreateOrEditTemplateActivity, binding.stickerView)
        }
    }
    override fun onAdClicked(p0: MaxAd?) {}
    override fun onAdLoadFailed(p0: String?, p1: MaxError?) {}
    override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {}
    override fun onAdExpanded(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    override fun onAdCollapsed(p0: MaxAd?) {
        TODO("Not yet implemented")
    }

    //Ads
    private fun Banner1Ads() {

        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        } else {
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner1.addView(adView)
            adView.loadAd(adRequest)
        }
    }
    private fun Banner2Ads() {

        if (BuildConfig.DEBUG) {

            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_DEBUG
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        } else {
            val adaptiveAdSize: AdSize = MainUtils.getAdaptiveAdSize(this, resources)
            val adView = AdView(this)
            adView.adUnitId = LogoMakerApp.BANNER_AD_ADMOB_ID_RELEASE
            adView.setAdSize(adaptiveAdSize)
            binding.adaptiveBanner2.addView(adView)
            adView.loadAd(adRequest)
        }
    }

    //Interstitial Ad
    private fun setAd() {
        if (BuildConfig.DEBUG) {

            InterstitialAd.load(
                this,
                LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_DEBUG,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdError", adError.toString())
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("AdError", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
        } else {

            InterstitialAd.load(
                this,
                LogoMakerApp.INTERSTITIAL_AD_ADMOB_ID_RELEASE,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("AdError", adError.toString())
                        mInterstitialAd = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d("AdError", "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
        }
    }

    private fun BannerAdAppLovinTop() {

        adViewTop = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewTop.setListener(this)
        //preparing the AdView
        adViewTop.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView2.addView(adViewTop)
    }

    private fun BannerAdAppLovinBottom() {

        adViewBottom = MaxAdView(resources.getString(R.string.bannerAd), this)
        adViewBottom.setListener(this)
        //preparing the AdView
        adViewBottom.layoutParams = binding.applovinAdView2.layoutParams

        binding.applovinAdView1.addView(adViewBottom)
    }
}