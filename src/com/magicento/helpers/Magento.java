package com.magicento.helpers;

import com.magicento.MagicentoSettings;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.WordUtils;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
import java.util.*;

/**
 * This class has code and functions translated from Magento to Java
 *
 * @author Enrique Piatti
 */
public class Magento {

    protected static Map<Project, Magento> instanceByProject;

    protected Project project;
    protected Document sortedModulesCache;

    protected Magento(Project project){
        this.project = project;
    }

    public static Magento getInstance(Project project)
    {
        if(instanceByProject == null){
            instanceByProject = new HashMap<Project, Magento>();
        }
        if( ! instanceByProject.containsKey(project)){
            Magento instance = new Magento(project);
            instanceByProject.put(project, instance);
        }
        return instanceByProject.get(project);
    }


    /**
     * simulates the uc_words function from magento
     * @param str
     * @return
     */
    public static String uc_words(String str, String destSep, String srcSep)
    {
        return WordUtils.capitalize(str.replace(srcSep, " ")).replace(" ", destSep);
    }

    public static String uc_words(String str)
    {
        return uc_words(str, "_", "_");
    }

    public static String uc_words(String str, String destSep)
    {
        return uc_words(str, destSep, "_");
    }

    /**
     * get all module files from app/etc/modules sorted by default (first Mage_All, then all Mage_ not taking into account <depends> )
     * @return
     */
    protected List<File> _getDeclaredModuleFiles()
    {
        //final GotoFileModel gotoFileModel = new GotoFileModel(project);

        // load XML files from /app/etc
        String pathToMagento = MagicentoSettings.getInstance(project).getPathToMagento();
        String etcDir = pathToMagento+"/app/etc";

        etcDir += "/modules";
        List <File> unsortedFiles = new ArrayList<File>();
        File[] moduleFiles = JavaHelper.getAllFilesFromDirectory(etcDir, "^.+\\.xml$");
        if(moduleFiles == null){
            return null;
        }
        unsortedFiles.addAll(Arrays.asList(moduleFiles));

        List <File> base = new ArrayList<File>();
        List <File> mage = new ArrayList<File>();
        List <File> custom = new ArrayList<File>();


        for(File moduleFile : unsortedFiles){
            String fileName = moduleFile.getName();
            if(fileName.equals("Mage_All")){
                base.add(moduleFile);
            }
            else if(fileName.startsWith("Mage_")){
                mage.add(moduleFile);
            }
            else {
                custom.add(moduleFile);
            }
        }

        List <File> sortedFiles = new ArrayList<File>();
        sortedFiles.addAll(base);
        sortedFiles.addAll(mage);
        sortedFiles.addAll(custom);

        if(sortedFiles == null || sortedFiles.size() == 0) {
            IdeHelper.logError("Impossible to file module files in path: "+pathToMagento);
        }
        //return sortedFiles.toArray(new File[sortedFiles.size()]);
        return sortedFiles;

    }


    /**
     * Inner class for simulating a "struct"
     */
    class DependsInfo {
        public String module;
        public Set<String> depends;
        public boolean active = false;
    }


    public void invalidateDeclaredModulesCache()
    {
        sortedModulesCache = null;
    }


    /**
     * Simulates Mage_Core_Model_Config::_loadDeclaredModules, load the <modules> in order (according to depends, etc)
     * this doesn't filter the active/inactive modules
     * @return
     */
    protected Document _loadDeclaredModules()
    {

        if(sortedModulesCache != null){
            return sortedModulesCache;
        }

        List<File> moduleFiles = _getDeclaredModuleFiles();
        if(moduleFiles != null && moduleFiles.size() > 0){
            Document unsortedConfig = XmlHelper.mergeXmlFiles(moduleFiles);
            if(unsortedConfig != null)
            {
                // use LinkedHashMap to preserve insertion order
                Map<String, DependsInfo> moduleDepends = new LinkedHashMap<String, DependsInfo>();
                Element modules = unsortedConfig.getRootElement().getChild("modules");
                if(modules != null){
                    List <Element> modulesChildren = modules.getChildren();
                    for(Element moduleChild : modulesChildren){
                        DependsInfo info = new DependsInfo();
                        info.module = moduleChild.getName();
                        info.active = _isModuleActive(moduleChild);
                        Element depends = moduleChild.getChild("depends");
                        if(depends != null){
                            List<Element> dependsList = depends.getChildren();
                            info.depends = new LinkedHashSet<String>();
                            for(Element depend : dependsList){
                                info.depends.add(depend.getName());
                            }
                        }
                        moduleDepends.put(info.module, info);
                    }


                    try {
                        List<String> sortedModules = _sortModuleDepends(moduleDepends);

                        Document sortedConfig = new Document(new Element("config").addContent(new Element("modules")));

                        // add remaining nodes (if they exist)
                        // new ArrayList for avoiding ConcurrentModificationException
                        List<Element> otherChildren = new ArrayList(unsortedConfig.getRootElement().getChildren());
                        for(Element otherChild : otherChildren){
                            if( ! otherChild.getName().equals("modules")){
                                sortedConfig.getRootElement().addContent(otherChild.detach());
                            }
                        }

                        for(String moduleName : sortedModules){
                            List<Element> node = XmlHelper.findXpath(unsortedConfig, "config/modules/"+moduleName);
                            if(node != null && node.size()>0){
                                sortedConfig.getRootElement().getChild("modules").addContent(node.get(0).detach());
                            }
                        }

                        sortedModulesCache = sortedConfig;
                        return sortedConfig;

                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

                    }
                }
            }
        }


        return null;
    }


    /**
     * returns sorted list of modules (module names) according to depends
     * this doesn't filter the inactive modules. But it wil throw an error if an active module doesn't have the corresponding depends
     * this is basically a topological sort, we are assuming there are no cycles here, but we are not using a topological sort algorithm
     * we are transalting from PHP to Java the original code from magento (which is buggy and ugly)
     * @return
     */
    protected List<String> _sortModuleDepends(Map<String, DependsInfo> unsortedModulesByName) throws Exception
    {
        if(unsortedModulesByName == null || unsortedModulesByName.size() == 0){
            return null;
        }

        List<DependsInfo> modules = new ArrayList<DependsInfo>();
        for(Map.Entry<String, DependsInfo> entry : unsortedModulesByName.entrySet())
        {

            DependsInfo info = entry.getValue();

            String moduleName = info.module;  // entry.getKey();
            boolean active = info.active;

            Set<String> newDependsWithIndirects = new LinkedHashSet<String>();
            if(info.depends != null){
                // check direct depends for this module, and add the indirect depends too
                for(String depend : info.depends)
                {
                    DependsInfo dependsInfo = unsortedModulesByName.containsKey(depend) ? unsortedModulesByName.get(depend) : null;
                    // throw error if this module is active and it doesn't have the required depends
                    if(active && ( dependsInfo == null || ! dependsInfo.active) )
                    {
                        String message = "Module "+moduleName+" requires module "+depend;
                        throw new Exception(message);
                    }
                    newDependsWithIndirects.add(depend);
                    if(dependsInfo != null && dependsInfo.depends != null)
                        newDependsWithIndirects.addAll(dependsInfo.depends);
                }
            }
            // always add a non null object, even if it's empty, it's easier for checking below
            info.depends = newDependsWithIndirects;

            modules.add(info);
        }

        // sort the modules according to depends
        int size = modules.size()-1;
        int i,j;
        for (i = size; i >= 0; i--) {
            for (j = size; i < j; j--)
            {
                // if module in "i" depends on a module positioned in "j" (after) interchange positions
                if( modules.get(i).depends.contains(modules.get(j).module) ){
                    Collections.swap(modules, i, j);
                }
            }
        }

        // we are checking again if every module has all dependencies defined before them
        Set<String> definedModules = new LinkedHashSet<String>();
        List<String> sortedModules = new ArrayList<String>();
        for(DependsInfo dependsInfo : modules){
            for(String dependency : dependsInfo.depends ){
                if( ! definedModules.contains(dependency)){
                    String message = "Module "+dependsInfo.module+" cannot depend on "+dependency;
                    throw new Exception(message);
                }
            }
            definedModules.add(dependsInfo.module);
            sortedModules.add(dependsInfo.module);
        }

        return sortedModules;
    }


    /**
     * returns full path to a local directory inside the module folder
     * simulates Mage_Core_Model_Config::getModuleDir
     * @return
     */
    public String getModuleDir(String type, String moduleName)
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null){
            Document modules = _loadDeclaredModules();
            if(modules != null){
                Element module = _getModuleConfig(moduleName);
                if(module != null){
                    Element codePoolNode = module.getChild("codePool");
                    if(codePoolNode != null){
                        String codePool = codePoolNode.getValue();
                        String dir = settings.getPathToMagento() + "/app/code/" + codePool + "/" + uc_words(moduleName, "/");
                        return dir + "/" + type;
                    }
                }
            }
        }
        return null;
    }


    protected Element _getModuleConfig(String moduleName)
    {
        Document declaredModules = _loadDeclaredModules();
        if(declaredModules != null){
            Element modulesNode = declaredModules.getRootElement().getChild("modules");
            if(modulesNode != null){
                return modulesNode.getChild(moduleName);
            }
        }
        return null;
    }


    /**
     * simulates the loadModulesConfiguration from Magento
     * this is a simplified version, it doesn't accept a mergeModel, and it always load the local modules (magento
     * can exclude local as an option)
     * @param fileName config.xml|system.xml|etc...
     * @return merged xml string
     */
    public Document loadModulesConfiguration(String fileName, Document mergeInto)
    {
        Document sortedModules = _loadDeclaredModules();
        if(sortedModules != null)
        {
            List<Element> modules = sortedModules.getRootElement().getChild("modules").getChildren();
            for(Element module : modules){
                if(_isModuleActive(module)){
                    String configFile = getModuleDir("etc", module.getName()) + "/" + fileName;
                    Document document = XmlHelper.getDocumentFromFile(new File(configFile));
                    mergeInto = XmlHelper.mergeXmlDocuments(mergeInto, document);
                }
            }
        }

        return mergeInto;
    }


    protected boolean _isModuleActive(Element module)
    {
        if(module != null){
            Element activeNode = module.getChild("active");
            if(activeNode != null)
            {
                return activeNode.getValue().equals("true") ? true : false;
            }
        }
        return false;
    }


    /**
     * simulates Mage_Core_Model_Config::loadModules. Load config.xml for only active modules, taking into account
     * <depends> and loading local.xml always at the end
     * @return merged config.xml
     */
    public Document loadModules()
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null)
        {
            Document configXml = new Document(new Element("config"));
            String pathToMagento = settings.getPathToMagento();
            String pathToEtc = pathToMagento+"/app/etc";
            String pathXml = pathToEtc + "/config.xml";
            configXml = XmlHelper.mergeXmlDocuments(configXml, XmlHelper.getDocumentFromFile(new File(pathXml)));

            pathXml = pathToEtc + "/enterprise.xml";
            configXml = XmlHelper.mergeXmlDocuments(configXml, XmlHelper.getDocumentFromFile(new File(pathXml)));

            configXml = loadModulesConfiguration("config.xml", configXml);

            // Prevent local.xml directives overwriting
            pathXml = pathToEtc + "/local.xml";
            configXml = XmlHelper.mergeXmlDocuments(configXml, XmlHelper.getDocumentFromFile(new File(pathXml)));

            // TODO: applyExtends
            // applyExtends()

            return configXml;
        }

        return null;
    }

}
