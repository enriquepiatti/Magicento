package com.magicento.actions;

import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.*;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.PhpStormMetaNamespace;
import org.jdom.Element;

import java.io.File;
import java.util.*;


public class CreatePhpStormMetaNamespaceAction extends MagicentoActionAbstract
{

    @Override
    public void executeAction()
    {

        GotoClassModel2 model = new GotoClassModel2(/*ProjectUtil.guessCurrentProject(null)*/getProject());
        String[] classNames = model.getNames(true);      // TODO: use false here?

        //List<String> clazzes = new ArrayList<String>(Arrays.asList(model.getNames(true)));
        //Collections.sort(clazzes);

        Map<MagentoClassInfo.UriType, Map<String, String>> mappingByType = Magicento.getUriFromClassNames(getProject(), Arrays.asList(classNames));

        PhpStormMetaNamespace phpStormMetaNamespace = PhpStormMetaNamespace.getInstance(getProject());
        phpStormMetaNamespace.savePhpStormMetaFile(mappingByType);

        phpStormMetaNamespace.refreshMetaFile();

        IdeHelper.showNotification("PHPStorm Meta Namespace file was generated", NotificationType.INFORMATION, getProject());

    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }





}
