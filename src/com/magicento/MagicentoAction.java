package com.magicento;

import com.magicento.actions.IMagicentoAction;
import com.magicento.actions.MagicentoActionAbstract;
import com.intellij.openapi.actionSystem.*;
//import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilBase;

import java.util.ArrayList;
import java.util.List;

/**
 * General magicento action for Alt+M (Option+M)
 * @author Enrique Piatti
 */
public class MagicentoAction extends MagicentoActionAbstract implements IMagicentoAction {

    public void executeAction()
    {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        //SimpleActionGroup actionGroup = new SimpleActionGroup();
        List<AnAction> actions = _getMagentoContextActions();
        if( actions.size() > 0){
            for (AnAction action : actions) {
                actionGroup.add(action);
            }
            final ListPopup popup =
                    JBPopupFactory.getInstance().createActionGroupPopup(
                            "Magicento Actions",
                            actionGroup,
                            getDataContext(),
                            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                            false);

            popup.showInBestPositionFor(getDataContext());
        }
    }


    /**
     * Define MagicentoActions to be listed when using Alt+M (Option+M)
     * @return
     */
    protected List<AnAction> _getMagentoContextActions()
    {
        List<AnAction> actions = new ArrayList<AnAction>();
        String[] actionIds = {
                "GotoMagentoClass",
                "AddVarPhpDoc",
                "CopyTemplate",
                "GetStoreConfig",
                "CompareWithOriginal",
                "EvaluateInMagento",
                "GotoClassesOfFactory",
                "CreateModule",
                "SetMagePath",
                "SetStore",
                "ToggleTemplateHints"
        };
        ActionManager actionManager = ActionManagerImpl.getInstance();
        for (String actionId : actionIds) {
            AnAction action = actionManager.getAction(actionId);
            if( ((IMagicentoAction)action).isApplicable(getEvent()) ) {
                actions.add(action);
            }
        }
        return actions;
    }


    @Override
    public void update(AnActionEvent e) {

        Presentation presentation = e.getPresentation();
        DataContext dataContext = e.getDataContext();

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            presentation.setEnabled(false);
            return;
        }

        final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        presentation.setEnabled(file != null);

        // disable the action if we are not inside an active editor
        //e.getPresentation().setEnabled(e.getDataContext().getData(DataConstants.EDITOR) != null);
    }

    public Boolean isApplicable(AnActionEvent e)
    {
        return true;
    }
}


enum MageContext {
    MAGE_FACTORY,
    MAGE_FACTORY_INCOMPLETE
}
