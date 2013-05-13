package com.magicento;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.magicento.file.MagicentoFileListener;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PHP;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.MagentoFactory;
import com.magicento.models.MagentoFactoryCache;
import com.magicento.models.PhpStormMetaNamespace;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlType;
import com.magicento.models.xml.config.MagentoConfigXml;
import com.magicento.models.xml.config.system.MagentoSystemXml;
import com.magicento.models.xml.layout.MagentoLayoutXml;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;



public class MagicentoProjectComponent implements ProjectComponent/*, PersistentStateComponent<MagicentoProjectComponent>*/ {

    private Project _project;
    private boolean _isCacheConfigXmlUpdated = false;
    private boolean _isCacheLayoutXmlUpdated = false;
    private MagentoFactory _magentoFactory;

//    final private static String CONFIG_XML = "config.xml";
//    final private static String SYSTEM_XML = "system.xml";
//    final private static String LAYOUT_XML = "layout.xml";


    public MagicentoProjectComponent(Project project) {
        // TODO: try to guess if this project is a Magento project, if not, disable Magicento for this project
        _project = project;
        _magentoFactory = new MagentoFactoryCache(_project); // new MagentoFactory(_project);
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

    public File getCachedLayoutXml(String area)
    {
        return getCachedLayoutXml(area, null, null);
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
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.MODEL, MagentoClassInfo.UriType.BLOCK, MagentoClassInfo.UriType.HELPER, MagentoClassInfo.UriType.RESOURCEMODEL});
    }

    public List<MagentoClassInfo> findModelsOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.MODEL});
    }

    public List<MagentoClassInfo> findBlocksOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.BLOCK});
    }

    public List<MagentoClassInfo> findHelpersOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.HELPER});
    }

    public List<MagentoClassInfo> findResourceModelsOfFactoryUri(String factory)
    {
        return findClassesOfFactoryUri(factory, getCachedConfigXml(), new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.RESOURCEMODEL});
    }


    /**
     *
     * @param factory uri of the class
     * @param xmlFile config.xml (merged)
     * @param types
     * @return
     */
    protected List<MagentoClassInfo> findClassesOfFactoryUri(String factory, File xmlFile, MagentoClassInfo.UriType[] types)
    {
        return getMagentoFactory().findClassesForFactory(factory, xmlFile, types);
    }

    public MagentoFactory getMagentoFactory()
    {
        return _magentoFactory;
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


    public MagicentoSettings getMagicentoSettings()
    {
        return MagicentoSettings.getInstance(_project);
    }

    public void savePhpStormMetaFile(String content)
    {
        PhpStormMetaNamespace.getInstance(_project).savePhpStormMetaFile(content);
    }

    public String getPhpStormMetaFilePath()
    {
        return PhpStormMetaNamespace.getInstance(_project).getPhpStormMetaFilePath();
    }

    public File getPhpStormMetaFile()
    {
        return PhpStormMetaNamespace.getInstance(_project).getPhpStormMetaFile();
    }


}
