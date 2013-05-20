package com.magicento.models.xml.layout;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.FileHelper;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author Enrique Piatti
 */
public class MagentoLayoutXml extends MagentoXml {

    public static MagentoXmlType TYPE = MagentoXmlType.LAYOUT;

    public static String BASE_PATH = "app/design/";

    protected String area = "frontend";
    protected String packageName = "base";
    protected String theme = "default";

    Map<String, Document> cachedXmlDocuments;


    // protected PsiElement currentContext;

    private enum MagentoDesignType
    {
        LAYOUT, TEMPLATE, SKIN, LOCALE;
    }

    public MagentoLayoutXml(Project project) {
        super(project);
    }


    protected void _init()
    {
        skeletonName = "LayoutSkeleton";
        classNamePrefix = fallbackClassNamePrefix+"layout.";
        fallbackClassName = "MagentoLayoutXmlTag";
        mergedXmlFilename = "layout.xml";
        cachedXmlDocuments = new HashMap<String, Document>();

        // super._init();   // don't create skeleton
        xml = _createRootTag();



    }

    @Override
    protected MagentoXmlTag _createRootTag()
    {
        return new LayoutXmlTag();
    }

    public String getMergedXmlFilename()
    {
        // return "layout_"+area+"_"+packageName+"_"+theme+".xml";
        // We are mergin all the .xml files from all packages and themes
        return "layout_"+area+".xml";
    }


    @Override
    protected String getMergedXml()
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null && area != null)
        {
            // don't use PHP anymore for getting the layout
//            if(settings.phpEnabled)
//            {
//                String phpCode = "echo '<layout>' . Mage::app()->getLayout()->getUpdate()" +
//                        "->getFileLayoutUpdatesXml('"+area+"', '"+packageName+"', '"+theme+"')" +
//                        "->innerXml() . '</layout>';";
//
//                MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
//                return magicentoProject.executePhpWithMagento(phpCode);
//            }

            if(settings.layoutEnabled) {
                List<String> xmlFileNames = getAllActiveXmlLayoutNames(area);
                List<File> xmlFiles = new ArrayList<File>();
                for(String xmlFileName : xmlFileNames){
                    xmlFiles.addAll(getFileFromAllPackagesAndThemes(xmlFileName, area));
                }
                if(xmlFiles.size() > 0){
                    return XmlHelper.combineXmlFiles(xmlFiles);
                }
            }
            else {
                IdeHelper.showNotification("Layout features are disabled. Please enable it from Magicento Settings if you want to use this feature", NotificationType.WARNING, project);
            }

        }

        return null;
    }


    @NotNull public List<String> getAllActiveXmlLayoutNames()
    {
        return getAllActiveXmlLayoutNames(area);
    }

    @NotNull public List<String> getAllActiveXmlLayoutNames(@NotNull String area)
    {
        List<String> updateFiles = new ArrayList<String>();
        MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
        if(magicentoProject != null){
            File configXmlFile = magicentoProject.getCachedConfigXml();
            if(configXmlFile != null && configXmlFile.exists()){
                String xpath = "//"+area+"/layout/updates/*";
                List<Element> updates = XmlHelper.findXpath(configXmlFile, xpath);
                if(updates != null){
                    for(Element update : updates)
                    {
                        Element fileNode = update.getChild("file");
                        if(fileNode != null){
                            updateFiles.add(fileNode.getValue());
                        }
                    }
                }
                updateFiles.add("local.xml");
            }
        }
        return updateFiles;
    }

    @NotNull public List<File> getFileFromAllPackagesAndThemes(@NotNull String fileName)
    {
        return getFileFromAllPackagesAndThemes(fileName, area, true);
    }

    @NotNull public List<File> getFileFromAllPackagesAndThemes(@NotNull String fileName, @NotNull String area)
    {
        return getFileFromAllPackagesAndThemes(fileName, area, true);
    }

    @NotNull public List<File> getFileFromAllPackagesAndThemes(@NotNull String fileName, @NotNull String area, boolean filterByAllowedPackagesAndThemes)
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        List<File> files = new ArrayList<File>();
        String basePath = getBasePathForArea(area);
        boolean isLayout = fileName.endsWith(".xml");
        String lastFolder = isLayout ? "layout" : "template";
        List<String> packages = getAllPackages(area);
        Set<String> allowedPackages = new LinkedHashSet<String>(settings.getPackages());
        Set<String> allowedThemes = new LinkedHashSet<String>(settings.getThemes());
        for(String packageName : packages){
            if(area.equals("adminhtml") || allowedPackages.size() == 0 || allowedPackages.contains(packageName)){
                List<String> themes = getAllThemes(area, packageName);
                for(String theme : themes){
                    if(area.equals("adminhtml") || allowedThemes.size() == 0 || allowedThemes.contains(theme)){
                        File file = new File(basePath+"/"+packageName+"/"+theme+"/"+lastFolder+"/"+fileName);
                        if(file.exists()){
                            boolean addFile = true;
                            if(filterByAllowedPackagesAndThemes){
                                LayoutFile test = new LayoutFile(file);
                                addFile = test.isValidPackageAndTheme(project);
                            }
                            if(addFile){
                                files.add(file);
                            }
                        }
                    }

                }
            }
        }
        return files;
    }

    @NotNull public List<String> getAllPackages()
    {
        return getAllPackages(area);
    }


    @NotNull public List<String> getAllPackages(@NotNull String area)
    {
        File areaFolder = new File(getBasePathForArea(area));
        return Arrays.asList(FileHelper.getSubdirectories(areaFolder));
    }

    public List<String> getAllThemes(String packageName)
    {
        return getAllThemes(area, packageName);
    }

    public List<String> getAllThemes(String area, String packageName)
    {
        File packageFolder = new File(getBasePathForArea(area)+"/"+packageName);
        return Arrays.asList(FileHelper.getSubdirectories(packageFolder));
    }


    protected String getFileLayoutUpdatesXml(String area, String packageName, String theme/*, int storeId*/)
    {
        // $updatesRoot = Mage::app()->getConfig()->getNode($area.'/layout/updates');
        List<String> updateFiles = getAllActiveXmlLayoutNames(area);
        for(String fileName : updateFiles)
        {
            File file = getFileWithFallback(fileName, MagentoDesignType.LAYOUT);
            // TODO: merge layout xml files
        }
        return null;
    }

    protected File getFileWithFallback(String relativeFilePath, MagentoDesignType type/*, String area, String packageName, String theme*/)
    {
        // TODO: deisgn fallback system
        return null;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getBasePathForArea(String area)
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null){
            return settings.getPathToMagento()+"/"+BASE_PATH+"/"+area;
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
            if(fullpath.size() == 1){
                LayoutXmlTag match = new LayoutXmlTag();
                match.setContext(xml.getContext());
                match.setManager(this);
                return match;
            }
            else if(fullpath.size() == 2){
                HandleXmlTag match = new HandleXmlTag();
                match.setContext(xml.getContext());
                match.setManager(this);
                return match;
            }
            // if it's a child of <action>
            else if(fullpath.get(fullpath.size()-2).equals("action")){
                ActionParameterXmlTag match = new ActionParameterXmlTag();
                match.setContext(xml.getContext());
                match.setManager(this);
                return match;
            }
            return getMatchedTag(fullpath.get(fullpath.size()-1));
        }
        return null;
    }


    /**
     * we can know the correct xmlTag with just the name in the layout...
     * @param name tag names
     * @return
     */
    public MagentoXmlTag getMatchedTag(String name)
    {
        MagentoLayoutXmlTag match = null;
        if(name.equals("block")){
            match = new BlockXmlTag();
        }
        else if(name.equals("action")){
            match = new ActionXmlTag();
        }
        else if(name.equals("reference")){
            match = new ReferenceXmlTag();
        }
        else if(name.equals("update")){
            match = new UpdateXmlTag();
        }
        else if(name.equals("remove")){
            match = new RemoveXmlTag();
        }

        if(match != null){
            match.setContext(xml.getContext());
            match.setManager(this);
        }

        return match;
    }


    public List<Element> findBlocksByName(@NotNull String blockName)
    {
        File layoutFile = getMergedXmlFile();
        String xpath = "//block[@name='"+blockName+"']";
        return XmlHelper.findXpath(layoutFile, xpath);
    }

    @NotNull public List<PsiElement> getPsiClassesForBlockWithName(@NotNull String blockName)
    {
        List<PsiElement> psiClasses = new ArrayList<PsiElement>();
        List<Element> blocks = findBlocksByName(blockName);
        if(blocks != null)
        {
            Set<String> checkedTypes = new HashSet<String>();
            for(Element block : blocks)
            {
                String type = block.getAttributeValue("type");
                if(type != null && ! checkedTypes.contains(type))
                {
                    checkedTypes.add(type);
                    MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
                    List<MagentoClassInfo> classes = magicento.findBlocksOfFactoryUri(type);
                    if(classes != null){
                        for(MagentoClassInfo classInfo : classes){
                            String className = classInfo.name;
                            psiClasses.addAll(PsiPhpHelper.getPsiElementsFromClassName(className, project));
                        }
                    }
                }
            }
        }
        return psiClasses;
    }



    @NotNull public List<XmlTag> findNodesInOriginalXml(@NotNull String[] nodeNames, Map<String, String> attribute)
    {
        List<XmlTag> xmlTags = new ArrayList<XmlTag>();

        List<String> xmlFileNames = getAllActiveXmlLayoutNames();
        List<File> xmlFiles = new ArrayList<File>();
        for(String xmlFileName : xmlFileNames)
        {
            xmlFiles.addAll(getFileFromAllPackagesAndThemes(xmlFileName));
        }
        for(File xmlFile : xmlFiles)
        {
            PsiFile psiFile = FileHelper.getPsiFileFromFile(xmlFile, project);
            if(psiFile != null){
                for(String nodeName : nodeNames){
                    xmlTags.addAll(XmlHelper.findTagInFile((XmlFile)psiFile, nodeName, attribute));
                }
            }
        }
        return xmlTags;
    }


    @NotNull public List<XmlTag> findNodesInOriginalXmlByBlockName(@NotNull String blockName, @NotNull String[] nodeNames)
    {
        Map<String, String> attribute = new HashMap<String, String>();
        attribute.put("name", blockName);

        return findNodesInOriginalXml(nodeNames, attribute);

    }

    @NotNull public List<XmlTag> findNodesInOriginalXmlByBlockAlias(@NotNull String blockAlias, @NotNull String[] nodeNames)
    {
        Map<String, String> attribute = new HashMap<String, String>();
        attribute.put("as", blockAlias);

        return findNodesInOriginalXml(nodeNames, attribute);

    }

    @NotNull public List<XmlTag> findNodesInOriginalXmlByBlockType(@NotNull String blockType)
    {
        Map<String, String> attribute = new HashMap<String, String>();
        attribute.put("type", blockType);

        return findNodesInOriginalXml(new String[]{"block"}, attribute);

    }

    @NotNull public List<XmlTag> findNodesInOriginalXmlByNodeName(@NotNull String nodeName)
    {
        return findNodesInOriginalXml(new String[]{nodeName}, null);
    }


    public Document getMergedXmlDocument()
    {
        if( ! isCacheInvalidated() && cachedXmlDocuments.get(area) != null){
            return cachedXmlDocuments.get(area);
        }
        cachedXmlDocuments.put(area, XmlHelper.getDocumentFromFile(getMergedXmlFile()));
        return cachedXmlDocuments.get(area);
    }


}
