package com.mydestiny.shoppinglistdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mydestiny.shoppinglistdemo.ui.theme.ShoppingListDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingListDemoTheme {

                val viewModel =ShoppingListViewModel( this.applicationContext)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShoppingListApp(modifier = Modifier.padding(innerPadding),viewModel)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(modifier: Modifier,viewModel: ShoppingListViewModel
) {



    val shoppingList    by viewModel.shoppingList.collectAsState()

    val cartList    by viewModel.cartList.collectAsState()

    var showCartPopup by remember { mutableStateOf(false  ) }

    var cartButtonPosition by remember { mutableStateOf(Offset.Zero) }



    var isGridLayout by remember { mutableStateOf(true) }
    val sortOrder by viewModel.sortOrder.collectAsState()



    Surface( modifier = modifier){
        Column {
            TopAppBar(
                title = { Text(text = "Magical Shopping List") },
                actions = {
                    IconButton(onClick = { isGridLayout = !isGridLayout }) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.AutoMirrored.Filled.List
                            else Icons.AutoMirrored.Filled.Grading,
                            contentDescription = "Toggle Layout"
                        )
                    }
                    IconButton(onClick = {
                        viewModel.sortList()
                    }) {
                        Icon(
                            imageVector = if (sortOrder == SortOrder.ALPHABETICAL) Icons.Filled.SortByAlpha
                                    else Icons.Filled.Numbers,
                            contentDescription = "Toggle Sort Order"
                        )
                    }

                    IconButton(
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            cartButtonPosition = coordinates.localToWindow(Offset.Zero)
                        },
                        onClick = {
                        showCartPopup=!showCartPopup
                    }) {
                        Icon(
                            imageVector = if (sortOrder == SortOrder.ALPHABETICAL) Icons.Filled.ShoppingCart
                            else Icons.Outlined.ShoppingCart,
                            contentDescription = "Toggle Sort Order"
                        )
                    }
                }
            )
            if (showCartPopup) {




                CartPopup(
                    shoppingList = cartList,
                    onDismissRequest = { showCartPopup = false },
                            offset = IntOffset(cartButtonPosition.x.toInt(), cartButtonPosition.y.toInt())

                )
            }


            Box {
                androidx.compose.animation. AnimatedVisibility(
                    visible = isGridLayout,
                    enter = fadeIn(animationSpec = tween(durationMillis =1000)),
                    exit = fadeOut(animationSpec = tween(durationMillis =1000)), label = ""
                ) {
                    ShoppingListGrid(
                        shoppingList = shoppingList, onItemClick = { item ->
                        viewModel.selectedItem(item)
                        viewModel.toCart(item)
                    })
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isGridLayout,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 1000)), label = ""
                ) {
                    ShoppingListColumn(   shoppingList = shoppingList , onItemClick = { item ->
                        viewModel.selectedItem(item)
                        viewModel.toCart(item)
                    })
                }
            }


        }
    }
}




@Composable
fun ShoppingListGrid(
                     shoppingList: List<ShoppingItem>, onItemClick: (ShoppingItem) -> Unit) {
    LazyVerticalGrid(
        columns =   GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        items(shoppingList , key = {it.id}) { item ->
            ShoppingListItemCard(modifier = Modifier.animateItem(
                fadeInSpec = spring(),
                fadeOutSpec = spring()
            ), item = item, onItemClick = onItemClick, isList = false  )

        }
    }
}


@Composable
fun ShoppingListColumn(
                       shoppingList: List<ShoppingItem>, onItemClick: (ShoppingItem) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shoppingList, key = {it.id}) { item ->
            ShoppingListItemCard(modifier = Modifier.animateItem(
                fadeInSpec = spring(),
                fadeOutSpec = spring()
            ), item = item, onItemClick = onItemClick,  isList = true)
        }
    }
}

@Composable
fun ShoppingListItemCard(modifier: Modifier,

    item: ShoppingItem,
                         isList  :Boolean ,
                         onItemClick: (ShoppingItem) -> Unit) {
    val purchaseAlpha: Float by animateFloatAsState(
        targetValue = if (item.isPurchased) 0.5f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable { onItemClick(item) }
            .alpha(purchaseAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ) {
        if(isList) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.name,
                    modifier = Modifier.size(48.dp),

                    )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineLarge,
                         maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                    )
                    Text(text = "Aisle: ${item.aisle}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }else{
            Image(
                painter = painterResource(id = item.iconResId),
                contentDescription = item.name,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),

                )
            Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally ),
                    text = item.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineLarge,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                )
                Text( modifier = Modifier.align(Alignment.CenterHorizontally ),
                    text = "Aisle: ${item.aisle}", style = MaterialTheme.typography.bodySmall)

        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

      //  ShoppingListApp(Modifier)

}




@Preview
@Composable
fun ProductItem(item: ShoppingItem= ShoppingItem(2, "Dragon Eggs", 1, R.drawable.egg)

                ) {
    Card {
        Row( modifier = Modifier

            .padding(8.dp)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = item.iconResId),
                contentDescription = item.name,
                modifier = Modifier
                    .padding(start = 18.dp)
                    .size(12.dp)
                 )
            Text(text = item.name)

        }
    }
}


@Composable
fun CartPopup(

     shoppingList: List<ShoppingItem> =
                       listOf(
                     ShoppingItem(1, "Enchanted Bread", 3, R.drawable.bread),
                     ShoppingItem(2, "Dragon Eggs", 1, R.drawable.egg),
                                   )  ,
        onDismissRequest: () -> Unit={},
         offset: IntOffset,



) {
    Popup(
        onDismissRequest = onDismissRequest,
       offset =  offset,
        properties = PopupProperties(focusable = true) // Position
    ) {
        Card  (
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),

        ) {

                Text(modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Shopping Cart", style = MaterialTheme.typography.bodySmall )

    LazyColumn(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        items(shoppingList) { item ->
                            ProductItem(

                                item = item


                            )

                        }
                    }
                }
            }
        }


