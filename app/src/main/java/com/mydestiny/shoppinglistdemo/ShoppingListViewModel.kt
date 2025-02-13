package com.mydestiny.shoppinglistdemo

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class SortOrder {
    ALPHABETICAL,
    AISLE
}
data class ShoppingItem(
    val id: Int,
    val name: String,
    val aisle: Int,
    val iconResId: Int,
    var isPurchased: Boolean = false
)



class ShoppingListViewModel(private val context: Context ) :ViewModel(){

    private var soundPool: SoundPool? = null  // Make it nullable
    private val soundMap: MutableMap<Int, Int> = mutableMapOf()  // Map resource ID to loaded sound ID
    private var isSoundPoolLoaded = false

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        isSoundPoolLoaded = true


    }


    private fun preloadSound(soundResId: Int) {
        if (!isSoundPoolLoaded) {
            Log.w("SoundPool", "SoundPool not yet initialized.  Cannot load sound.")
            return
        }

        val soundId = soundPool?.load(context, soundResId, 1)

        soundId?.let {
            soundMap[soundResId] = it
        } ?: run {
            Log.e("SoundPool", "Failed to load sound resource $soundResId")
        }
    }



    private fun playSound(soundResId: Int) {
        if (!isSoundPoolLoaded) {
            Log.w("SoundPool", "SoundPool not yet initialized. Cannot play sound.")
            return
        }

        val soundId = soundMap[soundResId] ?: run {
            Log.w("SoundPool", "Sound not loaded: $soundResId.  Loading now...")
            preloadSound(soundResId)  // Load on demand
            return // Return as preloadSound will be called asyncronously and playSound() must be called again
        }
        // Sound ID found. Play the sound:
        val streamId = soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
        streamId?.let {
            Log.d("SoundPool", "Playing sound with stream ID: $streamId")
        } ?: Log.e("SoundPool", "Failed to play sound.  Maybe stream limit reached?")

    }




    private var _shoppingList = MutableStateFlow (

        listOf(

        ShoppingItem(1, "Enchanted Bread", 1, R.drawable.bread),
        ShoppingItem(2, "Dragon Eggs", 1, R.drawable.egg),
        ShoppingItem(3, "Goblin Greens", 1, R.drawable.vegetable),
        ShoppingItem(4, "Potion Ingredients", 4, R.drawable.potion),
        ShoppingItem(5, "Mystical Cheese", 3, R.drawable.cheese),
         ShoppingItem(6, "Magic Crystal Ball", 3, R.drawable.crystal_ball),
        ShoppingItem(7, "Aladdin Lamp", 3, R.drawable.lamp),
        ShoppingItem(8, "Voodoo Puppet", 2, R.drawable.vodo),
         ShoppingItem(9, "Witch Hat", 2, R.drawable.witch_hat) ,
        ShoppingItem(10, "Magical Stick", 2, R.drawable.magic_wand)

        )


     )


      var  shoppingList : StateFlow<List<ShoppingItem>>  = _shoppingList.asStateFlow()


    private var _cartList = MutableStateFlow   ( listOf<  ShoppingItem>  ()  )


    var  cartList  : StateFlow<List<ShoppingItem>>  = _cartList .asStateFlow()








    private val _sortOrder = MutableStateFlow (SortOrder.AISLE )


    val  sortOrder  : StateFlow<SortOrder >  = _sortOrder.asStateFlow()


    fun sortList()=viewModelScope.launch{

        when (_sortOrder.value) {
            SortOrder.ALPHABETICAL -> _sortOrder.value= SortOrder.AISLE

            SortOrder.AISLE -> _sortOrder.value=SortOrder.ALPHABETICAL
        }

        when (_sortOrder.value) {

            SortOrder.ALPHABETICAL -> _shoppingList.value=   _shoppingList.value.sortedBy { it.name }

            SortOrder.AISLE ->_shoppingList.value=  _shoppingList.value.sortedBy { it.aisle }
        }

    }

    fun toCart(shoppingItem :ShoppingItem)=viewModelScope.launch{


        val existingItemIndex = _cartList.value.indexOfFirst { it.id == shoppingItem.id }

        if(existingItemIndex == -1) {
            val a = _cartList.value.toMutableList()
            a.add(shoppingItem)
            _cartList.value = a.toList()

            val sound = when (shoppingItem.id) {
                4 -> R.raw.magic3
                10 -> R.raw.magic3
                8 -> R.raw.evil
                else -> R.raw.magic
            }
            playSound(sound)

        }else  {

            val a = _cartList.value.toMutableList()
            a.removeAt(existingItemIndex)
            _cartList.value = a.toList()

            playSound(R.raw.disappointment)
        }

    }





    fun selectedItem( shoppingItem :ShoppingItem)=viewModelScope.launch{


        val itemIndex = _shoppingList.value.indexOfFirst { it.id == shoppingItem.id }

        val a=    _shoppingList.value.toMutableList()
        a[itemIndex] = shoppingItem.copy(isPurchased = !shoppingItem.isPurchased)

        _shoppingList.value=a.toList()





    }

    override fun onCleared() {
        super.onCleared()
        soundPool?.release()
        soundPool = null
    }



}