package com.magicento.actions;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.PsiNavigateUtil;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.PsiPhpHelper;

import java.util.List;

/**
 * base class for all the MagicentoActions which requires PHP enabled
 * @author Enrique Piatti
 */
public abstract class MagicentoPhpActionAbstract extends MagicentoActionAbstract
{

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        setEvent(e);
        boolean canExecuteAction = checkPhpEnabled();
        if(canExecuteAction){
            executeAction();
        }
        else {
            MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
            if(settings != null && settings.showPhpWarning){
                IdeHelper.showDialog(getProject(),"You need to enable PHP in magicento settings for this feature.\nGo to File > Settings > Magicento and Enable PHP.\n" +
                        "You can disable this warning going to File > Settings > Magicento too", "Feature available with PHP enabled only");
            }
        }
    }

    private boolean checkPhpEnabled()
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
        return (settings != null && settings.enabled && settings.phpEnabled);
    }


}



