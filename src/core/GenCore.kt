package core

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType
import utils.getAppPackageBaseDir
import utils.getAppLayoutDir
import utils.getAppPackageName
import utils.getFilePackageName
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by hzh on 2020/07/09.
 */
private val currUser = System.getProperty("user.name")
private val currDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

fun Project.genCore(prefix: String, fileType: Int, vmType: Int) {
    getAppPackageBaseDir()?.let { base ->
        if (vmType != -1) {
            // 生成Model
            var model = base.findChild("model")
            if (model == null) model = base.createFile("model")
            genModel(model!!, prefix)

            // 生成ViewModel
            var vm = base.findChild("viewmodel")
            if (vm == null) vm = base.createFile("viewmodel")
            genVM(vm!!, prefix, vmType)
        }

        var uiFile = base.findChild("ui")
        if (uiFile == null) uiFile = base.createFile("ui")
        when (fileType) {
            1 -> { // 生成Activity
                getAppLayoutDir()?.let {
                    val name = "activity_${prefix.camel2underline()}" // 生成布局文件
                    genLayout(it, name)
                }

                var actFile = uiFile!!.findChild("activity")
                if (actFile == null) actFile = uiFile.createFile("activity")
                genActivityCode(actFile!!, prefix, vmType != -1)
            }
            2 -> { // 生成Fragment
                getAppLayoutDir()?.let {
                    val name = "fragment_${prefix.camel2underline()}" // 生成布局文件
                    genLayout(it, name)
                }

                var frgFile = uiFile!!.findChild("fragment")
                if (frgFile == null) frgFile = uiFile.createFile("fragment")
                genFragmentCode(frgFile!!, prefix, vmType != -1)
            }
        }
    }
}

/**
 * 创建包目录
 */
private fun VirtualFile.createFile(name: String): VirtualFile? = try {
    createChildDirectory(null, name)
} catch (e: IOException) {
    e.printStackTrace()
    null
}

/**
 * 创建Model
 */
private fun Project.genModel(dir: VirtualFile, prefix: String) {
    val name = "${prefix}Model"
    val model = dir.findChild("${name}.kt")
    if (model == null) {
        val text = """
package ${dir.getFilePackageName()}

/**
 * Create by $currUser on $currDate.
 */
class $name {
}
""".trimIndent()

        val file = PsiFileFactory.getInstance(this).createFileFromText("${name}.kt", KotlinFileType.INSTANCE, text)

        PsiManager.getInstance(this).findDirectory(dir)?.add(file)
    }
}

/**
 * 创建ViewModel
 */
private fun Project.genVM(dir: VirtualFile, prefix: String, type: Int) {
    val name = "${prefix}VM"
    val vm = dir.findChild("${name}.kt")
    if (vm == null) {
        val parent = when (type) {
            1 -> "BaseVM"
            2 -> "PageVM"
            else -> "ViewModel"
        }

        val text = """
package ${dir.getFilePackageName()}

import com.example.hzh.common.viewmodel.$parent
import ${getAppPackageName()}.model.${prefix}Model

/**
 * Create by $currUser on $currDate.
 */
class $name(private val model: ${prefix}Model) : $parent() {
}
        """.trimIndent()

        val file = PsiFileFactory.getInstance(this).createFileFromText("${name}.kt", KotlinFileType.INSTANCE, text)

        PsiManager.getInstance(this).findDirectory(dir)?.add(file)
    }
}

/**
 * 创建布局
 */
private fun Project.genLayout(dir: VirtualFile, name: String) {
    val layout = dir.findChild("${name}.xml")
    if (layout == null) {
        val text = """
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

</LinearLayout>
""".trimIndent()

        val file = PsiFileFactory.getInstance(this).createFileFromText("${name}.xml", XmlFileType.INSTANCE, text)

        PsiManager.getInstance(this).findDirectory(dir)?.add(file)
    }
}

/**
 * 创建Activity
 */
private fun Project.genActivityCode(dir: VirtualFile, prefix: String, isCreateVM: Boolean) {
    val name = "${prefix}Activity"
    val activity = dir.findChild("$name.kt")
    if (activity == null) {
        val binding = "Activity${prefix}Binding"
        val vm = "${prefix}VM"
        val model = "${prefix}Model"
        val appPackage = getAppPackageName()

        val text = """
package ${dir.getFilePackageName()}

import com.xiaojinzi.component.anno.RouterAnno
import com.example.hzh.common.RouterConfig
import $appPackage.databinding.$binding
${if (isCreateVM) """
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import $appPackage.model.$model
import $appPackage.viewmodel.$vm
import com.example.hzh.common.activity.TwActivity
""".trimIndent() else """
com.example.hzh.common.activity.UIActivity
""".trimIndent()}

/**
 * Create by $currUser on $currDate.
 */
@RouterAnno(
    host = RouterConfig.ACTIVITY_HOST,
    path = RouterConfig.
)
${if (isCreateVM) "@Suppress(\"UNCHECKED_CAST\")" else ""}
class $name : ${if (isCreateVM) "TwActivity<$binding, $vm>" else "UIActivity<${binding}>"}() {
            
    ${if (isCreateVM) """
    override val mViewModel by viewModels<$vm> {
        object : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T = $vm($model()) as T
        }
    }
""".trimIndent() else ""}            
            
    override fun createViewBinding(): $binding {
        return $binding.inflate(layoutInflater)
    }

    override fun initView() {
                    
    }

    override fun initData() {
                    
    }

    override fun initListener() {
                    
    }
                
    ${if (isCreateVM) """
    override fun bindViewModelObserve() {
        super.bindViewModelObserve()
    }
""".trimIndent() else ""}
}
""".trimIndent()

        val file = PsiFileFactory.getInstance(this).createFileFromText("${name}.kt", KotlinFileType.INSTANCE, text)

        PsiManager.getInstance(this).findDirectory(dir)?.add(file)
    }
}

/**
 * 创建Fragment
 */
private fun Project.genFragmentCode(dir: VirtualFile, prefix: String, isCreateVM: Boolean) {
    val name = "${prefix}Fragment"
    val fragment = dir.findChild("$name.kt")
    if (fragment == null) {
        val binding = "Fragment${prefix}Binding"
        val vm = "${prefix}VM"
        val model = "${prefix}Model"
        val appPackage = getAppPackageName()

        val text = """
package ${dir.getFilePackageName()}

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.hzh.common.RouterConfig
import com.xiaojinzi.component.anno.FragmentAnno
import $appPackage.databinding.$binding
${if (isCreateVM) """
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import $appPackage.model.$model
import $appPackage.viewmodel.$vm
import com.example.hzh.common.fragment.TwFragment
""".trimIndent() else """
import com.example.hzh.common.fragment.UIFragment
""".trimIndent()}

/**
 * Create by $currUser on $currDate.
 */
@FragmentAnno(RouterConfig.FG_)
${if (isCreateVM) "@Suppress(\"UNCHECKED_CAST\")" else ""}
class $name : ${if (isCreateVM) "TwFragment<$binding, $vm>" else "UIFragment<${binding}>"}() {
            
    ${if (isCreateVM) """
    override val mViewModel by viewModels<$vm> {
        object : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T = $vm($model()) as T
        }
    }
""".trimIndent() else ""}            
            
    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): $binding {
        return $binding.inflate(inflater, container, false)
    }

    override fun initView() {

    }

    override fun initListener() {

    }
                
    ${if (isCreateVM) """
    override fun bindViewModelObserve() {
        super.bindViewModelObserve()
    }
""".trimIndent() else ""}
}
""".trimIndent()

        val file = PsiFileFactory.getInstance(this).createFileFromText("${name}.kt", KotlinFileType.INSTANCE, text)

        PsiManager.getInstance(this).findDirectory(dir)?.add(file)
    }
}

/**
 * 驼峰转下划线
 */
private fun String.camel2underline(): String {
    return StringBuilder().apply {
        this@camel2underline.forEachIndexed { index, c ->
            if (c in 'A'..'Z') {
                if (index != 0) append('_')
                append(c.toLowerCase())
            } else append(c)
        }
    }.toString()
}