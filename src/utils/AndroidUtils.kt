package utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlDocument
import java.io.File

/**
 * Create by hzh on 2020/07/07.
 */

private val sep = File.separator

/**
 * 获取App对应包名根目录
 */
fun Project.getAppPackageBaseDir(): VirtualFile? {
    val path = "$basePath${sep}app${sep}src${sep}main${sep}java${sep}${getAppPackageName().replace(".", sep)}"
    return LocalFileSystem.getInstance().findFileByPath(path)
}

fun Project.getAppLayoutDir(): VirtualFile? {
    val path = "$basePath${sep}app${sep}src${sep}main${sep}res${sep}layout"
    return LocalFileSystem.getInstance().findFileByPath(path)
}

/**
 * 获取App包名
 */
fun Project.getAppPackageName(): String = getManifestFile()?.run {
    (firstChild as XmlDocument).rootTag?.getAttribute("package")?.value ?: ""
} ?: ""

/**
 * 获取App清单文件
 */
fun Project.getManifestFile(): PsiFile? {
    val path = "$basePath${sep}app${sep}src${sep}main${sep}AndroidManifest.xml"
    return LocalFileSystem.getInstance().findFileByPath(path)?.let {
        PsiManager.getInstance(this).findFile(it)
    }
}

fun VirtualFile.getFilePackageName(): String {
    val path = if (!isDirectory) parent.path.replace(sep, ".") else path.replace(sep, ".")
    val preIndex = "src.main.java".let { path.indexOf(it) + it.length + 1 }
    return path.substring(preIndex)
}