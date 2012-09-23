package com.magicento.helpers;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

/**
 * Helper for working easily with the IDE
 * @author Enrique Piatti
 */
public class IdeHelper {

    public static void showDialog(final String message, final String title, final Icon icon)
    {
        final Throwable[] exception = {null};
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
//                try {
                    Messages.showMessageDialog(message, title, icon);
//                } catch (Throwable tearingDown) {
//                    if (exception[0] == null) exception[0] = tearingDown;
//                }
            }
        });
        // if (exception[0] != null) throw exception[0];
    }

    public static void showDialog(String message, String title)
    {
        showDialog(message, title, Messages.getInformationIcon());
    }

    // TODO: this log doesn't work
    public static void log(String message){
        Logger.getInstance("").info(message);
    }

    public static void logError(String message){
        message += " (if you think this is a bug please send the trace to issues@magicento.com)";
        Logger.getInstance("").error(message);
    }

    // TODO: this log doesn't work
    public static void logWarning(String message){
        Logger.getInstance("").warn(message);
    }

    public static void navigateToPsiElement(PsiElement psiElement)
    {
        Project project = psiElement.getProject();
        PsiElement navElement = psiElement.getNavigationElement();
        navElement = TargetElementUtilBase.getInstance().getGotoDeclarationTarget(psiElement, navElement);
        if (navElement instanceof Navigatable) {
            if (((Navigatable)navElement).canNavigate()) {
                ((Navigatable)navElement).navigate(true);
            }
        }
        else if (navElement != null) {
            int navOffset = navElement.getTextOffset();
            VirtualFile virtualFile = PsiUtilCore.getVirtualFile(navElement);
            if (virtualFile != null) {
                new OpenFileDescriptor(project, virtualFile, navOffset).navigate(true);
            }
        }
    }

    public static boolean promp(String message, String title)
    {
        // Messages.showOkCancelDialog(myProject, question, myTitle, Messages.getQuestionIcon()) == 0;
        return Messages.showOkCancelDialog(message, title, Messages.getQuestionIcon()) == 0;
    }


    public static CodeStyleSettings getSettings(Project project)
    {
        if(project != null){
            CodeStyleSettingsManager manager = CodeStyleSettingsManager.getInstance(project);
            if(manager != null){
                return manager.getCurrentSettings();
            }
        }
        return null;
    }

    public static XmlCodeStyleSettings getXmlSettings(Project project)
    {
        CodeStyleSettings settings = getSettings(project);
        if(settings != null){
            return settings.getCustomSettings(XmlCodeStyleSettings.class);
        }
        return null;
    }



}
