package com.magicento.models.xml.config;

import com.magicento.helpers.XmlHelper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;

import java.util.*;

/**
 * @author Enrique Piatti
 */
public class IdXmlTag extends MagentoConfigXmlTag {

    protected Set<String> names;

    public IdXmlTag(){
        super();
        name = null;
        names = new LinkedHashSet<String>();
        isRequired = false;

        help = "Unique identifier";
    }

    @Override
    public List<String> getPossibleNames() {
        // LocalFileSystem.getInstance().findFileByIoFile()
        //PsiManager.getInstance(project).findFile(virtualFile);

        addModuleName();

        return new ArrayList<String>(names);
    }

    @Override
    public Map<String, String> getPossibleDefinitions() {
        // we need to clear the names because we are reusing this object with different nodes
        names.clear();
        return super.getPossibleDefinitions();
    }

    protected void addModuleName()
    {
        String name = getModuleName();
        if(name != null){
            names.add(StringUtil.toLowerCase(name));
        }
    }



    /**
     * add to names all the sibling tag nodes from the merged config.xml
     */
    protected void addAllEquivalentNames()
    {
        PsiElement context = getContext();
        if(context != null){
            List<XmlTag> parents = XmlHelper.getParents(context);
            String xpath = "";
            if(parents != null){
                for(XmlTag tag : parents){
                    xpath += "/"+tag.getName();
                }
            }
            if( ! xpath.isEmpty()){
                xpath += "/*";
                addAllNamesFrom(xpath);
            }
        }
    }

    /**
     * add to names all the tag names from the merged xml that matches the xpath
     * @param xpath
     */
    protected void addAllNamesFrom(String xpath)
    {
        List<String> nodeNames = getAllNodeNamesFromMergedXml(xpath);
        if(nodeNames != null){
            names.addAll(nodeNames);
        }
    }



    /**
     * add to names the children of the node defined by path of the current config.xml
     * @param path path is not an xpath, is a list of subtags (without "config") separated by "/" ex: "global/blocks"
     */
    protected void addNamesFromCurrentFile(String path)
    {
        XmlTag subTag = getTagFromCurrentFile(path);
        if(subTag != null){
            XmlTag[] subTags = subTag.getSubTags();
            for(XmlTag equivalentId : subTags){
                names.add(equivalentId.getName());
            }
        }
    }

    /**
     * reads the current file searching for an Id inside /blocks|models|helpers/ with a <class> element
     * if no id is found then it uses the module name (in lowercase)
     * @return
     */
    protected String guessIdNameFromCurrentFile()
    {
        String[] tags = new String[]{"blocks", "models", "helpers"};
        for(String tagName : tags ){
            XmlTag tag = getTagFromCurrentFile("global/"+tagName);
            if(tag != null){
                for(XmlTag subTag : tag.getSubTags()){
                    // choose only the subtag if it has a <class> (and hence it it's not a <rewrite>)
                    if(subTag.findSubTags("class") != null){
                        return subTag.getName();
                    }
                }
            }
        }
        String moduleName = getModuleName();
        if(moduleName != null){
            moduleName = StringUtil.toLowerCase(moduleName);
        }
        return moduleName;
    }



}
