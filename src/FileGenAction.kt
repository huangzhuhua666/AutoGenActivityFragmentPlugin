import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import ui.InfoInputDialog

/**
 * Create by hzh on 2020/07/07.
 */
class FileGenAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(PlatformDataKeys.PROJECT)?.let {
            InfoInputDialog(it).run {
                pack()
                isVisible = true
            }
        }
    }
}