package com.magicento.models.xml.config;

import com.magicento.helpers.Magicento;
import com.magicento.models.xml.MagentoXmlTag;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;

/**
 * @author Enrique Piatti
 */
public class MagentoConfigXmlTag extends MagentoXmlTag {

    /**
     * Module name of the current config.xml (Namespace_Module)
     * @return
     */
    protected String getModuleName()
    {
        PsiElement context = getContext();
        if(context != null){
            XmlFile xmlFile = (XmlFile) context.getContainingFile();

            // suggest the module name:
            VirtualFile file = xmlFile.getOriginalFile().getVirtualFile();
            return Magicento.getNamespaceModule(file);
        }
        return null;
    }

}
