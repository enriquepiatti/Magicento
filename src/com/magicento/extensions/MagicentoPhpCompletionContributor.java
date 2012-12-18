package com.magicento.extensions;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.IconLoader;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ArrayListSet;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.layout.Template;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * completion contributor for PHP code
 * @author Enrique Piatti
 */
public class MagicentoPhpCompletionContributor extends CompletionContributor {

    private static final Icon myIcon = MagicentoIcons.MAGENTO_ICON_16x16; //IconLoader.getIcon("/icons/magento.png");

    public MagicentoPhpCompletionContributor()
    {
        final PsiElementPattern.Capture<PsiElement> everywhere = PlatformPatterns.psiElement();

        extend(CompletionType.BASIC, everywhere,
            new CompletionProvider<CompletionParameters>()
            {

                public void addCompletions(@NotNull final CompletionParameters parameters,
                                           final ProcessingContext matchingContext,
                                           @NotNull final CompletionResultSet _result) {


                    if(parameters == null || ! MagicentoProjectComponent.isEnabled(parameters.getOriginalFile().getProject())) {
                        return;
                    }

                    PsiElement currentElement = parameters.getPosition();

                    if(currentElement != null)
                    {
                        List<LookupElement> elements = null;
                        final String prefix = _result.getPrefixMatcher().getPrefix();

                        // autocomplete classname
                        if( isClassnameAutocomplete(currentElement))
                        {
                            String filePath = parameters.getOriginalFile().getVirtualFile().getPath();
                            elements = getAutocompleteForClassName(filePath);
                        }
                        // autocomplete factories
                        else if(isFactoryAutocomplete(currentElement))
                        {
                            elements = getAutocompleteForFactory(currentElement, prefix);
                        }
                        else if(isGetStoreConfigAutocomplete(currentElement))
                        {
                            elements = getAutocompleteForGetStoreConfig(currentElement, prefix);
                        }
                        else if(isGetTable(currentElement))
                        {
                            elements = getAutocompleteForGetTable(currentElement, prefix);
                        }
                        else if(isGetChildInTemplate(currentElement)){
                            elements = getAutocompleteForGetChildInTemplate(currentElement, prefix);
                        }
                        else if(MagentoParser.isBlockNameInTemplate(currentElement)){
                            elements = getAutocompleteForGetBlockInTemplate(currentElement, prefix);
                        }
                        else if(isClassExtendsAutocomplete(currentElement)){
                            elements = getAutocompleteForClassExtends(currentElement, prefix);
                        }

                        if(elements != null && elements.size() > 0)
                        {
                            _result.addAllElements(elements);
//                            for(LookupElement element : elements){
//                                _result.addElement(element);
//                            }
                        }
                    }
                }
            });
    }



    /**
     * checks if autocomplete is requested after "extends"
     * @param currentElement
     * @return
     */
    protected boolean isClassExtendsAutocomplete(PsiElement currentElement)
    {
        PsiElement prevSibling = currentElement.getPrevSibling();
        if(prevSibling == null){
            prevSibling = currentElement.getParent().getPrevSibling();
        }
        while(prevSibling != null && prevSibling instanceof PsiWhiteSpace){
            prevSibling = prevSibling.getPrevSibling();
        }
        if(prevSibling != null && prevSibling.getText().equals("extends")){
            return true;
        }
        return false;
    }


    protected boolean isGetChildInTemplate(@NotNull PsiElement currentElement)
    {

        return MagentoParser.isBlockAliasInTemplate(currentElement);

//        // autcomplete works only if cursor is over the parameter list of ->getChildHtml([HERE])
//        PsiElement element = getParameterListElement(currentElement);
//        if(element != null)
//        {
//            PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
//            if(methodReference != null){
//                if(MagentoParser.isGetChildInTemplate(methodReference)){
//                    return true;
//                }
//            }
//        }
//        return false;
    }


    protected PsiElement getParameterListElement(PsiElement currentElement)
    {
        PsiElement element = null;
        if(currentElement != null)
        {
            if(PsiPhpHelper.isParameterList(currentElement)){
                element = currentElement;
            }
            if(element == null){
                element = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.PARAMETER_LIST);
            }
        }
        return element;
    }



    protected boolean isGetTable(PsiElement currentElement)
    {
        if(currentElement != null)
        {
            // autcomplete works only if cursor is over the parameter list of ->getTable([HERE])
            PsiElement element = getParameterListElement(currentElement);
            if(element != null)
            {
                PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
                if(methodReference != null){
                    if(MagentoParser.isGetTable(methodReference)){
                        return true;
                    }
                    if(MagentoParser.isMethod(methodReference, "_init") && MagentoParser.isResourceModel(methodReference)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected List<LookupElement> getAutocompleteForClassName(String filePath)
    {
        List<LookupElement> elements = new ArrayList<LookupElement>();
        if(filePath != null){
            String className = Magicento.getClassNameFromFilePath(filePath);
            if(className != null){

                // we are using the "magicento_classname" in MagicentoPhpWeigher
                LookupElement element = LookupElementBuilder.create("magicento_classname", className)
                        .setPresentableText(className)
                        .setIcon(myIcon)
                        ;

                elements.add(element);
            }
        }
        return elements;
    }

    protected boolean isGetStoreConfigAutocomplete(PsiElement currentElement)
    {
        if(currentElement != null)
        {
            // autcomplete works only if cursor is over the oparameter list of Mage::getStoreConfig([HERE])
            PsiElement element = getParameterListElement(currentElement);
            if(element != null)
            {
                PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
                if(methodReference != null){
                    if(MagentoParser.isGetStoreConfig(methodReference)){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * checks if user is trying to autocomplete the name of the class ("class ...")
     * @param currentElement
     * @return
     */
    protected boolean isClassnameAutocomplete(PsiElement currentElement)
    {
        PsiElement prevSibling = currentElement.getPrevSibling();
        while(prevSibling != null && prevSibling instanceof PsiWhiteSpace){
            prevSibling = prevSibling.getPrevSibling();
        }
        if(prevSibling != null && prevSibling.getText().equals("class")){
            return true;
        }
        return false;
    }

    /**
     * checks if user is trying to autocomplete the uri of some factory
     * @param currentElement
     * @return
     */
    protected boolean isFactoryAutocomplete(PsiElement currentElement)
    {
        if(currentElement != null)
        {
            // autcomplete works only if cursor is over the parameter list of some Mage::[FACTORY]([CURSOR_HERE])
            PsiElement element = getParameterListElement(currentElement);
            if(element != null)
            {
                PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
                if(methodReference != null){
                    if(MagentoParser.isFactory(methodReference)){
                        return true;
                    }
                    if(MagentoParser.isCreateBlock(methodReference)){
                        return true;
                    }
                    if(MagentoParser.isMethod(methodReference, "_init")){
                        // resourceModels receives a table uri not a factory
                        if( ! MagentoParser.isResourceModel(methodReference)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected List<LookupElement> getAutocompleteForFactory(PsiElement currentElement, String prefix)
    {

        List<LookupElement> elements = new ArrayList<LookupElement>();
        if(prefix.isEmpty()){
            LookupElement element = LookupElementBuilder.create("", "")
                    .setPresentableText("Write at least one character to get code completion for factories")
                    .setIcon(myIcon)
                    ;
            elements.add(element);
        }
        else
        {
            String uri = prefix+"*";
            List<MagentoClassInfo> classes = null;
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(currentElement.getProject());
            if(magicento != null)
            {
                boolean isHelper = false;
                if(MagentoParser.isBlockUri(currentElement)){
                    classes = magicento.findBlocksOfFactoryUri(uri);
                }
                else if(MagentoParser.isModelUri(currentElement)){
                    classes = magicento.findModelsOfFactoryUri(uri);
                }
                else if(MagentoParser.isResourceModelUri(currentElement)){
                    classes = magicento.findResourceModelsOfFactoryUri(uri);
                }
                else if(MagentoParser.isHelperUri(currentElement)){
                    isHelper = true;
                    classes = magicento.findHelpersOfFactoryUri(uri);
                }
                else {
                    classes = magicento.findClassesOfFactoryUri(uri);
                }

                if(classes != null && classes.size() > 0)
                {
                    int count = 0;
                    for(MagentoClassInfo classInfo : classes)
                    {
                        String valueToInsert = classInfo.getUri();
                        if(isHelper && valueToInsert.endsWith("/data")){
                            valueToInsert = valueToInsert.substring(0, valueToInsert.length()-5);
                        }
                        String presentableText = valueToInsert;
                        count++;
                        LookupElement element = LookupElementBuilder.create("", valueToInsert)
                                .setPresentableText(presentableText)
                                .setIcon(myIcon)
                                        //.addLookupString(value.substring(1))
                                        //.setInsertHandler(INSERT_HANDLER)
                                .setTailText("  "+classInfo.name, true)
                                ;

                        LookupElement elementWithPriority = PrioritizedLookupElement.withPriority(element, -count);

                        elements.add(elementWithPriority);
                    }
                }
            }
        }
        return elements;
    }


    protected List<LookupElement> getAutocompleteForGetStoreConfig(PsiElement currentElement, String prefix)
    {
        List<LookupElement> elements = new ArrayList<LookupElement>();

        List<String> paths = Magicento.getStoreConfigPaths(currentElement.getProject(), prefix);

        for(String path : paths)
        {
            LookupElement element = LookupElementBuilder.create("", path)
                    .setPresentableText(path)
                    .setIcon(myIcon)
                    ;
            elements.add(element);
        }

        return elements;
    }


    protected List<LookupElement> getAutocompleteForGetTable(PsiElement currentElement, String prefix)
    {
        List<LookupElement> elements = new ArrayList<LookupElement>();
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(currentElement.getProject());
        if(magicento != null)
        {
            String[] parts = prefix.split("/");
            String modelUri = parts[0];
            String entityUri = "";
            if(parts.length > 1){
                entityUri = parts[1];
            }

            File configFile = magicento.getCachedConfigXml();


            String tablePrefix = "";
            List<Element> nodes = null;
            String xpath = null;

            // don't use prefix
//            xpath = "config/global/resources/db/table_prefix";
//            nodes = XmlHelper.findXpath(configFile, xpath);
//            if(nodes != null && nodes.size() > 0)
//            {
//                tablePrefix = nodes.get(0).getValue();
//            }

            Map<String, String> modelsAndResources = new HashMap<String, String>();
            xpath = "config/global/models/*";
            if( ! modelUri.isEmpty()){
                xpath += "[starts-with(name(),'" + modelUri + "')]";
            }

            nodes = XmlHelper.findXpath(configFile, xpath);
            if(nodes != null && nodes.size() > 0){
                for(Element node : nodes)
                {
                    Element resourceModel = node.getChild("resourceModel");
                    if(resourceModel != null){
                        modelsAndResources.put(node.getName(), resourceModel.getValue());
                    }
                }
            }

            for(Map.Entry<String, String> entry : modelsAndResources.entrySet())
            {
                String resource = entry.getValue();
                xpath = "config/global/models/"+resource+"/entities/*";
                if( ! entityUri.isEmpty()){
                    xpath += "[starts-with(name(),'" + entityUri + "')]";
                }
                nodes = XmlHelper.findXpath(configFile, xpath);
                if(nodes != null && nodes.size() > 0){
                    for(Element node : nodes)
                    {
                        Element tableElement = node.getChild("table");
                        if(tableElement != null){
                            String table = entry.getKey()+"/"+node.getName();
                            String tableName = tablePrefix + tableElement.getValue();
                            LookupElement lookupElement = LookupElementBuilder.create("", table)
                                    .setPresentableText(table)
                                    .setIcon(myIcon)
                                    .setTailText(tableName, true)
                                    ;
                            elements.add(lookupElement);
                        }
                    }
                }

            }

        }

        return elements;
    }


    @NotNull protected List<LookupElement> getAutocompleteForGetChildInTemplate(PsiElement currentElement, String prefix)
    {
        List<LookupElement> elements = new ArrayList<LookupElement>();
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(currentElement.getProject());
        if(magicento != null)
        {
            Template template = new Template(currentElement.getContainingFile().getOriginalFile().getVirtualFile());
            String area = template.getArea();
            if(area != null && ! area.isEmpty()){
                File layoutXml = magicento.getCachedLayoutXml(area);
                if(layoutXml != null && layoutXml.exists()){
                    List<Element> blocks = template.getBlockElements(currentElement.getProject());
                    if(blocks != null)
                    {
                        Set<String> blockNamesChecked = new HashSet<String>();
                        for(Element block : blocks)
                        {
                            String blockName = block.getAttributeValue("name");
                            if(blockName != null && ! blockName.isEmpty() && ! blockNamesChecked.contains(blockName))
                            {
                                blockNamesChecked.add(blockName);
                                String xpath = "//*[@name='"+blockName+"']/block";
                                List<Element> blockChildren = XmlHelper.findXpath(layoutXml, xpath);
                                if(blockChildren != null)
                                {
                                    for(Element blockChild : blockChildren){
                                        // as attr has priority over name
                                        String name = blockChild.getAttributeValue("as");
                                        if(name == null || name.isEmpty()){
                                            name = blockChild.getAttributeValue("name");
                                        }
                                        String type = blockChild.getAttributeValue("type");
                                        LookupElement lookupElement = LookupElementBuilder.create("", name)
                                                .setPresentableText(name)
                                                .setIcon(myIcon)
                                                .setTailText(type, true)
                                                ;
                                        elements.add(lookupElement);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return elements;
    }


    @NotNull protected List<LookupElement> getAutocompleteForGetBlockInTemplate(PsiElement currentElement, String prefix)
    {

        List<LookupElement> elements = new ArrayList<LookupElement>();
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(currentElement.getProject());
        if(magicento != null)
        {
            Template template = new Template(currentElement.getContainingFile().getOriginalFile().getVirtualFile());
            String area = template.getArea();
            if(area != null && ! area.isEmpty())
            {
                File layoutXml = magicento.getCachedLayoutXml(area);
                if(layoutXml != null && layoutXml.exists())
                {
                    // search all blocks
                    String xpath = "//block";
                    List<Element> blocks = XmlHelper.findXpath(layoutXml, xpath);

                    if(blocks != null)
                    {
                        for(Element block : blocks){
                            String name = block.getAttributeValue("name");
                            String type = block.getAttributeValue("type");
                            if(name != null){
                                LookupElement lookupElement = LookupElementBuilder.create("", name)
                                        .setPresentableText(name)
                                        .setIcon(myIcon)
                                        .setTailText(type, true)
                                        ;
                                elements.add(lookupElement);
                            }
                        }
                    }
                }
            }

        }
        return elements;

    }

    protected List<LookupElement> getAutocompleteForClassExtends(PsiElement currentElement, String prefix)
    {
        List<LookupElement> elements = new ArrayList<LookupElement>();

        List<String> names = new ArrayList<String>();
        if(MagentoParser.isBlock(currentElement)){
            names.add("Mage_Core_Block_Template");
            names.add("Mage_Adminhtml_Block_Widget_Grid_Container");
            names.add("Mage_Adminhtml_Block_Widget_Grid");
            names.add("Mage_Adminhtml_Block_Widget_Form_Container");
            names.add("Mage_Adminhtml_Block_Widget_Form");
        }
        else if(MagentoParser.isController(currentElement)){
            names.add("Mage_Core_Controller_Front_Action");
            names.add("Mage_Adminhtml_Controller_Action");
        }
        else if(MagentoParser.isModel(currentElement)){
            names.add("Mage_Core_Model_Abstract");
        }
        else if(MagentoParser.isResourceModel(currentElement)){
            names.add("Mage_Core_Model_Resource_Db_Abstract");
            names.add("Mage_Eav_Model_Entity_Abstract");
        }
        else if(MagentoParser.isCollection(currentElement)){
            names.add("Mage_Core_Model_Resource_Db_Collection_Abstract");
        }
        else if(MagentoParser.isHelper(currentElement)){
            names.add("Mage_Core_Helper_Abstract");
        }

        for(String name : names){
            LookupElement element = LookupElementBuilder.create("magicento_extends", name)
                    .setPresentableText(name)
                    .setIcon(myIcon)
                    ;

            elements.add(element);
        }

        return elements;
    }

}
