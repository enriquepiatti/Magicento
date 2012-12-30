package com.magicento.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import org.jdom.Element;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class RewriteControllerDialog extends ChooseModuleDialog
{

    protected JTextField subfolder;
    protected JComboBox before;
    protected JComboBox parentClassName;

    protected PsiElement currentElement;
    protected String area;
    protected Element routerElement;

    public RewriteControllerDialog(@org.jetbrains.annotations.Nullable Project project, PsiElement currentElement)
    {
        super(project);
        this.currentElement = currentElement;
        init();
        setTitle("Rewrite Controller");
    }


    @Override
    protected JComponent createCenterPanel()
    {
        JPanel panel = (JPanel) super.createCenterPanel();
        panel.add(new JLabel("Subfolder"));
        panel.add(subfolder);
        panel.add(new JLabel("Before"));
        panel.add(before);
        panel.add(new JLabel("Extends"));
        panel.add(parentClassName);
        return panel;
    }

    @Override
    protected void init()
    {
        if(currentElement != null){
            initRouterElement();
            initArea();
            initSubfolder();
            initBefore();
            initParentClassName();
            super.init();
        }
    }

    protected void initParentClassName()
    {
        String className = PsiPhpHelper.getClassName(currentElement);
        String originalParent = PsiPhpHelper.getParentClassName(currentElement);
        String defaultClass = getArea().equals("frontend") ? "Mage_Core_Controller_Front_Action" : "Mage_Adminhtml_Controller_Action";
        String[] options = originalParent.equals(defaultClass) ? new String[]{defaultClass, className} : new String[]{defaultClass, originalParent, className};
        parentClassName = new JComboBox(options);
    }

    protected void initBefore()
    {
        String module = getRouterModule();
        String[] beforeOptions = module != null ? new String[]{module,"-"} : new String[]{"-"};
        before = new JComboBox(beforeOptions);
    }

    protected void initSubfolder()
    {
//        String subf = "";
//        String parentClassName = PsiPhpHelper.getParentClassName(currentElement);
//        if(parentClassName != null && parentClassName.equals("Mage_Adminhtml_Controller_Action")){
//            subf = "Adminhtml";
//        }
        String subf = getArea().equals("admin") ? "Adminhtml" : "";
        subfolder = new JTextField(subf);
        subfolder.setToolTipText("subfolder inside /controllers/ this is normally used for Adminhtml controllers");
    }


    protected void initRouterElement()
    {
        Element bestMatch = null;
        String className = PsiPhpHelper.getClassName(currentElement);
        File configXml = MagicentoProjectComponent.getInstance(project).getCachedConfigXml();
        String xpath = "//routers/*/args/module|//routers/*/args/modules/*";
        List<Element> routers = XmlHelper.findXpath(configXml, xpath);
        if(routers != null){
            for(Element router : routers){
                String value = router.getValue().trim();
                if(className.contains(value) && (bestMatch == null || bestMatch.getValue().trim().length() < value.length())){
                    bestMatch = router;
                }
            }
        }
        routerElement = bestMatch;
    }

    protected void initArea()
    {
        if(routerElement != null){
            if(routerElement.getName().equals("module")){
                area = routerElement.getParentElement().getParentElement().getParentElement().getParentElement().getName();
            }
            else {
                area = routerElement.getParentElement().getParentElement().getParentElement().getParentElement().getParentElement().getName();
            }
        }
        else {
            area = "frontend";
        }
    }

    public String getArea()
    {
        return area;
    }


    public String getRouterName()
    {
        if(routerElement != null){
            if(routerElement.getName().equals("module")){
                return routerElement.getParentElement().getParentElement().getName();
            }
            else {
                return routerElement.getParentElement().getParentElement().getParentElement().getName();
            }
        }
        return null;
    }

    public String getRouterModule()
    {
        if(routerElement != null){
            return routerElement.getValue().trim();
        }
        return null;
    }

    public String getBefore()
    {
        return (String)before.getSelectedItem();
    }

    public String getSubfolder()
    {
        return subfolder.getText().trim();
    }

    public String getParentClassName()
    {
        return (String)parentClassName.getSelectedItem();
    }
}
