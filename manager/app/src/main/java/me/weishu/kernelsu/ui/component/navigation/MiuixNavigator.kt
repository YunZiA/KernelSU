package me.weishu.kernelsu.ui.component.navigation

import android.util.Log
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.navigation.DestinationsNavOptionsBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.Direction


fun ResultBackNavigator<Boolean>.navigateBackEx(result: Boolean){
    routePopupState.apply {
        if (isNotEmpty()) {
            Log.d("routePopupState", "navigateBackEx: last - ${keys.last()}")
            remove(keys.last())
        }
    }
    navigateBack(result)
}

fun DestinationsNavigator.popBackStackEx(){
    routePopupState.apply {
        if (isNotEmpty()) {
            Log.d("routePopupState", "popBackStackEX: last - ${keys.last()}")
            remove(keys.last())
        }
    }
    popBackStack()
}
fun DestinationsNavigator.navigateEx(
    direction: Direction,
    builder: DestinationsNavOptionsBuilder.() -> Unit
){
    routePopupState.apply {
        set(keys.last(),true)
        Log.d("routePopupState", "navigateEx: last - ${keys.last()}")
        set(direction.route,false)
        Log.d("routePopupState", "navigateEx: ${direction.route}")
    }
    navigate(direction,builder)
}

fun DestinationsNavigator.navigateEx(
    direction: Direction,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
){
    routePopupState.apply {
        set(keys.last(),true)
        Log.d("routePopupState", "navigateEx: last - ${keys.last()}")
        set(direction.route,false)
        Log.d("routePopupState", "navigateEx: ${direction.route}")
    }
    navigate(direction,navOptions,navigatorExtras)

}
