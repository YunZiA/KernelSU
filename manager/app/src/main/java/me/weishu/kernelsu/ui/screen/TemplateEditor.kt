package me.weishu.kernelsu.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.result.ResultBackNavigator
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.profile.RootProfileConfig
import me.weishu.kernelsu.ui.util.deleteAppProfileTemplate
import me.weishu.kernelsu.ui.util.getAppProfileTemplate
import me.weishu.kernelsu.ui.util.setAppProfileTemplate
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel
import me.weishu.kernelsu.ui.viewmodel.toJSON
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * @author weishu
 * @date 2023/10/20.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun TemplateEditorScreen(
    navigator: ResultBackNavigator<Boolean>,
    initialTemplate: TemplateViewModel.TemplateInfo,
    readOnly: Boolean = true,
) {

    val isCreation = initialTemplate.id.isBlank()
    val autoSave = !isCreation

    var template by rememberSaveable {
        mutableStateOf(initialTemplate)
    }

    val scrollBehavior = MiuixScrollBehavior()

    BackHandler {
        navigator.navigateBack(result = !readOnly)
    }

    Scaffold(
        topBar = {
            val author =
                if (initialTemplate.author.isNotEmpty()) "@${initialTemplate.author}" else ""
            val readOnlyHint = if (readOnly) {
                " - ${stringResource(id = R.string.app_profile_template_readonly)}"
            } else {
                ""
            }
            val titleSummary = "${initialTemplate.id}$author$readOnlyHint"
            val saveTemplateFailed = stringResource(id = R.string.app_profile_template_save_failed)
            val context = LocalContext.current

            TopBar(
                title = if (isCreation) {
                    stringResource(R.string.app_profile_template_create)
                } else if (readOnly) {
                    stringResource(R.string.app_profile_template_view)
                } else {
                    stringResource(R.string.app_profile_template_edit)
                },
                readOnly = readOnly,
                summary = titleSummary,
                onBack = dropUnlessResumed { navigator.navigateBack(result = !readOnly) },
                onDelete = {
                    if (deleteAppProfileTemplate(template.id)) {
                        navigator.navigateBack(result = true)
                    }
                },
                onSave = {
                    if (saveTemplate(template, isCreation)) {
                        navigator.navigateBack(result = true)
                    } else {
                        Toast.makeText(context, saveTemplateFailed, Toast.LENGTH_SHORT).show()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .pointerInteropFilter {
                    // disable click and ripple if readOnly
                    readOnly
                }
        ) {
            if (isCreation) {
                var errorHint by remember {
                    mutableStateOf("")
                }
                val idConflictError = stringResource(id = R.string.app_profile_template_id_exist)
                val idInvalidError = stringResource(id = R.string.app_profile_template_id_invalid)
                TextEdit(
                    label = stringResource(id = R.string.app_profile_template_id),
                    text = template.id,
                    errorHint = errorHint,
                    isError = errorHint.isNotEmpty()
                ) { value ->
                    errorHint = if (isTemplateExist(value)) {
                        idConflictError
                    } else if (!isValidTemplateId(value)) {
                        idInvalidError
                    } else {
                        ""
                    }
                    template = template.copy(id = value)
                }
            }

            TextEdit(
                label = stringResource(id = R.string.app_profile_template_name),
                text = template.name
            ) { value ->
                template.copy(name = value).run {
                    if (autoSave) {
                        if (!saveTemplate(this)) {
                            // failed
                            return@run
                        }
                    }
                    template = this
                }
            }
            TextEdit(
                label = stringResource(id = R.string.app_profile_template_description),
                text = template.description
            ) { value ->
                template.copy(description = value).run {
                    if (autoSave) {
                        if (!saveTemplate(this)) {
                            // failed
                            return@run
                        }
                    }
                    template = this
                }
            }

            RootProfileConfig(
                fixedName = true,
                profile = toNativeProfile(template),
                onProfileChange = {
                    template.copy(
                        uid = it.uid,
                        gid = it.gid,
                        groups = it.groups,
                        capabilities = it.capabilities,
                        context = it.context,
                        namespace = it.namespace,
                        rules = it.rules.split("\n")
                    ).run {
                        if (autoSave) {
                            if (!saveTemplate(this)) {
                                // failed
                                return@run
                            }
                        }
                        template = this
                    }
                })
        }
    }
}

fun toNativeProfile(templateInfo: TemplateViewModel.TemplateInfo): Natives.Profile {
    return Natives.Profile().copy(
        rootTemplate = templateInfo.id,
        uid = templateInfo.uid,
        gid = templateInfo.gid,
        groups = templateInfo.groups,
        capabilities = templateInfo.capabilities,
        context = templateInfo.context,
        namespace = templateInfo.namespace,
        rules = templateInfo.rules.joinToString("\n").ifBlank { "" })
}

fun isTemplateValid(template: TemplateViewModel.TemplateInfo): Boolean {
    if (template.id.isBlank()) {
        return false
    }

    if (!isValidTemplateId(template.id)) {
        return false
    }

    return true
}

fun saveTemplate(template: TemplateViewModel.TemplateInfo, isCreation: Boolean = false): Boolean {
    if (!isTemplateValid(template)) {
        return false
    }

    if (isCreation && isTemplateExist(template.id)) {
        return false
    }

    val json = template.toJSON()
    json.put("local", true)
    return setAppProfileTemplate(template.id, json.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    readOnly: Boolean,
    summary: String = "",
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    scrollBehavior: ScrollBehavior
) {
//    Column {
//        Text(title)
//        if (summary.isNotBlank()) {
//            Text(
//                text = summary,
//                style = MaterialTheme.typography.bodyMedium,
//            )
//        }
//    }
//}
    TopAppBar(
        title = title, navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onBackground
                )
            }
        }, actions = {
            if (readOnly) {
                return@TopAppBar
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteForever,
                    contentDescription = stringResource(id = R.string.app_profile_template_delete),
                    tint = MiuixTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(id = R.string.app_profile_template_save),
                    tint = MiuixTheme.colorScheme.onBackground
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun TextEdit(
    label: String,
    text: String,
    errorHint: String = "",
    isError: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    ListItem(headlineContent = {
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            value = text,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            suffix = {
                if (errorHint.isNotBlank()) {
                    Text(
                        text = if (isError) errorHint else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
            }),
            onValueChange = onValueChange
        )
    })
}

private fun isValidTemplateId(id: String): Boolean {
    return Regex("""^([A-Za-z][A-Za-z\d_]*\.)*[A-Za-z][A-Za-z\d_]*$""").matches(id)
}

private fun isTemplateExist(id: String): Boolean {
    return getAppProfileTemplate(id).isNotBlank()
}