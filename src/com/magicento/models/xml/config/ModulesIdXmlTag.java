package com.magicento.models.xml.config;

import com.magicento.helpers.Magicento;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class ModulesIdXmlTag extends MagentoConfigXmlTag {

    public ModulesIdXmlTag(){
        super();
        name = null;
        isRequired = true;

        help = "";
    }

    @Override
    public List<String> getPossibleNames() {
        // LocalFileSystem.getInstance().findFileByIoFile()
        //PsiManager.getInstance(project).findFile(virtualFile);
        List<String> names = new ArrayList<String>();
        PsiElement context = getContext();
        if(context != null){
            VirtualFile file = context.getContainingFile().getOriginalFile().getVirtualFile();
            String name = Magicento.getNamespaceModule(file);
            if(name != null){
                names.add(name);
            }
        }
        else{
            // TODO: return all modules if context is null?
        }
        return names;
    }
}
