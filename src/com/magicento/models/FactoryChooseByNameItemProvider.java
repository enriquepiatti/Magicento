package com.magicento.models;

import com.magicento.MagicentoProjectComponent;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.StringLenComparator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Enrique Piatti
 */
public class FactoryChooseByNameItemProvider extends DefaultChooseByNameItemProvider {

    protected File _configXml;
    protected Project _project;
    protected MagicentoProjectComponent _magicentoProject;

    public FactoryChooseByNameItemProvider(PsiElement context, Project project) {
        super(context);
        _project = project;
        _magicentoProject = MagicentoProjectComponent.getInstance(_project);
        _configXml = _magicentoProject.getCachedConfigXml();
    }

    public void filterElements(ChooseByNameBase base,
                               String pattern,
                               boolean everywhere,
                               Computable<Boolean> cancelled,
                               Processor<Object> consumer) {
        String namePattern = getNamePattern(base, pattern);


        List<String> classes = MagentoClassInfo.getNames(_magicentoProject.findClassesOfFactoryUri(pattern));

        // TODO: create new ChooseByName with 3 checkboxes (models, blocks and helpers) to filter by that
        //       or create a submenu for choosing between helper block or model factories

        List<Object> sameNameElements = new SmartList<Object>();

        for (String name : classes) {
            if (cancelled.compute()) {
                throw new ProcessCanceledException();
            }
            //final Object[] elements = base.getModel().getElementsByName(name, everywhere, namePattern);
            final Object[] elements = base.getModel().getElementsByName(name, everywhere, name);
            if (elements.length > 1) {      // duplicated name classes? for example in /local/ and /core/ ?
                sameNameElements.clear();
                for (final Object element : elements) {
                    sameNameElements.add(element);
                }
                /*sortByProximity(base, sameNameElements);*/
                //sortAlphabetically(base, sameNameElements);
                for (Object element : sameNameElements) {
                    if (!consumer.process(element)) return;
                }
            }
            else if (elements.length == 1 /*&& matchesQualifier(elements[0], qualifierPattern, base)*/) {
                if (!consumer.process(elements[0])) return;
            }
        }
    }


    private void getNamesByPattern(ChooseByNameBase base,
                                          String[] names,
                                          Computable<Boolean> cancelled,
                                          final List<String> list,
                                          String pattern)
            throws ProcessCanceledException {

        try {
            List<String> classes = MagentoClassInfo.getNames(_magicentoProject.findClassesOfFactoryUri(pattern));
            if(classes.size() > 0){
                String regex = StringUtils.join(classes, "|");
                Pattern p = Pattern.compile(regex);
                for (String name : names) {
                    if (cancelled != null && cancelled.compute()) {
                        break;
                    }
                    //if (matches(base, pattern, matcher, name))
                    if(p.matcher(name).find()) {
                        list.add(name);
                    }
                }
                // TODO: use a FactoryPximityComparator here
                Collections.sort(list, StringLenComparator.getInstance());
            }
        }
        catch (Exception e) {
            // Do nothing. No matches appears valid result for "bad" pattern
        }
    }

    private void sortAlphabetically(ChooseByNameBase base, final List<Object> sameNameElements) {
        Collections.sort(sameNameElements, new StringLengthComparator());
    }

    private static class StringLengthComparator implements Comparator<Object> {

        public int compare(final Object o1, final Object o2) {
            return 0;
            //return (o1.length() == o2.length()) ? 0 : (revertor * ((o1.length() < o2.length()) ? -1 : 1));
        }
    }

}
