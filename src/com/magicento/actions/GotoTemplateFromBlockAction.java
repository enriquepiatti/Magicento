package com.magicento.actions;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.*;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.layout.Template;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GotoTemplateFromBlockAction extends MagicentoActionAbstract  {

    @Override
    public void executeAction()
    {
        if( ! getMagicentoSettings().layoutEnabled){
            IdeHelper.showDialog(getProject(),"Layout features are disabled. Please enable them going to File > Settings > Magicento", "Magicento");
            return;
        }

        String className = PsiPhpHelper.getClassName(getPsiElementAtCursor());
        String blockUri = Magicento.getUriFromClassName(getProject(), className);
        Set<String> templates = findTemplatesInLayout(blockUri);

        // TODO: search for templates hardcoded in the Block Class (or parent classes), we could search for "->setTemplate(" string on the full text of the file

        List<PsiFile> psiFiles = new ArrayList<PsiFile>();

        for(String templatePath : templates){
            psiFiles.addAll( FileHelper.findPsiFiles(getProject(), templatePath));
        }

        if( ! psiFiles.isEmpty()){
            // filter by allowed packages and themes
            Iterator<PsiFile> i = psiFiles.iterator();
            while (i.hasNext()) {
                PsiFile file = i.next(); // must be called before i.remove()
                LayoutFile layoutFile = new LayoutFile(file.getVirtualFile());
                if( ! layoutFile.isValidPackageAndTheme(getProject())) {
                    i.remove();
                }
            }

            if( ! psiFiles.isEmpty()){
                if(psiFiles.size() > 1)
                {
                    if(EventQueue.isDispatchThread()){
                        try{
                            NavigationUtil.getPsiElementPopup(psiFiles.toArray(new PsiElement[psiFiles.size()]),
                                    new DefaultPsiElementCellRenderer(), "Go to Template")
                                    .showInBestPositionFor(getEditor());
                        }
                        catch(Exception e){
                            return;
                        }
                    }
                }
                else {
                    openFile(psiFiles.iterator().next());
                }
            }
        }

    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        setEvent(e);
        PsiElement psiElement = getPsiElementAtCursor();
        if(psiElement != null){
            return MagentoParser.isBlock(psiElement);
        }
        return false;
    }

    @NotNull
    protected Set<String> findTemplatesInLayout(String blockUri)
    {
        Set<String> templates = new LinkedHashSet<String>();
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {
            String[] areas = new String[]{"frontend", "adminhtml"};
            for(String area : areas){
                File layoutFile = magicento.getCachedLayoutXml(area);
                if(layoutFile != null && layoutFile.exists())
                {
                    String xpath = "//block[@type='"+blockUri+"']";
                    List<Element> blockElements = XmlHelper.findXpath(layoutFile, xpath);
                    Set<String> blockNames = new LinkedHashSet<String>();

                    if(blockElements != null){

                       for(Element block : blockElements)
                       {
                           String template = block.getAttributeValue("template");
                           if(template != null && ! template.isEmpty()){
                               templates.add(template);
                           }
                           String name = block.getAttributeValue("name");
                           if(name != null && ! name.isEmpty()){
                               blockNames.add(name);
                           }
                       }

                        // templates changed with <action>
                        for(String blockName : blockNames)
                        {
                            xpath = "//reference[@name='"+blockName+"']/action[@method='setTemplate']";
                            List<Element> actionsWithTemplate = XmlHelper.findXpath(layoutFile, xpath);
                            if(actionsWithTemplate != null)
                            {
                                for(Element actionElement : actionsWithTemplate){
                                    List<Element> paramElements = actionElement.getChildren();
                                    for(Element paramElement : paramElements){
                                        if(paramElement.getValue().endsWith(".phtml")){
                                            templates.add(paramElement.getValue());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        // templates for itemRenderer
                        if(templates.isEmpty()){
                            xpath = "//*[text()='"+blockUri+"']";
                            List<Element> itemRenderers = XmlHelper.findXpath(layoutFile, xpath);
                            if(itemRenderers != null)
                            {
                                for(Element itemRendererParam : itemRenderers){
                                    Element itemRenderer = itemRendererParam.getParentElement();
                                    List<Element> paramElements = itemRenderer.getChildren();
                                    for(Element paramElement : paramElements){
                                        if(paramElement.getValue().endsWith(".phtml")){
                                            templates.add(paramElement.getValue());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return templates;
    }
}
