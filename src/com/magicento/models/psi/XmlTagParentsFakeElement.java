package com.magicento.models.psi;

import com.magicento.helpers.XmlHelper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * this class is used in the DocumentationProvider for returning a psi element from the getDocumentationElementForLookupItem
 *
 * @author Enrique Piatti
 */
public class XmlTagParentsFakeElement extends FakePsiElement /*LightElement*/ {

    protected PsiElement parent;
    protected String name;

    @Override
    public PsiElement getParent() {
        return parent;
    }

    public void setParent(PsiElement newParent){
        parent = newParent;
    }

    public PsiElement setName(String tagName){
        name = tagName;
        return this;
    }

    public String getName(){
        return name;
    }

    public List<String> getParents()
    {
        //List<XmlTag> parents = XmlHelper.getParents(getParent());
        List<XmlTag> parents = XmlHelper.getParents(this);
        List<String> parentsNames = new ArrayList<String>();
        for(XmlTag curTag : parents) {
            parentsNames.add(curTag.getName());
        }
        return parentsNames;
    }

    public List<String> getFullPath()
    {
        List<String> path = getParents();
        if(this.getName() != null && ! this.getName().isEmpty()){
            path.add(this.getName());
        }

        return path;
    }

}
