package pl.put.poznan.student.mp160164.szlok


import android.icu.text.DecimalFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pl.put.poznan.student.mp160164.szlok.data.Favourite
import pl.put.poznan.student.mp160164.szlok.data.Trail
import pl.put.poznan.student.mp160164.szlok.data.TrailTime
import pl.put.poznan.student.mp160164.szlok.logic.FavController
import pl.put.poznan.student.mp160164.szlok.ui.theme.SzlokTheme
import pl.put.poznan.student.mp160164.szlok.viewModels.DetailListViewModel
import pl.put.poznan.student.mp160164.szlok.viewModels.MainListViewModel
import pl.put.poznan.student.mp160164.szlok.viewModels.StopwatchViewModel
import kotlin.collections.find
import kotlin.text.isEmpty

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SzlokTheme(dynamicColor = false) {
                Main()
            }
        }
    }
}

@Composable
fun Main(){
    val navController = rememberNavController()
    val mainViewModel: MainListViewModel = viewModel()
    val detailViewModel: DetailListViewModel = viewModel()
    val stopwatchViewModel: StopwatchViewModel = viewModel()
    val favController: FavController= viewModel()

    NavHost(navController=navController, startDestination="mainList"){
        composable("mainList"){
            MainList(navController=navController, mainViewModel, stopwatchViewModel, favController)
        }
        composable("detailList/{type}/{id}"){backStackEntry->
            val id=backStackEntry.arguments?.getString("id")
            Log.d("Nav", "to trail $id")

            val trails=mainViewModel.list.collectAsState().value
            var trail: Trail? = null

            if(id!=null){
                val trailList=trails.find{ list -> list.find{it.id==id}!=null}
                if (trailList!=null) {
                    Log.d("Nav", "Looking for $id")
                    trail = trailList.find { it.id == id }
                }
            }

            if(trail==null){
                Log.d("Detail list","Couldn't find the trail!!! Using a fallback to prevent crashing.")
                trail=Trail("err", "Błąd", "błędna", 5,"Podczas nawigacji wystąpił błąd. Przejdź do głównego ekranu i spróbuj ponownie.")
            }

            detailViewModel.changeTrail(trail)
            DetailList(navController=navController, detailViewModel, stopwatchViewModel, favController)
        }
        composable("favouriteList"){
            val trails=mainViewModel.list.collectAsState().value
            val favs by favController.favs.observeAsState(emptyList())
            val favIds=favs.map{it.trailId}

            val curFavs: MutableList<Trail> = mutableListOf()
            trails.forEach{ list ->
                list.filter{favIds.contains(it.id)}.forEach{curFavs+=it}
            }

            val favList=curFavs.toList().sortedBy{it.name}

            FavouriteList(navController, mainViewModel, stopwatchViewModel, favList, favController)
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainList(navController: NavController, viewModel: MainListViewModel, stopwatch: StopwatchViewModel, favController: FavController){
    val pieszeActive=viewModel.pieszeActive.collectAsState().value
    val roweroweActive=viewModel.roweroweActive.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    val lists=viewModel.list.collectAsState().value

    var idx=0

    if(roweroweActive){
        idx=1
    }

    val tabIndex=remember{mutableStateOf(idx)}
    val pagerState= rememberPagerState(idx, pageCount = {2})

    val windowClass=currentWindowAdaptiveInfo().windowSizeClass
    val wide=windowClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    var columns=1
    if(wide)columns=2

    val stopwatchTrailId=stopwatch.timerTrail.collectAsState().value
    val stopwatchTrailList=lists.find{ list -> list.find{it.id==stopwatchTrailId}!=null}
    var stopwatchTrail: Trail? = null
    if (stopwatchTrailList!=null){
        stopwatchTrail=stopwatchTrailList.find{it.id==stopwatchTrailId}
        if(stopwatchTrail!=null) Log.d("FAB","Found a trail to hop back to: ${stopwatchTrail.name}")
    }
    else Log.d("FAB", "No trail to hop back to")

    val shouldAnimate=viewModel.shouldAnimate.collectAsState().value
    val loaded=viewModel.loaded.collectAsState().value

    var fadeout by remember {mutableStateOf(false)}
    val alfa: Float by animateFloatAsState(
        targetValue=if(!fadeout) 1.0f else 0.0f,
        label="opacity",
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        finishedListener = {
            if(fadeout) fadeout=false
        }
    )

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val color by infiniteTransition.animateColor(
        initialValue = Color(0xFF00BCD4),
        targetValue = Color(0xFF3F51B5),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    val favs by favController.favs.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.startSync()
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page -> run{
            tabIndex.value=page
            if(page==0){
                viewModel.toStopa()
            }
            else viewModel.toRower()
        }
        }
    }

    LaunchedEffect(loaded){
        if(loaded && shouldAnimate){
            fadeout=true
            viewModel.doneAnimating()
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet{
                Text("Menu", modifier = Modifier.padding(16.dp))
                HorizontalDivider()

                Spacer(Modifier.height(5.dp))
                NavigationDrawerItem(
                    icon = {Icon(painter=painterResource(R.drawable.hiking_24px),
                        contentDescription="Pieszy"
                    )},
                    label = { Text(text = "Szlaki piesze") },
                    selected = pieszeActive,
                    onClick = {
                        tabIndex.value=0
                        coroutineScope.launch {
                            drawerState.apply {
                                if (!isClosed) close()
                            }
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )

                Spacer(Modifier.height(5.dp))
                NavigationDrawerItem(
                    icon = {Icon(painter=painterResource(R.drawable.directions_bike_24px),
                        contentDescription="Rowerowy"
                    )},
                    label = { Text(text = "Szlaki rowerowe") },
                    selected = roweroweActive,
                    onClick = {
                        tabIndex.value=1
                        coroutineScope.launch {
                            drawerState.apply {
                                if (!isClosed) close()
                            }
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                Spacer(Modifier.height(5.dp))
                NavigationDrawerItem(
                    icon = {Icon(Icons.Filled.Favorite,
                        contentDescription="Ulubione"
                    )},
                    label = { Text(text = "Ulubione szlaki") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch{
                            drawerState.apply {
                                if (!isClosed) close()
                            }
                        }
                        navController.navigate("favouriteList")
                    }
                )
            }
        },
    ) {
        Box(
            modifier=Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )
        {
            Scaffold(
                modifier=Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title={Text("Szlaki")},
                        navigationIcon = {
                            IconButton(onClick={
                                    coroutineScope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }
                            ){
                                Icon(imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if(stopwatchTrail!=null){
                        Log.d("FAB", "Showing FAB for trail ${stopwatchTrail.name}")
                        FloatingActionButton(
                            onClick={
                                navController.navigate(
                                    "detailList/${stopwatchTrail.type}/${stopwatchTrail.id}"
                                )
                            },
                            containerColor = ButtonDefaults.buttonColors().containerColor
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.timer_24px),
                                contentDescription = "Powrót do szlaku",
                                tint = ButtonDefaults.buttonColors().contentColor
                            )
                        }
                    }
                }
            ){innerPadding->
                Column(modifier=Modifier.padding(innerPadding)){
                    TabRow(selectedTabIndex = tabIndex.value){
                        Tab(selected = pieszeActive,
                            onClick={
                                tabIndex.value=0
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            text={Text("Piesze")},
                            icon={
                                Icon(painter=painterResource(R.drawable.hiking_24px),
                                    contentDescription="Pieszy"
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                        Tab(selected = roweroweActive,
                            onClick={
                                tabIndex.value=1
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            text={Text("Rowerowe")},
                            icon={
                                Icon(painter=painterResource(R.drawable.directions_bike_24px),
                                    contentDescription="Rower"
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalPager(pagerState){
                            page -> run{
                        val query = viewModel.queries.collectAsState().value[page]
                        val filtered = (
                                if (query.isEmpty())lists[page]
                                else {
                                    lists[page].filter { (it.name.contains(query, ignoreCase = true)||it.description.contains(query, ignoreCase = true)) }
                                }
                                )
                        NavigableList(
                            columns,
                            onClick=fun (trail: Trail){
                                navController.navigate("detailList/${trail.type}/${trail.id}")
                            },
                            filtered,
                            query,
                            {viewModel.changeQuery(page, it)},
                            favs.map{it.trailId},
                            fun (id:String){favController.toggleFav(id)}
                        )
                    }
                    }
                }
            }

            if(shouldAnimate || fadeout){
                Box(
                    modifier=Modifier.fillMaxSize()
                        .graphicsLayer{alpha = alfa}
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center

                ){
                    Box(
                        modifier=Modifier.size(150.dp, 150.dp)
                            .background(color, shape= RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            painterResource(R.drawable.globe_100px),
                            contentDescription = "Świat",
                            modifier = Modifier.width(100.dp),
                            tint= Color.White
                        )
                        Icon(
                            painterResource(R.drawable.hiking_24px),
                            contentDescription = "Człek",
                            modifier = Modifier.width(25.dp)
                                .rotate(rotation)
                                .offset(0.dp, (-50).dp),
                            tint= Color.White
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun CustIconButton(image: Painter, desc: String, onClick: ()->Unit, active: Boolean){

    var color= ButtonDefaults.buttonColors()
    if(!active){
        color= ButtonDefaults.filledTonalButtonColors()
    }
    Button(onClick=onClick,
        colors=color
    ){
        Icon(
            painter = image,
            contentDescription=desc
        )
    }
}

@Composable
fun NavigableListItem(item: Trail, onClick: (trail: Trail)->Unit, favs:List<String>, favToggle: (id: String)->Unit){
    val itemShape=RoundedCornerShape(5.dp)
    val image=item.image
    val imageModifier=Modifier
        .fillMaxHeight(0.9f)
        .aspectRatio(16.dp / 9.dp)
    var diff=""
    for(i in 0..4){
        if(i<item.difficulty)diff+="★"
        else diff+="☆"
    }

    val isFav=favs.contains(item.id)
    var heartPainter=Icons.Filled.FavoriteBorder
    if(isFav)heartPainter=Icons.Filled.Favorite

    Box(
        modifier=Modifier.fillMaxWidth(0.95f)
            .clickable { onClick(item) },
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(100.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = itemShape),
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(Modifier.width(5.dp))
            Box(
                modifier=imageModifier,
                contentAlignment = Alignment.Center
            ){
                if (image=="placeholder"||image==""){
                    Image(
                        painter = painterResource(R.drawable.placeholder),
                        contentDescription = "Zdjęcie szlaku",
                        contentScale = ContentScale.FillBounds
                    )
                }
                else{
                    val loading = remember{mutableStateOf(false)}

                    if (loading.value) CircularProgressIndicator()

                    AsyncImage(
                        model=image,
                        contentDescription = "Zdjęcie szlaku",
                        error = painterResource(R.drawable.placeholder),
                        fallback = painterResource(R.drawable.placeholder),
                        onLoading = { loading.value = true },
                        onSuccess = { loading.value = false },
                        onError = { loading.value = false },
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )
            {
                Text(
                    text = "${item.name}",
                    fontSize = 4.em,
                    color=MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Trudność: $diff",
                    fontSize = 3.em,
                    color=MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Icon(
                heartPainter,
                contentDescription = "Ulubione",
                tint= MaterialTheme.colorScheme.onPrimaryContainer,
                modifier=Modifier.clickable(
                    onClick={favToggle(item.id)}
                )
            )
            Spacer(Modifier.width(5.dp))

        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigableList(columns: Int, onClick: (trail: Trail)->Unit, list: List<Trail>, query: String, onSearch: (query: String)->Unit, favs:List<String>, favToggle: (id: String)->Unit){
    Log.d("List", "Got ${list.size} elements")
    Column(Modifier.fillMaxSize()){
        Box(
            modifier=Modifier.fillMaxWidth()
                .height(56.dp)
                .padding(horizontal=8.dp)
        ){
            SearchBar(
                modifier=Modifier.fillMaxSize(),
                inputField = { SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onSearch,
                    onSearch = onSearch,
                    placeholder = {Text("Wyszukaj szlak...")},
                    leadingIcon = {Icon(painterResource(R.drawable.search_24px), contentDescription = "Wyszukaj")},
                    expanded=false,
                    onExpandedChange = {}
                )},
                expanded=false,
                onExpandedChange = {},
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0,0,0,0)
            ){}
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns= GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            items(list.size){item -> NavigableListItem(list[item], onClick, favs, favToggle)}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailList(navController: NavController, viewController: DetailListViewModel, stopwatchViewModel: StopwatchViewModel, favController: FavController){
    val trail=viewController.trail.collectAsState().value
    val id=trail.id
    val name=trail.name
    val image=trail.image

    val times = stopwatchViewModel.trailTimes.observeAsState(emptyList())

    val windowClass=currentWindowAdaptiveInfo().windowSizeClass
    val wide=windowClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    val favs by favController.favs.observeAsState(emptyList())

    Scaffold(
        topBar = {TopAppBar(
            title={Text("Szczegóły szlaku")},
            navigationIcon={GoBack(onClick={
                if(!navController.popBackStack())
                    navController.popBackStack("mainList", false)
            })}
        )
        }
    ){innerPadding->run{
        if(wide){
            Row(modifier=Modifier.fillMaxSize()){
                Column(
                    modifier=Modifier.fillMaxWidth(0.5f)
                        .padding(innerPadding)
                        .background(color=MaterialTheme.colorScheme.primaryContainer, shape=RoundedCornerShape(5.dp)),
                    verticalArrangement = Arrangement.Center
                )
                {
                    Spacer(modifier=Modifier.weight(1f))
                    ImageAssembly(
                        image,
                        name,
                        id,
                        favs.map{it.trailId},
                        fun (id: String){favController.toggleFav(id)}
                    )
                    Spacer(modifier=Modifier.weight(1f))
                }
                Column(modifier=Modifier.fillMaxWidth()
                    .padding(innerPadding))
                {
                    Column(
                        Modifier.verticalScroll(rememberScrollState())
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DescriptionColumn(times.value,trail)
                        Spacer(modifier=Modifier.weight(1f))
                    }

                    Box(contentAlignment = Alignment.Center) {
                        Stopwatch(stopwatchViewModel, trail)
                    }
                }
            }

        }
        else{
            Column(modifier=Modifier.padding(innerPadding)){
                ImageAssembly(
                    image,
                    name,
                    id,
                    favs.map{it.trailId},
                    fun (id: String){favController.toggleFav(id)}
                )
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                        .weight(1f)
                ) {

                    Spacer(modifier = Modifier.height(16.dp))
                    DescriptionColumn(times.value,trail)
                    Spacer(modifier=Modifier.weight(1f))
                }

                Box(contentAlignment = Alignment.Center) {
                    Stopwatch(stopwatchViewModel, trail)
                }
            }
        }
    }

    }
}

@Composable
fun ImageAssembly(image:String, name:String, trailId: String, favs: List<String>, favToggle: (id: String) -> Unit){
    val isFav=favs.contains(trailId)
    var heartPainter=Icons.Filled.FavoriteBorder
    if(isFav)heartPainter=Icons.Filled.Favorite

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.Center
    ) {
        if (image == "placeholder" || image == "") {
            Image(
                painter = painterResource(R.drawable.placeholder),
                contentDescription = "Zdjęcie szlaku",
                contentScale = ContentScale.FillBounds
            )
        } else {
            val loading = remember { mutableStateOf(false) }
            AsyncImage(
                model = image,
                contentDescription = "Zdjęcie szlaku",
                error = painterResource(R.drawable.placeholder),
                fallback = painterResource(R.drawable.placeholder),
                onLoading = { loading.value = true },
                onSuccess = { loading.value = false },
                onError = { loading.value = false },
                contentScale = ContentScale.FillBounds
            )

            if (loading.value) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight()
                )
            }
        }
        OutlinedText(
            text = name,
            outlineColor = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.em,
            lineHeight = 1.em,
            textAlign = TextAlign.Center,
            background = Modifier.background(Color(0.75f, 0.75f, 0.75f, 0.66f))
        )
        Icon(
            heartPainter,
            contentDescription = "Ulubione",
            tint= MaterialTheme.colorScheme.onPrimaryContainer,
            modifier=Modifier.clickable(
                onClick={favToggle(trailId)}
            )
                .align(Alignment.TopEnd)
                .background(
                    Color(0.75f, 0.75f, 0.75f, 0.66f),
                    shape=RoundedCornerShape(0.dp,0.dp, 0.dp, 5.dp)
                )
        )

    }
}

@Composable
fun DescriptionColumn(times: List<TrailTime>, trail: Trail){
    var diff=""
    for(i in 0..4){
        if(i<trail.difficulty)diff+="★"
        else diff+="☆"
    }

    val type=trail.type
    val description=trail.description

    Column{
        Text(
            text = "Ścieżka $type\nTrudność: $diff",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            lineHeight = 1.em,
            fontSize = 6.em
        )

        Text(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Justify,
            fontSize = 4.em
        )


        Column {
            val timesFilt = times.filter { it.trailId == trail.id }
            if (timesFilt.isNotEmpty()) {
                val filtered = timesFilt.sortedBy{ it.time }
                var maxId=10
                if(filtered.size<maxId) maxId=filtered.size
                val best=filtered.subList(0, maxId)
                Text(
                    text = "Najlepsze czasy dla tej ścieżki:",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal=16.dp),
                    textAlign = TextAlign.Justify,
                    fontSize = 4.em
                )
                var idx=1
                best.forEach {
                    Text(
                        text = "${idx}. ${formatTime(it.time)} dnia ${it.date}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal=16.dp),
                        textAlign = TextAlign.Justify,
                        fontSize = 4.em
                    )
                    idx+=1
                }
            }
        }
    }
}

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    outlineColor: Color,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    background: Modifier?=null
){
    var specialModifier: Modifier
    if (background!=null)specialModifier = modifier.then(background)
    else specialModifier= modifier

    Text(
        text = text,
        modifier = specialModifier.then(Modifier.offset(1.dp, 1.dp)),
        color=outlineColor,
        fontSize=fontSize,
        fontStyle=fontStyle,
        fontWeight = fontWeight,
        fontFamily=fontFamily,
        letterSpacing=letterSpacing,
        textDecoration=textDecoration,
        textAlign=textAlign,
        lineHeight = lineHeight,
        overflow=overflow,
        softWrap=softWrap,
        maxLines= maxLines,
        minLines=minLines,
        onTextLayout=onTextLayout,
        style=style
    )
    Text(
        text = text,
        modifier = modifier.then(Modifier.offset(-1.dp, 1.dp)),
        color=outlineColor,
        fontSize=fontSize,
        fontStyle=fontStyle,
        fontWeight = fontWeight,
        fontFamily=fontFamily,
        letterSpacing=letterSpacing,
        textDecoration=textDecoration,
        textAlign=textAlign,
        lineHeight = lineHeight,
        overflow=overflow,
        softWrap=softWrap,
        maxLines= maxLines,
        minLines=minLines,
        onTextLayout=onTextLayout,
        style=style
    )
    Text(
        text = text,
        modifier = modifier.then(Modifier.offset(1.dp, -1.dp)),
        color=outlineColor,
        fontSize=fontSize,
        fontStyle=fontStyle,
        fontWeight = fontWeight,
        fontFamily=fontFamily,
        letterSpacing=letterSpacing,
        textDecoration=textDecoration,
        textAlign=textAlign,
        lineHeight = lineHeight,
        overflow=overflow,
        softWrap=softWrap,
        maxLines= maxLines,
        minLines=minLines,
        onTextLayout=onTextLayout,
        style=style
    )
    Text(
        text = text,
        modifier = modifier.then(Modifier.offset(-1.dp, -1.dp)),
        color=outlineColor,
        fontSize=fontSize,
        fontStyle=fontStyle,
        fontWeight = fontWeight,
        fontFamily=fontFamily,
        letterSpacing=letterSpacing,
        textDecoration=textDecoration,
        textAlign=textAlign,
        lineHeight = lineHeight,
        overflow=overflow,
        softWrap=softWrap,
        maxLines= maxLines,
        minLines=minLines,
        onTextLayout=onTextLayout,
        style=style
    )
    Text(
        text = text,
        modifier = modifier,
        color=color,
        fontSize=fontSize,
        fontStyle=fontStyle,
        fontWeight = fontWeight,
        fontFamily=fontFamily,
        letterSpacing=letterSpacing,
        textDecoration=textDecoration,
        textAlign=textAlign,
        lineHeight = lineHeight,
        overflow=overflow,
        softWrap=softWrap,
        maxLines= maxLines,
        minLines=minLines,
        onTextLayout=onTextLayout,
        style=style
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoBack(onClick: ()->Unit){
    IconButton(onClick=onClick){
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back")
    }
}

fun formatTime(seconds: Int): String{
    val format= DecimalFormat("00")

    val secs= format.format(seconds%60)
    val minutes=format.format(seconds.div(60))
    val hours=format.format(seconds.div(3600))
    return "$hours:$minutes:$secs"
}

@Composable
fun Stopwatch(viewModel: StopwatchViewModel, trail: Trail, showName: Boolean=false){
    val time=viewModel.time.collectAsState().value
    val state=viewModel.state.collectAsState().value
    val timerTrail=viewModel.timerTrail.collectAsState().value
    val timerTrailName = viewModel.timerTrailName.collectAsState().value

    val formattedTime=formatTime(time)

    var text=""
    if ((!(timerTrail==trail.id || timerTrail==""))||showName) text="Czas dla szlaku $timerTrailName:\n "
    text+=formattedTime

    Box(
        modifier=Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier=Modifier
                .fillMaxWidth(0.95f)
                .padding()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(5.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(Modifier.height(8.dp))
            Row(modifier=Modifier.fillMaxWidth()){
                Text(
                    text=text,
                    modifier=Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize=6.em,
                    lineHeight=1.em
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()){
                var start= painterResource(R.drawable.timer_24px)
                if (state== StopwatchViewModel.State.STOPPED)start=painterResource(R.drawable.timer_play_24px)
                val pause= painterResource(R.drawable.timer_pause_24px)
                val save= painterResource(R.drawable.save_24px)
                val reset= painterResource(R.drawable.timer_off_24px)

                Spacer(Modifier.weight(1f))
                CustIconButton(start, "Start", {viewModel.startTimer(trail.id)},
                    (state==StopwatchViewModel.State.STOPPED && (timerTrail==trail.id || timerTrail=="")))
                Spacer(Modifier.weight(1f))
                CustIconButton(pause,"Przerwa", {viewModel.pauseTimer(trail.id)},
                    (state==StopwatchViewModel.State.STARTED && (timerTrail==trail.id || timerTrail=="")))
                Spacer(Modifier.weight(1f))
                CustIconButton(save, "Zapisz", {viewModel.saveTime()}, true)
                Spacer(Modifier.weight(1f))
                CustIconButton(reset, "Reset", {viewModel.resetTimer()}, true)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteList(navController: NavController, mainListViewModel: MainListViewModel, stopwatch: StopwatchViewModel, favs: List<Trail>, favController: FavController){

    val lists=mainListViewModel.list.collectAsState().value
    val stopwatchTrailId=stopwatch.timerTrail.collectAsState().value
    val stopwatchTrailList=lists.find{ list -> list.find{it.id==stopwatchTrailId}!=null}
    var stopwatchTrail: Trail? = null
    if (stopwatchTrailList!=null){
        stopwatchTrail=stopwatchTrailList.find{it.id==stopwatchTrailId}
        if(stopwatchTrail!=null) Log.d("FAB","Found a trail to hop back to: ${stopwatchTrail.name}")
    }
    else Log.d("FAB", "No trail to hop back to")

    val windowClass=currentWindowAdaptiveInfo().windowSizeClass
    val wide=windowClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    val curFavs by favController.favs.observeAsState(emptyList())

    var columns=1
    if(wide)columns=2

    Scaffold(
        modifier=Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title={Text("Ulubione szlaki")},
                navigationIcon={GoBack(onClick={navController.popBackStack("mainList", false)})}
            )
        },
        floatingActionButton = {
            if(stopwatchTrail!=null){
                Log.d("FAB", "Showing FAB for trail ${stopwatchTrail.name}")
                FloatingActionButton(
                    onClick={
                        navController.navigate(
                            "detailList/${stopwatchTrail.type}/${stopwatchTrail.id}"
                        )
                    },
                    containerColor = ButtonDefaults.buttonColors().containerColor
                ) {
                    Icon(
                        painter = painterResource(R.drawable.timer_24px),
                        contentDescription = "Powrót do szlaku",
                        tint = ButtonDefaults.buttonColors().contentColor
                    )
                }
            }
        }
    ){
        innerPadding->
        Column(
            modifier=Modifier.fillMaxSize()
                .padding(innerPadding))
        {
            var query by rememberSaveable { mutableStateOf("") }
            val filtered = (
                    if (query.isEmpty()) favs
                    else {
                        favs.filter {
                            (it.name.contains(
                                query,
                                ignoreCase = true
                            ) || it.description.contains(query, ignoreCase = true))
                        }
                    }
                    )
            NavigableList(
                columns,
                onClick = fun(trail: Trail) {
                    navController.navigate("detailList/${trail.type}/${trail.id}")
                },
                filtered,
                query,
                { query = it },
                curFavs.map { it.trailId },
                fun(id: String) { favController.toggleFav(id) }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    SzlokTheme {
        Main()
    }
}