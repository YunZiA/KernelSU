package me.weishu.kernelsu.ui.component.navigation

import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navigation.DestinationsNavOptionsBuilder
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.scope.DestinationScopeWithNoDependencies
import com.ramcosta.composedestinations.scope.resultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.RouteOrDirection

class MiuixDestinationsNavigator(
    private val destinationsNavigator: DestinationsNavigator,
    private val routePopupState: RoutePopupStack,
    private val temporaryAnimation: MutableState<DestinationStyle.Animated?>? = null
)  {
    fun navigate(
        direction: Direction,
        builder: DestinationsNavOptionsBuilder.() -> Unit
    ){
        routePopupState.apply {
            putLast( true)
            put(direction.route.substringBefore('/'),false)
        }
        destinationsNavigator.navigate(direction,builder)
    }

    fun navigate(
        direction: Direction,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ){
        routePopupState.apply {
            putLast( true)
            put(direction.route.substringBefore('/'),false)
        }
        destinationsNavigator.navigate(direction,navOptions,navigatorExtras)
    }


    fun navigateWithAnim(
        direction: Direction,
        animation: DestinationStyle.Animated,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ){
        routePopupState.apply {
            putLast( true)
            put(direction.route.substringBefore('/'),false)
        }
        temporaryAnimation?.run {
            temporaryAnimation.value = animation
        }
        destinationsNavigator.navigate(direction,navOptions,navigatorExtras)
    }

    @MainThread
    fun navigateUp(): Boolean{
        return destinationsNavigator.navigateUp()
    }

    @MainThread
    fun popBackStack(): Boolean{
        routePopupState.removeLast()
        return destinationsNavigator.popBackStack()
    }

    @MainThread
    fun popBackStack(
        route: RouteOrDirection,
        inclusive: Boolean,
        saveState: Boolean = false,
    ): Boolean{
        routePopupState.remove(route.route)
        return destinationsNavigator.popBackStack(route,inclusive,saveState)
    }

    @MainThread
    fun clearBackStack(route: RouteOrDirection): Boolean{
        routePopupState.clear()
        return destinationsNavigator.clearBackStack(route)
    }

    fun getBackStackEntry(
        route: RouteOrDirection
    ): NavBackStackEntry?{
        return  destinationsNavigator.getBackStackEntry(route)
    }


}

@Composable
fun <T> DestinationScope<T>.miuixDestinationsNavigator():MiuixDestinationsNavigator = MiuixDestinationsNavigator(destinationsNavigator,LocalRoutePopupStack.current)
