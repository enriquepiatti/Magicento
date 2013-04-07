package com.magicento.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.magicento.helpers.FileHelper;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.PhpStormMetaNamespace;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddFactoryToPhpStormMetaNamespaceAction extends MagicentoActionAbstract {

    @Override
    public void executeAction()
    {
        File metaFile = getMagicentoComponent().getPhpStormMetaFile();
        if( ! metaFile.exists()){
            ActionManager actionManager = ActionManagerImpl.getInstance();
            String actionId = "CreatePhpStormMetaNamespace";
            CreatePhpStormMetaNamespaceAction action = (CreatePhpStormMetaNamespaceAction) actionManager.getAction(actionId);
            action.actionPerformed(getEvent());
        }
        else {
            PsiElement psiElement = getPsiElementAtCursor();
            PhpStormMetaNamespace phpStormMetaNamespace = PhpStormMetaNamespace.getInstance(getProject());
            String method = getFactoryMethod();
            String uri = MagentoParser.getUri(psiElement);
            PsiElement factory = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
            List<MagentoClassInfo> classes = getMagicentoComponent().findClassesOfFactory(factory);
            if(classes != null){
                String className = classes.get(0).name;
                phpStormMetaNamespace.addFactory(phpStormMetaNamespace.getUriTypeFromMethodName(method), uri, className);
                phpStormMetaNamespace.refreshMetaFile();
                IdeHelper.showNotification("PHPStorm Meta Namespace file was updated", NotificationType.INFORMATION, getProject());
            }
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        setEvent(e);
        PsiElement psiElement = getPsiElementAtCursor();
        return MagentoParser.isUri(psiElement) && MagentoParser.isFactory(PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE));
    }

    protected String getFactoryMethod()
    {
        PsiElement psiElement = getPsiElementAtCursor();
        psiElement = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
        return MagentoParser.getMethodName(psiElement);
    }

}
