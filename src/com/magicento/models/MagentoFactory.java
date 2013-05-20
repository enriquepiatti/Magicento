package com.magicento.models;

import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.project.Project;
import com.intellij.util.StringLenComparator;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.XmlHelper;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Enrique Piatti
 */
public class MagentoFactory
{

    protected Project _project;

    public MagentoFactory(Project _project) {
        this._project = _project;
    }

    protected boolean isExactMatch(@NotNull String factoryToSearch)
    {
        Character lastChar = factoryToSearch.charAt(factoryToSearch.length()-1);
        boolean exactMatch = true;
        if(lastChar == '*' || lastChar == '_'){
            exactMatch = false;
        }
        return exactMatch;
    }

    protected String[] prepareUriParts(@NotNull String factory)
    {
        Character lastChar = factory.charAt(factory.length()-1);
        if(lastChar == '*'){
            factory = factory.substring(0, factory.length()-1);
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

        return new String[]{firstPart, secondPart};

    }

    protected String createXpath(String factoryToSearch, boolean searchModels, boolean searchBlocks, boolean searchHelpers, boolean searchResourceModels)
    {
        boolean exactMatch = isExactMatch(factoryToSearch);
        String[] uriParts = prepareUriParts(factoryToSearch);
        String firstPart = uriParts[0];
        // String secondPart = uriParts[1];
        String firstCondition = factoryToSearch.contains("/") || exactMatch ? ("name()='"+firstPart+"'") : ("starts-with(name(),'" + firstPart + "')");

        List<String> typeConditions = new ArrayList<String>();

        if(searchModels || searchResourceModels){
            typeConditions.add("name()='models'");
        }
        if(searchBlocks){
            typeConditions.add("name()='blocks'");
        }
        if(searchHelpers){
            typeConditions.add("name()='helpers'");
        }

        String typeExp = StringUtils.join(typeConditions, " or ");
        //String xpath = "//config/global/*[name()='models' or name()='helpers' or name()='blocks']/*["+firstCondition+"]";
        String xpath = "/config/global/*["+typeExp+"]/*["+firstCondition+"]";

        return xpath;
    }


    protected List<String> getDefaultGroupsFromMagento()
    {
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
        return defaultGroupsFromMagento;
    }

    protected List<MagentoClassInfo> addDefaultClasses(List<MagentoClassInfo> classes, String firstPart, String secondPart, boolean firstPartIsComplete, boolean searchBlocks, boolean searchHelpers, boolean searchModels)
    {
        List<String> defaultGroupsFromMagento = getDefaultGroupsFromMagento();

        for(int i=defaultGroupsFromMagento.size()-1; i>=0; i--){
            String defaultGroup = defaultGroupsFromMagento.get(i);
            // if firstpart is complete, remove from default group if it doesn't match
            if( (firstPartIsComplete && ! defaultGroup.equals(firstPart)) || ! defaultGroup.startsWith(firstPart) ){
                defaultGroupsFromMagento.remove(i);
            }
        }

        // Add always the default className (magento uses this if there isn't any <class> in <helper> <models> or <blocks>
        if(searchBlocks){
            for(String defaultGroup : defaultGroupsFromMagento)
            {
                MagentoClassInfo genericHelper = new MagentoClassInfo();
                genericHelper.name = Magicento.uc_words("Mage_"+defaultGroup+"_Block_"+secondPart);
                genericHelper.setType("block");
                genericHelper.uriFirstPart = defaultGroup;
                classes.add(genericHelper);
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

        }
        if(searchModels){
            for(String defaultGroup : defaultGroupsFromMagento)
            {
                MagentoClassInfo genericHelper = new MagentoClassInfo();
                genericHelper.name = Magicento.uc_words("Mage_"+defaultGroup+"_Model_"+secondPart);
                genericHelper.setType("model");
                genericHelper.uriFirstPart = defaultGroup;
                classes.add(genericHelper);
            }
        }
        return classes;
    }



    protected List<String> getExistentClassNamesFromPossibleClassNames(List<MagentoClassInfo> classes, boolean exactMatch)
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
        GotoClassModel2 model = new GotoClassModel2(_project);
        classesNames.clear();

        for(String clazz: model.getNames(true)){    // TODO: use false here?
            if(p.matcher(clazz).find()) {
                classesNames.add(clazz);
            }
        }
        return classesNames;
    }


    /**
     *
     * @param factory uri of the class
     * @param xmlFile config.xml (merged)
     * @param types (blocks|models|helpers)
     * @return
     */
    public List<MagentoClassInfo> findClassesForFactory(String factory, File xmlFile, MagentoClassInfo.UriType[] types)
    {
        if(factory == null || factory.isEmpty() || xmlFile == null || ! xmlFile.exists()){
            return null;
        }
        Document xmlDocuemnt = XmlHelper.getDocumentFromFile(xmlFile);
        return findClassesForFactory(factory, xmlDocuemnt, types);
    }



    /**
     *
     * @param factory uri of the class
     * @param xmlDocument config.xml (merged)
     * @param types (blocks|models|helpers)
     * @return
     */
    public List<MagentoClassInfo> findClassesForFactory(String factory, Document xmlDocument, MagentoClassInfo.UriType[] types)
    {
        if(factory == null || factory.isEmpty() || xmlDocument == null){
            return null;
        }

        if(types == null){
            types = new MagentoClassInfo.UriType[]{};
        }


        boolean exactMatch = isExactMatch(factory);

        String[] uriParts = prepareUriParts(factory);
        if(uriParts == null){
            return null;
        }

        String firstPart = uriParts[0];
        String secondPart = uriParts[1];

        boolean searchModels = false;
        boolean searchBlocks = false;
        boolean searchHelpers = false;
        boolean searchResourceModels = false;

        for(int i=0; i<types.length; ++i ){
            if(types[i] == MagentoClassInfo.UriType.MODEL){
                searchModels = true;
            }
            else if(types[i] == MagentoClassInfo.UriType.BLOCK) {
                searchBlocks = true;
            }
            else if(types[i] == MagentoClassInfo.UriType.HELPER){
                searchHelpers = true;
            }
            else if(types[i] == MagentoClassInfo.UriType.RESOURCEMODEL){
                searchResourceModels = true;
            }
        }

        if(searchHelpers && exactMatch && secondPart.isEmpty()){
            secondPart = "data";
        }

        List<MagentoClassInfo> classes = new ArrayList<MagentoClassInfo>();

        List<String> classesRewritten = new ArrayList<String>();

        String xpath = createXpath(factory, searchModels, searchBlocks, searchHelpers, searchResourceModels);

        List<Element> nodes = XmlHelper.findXpath(xmlDocument, xpath);
        if(nodes != null)
        {
            for (int i = 0; i < nodes.size(); ++i)
            {

                MagentoClassInfo classInfo = new MagentoClassInfo();

                Element node = nodes.get(i);
                String type = ((Element)node.getParent()).getName();
                String group = node.getName();

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
                            List<MagentoClassInfo> resources = findClassesForFactory(newFactory, xmlDocument, new MagentoClassInfo.UriType[]{MagentoClassInfo.UriType.MODEL});

                            if(resources != null && resources.size() > 0){
                                for(MagentoClassInfo resourceInfo : resources){
                                    if(resourceInfo.isRewrite){
                                        classesRewritten.add(resourceInfo.name);
                                    }
                                    resourceInfo.uriFirstPart = group;
                                    resourceInfo.setType(MagentoClassInfo.UriType.RESOURCEMODEL);
                                    classes.add(resourceInfo);
                                }
                            }

                        }
                    }
                    if( ! searchModels){
                        continue;
                    }
                }


                String baseClass = node.getChildText("class");
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
                // if it's not rewrite and doesn't have a <class> node, try with the default "mage_" option
                else if(baseClass == null || baseClass.isEmpty()){
                    String groupType = ((Element)node.getParent()).getName();
                    baseClass = "mage_"+group+"_"+ groupType.substring(0, groupType.length()-1);
                }

                if(rewrite == null || ! (baseClass == null || baseClass.isEmpty())){
                    String classNamePrefix = Magicento.uc_words(baseClass + "_" + secondPart);

                    classInfo.name = classNamePrefix;
                    classInfo.isRewrite = false;
                    classInfo.uriFirstPart = group;
                    classInfo.setType(type);
                    classes.add(classInfo);
                }
            }
        }


        boolean firstPartIsComplete = exactMatch || ! secondPart.isEmpty();

        classes = addDefaultClasses(classes, firstPart, secondPart, firstPartIsComplete, searchBlocks, searchHelpers, searchModels);

        if(classes.size() > 0)
        {
            // filter classes
            List<String> classesNames = getExistentClassNamesFromPossibleClassNames(classes, exactMatch);

            // TODO: use a FactoryProximityComparator here?
            Collections.sort(classesNames, StringLenComparator.getInstance());

            // move rewrites to beginning so they are shown first
            int size = classesNames.size();
            for(String rewrite : classesRewritten){
                for(int i=size-1; i>=0; i--){
                    if(rewrite.equals(classesNames.get(i))){
                        classesNames.add(0, classesNames.remove(i));
                    }
                }
            }

            // inside classesNames we have the valid classes and they are sorted now
            // update the MagentoClassInfo (put the complete class name)
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



}
