package com.magicento.models;

import com.magicento.MagicentoProjectComponent;
import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * @deprecated this is not needed anymore, we are using a custom ChooseByNameItemProvider
 * @author Enrique Piatti
 */
public class GotoFactoryModel extends GotoClassModel2 implements CustomMatcherModel {

    protected File _configXml;
    protected MagicentoProjectComponent _magicentoProject;
    protected String _currentPattern;
    protected List<String> _classesOfCurrentPattern;

    public GotoFactoryModel(Project project, File configXml) {
        super(project);
        _configXml = configXml;
        _magicentoProject = MagicentoProjectComponent.getInstance(project);
    }

//    @Override
//    public Object[] getElementsByName(String name, boolean checkBoxState, String pattern) {
//
//        Object[] completeList = {};
//        List<String> classes = _magicentoProject.findClassesOfFactoryUri(pattern, _configXml);
//        for (String classPrefix: classes ){
//            Object[] list = super.getElementsByName(name, checkBoxState, classPrefix);
//            completeList = ArrayUtils.addAll(completeList, list);
//        }
//        return completeList;
//    }
//
//    @Override
//    public String[] getNames(boolean checkBoxState) {
//        return super.getNames(checkBoxState);
//    }

    @Override
    public boolean matches(@NotNull String popupItem, @NotNull String userPattern) {
        if(_currentPattern == null || ! userPattern.equals(_currentPattern)){
            _classesOfCurrentPattern = MagentoClassInfo.getNames(_magicentoProject.findClassesOfFactoryUri(userPattern));
            _currentPattern = userPattern;
        }
        for (String classPrefix: _classesOfCurrentPattern ){
            if(popupItem.startsWith(classPrefix)){
                return true;
            }
        }
        return false;
    }
}
