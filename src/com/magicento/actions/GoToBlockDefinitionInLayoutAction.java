package com.magicento.actions;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.*;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlType;
import com.magicento.models.xml.layout.MagentoLayoutXml;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Enrique Piatti
 */
public class GoToBlockDefinitionInLayoutAction extends MagicentoActionAbstract {

    @Override
    public void executeAction()
    {
        if( ! getMagicentoSettings().layoutEnabled){
            IdeHelper.showDialog(getProject(), "Layout features are disabled. Please enable them going to File > Settings > Magicento", "Magicento");
            return;
        }

        String className = PsiPhpHelper.getClassName(getPsiElementAtCursor());
        String blockUri = Magicento.getUriFromClassName(getProject(), className);

        MagentoXml xml = MagentoXmlFactory.getInstance(MagentoXmlType.LAYOUT, getProject());
        if(xml != null && xml instanceof MagentoLayoutXml)
        {
            MagentoLayoutXml layoutXml = (MagentoLayoutXml)xml;
            layoutXml.setArea("frontend");
            List<XmlTag> xmlTags = layoutXml.findNodesInOriginalXmlByBlockType(blockUri);
            layoutXml.setArea("adminhtml");
            xmlTags.addAll(layoutXml.findNodesInOriginalXmlByBlockType(blockUri));

            if( ! xmlTags.isEmpty()){
                if(xmlTags.size() > 1)
                {
                    if(EventQueue.isDispatchThread()){
                        try{
                            NavigationUtil.getPsiElementPopup(xmlTags.toArray(new PsiElement[xmlTags.size()]),
                                    new DefaultPsiElementCellRenderer(), "Go to Block Definition in Layout")
                                    .showInBestPositionFor(getEditor());
                        }
                        catch(Exception e){
                            return;
                        }
                    }
                }
                else
                {
                    PsiElement navElement = xmlTags.get(0).getNavigationElement();
                    if (navElement instanceof Navigatable) {
                        if (((Navigatable)navElement).canNavigate()) {
                            ((Navigatable)navElement).navigate(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        return MagentoParser.isBlock(getPsiElementAtCursor());
    }
}
