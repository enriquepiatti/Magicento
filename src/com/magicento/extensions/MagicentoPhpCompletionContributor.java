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
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

                        if(elements != null)
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
            PsiElement element = null;
            // autcomplete works only if cursor is over the oparameter list of Mage::getStoreConfig([HERE])
            if(PsiPhpHelper.isParameterList(currentElement)){
                element = currentElement;
            }
            if(element == null){
                element = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.PARAMETER_LIST);
            }
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
            PsiElement element = null;
            if(PsiPhpHelper.isParameterList(currentElement)){
                element = currentElement;
            }
            if(element == null){
                element = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.PARAMETER_LIST);
            }
            if(element != null)
            {
                PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
                if(methodReference != null){
                    if(MagentoParser.isFactory(methodReference)){
                        return true;
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
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(currentElement.getProject());
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
                        String lookup = prefix.substring(0, prefix.length()-lastPrefix.length())+nodeName;
                        LookupElement element = LookupElementBuilder.create("", lookup)
                                .setPresentableText(lookup)
                                .setIcon(myIcon)
                                ;
                        elements.add(element);
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
                    String lookup = prefix.substring(0, prefix.length()-lastPrefix.length())+nodeName;
                    LookupElement element = LookupElementBuilder.create("", lookup)
                            .setPresentableText(lookup)
                            .setIcon(myIcon)
                            ;
                    elements.add(element);
                }
            }

        }
        return elements;
    }


}
