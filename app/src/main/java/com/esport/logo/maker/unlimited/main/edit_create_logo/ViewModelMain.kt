package com.esport.logo.maker.unlimited.main.edit_create_logo

import android.graphics.Bitmap
import android.text.Layout.Alignment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.FontItem
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.Image
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.StickerBackgroundState
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.models.helper.UndoRedoStack
import com.esport.logo.maker.unlimited.main.edit_create_logo.data_injection.repository.Repository
import com.xiaopo.flying.sticker.Sticker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(private val repository: Repository) : ViewModel() {

    //load or not to load the template list in the list of graphics
    var templateEnabled: Boolean = false

    //This object tells that 'sticker' button was clicked or 'background' was clicked!
    var imageAsBackgroundIsClicked = true
    //background fragment
    private var graphicsLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()
    private var texturesLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()
    private var shapesLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()
    //text fragment
    private var fontsLiveData: MutableLiveData<ArrayList<FontItem>> = MutableLiveData()
    //effects fragment
    private var effectsLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()
    //logos fragment
    private var logosLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()
    //elements fragment
    private var elementsLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()

    //{This is for the templates list to show it in the template portion}
    //templates portion liveData
    private var templatesListLiveData: MutableLiveData<ArrayList<Image>> = MutableLiveData()

    //data holding variables
    var gradientSelected = false
    var colorWasSelected = false
    var gradientColor1 = 0
    var gradientColor2 = 0
    var listName = ""
    var imageItem: Image? = null
    var colorItem = 0
    var selectedFontItem: FontItem = FontItem(null)
    var selectedColor = 0
    var alignment: Alignment = Alignment.ALIGN_CENTER

    var graphicsTabSelected = false
    var textureTabSelected = false
    var colorTabSelected = false
    var shapesTabSelected = false
    var gradientTabSelected = false

    var selectedImageShapeApplied: String = ""
    var selectedImageShapeAppliedWithEffect: String = ""
    var selectedImageSimple: String = ""
    var selectedImageSimpleWithEffect: String = ""
    var selectedShapeToAdd: Int? = 0
    var backgroundImageFromGallery: String = ""
    var selectedGradientType: String = ""
    var selectedAlphaColorValue: Float = 0f
    var selectedAlphaEffectValue: Int = 0
    var gradientApplied = false
    var shadowColor = 0
    var shadowRadius = 0
    var shadowClicked = false

    var selectedEffect: Image? = null
    var background: Bitmap? = null

    var backgroundEffect = ""

    //undo redo array
    var undoRedoArray: ArrayList<UndoRedoStack> = ArrayList()

    //getters
    fun getElementsLiveData() : MutableLiveData<ArrayList<Image>>{
        return elementsLiveData
    }

    fun getLogosLiveData() : MutableLiveData<ArrayList<Image>>{
        return logosLiveData
    }

    fun getEffectsLiveData() : MutableLiveData<ArrayList<Image>>{
        return effectsLiveData
    }

    fun getFontsLiveData() : MutableLiveData<ArrayList<FontItem>>{
        return fontsLiveData
    }

    fun graphicListLiveData(): MutableLiveData<ArrayList<Image>> {
        return graphicsLiveData
    }

    fun textureListLiveData(): MutableLiveData<ArrayList<Image>> {
        return texturesLiveData
    }

    fun shapeListLiveData(): MutableLiveData<ArrayList<Image>> {
        return shapesLiveData
    }

    fun templateListLiveData() :MutableLiveData<ArrayList<Image>>{
        return templatesListLiveData
    }

    // These functions are called in the editing activity
    //get graphics list
    fun graphicsList() {
        val list = repository.getDataBackgroundImagesList()
        graphicsLiveData.postValue(list)
    }
    //get graphics list
    fun texturesList() {

        val list = repository.getDataTextureImagesList()
        texturesLiveData.postValue(list)
    }
    //get shapes list
    fun shapesList(){
        val list = repository.getDataShapeImagesList()
        shapesLiveData.postValue(list)
    }
    //get fonts list
    fun fontsList(){
        val list = repository.getFontsListForStickers()
        fontsLiveData.postValue(list)
    }
    //get effects list
    fun effectsList(){
        val list = repository.getDataEffectsImageList()
        effectsLiveData.postValue(list)
    }
    //get logos list
    fun logosList(){
        val list = repository.getDataLogosStickerList()
        logosLiveData.postValue(list)
    }
    //get logos list
    fun elementsList(){
        val list = repository.getDataElementsStickerList()
        elementsLiveData.postValue(list)
    }

    //get Templates List to edit
    fun templatesList(){
        val list = repository.getDataTemplatesStickerList()
        templatesListLiveData.postValue(list)
    }
}