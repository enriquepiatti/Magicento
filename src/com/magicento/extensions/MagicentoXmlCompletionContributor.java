package com.magicento.extensions;

//import com.magicento.helpers.MagentoConfigXml;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.IconLoader;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * completion contributor for XML
 * @author Enrique Piatti
 */
public class MagicentoXmlCompletionContributor extends CompletionContributor {

    //private static final String INTELLIJ_IDEA_RULEZZZ = "IntellijIdeaRulezzz ";
    //private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*)\\b(\\w+)$");

    private static final Icon myIcon = MagicentoIcons.MAGENTO_ICON_16x16; //IconLoader.getIcon("/icons/magento.png");


    private static final InsertHandler<LookupElement> INSERT_HANDLER = new InsertHandler<LookupElement>()
    {
        public void handleInsert(InsertionContext context, LookupElement item)
        {
            //String stringInserted = (String) item.getObject();
            context.commitDocument();
            final Editor editor = context.getEditor();
            final Document document = editor.getDocument();
            PsiFile psiFile = context.getFile();

            //int tailOffset = editor.getCaretModel().getOffset();
            int tailOffset = context.getTailOffset();
            int startOffset = context.getStartOffset();

            // put caret in the middle (inside the new tags)
            // editor.getCaretModel().moveToOffset((int) (startOffset + (tailOffset - startOffset)*0.5));
            int pos = document.getText().indexOf('>', startOffset);
            if(pos != -1){
                editor.getCaretModel().moveToOffset(pos+1);
            }


            PsiElement psiElement = psiFile.findElementAt(startOffset);
            if(psiElement != null){
                ASTNode node = psiElement.getNode();
                XmlTag parentElement = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
                if(parentElement != null){
                    psiElement = parentElement;
                    XmlTag grandParentElement = parentElement.getParentTag();
                    if(grandParentElement != null){
                        psiElement = grandParentElement;
                    }
                }

                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(context.getProject());
                //codeStyleManager.reformatText(psiFile, startOffset, tailOffset);

                XmlCodeStyleSettings xmlSettings = IdeHelper.getXmlSettings(psiElement.getProject());
                if(xmlSettings != null){
                    boolean wrap = xmlSettings.XML_TEXT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP;
                    if( wrap){
                        IdeHelper.showDialog(psiFile.getProject(), "It's not safe to use wrap the text in xml files for magento projects.\n" +
                                "Please change this going to Settings > Code Style > Xml > Other > Wrap text", "Warning: Wrap Text in XML files");
                    }
                }

                codeStyleManager.reformat(psiElement);
            }

        }

    };

    public MagicentoXmlCompletionContributor() {
        final PsiElementPattern.Capture<PsiElement> everywhere = PlatformPatterns.psiElement();
        // otroPattern = XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue());
        extend(CompletionType.BASIC, everywhere, new CompletionProvider<CompletionParameters>() {
            public void addCompletions(@NotNull final CompletionParameters parameters,
                                       final ProcessingContext matchingContext,
                                       @NotNull final CompletionResultSet _result) {


                if(parameters == null || ! MagicentoProjectComponent.isEnabled(parameters.getOriginalFile().getProject())) {
                    return;
                }


                //final PsiFile file = parameters.getOriginalFile();
                //String fileName = file./*getVirtualFile().*/getName();
                //final int startOffset = parameters.getOffset();

                final PsiElement currentElement = parameters.getPosition();   // matchedElement?
                MagentoXml magentoXml = MagentoXmlFactory.getInstance(currentElement);

                if(magentoXml == null){
                    return;
                }


                XmlAttribute attribute = PsiTreeUtil.getParentOfType(currentElement, XmlAttribute.class, false);
                boolean isAttribute = attribute != null;

                if(isAttribute){
                    //final XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(currentElement, XmlAttributeValue.class, false);
                    // TODO: add code completion for attributes
                    return;
                }


//                final PsiElement psiElement = PsiTreeUtil.getParentOfType(currentElement, XmlTag.class, XmlAttributeValue.class);
//                if(psiElement == null){
//                    return;
//                }


                final String prefix = _result.getPrefixMatcher().getPrefix();

                CompletionResultSet result = _result;

                // force prefix to begin with "<" if it's a new tag
                PsiElement prevSibling = currentElement.getPrevSibling();
                if( prevSibling != null && ! prefix.startsWith("<") )
                {
                    if( prevSibling.getText().equals("<") ||
                        prevSibling.getNode().getElementType() == XmlElementType.XML_START_TAG_START )
                    {
                        result = _result.withPrefixMatcher("<"+prefix);
                    }
                }

                //MagentoXmlTag matchedTag = MagentoConfigXml.getInstance().getMatchedTag(currentElement);
                MagentoXmlTag matchedTag = magentoXml.getMatchedTag(currentElement);
                if(matchedTag != null){

                    Map<String, String> items = new LinkedHashMap<String, String>();    // we are using LinkedHashMap to preserve order of insertion

                    List<MagentoXmlTag> children = matchedTag.getChildren();
                    boolean hasChildren = children != null && children.size() > 0;
                    if(hasChildren){
                        for(MagentoXmlTag child : children){
                            // each children tag can have multiple definitions (for example Id tags)
                            Map<String,String> childItems = child.getPossibleDefinitions();
                            if(childItems != null){
                                items.putAll(childItems);
                            }
                        }
                    }
                    else {          // it's a leaf node, show the possible values instead of children
                        Map<String, String> values = matchedTag.getPossibleValues();
                        if(values != null){
                            items.putAll(values);
                        }
                    }

                    if( ! items.isEmpty()){

                        // jIdea is sorting the completion items in lexicographic order (we need a CompletionWeigher for changing that) or use something like PrioritizedLookupElement
                        int count = 0;
                        int addedElementsToResult = 0;
                        for(Map.Entry<String, String> entry : items.entrySet())
                        {
                            count++;
                            String name = entry.getKey();
                            String value = entry.getValue();
                            XmlTag newTag = XmlElementFactory.getInstance(currentElement.getProject()).createTagFromText(value);
                            // prefix doesn't contain the initial '<'
                            //if( true || prefix == null || prefix.isEmpty() || value.startsWith(prefix, 1)){
                                // value = prefix == null || prefix.isEmpty() ? value : value.substring(1);
                                // TODO: create a new class extending LookupElement?
//                                LookupElement element = LookupElementBuilder.create(value)
                                // if this is a leaf node we are passing an empty string as the Object for the lookupElement because
                                // we are using that object in MagicentoXmlDocumentationProvider (and we don't need it for leaf nodes), this is really ugly
                                LookupElement element = LookupElementBuilder.create(hasChildren ? name : "" /*newTag*/, value)
                                        .setPresentableText(name)
                                        .setIcon(myIcon)
                                        //.addLookupString(value.substring(1))
                                        .setInsertHandler(INSERT_HANDLER)
                                        ;

                                LookupElement elementWithPriority = PrioritizedLookupElement.withPriority(element, -count);

                                //element.renderElement();
                                //element.handleInsert();
                                //_result.addElement(element);
                                result.addElement(elementWithPriority);
                                addedElementsToResult++;
                            //}

                        }
                        // prevents extra codeCompletion from jIDEA (PHPStorm) because it's confusing and they are appearing first !
                        // anyway, we have a custom magento icon for differentiating it
                        if(addedElementsToResult > 5){
                            _result.stopHere();
                            result.stopHere();
                        }

                    }
                }

            }
        });
    }


    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);    //To change body of overridden methods use File | Settings | File Templates.
    }


}
