package com.magicento.helpers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for managing and parsing Psi files for PHP
 * @author Enrique Piatti
 */
public class PsiPhpHelper {

    public static final String LANGUAGE_PHP = "PHP";
    public static final String ASSIGNMENT_EXPRESSION = "Assignment expression";
    public static final String VARIABLE_DECLARATION = "Variable";
    public static final String VARIABLE = "variable";
    public static final String METHOD_REFERENCE = "Method reference";
    public static final String WHITE_SPACE = "WHITE_SPACE";
    public static final String ASSIGN = "assign";
    public static final String STATEMENT = "Statement";
    public static final String GROUP_STATEMENT = "Group statement";
    public static final String CLASS_METHOD = "Class method";
    public static final String CLASS = "Class";
    public static final String FILE = "FILE";
    public static final String ARROW = "arrow";
    public static final String COMMENT_BLOCK = "C style comment";
    public static final String COMMENT_LINE = "line comment";
    public static final String IDENTIFIER = "identifier";
    public static final String PARAMETER_LIST = "Parameter list";


    private static String getElementType(PsiElement psiElement){
        if(psiElement != null && psiElement.getNode() != null && psiElement.getNode().getElementType() != null){
            return psiElement.getNode().getElementType().toString();
        }
        return null;
    }

    public static boolean isElementType(PsiElement psiElement, String type){
        return isElementType(psiElement, new String[]{type});
    }

    public static boolean isElementType(PsiElement psiElement, String[] types)
    {
        if(psiElement != null && types != null){
            String elementType = getElementType(psiElement);
            if(elementType != null && ! elementType.isEmpty()){
                for (String type : types){
                    if(elementType == type)
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean isNotElementType(PsiElement psiElement, String[] types){
        return ! isElementType(psiElement, types);
    }

    public static boolean isNotElementType(PsiElement psiElement, String type){
        return isNotElementType(psiElement, new String[]{type});
    }

    public static boolean isPhp(PsiElement psiElement){
        return psiElement.getLanguage().getID() == LANGUAGE_PHP;
    }

    public static boolean isVariable(PsiElement psiElement){
        String[] types = {VARIABLE_DECLARATION, VARIABLE};
        return isElementType(psiElement, types);
    }

    public static boolean isMethodRefence(PsiElement psiElement){
        String[] types = {METHOD_REFERENCE};
        return isElementType(psiElement, types);
    }

    public static boolean isWhiteSpace(PsiElement psiElement){
        String[] types = {WHITE_SPACE};
        return isElementType(psiElement, types);
    }

    public static boolean isIdentifier(PsiElement psiElement){
        String[] types = {IDENTIFIER};
        return isElementType(psiElement, types);
    }

    public static boolean isNotWhiteSpaceOrComment(PsiElement psiElement){
        String[] types = {WHITE_SPACE, COMMENT_BLOCK, COMMENT_LINE};
        return isNotElementType(psiElement, types);
    }

    public static boolean isAssignmentExpression(PsiElement psiElement){
        String[] types = {ASSIGNMENT_EXPRESSION};
        return isElementType(psiElement, types);
    }

    public static boolean isAssign(PsiElement psiElement){
        String[] types = {ASSIGN};
        return isElementType(psiElement, types);
    }

    public static boolean isStatement(PsiElement psiElement){
        String[] types = {STATEMENT};
        return isElementType(psiElement, types);
    }

    public static String getVarName(PsiElement psiElement){
        if(getElementType(psiElement) == VARIABLE){
            return psiElement.getText();    // with "$"
        }
        else if(getElementType(psiElement) == VARIABLE_DECLARATION){
            //return psiElement.getChildren()[0].getText();
            return psiElement.getText();
        }
        return null;
    }

    public static boolean isArrow(PsiElement psiElement){
        String[] types = {ARROW};
        return isElementType(psiElement, types);
    }

    public static boolean hasChainedMethod(PsiElement psiElement)
    {
        PsiElement[] children = psiElement.getChildren();
        if(children != null && children.length > 0){
            PsiElement child = children[0];
            while(child != null){
                if(isArrow(child))
                    return true;
                child = child.getNextSibling();
            }
        }
        return false;
    }

    /**
     *
     * @param psiMethodReference
     * @return
     */
    public static String getMethodName(PsiElement psiMethodReference)
    {
        if( isMethodRefence(psiMethodReference)){
            PsiElement[] children = psiMethodReference.getChildren();
            if(children != null && children.length > 0){

                // This fails because getChildren is not returning all the children !! why???
                for(PsiElement child : children){
                    if(isIdentifier(child)){
                        return child.getText();
                    }
                }

                // the identifier after the arrow is the method name
                // TODO add support for static methods
                PsiElement child = children[0];
                boolean prevSiblingWasArrow = false;
                while(child != null)
                {
                    // if previous element was an arrow, and the element is an identifier (this bypass whitespaces...)
                    if(prevSiblingWasArrow && isIdentifier(child)){
                        // and the next element is a parenthesis (this bypass properties)
                        PsiElement nextSibling = child.getNextSibling();
                        if(nextSibling.getText().equals("(")){
                            return child.getText();
                        }
                    }
                    else {
                        if(isArrow(child)){
                            prevSiblingWasArrow = true;
                        }
                        else if( ! (child instanceof PsiWhiteSpace)){
                            // allow methods separated of the arrow by whitespaces
                            prevSiblingWasArrow = false;
                        }
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return null;
    }


    public static List<PsiElement> getChainedMethodReferences(PsiElement psiElement)
    {
        if(psiElement == null){
            return null;
        }
        List<PsiElement> methods = new ArrayList<PsiElement>();
        if(isMethodRefence(psiElement)){
            methods.add(psiElement);
        }
        PsiElement childMethodReference = PsiPhpHelper.findFirstChildOfType(psiElement, METHOD_REFERENCE);
        while(childMethodReference != null){
            methods.add(childMethodReference);
            childMethodReference = PsiPhpHelper.findFirstChildOfType(childMethodReference, METHOD_REFERENCE);
        }

        if(methods != null){
            Collections.reverse(methods);
        }

        return methods;
    }

    public static List<String> getChainedMethodNames(PsiElement psiElement)
    {
        if(psiElement == null){
            return null;
        }

        List<String> methodNames = new ArrayList<String>();

        List<PsiElement> methods = getChainedMethodReferences(psiElement);
        if(methods != null){
            for(PsiElement methodReference : methods){
                String methodName = getMethodName(methodReference);
                if(methodName != null){
                    methodNames.add(methodName);
                }
            }
        }

        return methodNames;

    }

    public static String getLastChainedMethodName(PsiElement psiElement)
    {
        List<String> methods = getChainedMethodNames(psiElement);
        if(methods != null && methods.size() > 0){
            return methods.get(methods.size()-1);
        }
        return null;
    }


    public static PsiElement findFirstChildOfType(PsiElement psiElement, String type){
        PsiElement[] children = psiElement.getChildren();
        if(children.length > 0){
            for(PsiElement child : children){
                if(isElementType(child, new String[]{type})){
                    return child;
                }
            }
        }
        return null;
    }


    public static PsiElement findNextSiblingOfType(PsiElement psiElement, String type){
        String[] types = {type};
        return findNextSiblingOfType(psiElement, types);
    }

    public static PsiElement findNextSiblingOfType(PsiElement psiElement, String[] types){
        PsiElement siblingElement = psiElement.getNextSibling();
        while(siblingElement != null && isNotElementType(siblingElement, types) ){
            siblingElement = siblingElement.getNextSibling();
        }
        return siblingElement;
    }

    public static PsiElement findPrevSiblingOfType(PsiElement psiElement, String type){
        String[] types = {type};
        return findPrevSiblingOfType(psiElement, types);
    }

    public static PsiElement findPrevSiblingOfType(PsiElement psiElement, String[] types){
        PsiElement siblingElement = psiElement.getPrevSibling();
        while(siblingElement != null && isNotElementType(siblingElement, types) ){
            siblingElement = siblingElement.getPrevSibling();
        }
        return siblingElement;
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String type){
        String[] types = {type};
        return findFirstParentOfType(psiElement, types);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String[] types){
        return findFirstParentOfType(psiElement, types, null);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String type, String limitType){
        String[] types = {type};
        String[] limits = {limitType};
        return findFirstParentOfType(psiElement, types, limits);
    }

    public static PsiElement findFirstParentOfType(PsiElement psiElement, String[] types, String[] limitTypes){
        PsiElement parentElement = psiElement.getParent();
        boolean isRequestingSecuredLimits = false;
        if(limitTypes != null && limitTypes.length > 0){
            for(String limit : limitTypes){
                if(limit == FILE || limit == CLASS || limit == GROUP_STATEMENT){
                    isRequestingSecuredLimits = true;
                    break;
                }
            }
        }
        if( ! isRequestingSecuredLimits ){
            String[] securityLimits = {FILE, CLASS, GROUP_STATEMENT};
            limitTypes = (String[]) ArrayUtils.addAll(limitTypes, securityLimits);
        }
//        String[] result = Arrays.copyOf(first, first.length + second.length);
//        System.arraycopy(second, 0, result, first.length, second.length);

        while(parentElement != null && isNotElementType(parentElement, types) ){
            if(limitTypes != null){
                if(isElementType(parentElement, limitTypes))
                    return null;
            }
            parentElement = parentElement.getParent();
        }
        return parentElement;
    }

}
