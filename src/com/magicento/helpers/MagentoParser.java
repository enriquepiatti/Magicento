package com.magicento.helpers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.magicento.models.MagentoClassInfo;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
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
                if(methodCall != null && methodCall.startsWith("Mage::"))
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


    public static boolean isGetStoreConfig(PsiElement methodReference) {
        if(methodReference != null){
            return isGetStoreConfig(methodReference.getText());
        }
        return false;
    }

    public static boolean isGetStoreConfig(String phpCode)
    {
        if(phpCode != null)
        {
            String[] validMethods = {"getStoreConfig"};
            String options = "(" + StringUtils.join(validMethods, "|") + ")";
            String regex = "^Mage\\s*::\\s*"+options+"\\s*\\(";
            //return phpCode.matches(regex);   // for using this we need regex+".*"
            return JavaHelper.testRegex(regex, phpCode);
        }
        return false;
    }
}
