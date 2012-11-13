package com.magicento.models.xml;

import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.JavaHelper;
import com.magicento.helpers.XmlHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
abstract public class MagentoXml {


    final protected static String WILDCARD_IN_XML = "___";
    final protected static String WILDCARD_IN_CLASSNAME = "Id";

    protected String skeletonName = "";
    protected String fallbackClassNamePrefix = "com.magicento.models.xml.";
    protected String classNamePrefix = fallbackClassNamePrefix;
    protected String fallbackClassName = "MagentoXmlTag"; // "MagentoConfigXmlTag";

    protected MagentoXmlTag xml;

    protected boolean isCacheInvalid = true;

    public String getMergedXmlFilename()
    {
        return mergedXmlFilename;
    }

    protected String mergedXmlFilename = "";

    public static MagentoXmlType TYPE = null;

    protected Project project;

    public MagentoXml(Project project){
        this.project = project;
        _init();
    }

    protected void _init()
    {
        _create();
    }

    protected void _create()
    {
        XmlFile xmlFile = _loadXmlSkeleton();
        if(xmlFile != null){
            XmlTag configTag = xmlFile.getRootTag();
            // root tag is <config> => ConfigXmlTag
            xml = _createRootTag();
            xml.setManager(this);
            XmlTag[] children = configTag.getSubTags();
            // creates the hierarchy (recursively)
            _createChildren(children, xml);
        }
    }


    abstract protected MagentoXmlTag _createRootTag();
//    {
//        //return new ConfigXmlTag();
//        return new MagentoXmlTag();
//    }

    /**
     * each children has its own class, created dinamically according to the node name and parents names.
     * if the class is not found, it uses the default class
     * @param children
     * @param parent
     */
    protected void _createChildren(XmlTag[] children, MagentoXmlTag parent)
    {
        for(XmlTag el : children){
            String className = _getClassNameFromTag(el);
            MagentoXmlTag child = JavaHelper.factory(className, MagentoXmlTag.class /*_createRootTag().class*/);
            if(child != null){
                // if our class doesn't have a name, and the node is not a wildcard, we assign the node name to our object
                // this could happen when we are using the default class for modelling the node.
                if(child.getName() == null
                        && ! el.getName().equals(WILDCARD_IN_XML) ){
                    //&& ! (child instanceof IdXmlTag) ){
                    child.setName(el.getName());
                }
                String helpAttribute = el.getAttributeValue("help");
                if(helpAttribute != null){
                    child.setHelp(helpAttribute);
                }
                //child.setParent(parent);  // this is done inside addChild
                child.setManager(this);
                parent.addChild(child);
                // recursion:
                XmlTag[] grandChildren = el.getSubTags();
                if(grandChildren != null){
                    _createChildren(grandChildren, child);
                }
            }
        }
    }

    protected XmlFile _loadXmlSkeleton()
    {

        // we need to load is as a resource stream because this file will be inside the .jar when we deploy the plugin

        if(skeletonName == null || skeletonName.isEmpty()){
            return null;
        }

        InputStream is = this.getClass().getResourceAsStream(skeletonName+".xml");

        // String skeleton = IOUtils.toString()
        try {
            String skeleton = FileUtil.loadTextAndClose(is);
            Project project = ProjectUtil.guessCurrentProject(null);
            PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(skeletonName+"Temp.xml", skeleton);
            return (XmlFile) psiFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * return the classname associated to the tag passed
     * @param tag
     * @return
     */
    protected String _getClassNameFromTag(XmlTag tag)
    {
        String prefix = classNamePrefix;
        String suffix = "XmlTag";       // maybe this should be "Descriptor"
        String wildcard = WILDCARD_IN_CLASSNAME;
        //String tagName = tag.getName();
        String name = "";
        List<XmlTag> parents = XmlHelper.getParents(tag);
        parents.add(tag);   // include current tag
        parents.remove(0);  // delete root from list, we are not using it in our class names
        for (XmlTag curTag : parents) {
            String tagName = curTag.getName();
            if( tagName.equals(WILDCARD_IN_XML) ){
                name += wildcard;
            }
            else{
                name += StringUtils.capitalize(tagName);
            }
        }

        String className = prefix+name+suffix;

        // if class doesn't exist, try with the fallback
        if( ! JavaHelper.classExists(className)){
            if(tag.getName().equals(WILDCARD_IN_XML)){
                className = prefix+WILDCARD_IN_CLASSNAME+suffix;                        // IdXmlTag
                if( ! JavaHelper.classExists(className)){
                    className = fallbackClassNamePrefix+WILDCARD_IN_CLASSNAME+suffix;   // IdXmlTag from fallback
                }
            }
            else{
                className = prefix+fallbackClassName;
                if( ! JavaHelper.classExists(className)){
                    className = fallbackClassNamePrefix+fallbackClassName;
                }
            }
        }

        return className;
    }


    public MagentoXmlTag getMatchedTag(PsiElement context)
    {
        xml.setContext(context);
        List<XmlTag> parents = XmlHelper.getParents(context);
        if(parents != null){
            return getMatchedTag(parents.toArray(new XmlTag[]{}));
        }
        return null;
    }

    public MagentoXmlTag getMatchedTag(XmlTag[] parents)
    {
        if(parents != null && parents.length > 0)
        {
            List<String> parentsNames = new ArrayList<String>();
            for(XmlTag curTag : parents) {
                parentsNames.add(curTag.getName());
            }
            return getMatchedTag(parentsNames);
        }
        return null;
    }

    /**
     *
     * @param fullpath tag names
     * @return
     */
    public MagentoXmlTag getMatchedTag(List<String> fullpath)
    {
        if(fullpath != null && fullpath.size() > 0)
        {
            // check if we have a tag matching the same path
            if( ! fullpath.get(0).equals(xml.getName()) ){
                return null;
            }
            MagentoXmlTag match = xml;
            fullpath.remove(0);  // remove root
            for(String curTag : fullpath) {
                List<MagentoXmlTag> tags = match.getChildren();
                if(tags == null){
                    return null;
                }
                match = null;
                MagentoXmlTag wildcardMatch = null;
                for(MagentoXmlTag testTag : tags){
                    if(testTag.getName() == null){
                        // getName is null for wildcards (dynamic names)
                        // but we don't break the for here, the wildcard is only used as a fallback if there isn't any exact match
                        wildcardMatch = testTag;
                    }
                    else if( testTag.getName().equals( curTag ) ){
                        // exact match
                        match = testTag;
                        break;
                    }
                }
                if(match == null){
                    if(wildcardMatch != null){
                        match = wildcardMatch;
                    }
                    else {
                        return null;
                    }
                }
            }
            return match;
        }
        return null;
    }


    public File getMergedXmlFile()
    {
        MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
        File cachedFile = magicentoProject.getCachedFile(getMergedXmlFilename());

        if(isCacheInvalid || ! cachedFile.exists() ){
            String result = getMergedXml();
            if(result == null || result.charAt(0) != '<'){
                // we are checking if its disabled because when this is requested could be open a popup for inserting the path to magento,
                // and the user can disable magicento from there if magicento is enabled but is not working
                if( ! magicentoProject.isDisabled() ){
                    magicentoProject.showMessageError(result);
                }
                else {
                    // exit silently if magicento is disabled
                    return null;
                }
            }
            else {
                if( ! magicentoProject.saveCacheFile(cachedFile, result) ){
                    invalidateCache();
                }
                else {
                    isCacheInvalid = false;
                }
            }
        }

        return cachedFile;
    }


    abstract protected String getMergedXml();


    public void invalidateCache()
    {
        isCacheInvalid = true;
    }

}
