package com.magicento.actions;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.template.zencoding.filters.ZenCodingFilter;
import com.intellij.codeInsight.template.zencoding.generators.ZenCodingGenerator;
import com.intellij.codeInsight.template.zencoding.nodes.ZenCodingNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.magicento.helpers.PsiPhpHelper;

import java.util.List;

/**
 * @author Enrique Piatti
 */
public class TranslateTextAction extends MagicentoActionAbstract {

    @Override
    public void executeAction() {

        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

        SelectionModel selectionModel = getEditor().getSelectionModel();
        final Document doc = getEditor().getDocument();
        // PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(doc);
        final int startOffset = selectionModel.getSelectionStart();
        final int endOffset = selectionModel.getSelectionEnd();
        // final TextRange textRange = new TextRange(startOffset, endOffset);

        String selectedText = getSelectedText();
        String startText = "$this->__(";
        String endText = ")";
        if(isHtml()){
            startText = "<?php echo " + startText + "'";
            endText = "'" + endText + " ?>";
            StringBuilder buffer = new StringBuilder();
            StringUtil.escapeStringCharacters(selectedText.length(), selectedText, "'\"", buffer);
            selectedText = buffer.toString();
        }

        final String insertText = startText+selectedText+endText;

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {

                CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
                    public void run() {
                        doc.replaceString(startOffset, endOffset, insertText);
                    }
                }, null, null);

            }
        });
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        setEvent(e);
        String selectedText = getSelectedText();
        if(selectedText != null && ! selectedText.isEmpty()){
            return isHtml() || (isPhp() && PsiPhpHelper.isString(getPsiElementAtCursor()));
        }
        return false;
    }
}
