package com.magicento.actions;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.layout.Template;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GoToBlockFromTemplateAction extends MagicentoActionAbstract {

    @Override
    public void executeAction()
    {
        if( ! getMagicentoSettings().layoutEnabled){
            IdeHelper.showDialog(getProject(),"Layout features are disabled. Please enable them going to File > Settings > Magicento", "Magicento");
            return;
        }

        final VirtualFile file = getVirtualFile();
        Template template = new Template(file);
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {
            List<String> blocks = template.getBlocksClasses(getProject());
            if(blocks != null)
            {
                if(blocks.size() > 0)
                {
                    if(blocks.size() > 1)
                    {
                        if(EventQueue.isDispatchThread()){
                            try{
                                List<PsiElement> psiElements = PsiPhpHelper.getPsiElementsFromClassesNames(new ArrayList<String>(blocks), getProject());
                                NavigationUtil.getPsiElementPopup(psiElements.toArray(new PsiElement[psiElements.size()]),
                                        new DefaultPsiElementCellRenderer(), "Go to Block")
                                        .showInBestPositionFor(getEditor());
                            }
                            catch(Exception e){
                                return;
                            }
                        }
                    }
                    else {
                        gotoClass(blocks.iterator().next());
                    }
                }
                else {
                    IdeHelper.showDialog(getProject(), "Cannot find corresponding block from merged layout xml\n" +
                            "Probably this template is assigned directly from PHP using ->setTemplate instead of using the layout",
                            "Block not found");
                }
            }
        }
        else {
            IdeHelper.showDialog(getProject(), "Cannot get Magicento Project Component", "Magicento Go to Block Error");
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        VirtualFile file = getVirtualFile();
        Template template = new Template(file);
        return template.isTemplate();
    }


}
