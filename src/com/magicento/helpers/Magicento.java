package com.magicento.helpers;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.magicento.MagicentoProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoSettings;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for doing magic things
 * @author Enrique Piatti
 */
public class Magicento {

    /**
     * get the Namespace_Module of the file
     * @param path
     * @return
     */
    public static String getNamespaceModule(String path)
    {
        return MagentoParser.getModuleNameFromModulePath(path);

//        String[] pools = {"core", "community", "local"};
//        String pool = "(" + StringUtils.join(pools, "|") + ")";
//        String regex = "/app/code/"+pool+"/([^/]+)/([^/]+)/";
//        // regex = "/app/code/(core|community|local)/([^/]+)/([^/]+)/";
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(path);
//        if (m.find()) {
//            String namespace = m.group(2);
//            String module = m.group(3);
//            return namespace+"_"+module;
//        }
//        return null;
    }

    public static String getNamespaceModule(File file)
    {
        String path = file.getPath();
        return getNamespaceModule(path);
    }

    public static String getNamespaceModule(VirtualFile file)
    {
        String path = file.getPath();
        return getNamespaceModule(path);
    }

    public static String getNamespaceModuleFromClassName(String className)
    {
        return MagentoParser.getNamespaceModuleFromClassName(className);
    }

    /**
     * @deprecated
     * @see Magento uc_words
     * simulates the uc_words function from magento
     * @param str
     * @return
     */
    public static String uc_words(String str)
    {
        return Magento.uc_words(str);
    }

    public static File getCachedConfigXml(Project project)
    {
        if(project != null){
            MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
            if(magicentoProject != null){
                return MagicentoProjectComponent.getInstance(project).getCachedConfigXml();
            }
        }
        return null;
    }


    public static String getClassNameFromFilePath(String filePath)
    {
        String[] pools = {"core", "community", "local"};
        String pool = "(" + StringUtils.join(pools, "|") + ")";
        String regex = "/app/code/"+pool+"/(.+)\\.php$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filePath);
        if (m.find()) {
            String classPath = m.group(2);
            classPath = classPath.replace("/controllers", "").replace("\\controllers", "");
            String className = classPath.replace('/', '_').replace('\\', '_');
            return className;
        }
        return null;
    }


    @NotNull
    public static String[] getAllPackages(Project project, String area)
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null){
            String pathToMagento = settings.getPathToMagento();
            File areaDirectory = new File(pathToMagento+"/app/design/"+area);
            return FileHelper.getSubdirectories(areaDirectory);
        }
        return new String[]{};
    }

    @NotNull
    public static String[] getAllThemesFromPackage(Project project, String area, String packageName)
    {
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null){
            String pathToMagento = settings.getPathToMagento();
            File areaDirectory = new File(pathToMagento+"/app/design/"+area+"/"+packageName);
            return FileHelper.getSubdirectories(areaDirectory);
        }
        return new String[]{};
    }


    /**
     * get factory uri given the class name
     * @param className
     * @return
     */
    public static String getUriFromClassName(@NotNull Project project, @NotNull String className)
    {
        // check for rewrites first
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        File configXml = magicento.getCachedConfigXml();
        if(configXml != null && configXml.exists())
        {
            String xpath = "//rewrite/*[text()='"+className+"']";
            List<Element> rewrites = XmlHelper.findXpath(configXml, xpath);
            if(rewrites != null && rewrites.size() > 0){
                // ideally we will have only one rewrite
                Element rewrite = rewrites.get(0);
                String secondPart = rewrite.getName();
                String firstPart = rewrite.getParentElement().getParentElement().getName();
                return firstPart+"/"+secondPart;
            }

            String classPrefix = MagentoParser.getClassPrefix(className);
            xpath = "//global/*[name()='models' or name()='blocks' or name()='helpers']/*[class[text()='"+classPrefix+"']]";
            List<Element> factories = XmlHelper.findXpath(configXml, xpath);
            if(factories != null && factories.size() > 0){
                // ideally we will have only one factory for the class prefix
                Element factory = factories.get(0);
                String firstPart = factory.getName();
                String secondPart = MagentoParser.getSecondPartUriFromClassName(className);
                return firstPart+"/"+secondPart;
            }

        }

        return null;
    }


    @NotNull public static List<String> getStoreConfigPaths(Project project, String prefix)
    {
        List<String> paths = new ArrayList<String>();
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        if(magicento != null)
        {
            String lastPrefix = prefix;
            boolean endsWithSlash = prefix.endsWith("/");

            String[] parts = prefix.split("/");
            if(endsWithSlash){
                lastPrefix = "";
            }
            else if(parts.length > 0){
                lastPrefix = parts[parts.length-1];
            }

            // cehck for system config values
            if(parts.length < 3 || (parts.length == 3 && ! endsWithSlash))
            {
                File configFile = magicento.getCachedSystemXml();
                String xpath = "config/sections/";
                if(parts.length > 1 || (parts.length == 1 && endsWithSlash)){
                    xpath += parts[0]+"/groups/";
                }
                if(parts.length > 2 || (parts.length == 2 && endsWithSlash)){
                    xpath += parts[1]+"/fields/";
                }
                xpath += "*";
                if( ! lastPrefix.isEmpty()){
                    xpath += "[starts-with(name(),'" + lastPrefix + "')]";
                }

                List<Element> nodes = XmlHelper.findXpath(configFile, xpath);
                if(nodes != null && nodes.size() > 0){
                    for(Element node : nodes)
                    {
                        String nodeName = node.getName();
                        String path = prefix.substring(0, prefix.length()-lastPrefix.length())+nodeName;
                        paths.add(path);
                    }
                }

            }

            // get list of config values from config.xml
            File configFile = magicento.getCachedConfigXml();
            String xpath = "config/default/";
            int limit = endsWithSlash ? parts.length : (parts.length-1);
            for(int i=0; i<limit; i++){
                xpath += parts[i]+"/";
            }
            xpath += "*";
            if( ! lastPrefix.isEmpty()){
                xpath += "[starts-with(name(),'" + lastPrefix + "')]";
            }
            List<Element> nodes = XmlHelper.findXpath(configFile, xpath);
            if(nodes != null && nodes.size() > 0){
                for(Element node : nodes)
                {
                    String nodeName = node.getName();
                    String path = prefix.substring(0, prefix.length()-lastPrefix.length())+nodeName;
                    paths.add(path);
                }
            }

        }
        return paths;
    }

}
