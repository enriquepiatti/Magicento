package com.magicento.helpers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.layout.BlockXmlTag;
import com.magicento.models.xml.layout.HandleXmlTag;
import com.magicento.models.xml.layout.MagentoLayoutXml;
import com.magicento.models.xml.layout.attribute.BlockNameXmlAttribute;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.omg.CosNaming._BindingIteratorImplBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing Magento code
 * @author Enrique Piatti
 */
public class MagentoParser {


    protected static final String[] FACTORIES = {"helper", "getModel","getResourceModel", "getSingleton", "getResourceSingleton", "getSingletonBlock", "getResourceHelper"};

    /**
     * returns the factory string, something like "Mage::getModel('catalog/product')" or null if the position is not over (or beginning of) the factory
     * @param code
     * @param position
     * @return
     */
    public static String getFactory(String code, int position)
    {
        // TODO: we should use Psi here, (@see MagicentoGotoDeclarationHandler::isModelUri)
//        if(PsiPhpHelper.isPhp(psiElement)){
//          String factory = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE).getText();
//        }

        String[] factories = FACTORIES;
        String paramRegex = "['\"][a-zA-Z/_0-9]+['\"]";
        return getMageMethod(code, position, factories, paramRegex);

    }

    /**
     * returns variable name (without the "$") if the position is over a PHP variable, null if the position is not over some variable
     * @param code
     * @param position
     * @return
     */
    public static String getVarName(String code, int position)
    {
        int pos1 = code.lastIndexOf("$", position);
        if(pos1 != -1)
        {
            String test = code.substring(pos1);
            String regex = "^\\$([a-zA-Z_][a-zA-Z0-9_]*)";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(test);
            if (m.find()) {
                System.out.println(m.group(1));
            }

            if( test.matches(regex) )
            {
                return test;
            }
        }
        return null;
    }

    public static String getStoreConfig(String code, int position){
        String[] methods = {"getStoreConfig"};
        //String paramRegex = ".*";
        String paramRegex = "['\"][a-zA-Z/_0-9]+['\"]";     // we support only string path for now, not variables for example Mage::geStoreConfig($path);
        return getMageMethod(code, position, methods,paramRegex);
    }


    private static String getMageMethod(String code, int position, String[] posibleMethods, String methodParametersRegex)
    {
        int pos1 = code.lastIndexOf("Mage::", position);
        if(pos1 != -1)
        {
            int pos2 = code.indexOf(")", position);
            if(pos2 != -1)
            {
                String test = code.substring(pos1, pos2+1);
                String options = "(" + StringUtils.join(posibleMethods, "|") + ")";
                String regex = "^Mage::"+options+"\\s*\\(\\s*"+methodParametersRegex+"\\s*\\)$";
                if( test.matches(regex) )
                {
                    return test;
                }
            }
        }
        return null;
    }


    public static String getMethodName(PsiElement psiElement)
    {
        if(psiElement != null && PsiPhpHelper.isMethodRefence(psiElement)){
            //PsiElement[] children = psiElement.getChildren();
            List<PsiElement> children = PsiPhpHelper.getFullListOfChildren(psiElement);
            for(PsiElement child : children){
                if(PsiPhpHelper.isIdentifier(child)){
                    return child.getText().replace("'", "").replace("\"", "");
                }
            }
        }
         return null;
    }

    public static boolean isUri(String test){
        String regex = "^[a-zA-Z][a-zA-Z0-9_]*/[a-zA-Z0-9_]+$";
        return JavaHelper.testRegex(regex, test);
    }

    public static boolean isFilePath(String test){
        String regex = "^.+\\.(xml|phtml|css|js)$";
        return JavaHelper.testRegex(regex, test);
    }

    public static boolean isFilePath(PsiElement psiElement){
        return getFilePath(psiElement) != null;
    }

    public static String getFilePath(PsiElement psiElement)
    {
        if(psiElement != null){
            String value = psiElement.getText();
            if(value != null){
                value = value.replaceAll("\"", "").replaceAll("'", "");
                if(MagentoParser.isFilePath(value)){
                    return value;
                }
            }
        }
        return null;
    }


    public static boolean isUri(PsiElement psiElement)
    {
        return getUri(psiElement) != null;
    }


    public static String getUri(PsiElement psiElement)
    {
        if(psiElement != null){
            String value = psiElement.getText();
            value = value.replaceAll("\"", "").replaceAll("'", "");
            if(MagentoParser.isUri(value)){
                return value;
            }
            else if( ! value.isEmpty() && MagentoParser.isHelperUri(psiElement)){
                return value;
            }
        }
        return null;
    }

    public static boolean isBlockUri(PsiElement psiElement)
    {
        return getUriType(psiElement) == MagentoClassInfo.UriType.BLOCK;
    }

    public static boolean isModelUri(PsiElement psiElement)
    {
        return getUriType(psiElement) == MagentoClassInfo.UriType.MODEL;
    }

    public static boolean isResourceModelUri(PsiElement psiElement)
    {
        return getUriType(psiElement) == MagentoClassInfo.UriType.RESOURCEMODEL;
    }

    public static boolean isHelperUri(PsiElement psiElement)
    {
        return getUriType(psiElement) == MagentoClassInfo.UriType.HELPER;
    }

    protected static MagentoClassInfo.UriType getUriType(PsiElement psiElement)
    {
        if(PsiPhpHelper.isPhp(psiElement))
        {
            // MagentoParser.getFactory(psiElement.getContainingFile().getText(), editor);
            PsiElement methodReference = PsiPhpHelper.isMethodRefence(psiElement) ?
                    psiElement : PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
            if(methodReference != null){
                String methodCall = methodReference.getText();
                if(methodCall != null){
                    if(methodCall.startsWith("Mage::"))
                    {
                        Map<MagentoClassInfo.UriType, String[]> uriTypes = new HashMap<MagentoClassInfo.UriType, String[]>();
                        uriTypes.put(MagentoClassInfo.UriType.MODEL, new String[]{"getModel", "getSingleton" });
                        uriTypes.put(MagentoClassInfo.UriType.RESOURCEMODEL, new String[]{"getResourceModel", "getResourceSingleton" });
                        uriTypes.put(MagentoClassInfo.UriType.HELPER, new String[]{"helper" });
                        uriTypes.put(MagentoClassInfo.UriType.BLOCK, new String[]{"getSingletonBlock"});

                        for(Map.Entry<MagentoClassInfo.UriType, String[]> entry : uriTypes.entrySet()){
                            String[] factories = entry.getValue();
                            for(String factory : factories){
                                if(methodCall.startsWith("Mage::"+factory)){
                                    return entry.getKey();
                                }
                            }
                        }
                    }
                    if(isCreateBlock(methodCall)){
                        return MagentoClassInfo.UriType.BLOCK;
                    }
                    if(isMethod(methodCall, "_init"))
                    {
                        if(MagentoParser.isCollection(methodReference)){
                            return MagentoClassInfo.UriType.MODEL;
                        }
                        else if(MagentoParser.isModel(methodReference)){
                            return MagentoClassInfo.UriType.RESOURCEMODEL;
                        }
                    }
                }
            }
        }
        else {
            XmlTag parentXmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class, false);
            if(parentXmlTag != null){
                // attributeValue.getPrevSibling().getPrevSibling().getText() == "type"
                if( parentXmlTag.getName().equals("block") ){
                    return MagentoClassInfo.UriType.BLOCK;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param fullFactoryMethodCall something like "Mage::getModel('catalog/product')"
     * @return
     */
    public static String getUriFromFactory(String fullFactoryMethodCall)
    {
        String regex = ".+\\(['\"](.*?)['\"]\\)";
        return JavaHelper.extractFirstCaptureRegex(regex, fullFactoryMethodCall);
    }


    public static String getUriFromFactory(PsiElement psiMethodReference)
    {
        // factoryElement can have multiple MethodReference chained, and we need the method reference from the factory
        PsiElement firstChild = psiMethodReference.getFirstChild();
        while(firstChild != null && PsiPhpHelper.isMethodRefence(firstChild)){
            psiMethodReference = firstChild;
            firstChild = psiMethodReference.getFirstChild();
        }
        PsiElement parameterList = PsiPhpHelper.findFirstChildOfType(psiMethodReference, PsiPhpHelper.PARAMETER_LIST);
        if(parameterList != null && MagentoParser.isUri(parameterList))
        {
            return MagentoParser.getUri(parameterList);
        }
        return null;
    }

    public static boolean isFactory(PsiElement psiElement)
    {
        if(psiElement != null){
            return isFactory(psiElement.getText());
        }
        return false;
    }

    public static boolean isFactory(String phpCode)
    {
        if(phpCode != null)
        {
            String options = "(" + StringUtils.join(FACTORIES, "|") + ")";
            String regex = "^Mage::\\s*"+options+"\\s*\\(";
            //return phpCode.matches(regex);   // for using this we need regex+".*"
            return JavaHelper.testRegex(regex, phpCode);
        }
        return false;
    }


    public static boolean isMethod(PsiElement methodReference, String method)
    {
        if(methodReference != null){

            // we could do this here:
//            String lastMethod = PsiPhpHelper.getLastChainedMethodName(methodReference);
//            return (lastMethod != null && lastMethod.equals(method));

            return isMethod(methodReference.getText(), method);
        }
        return false;
    }

    public static boolean isMethod(String phpCode, String method)
    {
        if(phpCode != null)
        {
            String[] validMethods = {method};
            String options = "(" + StringUtils.join(validMethods, "|") + ")";
            String regex = "^.+->\\s*"+options+"\\s*\\(";
            //return phpCode.matches(regex);   // for using this we need regex+".*"
            return JavaHelper.testRegex(regex, phpCode);
        }
        return false;
    }

    public static boolean isMageMethod(PsiElement methodReference, String method)
    {
        if(methodReference != null){
            return isMageMethod(methodReference.getText(), method);
        }
        return false;
    }

    public static boolean isMageMethod(String phpCode, String method)
    {
        if(phpCode != null)
        {
            String[] validMethods = {method};
            String options = "(" + StringUtils.join(validMethods, "|") + ")";
            String regex = "^Mage\\s*::\\s*"+options+"\\s*\\(";
            //return phpCode.matches(regex);   // for using this we need regex+".*"
            return JavaHelper.testRegex(regex, phpCode);
        }
        return false;
    }



    public static boolean isGetTable(PsiElement methodReference) {
        return isMethod(methodReference, "getTable");
    }

    public static boolean isGetTable(String phpCode)
    {
        return isMethod(phpCode, "getTable");
    }


    public static boolean isGetStoreConfig(PsiElement methodReference) {
        return isMageMethod(methodReference, "getStoreConfig");
    }

    public static boolean isGetStoreConfig(String phpCode)
    {
        return isMageMethod(phpCode, "getStoreConfig");
    }

    public static boolean isEventDispatcherName(PsiElement sourceElement)
    {
        String eventName = getEventDispatcherName(sourceElement);
        return eventName != null;
    }

    public static String getEventDispatcherName(PsiElement psiElement)
    {
        if(psiElement != null){
            if( PsiPhpHelper.isString(psiElement)){
                PsiElement parameterList = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.PARAMETER_LIST); // psiElement.getParent();
                if( PsiPhpHelper.isParameterList(parameterList)){
                    PsiElement methodReference = parameterList.getParent();
                    String methodName = getMethodName(methodReference);
                    if(methodName != null && ! methodName.isEmpty() && methodName.equals("dispatchEvent")){
                        return psiElement.getText().replace("'", "").replace("\"", "");
                    }
                }
            }
        }
        return null;
    }

    public static boolean isCreateBlock(PsiElement methodReference) {
        return isMethod(methodReference, "createBlock");
    }

    public static boolean isCreateBlock(String phpCode)
    {
        return isMethod(phpCode, "createBlock");
    }

    public static MagentoClassInfo.ClassType getClassType(PsiElement child)
    {
        if(child != null)
        {
            String className = PsiPhpHelper.getClassName(child);
            if(className != null)
            {
                MagentoClassInfo.ClassType classType = getClassTypeFromClassName(className);
                if(classType != null){
                    return classType;
                }
            }

            String filePath = child.getContainingFile().getOriginalFile().getVirtualFile().getPath().replace("\\", "/");
            if(filePath.contains("/controllers/")){
                return MagentoClassInfo.ClassType.CONTROLLER;
            }
            if(filePath.contains("/sql/") || filePath.contains("/data/")){
                return MagentoClassInfo.ClassType.INSTALLER;
            }
        }

        return null;
    }

    public static MagentoClassInfo.ClassType getClassTypeFromClassName(String className)
    {
        if(className != null)
        {
            if(className.contains("_Model_"))
            {
                if(className.endsWith("_Collection")){
                    return MagentoClassInfo.ClassType.COLLECTION;
                }
                if(className.contains("_Resource_") || className.contains("_Mysql4_")){
                    return MagentoClassInfo.ClassType.RESOURCEMODEL;
                }
                return MagentoClassInfo.ClassType.MODEL;
            }

            if(className.contains("_Block_")){
                return MagentoClassInfo.ClassType.BLOCK;
            }

            if(className.contains("_Helper_")){
                return MagentoClassInfo.ClassType.HELPER;
            }
        }
        return null;
    }

    public static boolean isCollection(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.COLLECTION;
    }

    public static boolean isResourceModel(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.RESOURCEMODEL;
    }

    public static boolean isModel(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.MODEL;
    }

    public static boolean isHelper(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.HELPER;
    }

    public static boolean isBlock(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.BLOCK;
    }

    public static boolean isController(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.CONTROLLER;
    }

    public static boolean isInstaller(PsiElement child)
    {
        return getClassType(child) == MagentoClassInfo.ClassType.INSTALLER;
    }


    public static String getModuleNameFromModulePath(@NotNull String modulePath)
    {
        modulePath = modulePath.replace("\\", "/");
        String regex = "^.+?/app/code/(?:core|community|local)/([a-zA-Z0-9]+/[a-zA-Z0-9]+).*";
        String moduleName = JavaHelper.extractFirstCaptureRegex(regex, modulePath);
        if(moduleName != null && ! moduleName.isEmpty()){
            return moduleName.replace("/", "_");
        }
        return null;
    }


    public static String getNamespaceModuleFromClassName(String className)
    {
        String classNameParts[] = className.split("_");
        if(classNameParts.length > 2) {
            return classNameParts[0]+"_"+classNameParts[1];
        }
        return null;
    }


    public static String getClassPrefix(String className)
    {
        return getClassPrefix(className, false);
    }


    public static String getClassPrefix(String className, boolean isResource)
    {
        String classNameParts[] = className.split("_");
        String prefix = null;
        if(classNameParts.length > 3) {
            prefix = classNameParts[0]+"_"+classNameParts[1]+"_"+classNameParts[2];
            // for cimplicity, we are assuming resources have a separated folder (and just one), this is the convention anyway, is hard to find a case when this is not the case
            if(isResource && classNameParts.length > 4){
                prefix += "_"+classNameParts[3];
            }
        }
        return prefix;
    }


    /**
     * Warning: this doesn't take into account resource models, use getSecondPartUriFromClassName(String className, String prefix) for that
     * @param className
     * @return
     */
    public static String getSecondPartUriFromClassName(String className)
    {
        String prefix = getClassPrefix(className);
        return getSecondPartUriFromClassName(className, prefix);
    }

    /**
     *
     * @param className
     * @return
     */
    public static String getSecondPartUriFromClassName(String className, String prefix)
    {
        String secondPartClassName = className.substring(prefix.length()+1);
        String secondPart = WordUtils.uncapitalize(secondPartClassName.replace("_", " ")).replace(" ", "_");
        return secondPart;
    }

    public static boolean isGetChildInTemplate(PsiElement methodReference)
    {
        return isMethod(methodReference, "getChild") || isMethod(methodReference, "getChildHtml");
    }

    public static boolean isGetBlockInTemplate(PsiElement methodReference)
    {
        return isMethod(methodReference, "getBlock") || isMethod(methodReference, "getBlockHtml");
    }

    public static boolean isBlockNameInLayoutXml(PsiElement psiElement)
    {
        if(psiElement != null)
        {
            String filePath = psiElement.getContainingFile().getOriginalFile().getVirtualFile().getPath();
            if(filePath.contains(MagentoLayoutXml.BASE_PATH) && filePath.endsWith(".xml")){
                if(XmlHelper.isAttributeValue(psiElement))
                {
                    XmlAttribute attribute = XmlHelper.getParentOfType(psiElement, XmlAttribute.class, true);
                    String attrName = XmlHelper.getAttributeName(attribute);
                    return (attrName != null && attrName.equals("name"));
                }
            }
        }
        return false;

    }

    public static boolean isBlockNameInTemplate(PsiElement psiElement)
    {
        if(psiElement != null)
        {
            String filePath = psiElement.getContainingFile().getOriginalFile().getVirtualFile().getPath();
            if(filePath.contains(MagentoLayoutXml.BASE_PATH) && filePath.endsWith(".phtml"))
            {
                PsiElement element = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.PARAMETER_LIST);
                if(element != null)
                {
                    PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
                    if(methodReference != null){
                        if(MagentoParser.isGetBlockInTemplate(methodReference)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBlockAliasInTemplate(PsiElement psiElement)
    {
        if(psiElement != null)
        {
            String filePath = psiElement.getContainingFile().getOriginalFile().getVirtualFile().getPath();
            if(filePath.contains(MagentoLayoutXml.BASE_PATH) && filePath.endsWith(".phtml"))
            {
                PsiElement element = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.PARAMETER_LIST);
                if(element != null)
                {
                    PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
                    if(methodReference != null){
                        if(MagentoParser.isGetChildInTemplate(methodReference)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isHandleNode(PsiElement sourceElement)
    {
        if(sourceElement != null){
            MagentoXml magentoXml = MagentoXmlFactory.getInstance(sourceElement);
            if(magentoXml != null && magentoXml instanceof MagentoLayoutXml)
            {
                // MagentoLayoutXml layoutXml = (MagentoLayoutXml)magentoXml;
                XmlTag xmlTag = XmlHelper.getParentOfType(sourceElement, XmlTag.class, false);
                if(xmlTag != null){
                    MagentoXmlTag matchedTag = magentoXml.getMatchedTag(xmlTag.getLastChild());
                    return matchedTag != null && matchedTag instanceof HandleXmlTag;
                }
            }
        }
        return false;
    }

    public static boolean isUpdateHandleInLayoutXml(PsiElement psiElement)
    {
        if(psiElement != null)
        {
            String filePath = psiElement.getContainingFile().getOriginalFile().getVirtualFile().getPath();
            if(filePath.contains(MagentoLayoutXml.BASE_PATH) && filePath.endsWith(".xml")){
                if(XmlHelper.isAttributeValue(psiElement))
                {
                    XmlAttribute attribute = XmlHelper.getParentOfType(psiElement, XmlAttribute.class, true);
                    String attrName = XmlHelper.getAttributeName(attribute);
                    return (attrName != null && attrName.equals("handle"));
                }
            }
        }
        return false;

    }

}
