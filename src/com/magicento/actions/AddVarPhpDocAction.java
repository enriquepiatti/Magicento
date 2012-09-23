package com.magicento.actions;

import com.magicento.MagicentoSettings;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magicento.models.MagentoClassInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class AddVarPhpDocAction extends MagicentoActionAbstract {

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        // applicable when options,
        // 1) the cursor is over a variable and that variable receives a factory on the same line/statement
        // TODO: option 3: the cursor is over a variable but that variable receives the factory on a previous statement

        //PsiFile psiFile = getPsiFile();
        PsiElement psiElement = getPsiElementAtCursor();//psiFile.findElementAt(getCursorOffset());

        if(psiElement == null)
            return false;

        if( ! PsiPhpHelper.isPhp(psiElement) )
            return false;

        if( ! PsiPhpHelper.isVariable(psiElement) )
            return false;

        //String varName = MagentoParser.getVarName(getCode(), getCursorOffset());
        //String varName = PsiPhpHelper.getVarName(psiElement);

        PsiElement parentElement = psiElement.getParent();
        while(parentElement != null )
        {
            // these cases are not a factory assignment
            // $var = Mage::helper('uri')->methodNotReturningThis();
            // $var = array(Mage::helper('uri'))
            // $var = method(Mage::helper('uri'));
            // $var = Mage::helper($var2 = 'cms');    // when cursor is over $var2

            // these cases should be valid for adding @var
            // $var = Mage::helper('cms');
            // if($var = Mage::helper('cms'))
            // if($algo = Mage::helper('cms') && ...)       // TODO: this is not working
            // $var = Mage::helper('cms'); $var2 = $var;    // TODO: this is not working

            if (PsiPhpHelper.isAssignmentExpression(parentElement))
            {
                PsiElement[] children = parentElement.getChildren();
                if(children.length > 0){
                    if( ! PsiPhpHelper.isVariable(children[0]) ){
                        return false;
                    }
                    if(children[0] != psiElement.getParent()){
                        return false;
                    }
                    PsiElement siblingElement = children[0].getNextSibling();
                    Boolean lastSiblingWasAssign = false;
                    while(siblingElement != null)
                    {
                        if(lastSiblingWasAssign && PsiPhpHelper.isNotWhiteSpaceOrComment(siblingElement) )
                        {
                            // if we are not assigning a factory, return false
                            if( ! PsiPhpHelper.isMethodRefence(siblingElement) ){
                                return false;
                            }

                            // siblingElement is the MethodReference of the Factory
                            if( PsiPhpHelper.hasChainedMethod(siblingElement) ){
                                // it could be something like $locale = Mage::getModel('moneybookers/acc')->getLocale(); and this is not a factory
                                // but something like ->load($id) should be working too for adding a @var ! that is, methods returning "$this" !

                                // String lastChainedMethod = PsiPhpHelper.getLastChainedMethod(siblingElement);

                                List<String> chainedMethods = PsiPhpHelper.getChainedMethodNames(siblingElement);
                                for(String chainedMethod : chainedMethods){
                                    // if it's not returning this or the collection or the resource
                                    if( ! (isMethodReturningThis(chainedMethod) || isMethodReturningCollection(chainedMethod) || isMethodReturningResource(chainedMethod)) ){
                                        return false;
                                    }
                                }
                                return true;
                            }
                            else{
                                String factory = MagentoParser.getFactory(siblingElement.getText(),0);
                                if(factory != null){
                                    return true;
                                }
                            }
                            return false;
                        }
                        else if (PsiPhpHelper.isAssign(siblingElement)) {
                            lastSiblingWasAssign = true;
                        }
                        siblingElement = siblingElement.getNextSibling();
                    }
                }
                break;
            }
            else if (PsiPhpHelper.isStatement(parentElement))
            {
                break;
            }
            parentElement = parentElement.getParent();
        }

        return false;
    }

    public void executeAction()
    {
        PsiFile psiFile = getPsiFile();
        PsiElement psiElement = psiFile.findElementAt(getCursorOffset());

        String varName = PsiPhpHelper.getVarName(psiElement);

        if(psiElement.getNextSibling() == null){
            psiElement = psiElement.getParent();
        }
        PsiElement factoryElement = PsiPhpHelper.findNextSiblingOfType(psiElement, PsiPhpHelper.METHOD_REFERENCE);
        if(factoryElement != null)
        {

            MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
            if(settings != null && settings.enabled)
            {
                String className = null;

                // TODO: use PHP here if it's enabled?
//                if(settings.phpEnabled){
//                    String factory = MagentoParser.getFactory(factoryElement.getText(),0);
//                    className = getMagicentoComponent().getClassNameFromFactory(factory);
//                }
//                else {
//                    className = getMagicentoComponent().getClassNameFromFactory(factoryElement);
//                }

                if(isResource(factoryElement))
                {
                    // TODO: we should read the uri from the _init() method inside _construct() in the model class...
                    String uri = MagentoParser.getUriFromFactory(factoryElement);
                    List<MagentoClassInfo> classes = getMagicentoComponent().findResourceModelsOfFactoryUri(uri);
                    if(classes != null && classes.size() > 0){
                        className = classes.get(0).name;
                    }
                }
                else if(isCollection(factoryElement))
                {

                    String uri = MagentoParser.getUriFromFactory(factoryElement);
                    uri += "_collection";
                    List<MagentoClassInfo> classes = getMagicentoComponent().findResourceModelsOfFactoryUri(uri);
                    if(classes != null && classes.size() > 0){
                        className = classes.get(0).name;
                    }
                }
                else {
                    className = getMagicentoComponent().getClassNameFromFactory(factoryElement);
                }

                if(className != null){
                    final String varDoc = "/* @var "+ varName + " " + className +" */";
                    //com.intellij.openapi.command.CommandProcessor.executeCommand()
                    CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
                        public void run() {
                            addNewLineBeforeCaret();
                            writeStringInCaret(varDoc);
                        }
                    }, null, null);
                }
            }
        }
    }

    protected boolean isMethodReturningCollection(String method)
    {
        if(method == null){
            return false;
        }
        return method.equals("getCollection");
    }

    protected boolean isMethodReturningResource(String method)
    {
        if(method == null){
            return false;
        }
        return method.equals("getResource");
    }


    protected boolean isCollection(PsiElement psiElement)
    {
        boolean isCollection = false;
        if( PsiPhpHelper.hasChainedMethod(psiElement) )
        {
            List<String> chainedMethods = PsiPhpHelper.getChainedMethodNames(psiElement);
            for(String chainedMethod : chainedMethods)
            {
                // if method returns $this it doesn't change the last object
                if( isMethodReturningThis(chainedMethod)){
                    continue;
                }

                if(isMethodReturningCollection(chainedMethod)){
                    isCollection = true;
                }
            }
        }
        return isCollection;
    }

    protected boolean isResource(PsiElement psiElement)
    {
        boolean isResource = false;
        if( PsiPhpHelper.hasChainedMethod(psiElement) )
        {
            List<String> chainedMethods = PsiPhpHelper.getChainedMethodNames(psiElement);
            for(String chainedMethod : chainedMethods)
            {
                // if method returns $this it doesn't change the last object
                if( isMethodReturningThis(chainedMethod)){
                    continue;
                }

                if(isMethodReturningResource(chainedMethod)){
                    isResource = true;
                }
            }
        }
        return isResource;
    }


    protected void addNewLineAfterCaret()
    {
        ActionManager actionManager = ActionManagerImpl.getInstance();
        final AnAction action = actionManager.getAction(IdeActions.ACTION_EDITOR_START_NEW_LINE);
        AnActionEvent event = new AnActionEvent(null, getDataContext(), IdeActions.ACTION_EDITOR_START_NEW_LINE, getEvent().getPresentation(), ActionManager.getInstance(), 0);
        action.actionPerformed(event);
    }

    protected void addNewLineBeforeCaret()
    {
        ActionManager actionManager = ActionManagerImpl.getInstance();
        AnAction action = actionManager.getAction(IdeActions.ACTION_EDITOR_MOVE_CARET_UP);
        AnActionEvent event = new AnActionEvent(null, getDataContext(), IdeActions.ACTION_EDITOR_COMPLETE_STATEMENT, getEvent().getPresentation(), ActionManager.getInstance(), 0);
        action.actionPerformed(event);

        addNewLineAfterCaret();
    }

    private void writeStringInCaret(String text)
    {
        final String write = text;
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                getDocument().insertString(getCaretModel().getOffset(), write);
            }
        });
    }

    private boolean isMethodReturningThis(String methodName)
    {
        // TODO: add more methods returning $this
        List<String> methodsReturningThis = new ArrayList<String>();
        methodsReturningThis.add("load");
        methodsReturningThis.add("save");
        if(methodName != null && methodsReturningThis.contains(methodName)){
            return true;
        }
        return false;
    }

}



