package com.magicento.actions;

import com.magicento.MagicentoProjectComponent;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;

/**
 * base class for all the MagicentoActions
 * @author Enrique Piatti
 */
public abstract class MagicentoActionAbstract extends AnAction
{

    protected String _code;
    protected int _cursorOffset = -1;
    protected AnActionEvent _event;
    protected DataContext _dataContext;
    protected Project _project;
    protected Editor _editor;
//    protected FindModel findModel;
//    protected FindManager findManager;
//    protected AbstractPopup popup;
    protected VirtualFile _virtualFile;
    protected PsiFile _psiFile;
    protected DocumentImpl _document;

//    protected FoldingModelImpl foldingModel;
//    protected SearchBox searchBox;
    protected CaretModel _caretModel;




    public abstract Boolean isApplicable(AnActionEvent e);

//    public void actionPerformed(AnActionEvent e)
//    {
//        initFromEvent(e);
//    }

//    public void initFromEvent(AnActionEvent e)
//    {
//        _event = e;
//    }

    private void reset()
    {
        _event = null;
        _caretModel = null;
        _code = null;
        _cursorOffset = -1;
        _dataContext = null;
        _document = null;
        _editor = null;
        _project = null;
        _psiFile = null;
        _virtualFile = null;
    }

    //////////////////////////////////////////////////////////
    // SETTERS
    //////////////////////////////////////////////////////////

    public void setCaretModel(CaretModel caretModel) {
        this._caretModel = caretModel;
    }

    public void setCode(String _code) {
        this._code = _code;
    }

    public void setCursorOffset(int _cursorOffset) {
        this._cursorOffset = _cursorOffset;
    }

    public void setEvent(AnActionEvent _event) {
        reset();
        this._event = _event;
    }

    public void setDataContext(DataContext _dataContext) {
        this._dataContext = _dataContext;
    }

    public void setProject(Project _project) {
        this._project = _project;
    }

    public void setEditor(Editor _editor) {
        this._editor = _editor;
    }

    public void setVirtualFile(VirtualFile _virtualFile) {
        this._virtualFile = _virtualFile;
    }

    public void setPsiFile(PsiFile _psiFile) {
        this._psiFile = _psiFile;
    }

    public void setDocument(DocumentImpl _document) {
        this._document = _document;
    }

    //////////////////////////////////////////////////////////
    // GETTERS
    //////////////////////////////////////////////////////////

    public CaretModel getCaretModel() {
        if(_caretModel == null){
            if(getEditor() != null){
                _caretModel = getEditor().getCaretModel();
            }
        }
        return _caretModel;
    }

    /**
     * source code
     * @return
     */
    public String getCode() {
        if(_code == null){
            if(getEditor() != null){
                _code = getEditor().getDocument().getCharsSequence().toString();
            }
        }
        return _code;
    }

    public int getCursorOffset() {
        if(_cursorOffset == -1){
            if(getEditor() != null){
                _cursorOffset = getEditor().getCaretModel().getOffset();
            }
        }
        return _cursorOffset;
    }

    public AnActionEvent getEvent() {
        return _event;
    }

    public DataContext getDataContext() {
        if(_dataContext == null){
            if(getEvent() != null){
                _dataContext = getEvent().getDataContext();
            }
        }
        return _dataContext;
    }

    public Project getProject() {
        if(_project == null){
            if(getDataContext() != null){
                //_project = (Project) getDataContext().getData(DataConstants.PROJECT); // DataKeys.PROJECT.getData(getDataContext());
                _project = PlatformDataKeys.PROJECT.getData(getDataContext());
            }
        }
        return _project;
    }

    public Editor getEditor() {
        if(_editor == null){
            if(getDataContext() != null){
                //_editor = (Editor) getDataContext().getData(DataConstants.EDITOR);  //DataKeys.EDITOR.getData(e.getDataContext());
                _editor = PlatformDataKeys.EDITOR.getData(getDataContext());
            }
        }
        return _editor;
    }

    public VirtualFile getVirtualFile() {
        if(_virtualFile == null){
            if(getDataContext() != null){
                //_virtualFile = (VirtualFile) getDataContext().getData(DataConstants.VIRTUAL_FILE); //DataKeys.VIRTUAL_FILE.getData(_dataContext);
                _virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(getDataContext());
            }
        }
        return _virtualFile;
    }

    public PsiFile getPsiFile() {
        if(_psiFile == null){
            if(getProject() != null && getVirtualFile() != null){
                _psiFile = PsiManager.getInstance(getProject()).findFile(getVirtualFile());
            }
        }
        // _psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        return _psiFile;
    }

    public DocumentImpl getDocument() {
        if(_document == null){
            if(getEditor() != null){
                _document = (DocumentImpl) getEditor().getDocument();
            }
        }
        return _document;
    }


    public PsiElement getPsiElementAtCursor(){
        PsiFile psiFile = getPsiFile();
        if(psiFile == null)
            return null;
        return psiFile.findElementAt(getCursorOffset());
    }

    public String getSelectedText(){
        if(getEditor() == null)
            return null;
        SelectionModel selectionModel = getEditor().getSelectionModel();
        return selectionModel.getSelectedText();
    }


    protected void gotoClass(String className)
    {
        if(className == null)
            return;
        GotoClassModel2 model = new GotoClassModel2(getProject());
        Object[] elements = model.getElementsByName(className, true, className);
        //com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl
        if(elements.length > 0){
            PsiElement element = (PsiElement) elements[0];
            /*
            PsiFile containingFile = element.getContainingFile();
            //PsiReference psiReference = element.getReference(); // element.getReferences() // (PsiReference) element;
            //PsiFile containingFile = psiReference.resolve().getContainingFile();
            VirtualFile virtualFile = containingFile.getVirtualFile();
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
            */
            PsiElement navElement = element.getNavigationElement();
            navElement = TargetElementUtilBase.getInstance().getGotoDeclarationTarget(element, navElement);
            if (navElement instanceof Navigatable) {
                if (((Navigatable)navElement).canNavigate()) {
                    ((Navigatable)navElement).navigate(true);
                }
            }
            else if (navElement != null) {
                int navOffset = navElement.getTextOffset();
                VirtualFile virtualFile = PsiUtilCore.getVirtualFile(navElement);
                if (virtualFile != null) {
                    new OpenFileDescriptor(getProject(), virtualFile, navOffset).navigate(true);
                }
            }
        }
    }

    public MagicentoProjectComponent getMagicentoComponent()
    {
        Project project = getProject();
        if(project != null){
            //return (MagicentoProjectComponent) project.getComponent("MagicentoProjectComponent");
           return MagicentoProjectComponent.getInstance(project);
        }
        return null;
    }


    protected String getWordAtCursor(CharSequence editorText, int cursorOffset)
    {
        if(editorText.length() == 0) return null;
        if(cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset)) &&
                Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }
        if(Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))){
            int start = cursorOffset;
            int end = cursorOffset;
            while(start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            while(end < editorText.length() && Character.isJavaIdentifierPart(editorText.charAt(end))) {
                end++;
            }
            return editorText.subSequence(start, end).toString();
        }
        return null;
    }


}



