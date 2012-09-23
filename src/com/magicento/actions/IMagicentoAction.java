package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Enrique Piatti
 */
public interface IMagicentoAction {

    Boolean isApplicable(AnActionEvent e);

}
