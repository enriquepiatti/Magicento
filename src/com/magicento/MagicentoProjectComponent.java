package com.magicento;

import com.intellij.psi.PsiElement;
import com.magicento.file.MagicentoFileListener;
import com.magicento.helpers.*;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlType;
import com.magicento.models.xml.config.MagentoConfigXml;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.StringLenComparator;
import com.magicento.models.xml.config.system.MagentoSystemXml;
import com.magicento.models.xml.layout.MagentoLayoutXml;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;



public class MagicentoProjectComponent implements ProjectComponent/*, PersistentStateComponent<MagicentoProjectComponent>*/ {

    private Project _project;
    private boolean _isCacheConfigXmlUpdated = false;
    private boolean _isCacheLayoutXmlUpdated = false;

//    final private static String CONFIG_XML = "config.xml";
//    final private static String SYSTEM_XML = "system.xml";
//    final private static String LAYOUT_XML = "layout.xml";


    public MagicentoProjectComponent(Project project) {
        // TODO: try to guess if this project is a Magento project, if not, disable Magicento for this project
        _project = project;
    }

    public static MagicentoProjectComponent getInstance(Project project) {
        //return ApplicationManager.getApplication().getComponent(MagicentoProjectComponent.class);
        //return (MagicentoProjectComponent) project.getComponent("MagicentoProjectComponent");
        return project.getComponent(MagicentoProjectComponent.class);
    }

    public void initComponent()
    {
        _isCacheConfigXmlUpdated = false;
        _isCacheLayoutXmlUpdated = false;
        String cachePath = getCacheDirectoryPath();
        if(cachePath != null){
           final File dir = new File(cachePath);
           if( ! dir.exists() || ! dir.isDirectory()){
               if( ! dir.mkdirs()){
                    IdeHelper.logError("Cache dir: "+cachePath+" cannot be created");
               }
           }
        }
        else{
           IdeHelper.logError("Cannot find magicento cache path");
        }
        initFileListeners();
    }

    private void initFileListeners()
    {
        // TODO: I think it's wrong to put this here, VFS listeners are application level
        VirtualFileManager.getInstance().addVirtualFileListener(new MagicentoFileListener(_project));
    }

    public void disposeComponent()
    {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "MagicentoProjectComponent";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }



    public void disableMagicento()
    {
        MagicentoSettings.getInstance(_project).enabled = false;
    }


    /**
     * @return
     */
    public File getCachedConfigXml()
    {
        return getCachedXml(MagentoConfigXml.TYPE);
    }

    public File getCachedSystemXml()
    {
        return getCachedXml(MagentoSystemXml.TYPE);
    }

    public File getCachedLayoutXml(String area, String packageName, String theme)
    {
        MagentoXml magentoXml = MagentoXmlFactory.getInstance(MagentoLayoutXml.TYPE, _project);
        if(magentoXml != null && magentoXml instanceof MagentoLayoutXml){
            MagentoLayoutXml layout = (MagentoLayoutXml)magentoXml;
            layout.setArea(area);
            layout.setPackageName(packageName);
            layout.setTheme(theme);
            return layout.getMergedXmlFile();
        }
        return null;
    }

    protected File getCachedXml(MagentoXmlType xmlType)
    {
        MagentoXml magentoXml = MagentoXmlFactory.getInstance(xmlType, _project);
        if(magentoXml != null){
            return magentoXml.getMergedXmlFile();
        }
        return null;
    }


    public boolean isDisabled() {
        return ! isEnabled();
    }


    public boolean isEnabled()
    {
        return isEnabled(_project);
    }


    public static boolean isEnabled(Project project) {
        if(project != null){
            MagicentoSettings settings = MagicentoSettings.getInstance(project);
            if(settings != null){
                return settings.enabled;
            }
        }
        return false;
    }


    public void clearAllCache() {
        // TODO: remove all cached files
    }

    public String getDefaultPathToMagento()
    {
        String projectPath = _project.getBaseDir().getPath();
        if(projectPath == null || projectPath.isEmpty()){
            // projectPath = _project.getLocation();
            projectPath = _project.getPresentableUrl();  // getBasePath()
        }
       return projectPath;
    }

    protected String getCacheDirectoryPath()
    {
        String projectPath = _project.getBaseDir().getPath(); //+"/.idea/magicento/";
        //_project.getBaseDir().findChild(".idea")
//        ApplicationManager.getApplication().runWriteAction(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    _project.getBaseDir().findOrCreateChildData(this, ".idea");
//                } catch (IOException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            }
//        });
        File projectFile = new File(projectPath);
        if (projectFile.exists()){
            return projectFile.getAbsolutePath()+"/.idea/magicento/";
        }
        else{
            IdeHelper.logError("Project file: " + projectPath + " does not exist");
        }
        return null;
    }

    public void showMessage(String message, String title, Icon icon)
    {
        // Messages.showMessageDialog(message, title, icon);
        IdeHelper.showDialog(_project, message, title, icon);
    }

    public void showMessageError(String message)
    {
        showMessage(message, "Error", Messages.getErrorIcon());
    }

    public void showMessageInfo(String message)
    {
        showMessage(message, "Info", Messages.getInformationIcon());
    }


    public String executePhpWithMagento(String phpCode)
    {

        MagicentoSettings settings = MagicentoSettings.getInstance(_project);

        if(settings != null)
        {

            String store = settings.store == null ? "" : settings.store;

            String phpMageApp = "require_once PATH_TO_MAGENTO.'/app/Mage.php';Mage::app('"+store+"');";
            String php = phpMageApp;

            php += phpCode;

            String pathToMage = settings.getPathToMage();
            File f = new File(pathToMage);
            while( ! f.isFile() ){

                //Project project = ProjectUtil.guessCurrentProject(null);
                // Project[] projects = ProjectManager.getInstance().getOpenProjects();
                //if(projects.length > 0){
                //Project project = projects[0];
                IdeHelper.showDialog(_project, pathToMage + " is not correct!", "Path to Mage.php incorrect", Messages.getInformationIcon());
                pathToMage = Messages.showInputDialog(_project, "Absolute path to Mage.php (empty or cancel for disabling magicento on this project)", "Path to Mage.php" , Messages.getQuestionIcon(), pathToMage ,null);

                // if user removes the path, disable Magicento for this project
                if( pathToMage == null || pathToMage.isEmpty() ) {
                    disableMagicento();
                    return null;
                }

                settings.setPathToMage(pathToMage);
                f = new File(settings.getPathToMage());

            }

            String pathToMagento = settings.useHttp ? "../.." : ".";
            pathToMagento = "define('PATH_TO_MAGENTO', '"+pathToMagento+"');";
            php = pathToMagento + php;
            if( ! settings.useHttp){
                php = "chdir('" + settings.getPathToMagento() + "');"+php;
            }

            return PHP.execute(php, _project);
        }

        return null;
    }

    /**
     *
     * @param factory full factory, ie: "Mage::getModel('catalog/product')"
     * @return String
     */
    public String getClassNameFromFactory(String factory)
    {
//        if( ! MagentoParser.isUri(factory)){
//            factory = MagentoParser.getUriFromFactory(factory);
//        }
//        List<String> classes = findClassesOfFactoryUri(factory);
//        if(classes != null && classes.size() > 0){
//            return classes.get(0);
//        }
        return executePhpWithMagento("echo get_class("+factory+");");
    }


    /**
     *
     * @param factoryElement PsiMethodReference to the factory (can be chained)
     * @return
     */
    public String getClassNameFromFactory(PsiElement factoryElement)
    {
        List<MagentoClassInfo> classes = findClassesOfFactory(factoryElement);
        if(classes != null && classes.size() > 0){
            return classes.get(0).name;
        }
        return null;
    }


    /**
     * find only classes corresponding to the factory (models, helpers, blocks, etc)
     * @param factoryElement PsiMethodReference to the factory (can be chained)
     * @return
     */
    public List<MagentoClassInfo> findClassesOfFactory(PsiElement factoryElement)
    {
        String uri = MagentoParser.getUriFromFactory(factoryElement);
        if(uri != null)
        {
            List<MagentoClassInfo> classes = null;
            if(MagentoParser.isBlockUri(factoryElement)){
                classes = findBlocksOfFactoryUri(uri);
            }
            else if(MagentoParser.isModelUri(factoryElement)){
                classes = findModelsOfFactoryUri(uri);
            }
            else if(MagentoParser.isResourceModelUri(factoryElement)){
                classes = findResourceModelsOfFactoryUri(uri);
            }
            else if(MagentoParser.isHelperUri(factoryElement)){
                classes = findHelpersOfFactoryUri(uri);
            }
            else {
                classes = findClassesOfFactoryUri(uri);
            }
            return classes;
        }

        return null;
    }


    public List<MagentoClassInfo> findClassesOfFactoryUri(String factory) {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new String[]{"models","blocks","helpers"}, true);
    }

    public List<MagentoClassInfo> findModelsOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new String[]{"models"}, false);
    }

    public List<MagentoClassInfo> findBlocksOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new String[]{"blocks"}, false);
    }

    public List<MagentoClassInfo> findHelpersOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new String[]{"helpers"}, false);
    }

    public List<MagentoClassInfo> findResourceModelsOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), null, true);
    }


    /**
     * TODO: split (simplify) this method and move to another class?
     *
     * @param factory uri of the class
     * @param xmlFile config.xml (merged)
     * @param types (blocks|models|helpers)
     * @param searchResourceModels pass true if you want to retrieve resourceModels too (if types is null or empty and this is true it will search only resourceModels)
     * @return
     */
    protected List<MagentoClassInfo> findClassesOfFactoryUri(String factory, File xmlFile, String[] types, boolean searchResourceModels)
    {
        if(factory == null || factory.isEmpty() || xmlFile == null || ! xmlFile.exists()){
            return null;
        }

        if(types == null){
            types = new String[]{};
        }

        if( ! searchResourceModels && types.length == 0){
            return null;
        }


        Character lastChar = factory.charAt(factory.length()-1);
        boolean exactMatch = true;
        if(lastChar == '*' || lastChar == '_'){
            exactMatch = false;
            if(lastChar == '*'){
                factory = factory.substring(0, factory.length()-1);
            }
        }

        String[] uriParts = factory.split("/");
        if(uriParts.length > 2){
            return null;
        }

        String firstPart = uriParts[0];
        String secondPart = uriParts.length == 2 ? uriParts[1] : "";

        if( ! firstPart.matches("^[A-Za-z0-9_]+$") || ! secondPart.matches("^[A-Za-z0-9_]*$")){
            return null;
        }

        String firstCondition = factory.contains("/") || exactMatch ? ("name()='"+firstPart+"'") : ("starts-with(name(),'" + firstPart + "')");

        boolean searchModels = false;
        boolean searchBlocks = false;
        boolean searchHelpers = false;

        for(int i=0; i<types.length; ++i ){
            if(types[i] == "models")
                searchModels = true;
            else if(types[i] == "blocks")
                searchBlocks = true;
            else if(types[i] == "helpers")
                searchHelpers = true;
            types[i] = "name()='"+types[i]+"'";

        }

        if(searchResourceModels && ! searchModels){
            types = Arrays.copyOf(types, types.length + 1);
            types[types.length-1] = "name()='models'";
        }

        if(searchHelpers && exactMatch && secondPart.isEmpty()){
            secondPart = "data";
        }


        List<MagentoClassInfo> classes = new ArrayList<MagentoClassInfo>();

        List<String> classesRewritten = new ArrayList<String>();

        String typeExp = StringUtils.join(types, " or ");
        //String xpath = "//config/global/*[name()='models' or name()='helpers' or name()='blocks']/*["+firstCondition+"]";
        String xpath = "//config/global/*["+typeExp+"]/*["+firstCondition+"]";
        List<Element> nodes = XmlHelper.findXpath(xmlFile, xpath);
        if(nodes != null){
            for (int i = 0; i < nodes.size(); ++i)
            {

                MagentoClassInfo classInfo = new MagentoClassInfo();

                Element node = nodes.get(i);
                String type = ((Element)node.getParent()).getName();

                if(type == "models")
                {
                    // resourceModels
                    if( searchResourceModels )
                    {
                        String resourceModel = node.getChildText("resourceModel");
                        if(resourceModel != null && ! resourceModel.isEmpty()){
                            String newFactory = resourceModel+"/"+secondPart;
                            if( ! exactMatch){
                                newFactory += "*";
                            }
                            List<MagentoClassInfo> resources = findClassesOfFactoryUri(newFactory, xmlFile, new String[]{"models"}, false);

                            if(resources != null && resources.size() > 0){
                                for(MagentoClassInfo resourceInfo : resources){
                                    if(resourceInfo.isRewrite){
                                        classesRewritten.add(resourceInfo.name);
                                    }
                                }
                                classes.addAll(resources);
                            }

                        }
                    }
                    if( ! searchModels){
                        continue;
                    }
                }

                String group = node.getName();
                String baseClass = node.getChildText("class");
                if(baseClass == null || baseClass.isEmpty()){
                    String groupType = ((Element)node.getParent()).getName();
                    baseClass = "mage_"+group+"_"+ groupType.substring(0, groupType.length()-1);
                }
                // add rewrite class:
                Element rewrite = node.getChild("rewrite");
                if(rewrite != null){
                    List<Element> rewrites = rewrite.getChildren();
                    if(rewrites.size() > 0){
                        for (int j = 0; j < rewrites.size(); ++j) {
                            Element uriNode = rewrites.get(j);
                            String uri = uriNode.getName();
                            if(uri.equals(secondPart) || ( ! exactMatch && uri.startsWith(secondPart))){
                                String classRewrite = uriNode.getValue();
                                classesRewritten.add(classRewrite);

                                MagentoClassInfo classInfoRewrite = new MagentoClassInfo();
                                classInfoRewrite.name = classRewrite;
                                classInfoRewrite.isRewrite = true;
                                classInfoRewrite.uriFirstPart = group;
                                classInfoRewrite.uriSecondPart = uri;
                                classInfoRewrite.setType(type);
                                classes.add(classInfoRewrite);

                            }
                        }
                    }
                }

                String classNamePrefix = Magicento.uc_words(baseClass + "_" + secondPart);

                classInfo.name = classNamePrefix;
                classInfo.isRewrite = false;
                classInfo.uriFirstPart = group;
                classInfo.setType(type);
                classes.add(classInfo);

            }
        }

//        if( ! secondPart.isEmpty()){

//            if (empty($className)) {
//                $className = 'mage_'.$group.'_'.$groupType;
//            }

            List<String> defaultGroupsFromMagento = new ArrayList<String>();
            defaultGroupsFromMagento.add("admin");
            defaultGroupsFromMagento.add("adminhtml");
            defaultGroupsFromMagento.add("api");
            defaultGroupsFromMagento.add("api2");
            defaultGroupsFromMagento.add("backup");
            defaultGroupsFromMagento.add("bundle");
            defaultGroupsFromMagento.add("captcha");
            defaultGroupsFromMagento.add("catalog");
            defaultGroupsFromMagento.add("centinel");
            defaultGroupsFromMagento.add("checkout");
            defaultGroupsFromMagento.add("cms");
            defaultGroupsFromMagento.add("compiler");
            defaultGroupsFromMagento.add("connect");
            defaultGroupsFromMagento.add("contacts");
            defaultGroupsFromMagento.add("cron");
            defaultGroupsFromMagento.add("customer");
            defaultGroupsFromMagento.add("dataflow");
            defaultGroupsFromMagento.add("directory");
            defaultGroupsFromMagento.add("downloadable");
            defaultGroupsFromMagento.add("eav");
            defaultGroupsFromMagento.add("page");
            defaultGroupsFromMagento.add("payment");
            defaultGroupsFromMagento.add("paypal");
            defaultGroupsFromMagento.add("rule");
            defaultGroupsFromMagento.add("sales");
            defaultGroupsFromMagento.add("shipping");
            defaultGroupsFromMagento.add("tax");
            defaultGroupsFromMagento.add("usa");
            defaultGroupsFromMagento.add("wishlist");

            for(int i=defaultGroupsFromMagento.size()-1; i>=0; i--){
                String defaultGroup = defaultGroupsFromMagento.get(i);
                if( ! secondPart.isEmpty() || exactMatch){
                    if( ! defaultGroup.equals(firstPart)){
                        defaultGroupsFromMagento.remove(i);
                    }
                }
                else {
                    if( ! defaultGroup.startsWith(firstPart)){
                        defaultGroupsFromMagento.remove(i);
                    }
                }
            }


            // Add always the default className (magento uses this if there isn't any <class> in <helper> <models> or <blocks>
            if(searchBlocks){
                if( ! secondPart.isEmpty() || exactMatch){
                    MagentoClassInfo genericBlock = new MagentoClassInfo();
                    genericBlock.name = Magicento.uc_words("Mage_"+firstPart+"_Block_"+secondPart);
                    genericBlock.setType("block");
                    genericBlock.uriFirstPart = firstPart;
                    classes.add(genericBlock);
                }
            }
            if(searchHelpers){

                for(String defaultGroup : defaultGroupsFromMagento)
                {
                    MagentoClassInfo genericHelper = new MagentoClassInfo();
                    genericHelper.name = Magicento.uc_words("Mage_"+defaultGroup+"_Helper_"+secondPart);
                    genericHelper.setType("helper");
                    genericHelper.uriFirstPart = defaultGroup;
                    classes.add(genericHelper);
                }

                if( ! secondPart.isEmpty() || exactMatch){
                    MagentoClassInfo genericHelper = new MagentoClassInfo();
                    genericHelper.name = Magicento.uc_words("Mage_"+firstPart+"_Helper_"+secondPart);
                    genericHelper.setType("helper");
                    genericHelper.uriFirstPart = firstPart;
                    classes.add(genericHelper);
                }
            }
            if(searchModels){
                if( ! secondPart.isEmpty() || exactMatch){
                    MagentoClassInfo genericModel = new MagentoClassInfo();
                    genericModel.name = Magicento.uc_words("Mage_"+firstPart+"_Model_"+secondPart);
                    genericModel.setType("model");
                    genericModel.uriFirstPart = firstPart;
                    classes.add(genericModel);
                }
            }
//        }

        if(classes.size() > 0)
        {
            // filter classes
            List<String> classesNames = new ArrayList<String>();

            for(MagentoClassInfo classInfo : classes){
                if( ! classInfo.name.isEmpty()){
                    classesNames.add(classInfo.name);
                }
            }

            // remove duplicates
            classesNames = new ArrayList<String>(new HashSet<String>(classesNames));

            // leave only existent classes
            String regex = "^("+StringUtils.join(classesNames, "|")+")";
            regex += exactMatch ? "$" : ".*";
            Pattern p = Pattern.compile(regex);
            GotoClassModel2 model = new GotoClassModel2(/*ProjectUtil.guessCurrentProject(null)*/_project);
            classesNames.clear();

            //List<String> clazzes = new ArrayList<String>(Arrays.asList(model.getNames(true)));
            //Collections.sort(clazzes);

            for(String clazz: model.getNames(true)){    // TODO: use false here?
                if(p.matcher(clazz).find()) {
                    classesNames.add(clazz);
                }
            }

            // TODO: use a FactoryProximityComparator here?
            Collections.sort(classesNames, StringLenComparator.getInstance());

            // move rewrites to beginning...
            int size = classesNames.size();
            for(String rewrite : classesRewritten){
                for(int i=size-1; i>=0; i--){
                    if(rewrite.equals(classesNames.get(i))){
                        classesNames.add(0, classesNames.remove(i));
                    }
                }
            }

            // inside classesNames we have the valid classes and they are sorted now
            List<MagentoClassInfo> sortedAndValidClasses = new ArrayList<MagentoClassInfo>();
            for(String className : classesNames){
                Iterator<MagentoClassInfo> i = classes.iterator();
                while (i.hasNext()) {
                    MagentoClassInfo c = i.next();
                    if(className.startsWith(c.name)){
                        MagentoClassInfo newClassInfo = c.clone();
                        newClassInfo.name = className;
                        if( ! newClassInfo.isRewrite){
                            newClassInfo.uriSecondPart = null;
                        }
                        sortedAndValidClasses.add(newClassInfo);
                        // i.remove();  // we don't remove it because it could have more classes names (because inside "classes" we are saving prefixes)
                        break;
                    }
                }
            }

            return sortedAndValidClasses;

        }

        return classes;
    }


    public File getCachedFile(String fileName) {
        String path = getCacheDirectoryPath()+fileName;
        File file = new File(path);
        //FileUtil.createIfDoesntExist(file);
        return file;
    }

    public boolean saveCacheFile(File file, String content)
    {
        try{
            FileUtil.createIfDoesntExist(file);
            FileUtil.writeToFile(file, content);
            return true;
        }
        catch (IOException e) {
            showMessageError("Error writing " + file.getPath());
        }
        return false;
    }

}
