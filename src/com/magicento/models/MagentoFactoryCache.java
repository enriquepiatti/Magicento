package com.magicento.models;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class MagentoFactoryCache extends MagentoFactory
{

    protected Map<String, List<MagentoClassInfo>> _cachedFactories;

    public MagentoFactoryCache(Project _project) {
        super(_project);
    }


    @Override
    public List<MagentoClassInfo> findClassesForFactory(String factory, File xmlFile, MagentoClassInfo.UriType[] types)
    {
        if(_cachedFactories == null){
            _cachedFactories = new HashMap<String, List<MagentoClassInfo>>();
        }
        boolean isExactMatch = isExactMatch(factory);
        if( ! isExactMatch){
            return super.findClassesForFactory(factory, xmlFile, types);
        }
        String key = createKey(factory, types);
        List<MagentoClassInfo> classes = _cachedFactories.get(key);
        if(classes == null){
            classes = super.findClassesForFactory(factory, xmlFile, types);
            _cachedFactories.put(key, classes);
        }
        return classes;
    }

    protected String createKey(String factory, MagentoClassInfo.UriType[] types)
    {
        List<MagentoClassInfo.UriType> typesAsList = Arrays.asList(types);
        String key = factory;
        if(typesAsList.contains(MagentoClassInfo.UriType.MODEL)){
            factory += "&&model";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.RESOURCEMODEL)){
            factory += "&&resourcemodel";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.HELPER)){
            factory += "&&helper";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.BLOCK)){
            factory += "&&block";
        }
        return factory;
    }

    public void invalidateCache()
    {
        _cachedFactories = null;
    }


}
