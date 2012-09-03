package com.magicento.actions;

import com.magicento.models.FactoryChooseByNameItemProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.ide.util.gotoByName.*;
import com.intellij.lang.Language;
import com.intellij.navigation.AnonymousElementProvider;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GotoClassesOfFactoryAction extends MagicentoActionAbstract {

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        // show always
        return true;
    }

    public void actionPerformed(AnActionEvent e) {
        setEvent(e);
        File configXml = getMagicentoComponent().getCachedConfigXml();
        if(configXml != null && configXml.exists())
        {

            final Project project = getProject();
            PsiDocumentManager.getInstance(project).commitAllDocuments();

            //final GotoFactoryModel model = new GotoFactoryModel(project, configXml);
            final GotoClassModel2 model = new GotoClassModel2(project);
            showNavigationPopup(e, model, new GotoActionCallback<Language>() {
                @Override
                protected ChooseByNameFilter<Language> createFilter(@NotNull ChooseByNamePopup popup) {
                    return new ChooseByNameLanguageFilter(popup, model, GotoClassSymbolConfiguration.getInstance(project), project);
                }

                @Override
                public void elementChosen(ChooseByNamePopup popup, Object element) {
                    AccessToken token = ReadAction.start();
                    try {
                        if (element instanceof PsiElement) {
                            final PsiElement psiElement = getElement(((PsiElement) element), popup);
                            NavigationUtil.activateFileWithPsiElement(psiElement, !popup.isOpenInCurrentWindowRequested());
                        } else {
                            EditSourceUtil.navigate(((NavigationItem) element), true, popup.isOpenInCurrentWindowRequested());
                        }
                    } finally {
                        token.finish();
                    }
                }
            }, "Classes matching pattern");
        }
    }

    protected abstract static class GotoActionCallback<T> {
        @Nullable
        protected ChooseByNameFilter<T> createFilter(@NotNull ChooseByNamePopup popup) {
            return null;
        }

        public abstract void elementChosen(ChooseByNamePopup popup, Object element);
    }

    protected static <T> void showNavigationPopup(AnActionEvent e,
                                                  ChooseByNameModel model,
                                                  final GotoActionCallback<T> callback,
                                                  final String findTitle) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);

        // boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() && FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        //final ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, model, getPsiContext(e));
        ChooseByNameItemProvider itemProvider = new FactoryChooseByNameItemProvider(getPsiContext(e), project);
        final ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, model, itemProvider);
        popup.setFindUsagesTitle(findTitle);
        final ChooseByNameFilter<T> filter = callback.createFilter(popup);

        popup.invoke(new ChooseByNamePopupComponent.Callback() {

            @Override
            public void onClose() {
                if (filter != null) {
                    filter.close();
                }
            }

            @Override
            public void elementChosen(Object element) {
                callback.elementChosen(popup, element);
            }
        }, ModalityState.current(), true);
    }

    @Nullable
    public static PsiElement getPsiContext(final AnActionEvent e) {
        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
        if (file != null) return file;
        Project project = e.getData(PlatformDataKeys.PROJECT);
        return getPsiContext(project);
    }

    @Nullable
    public static PsiElement getPsiContext(final Project project) {
        if (project == null) return null;
        Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedEditor == null) return null;
        Document document = selectedEditor.getDocument();
        return PsiDocumentManager.getInstance(project).getPsiFile(document);
    }

    private static PsiElement getElement(PsiElement element, ChooseByNamePopup popup) {
        final String path = popup.getPathToAnonymous();
        if (path != null) {

            final String[] classes = path.split("\\$");
            List<Integer> indexes = new ArrayList<Integer>();
            for (String cls : classes) {
                if (cls.isEmpty()) continue;
                try {
                    indexes.add(Integer.parseInt(cls) - 1);
                } catch (Exception e) {
                    return element;
                }
            }
            PsiElement current = element;
            for (int index : indexes) {
                final PsiElement[] anonymousClasses = getAnonymousClasses(current);
                if (anonymousClasses.length > index) {
                    current = anonymousClasses[index];
                } else {
                    return current;
                }
            }
            return current;
        }
        return element;
    }

    static PsiElement[] getAnonymousClasses(PsiElement element) {
        for (AnonymousElementProvider provider : Extensions.getExtensions(AnonymousElementProvider.EP_NAME)) {
            final PsiElement[] elements = provider.getAnonymousElements(element);
            if (elements != null && elements.length > 0) {
                return elements;
            }
        }
        return PsiElement.EMPTY_ARRAY;
    }

}
