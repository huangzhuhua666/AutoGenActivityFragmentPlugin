package ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import core.GenCoreKt;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InfoInputDialog extends JDialog {

    private JPanel contentPane;
    private JButton btnCancel;
    private JButton btnOk;
    private JTextField etFileName;
    private JRadioButton rbActivity;
    private JRadioButton rbFragment;
    private JCheckBox cbNeedVM;
    private JRadioButton rbBase;
    private JRadioButton rbPage;

    private int typeFile = -1;
    private int typeVM = -1;

    private final Project project;

    public InfoInputDialog(Project project) {
        this.project = project;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnOk);

        ButtonGroup bgTypeFile = new ButtonGroup();
        bgTypeFile.add(rbActivity);
        bgTypeFile.add(rbFragment);

        ButtonGroup bgTypeVM = new ButtonGroup();
        bgTypeVM.add(rbBase);
        bgTypeVM.add(rbPage);
        rbBase.setEnabled(false);
        rbPage.setEnabled(false);

        btnCancel.addActionListener(e -> onCancel());

        btnOk.addActionListener(e -> onOK());

        rbActivity.addActionListener(e -> {
            if (rbActivity.isSelected()) typeFile = 1;
        });

        rbFragment.addChangeListener(e -> {
            if (rbFragment.isSelected()) typeFile = 2;
        });

        cbNeedVM.addChangeListener(e -> onNeed());

        rbBase.addChangeListener(e -> {
            if (rbBase.isSelected()) typeVM = 1;
        });

        rbPage.addChangeListener(e -> {
            if (rbPage.isSelected()) typeVM = 2;
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String prefix = etFileName.getText();
        if (prefix == null || prefix.equals("")) {
            Messages.showErrorDialog("请输入文件名前缀", "请输入");
            return;
        }

        if (typeFile == -1) {
            Messages.showErrorDialog("请选择要创建的文件类型", "请选择");
            return;
        }

        if (cbNeedVM.isSelected() && typeVM == -1) {
            Messages.showErrorDialog("请选择要创建的ViewModel类型", "请选择");
            return;
        }

        ApplicationManager.getApplication().runReadAction(() ->
                ApplicationManager.getApplication().runWriteAction(() ->
                        GenCoreKt.genCore(project, prefix, typeFile, cbNeedVM.isSelected() ? typeVM : -1)));

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void onNeed() {
        rbBase.setEnabled(cbNeedVM.isSelected());
        rbPage.setEnabled(cbNeedVM.isSelected());
    }
}