package com.magicento.models;

import com.intellij.openapi.project.Project;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.config.MagentoConfigXml;
import org.jdom.Document;
import org.jdom.Element;

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
    protected Document _cachedXmlDocument;

    public MagentoFactoryCache(Project _project) {
        super(_project);
    }


    @Override
    public List<MagentoClassInfo> findClassesForFactory(String factory, File xmlFile, MagentoClassInfo.UriType[] types)
    {
        if( MagentoXmlFactory.getInstance(MagentoConfigXml.TYPE, _project).isCacheInvalidated()){
            invalidateCache();
        }

        if(_cachedFactories == null){
            _cachedFactories = new HashMap<String, List<MagentoClassInfo>>();
        }

        if(_cachedXmlDocument == null){
            _cachedXmlDocument = getXmlDocument(xmlFile);
        }

        boolean isExactMatch = isExactMatch(factory);
        if( ! isExactMatch){
            return super.findClassesForFactory(factory, _cachedXmlDocument, types);
        }
        String key = createKey(factory, types);
        List<MagentoClassInfo> classes = _cachedFactories.get(key);
        if(classes == null){
            classes = super.findClassesForFactory(factory, _cachedXmlDocument, types);
            _cachedFactories.put(key, classes);
        }
        return classes;
    }

    /**
     * returns only useful part from config.xml
     * @param xmlFile
     * @return
     */
    protected Document getXmlDocument(File xmlFile)
    {
        if(xmlFile == null || ! xmlFile.exists()){
            return null;
        }
        String typeExp = "name()='models' or name()='helpers' or name()='blocks'";
        String xpath = "/config/global/*["+typeExp+"]";
        List<Element> elements = XmlHelper.findXpath(xmlFile, xpath);
        if(elements == null || elements.size() == 0){
            return null;
        }
        Document cachedDocument = new Document(new Element("config"));
        cachedDocument.getRootElement().addContent(new Element("global"));
        Element globalElement = cachedDocument.getRootElement().getChild("global");
        for(Element element : elements){
            XmlHelper.mergeXmlElement(element, globalElement, false);
        }
        return cachedDocument;
    }

    protected String createKey(String factory, MagentoClassInfo.UriType[] types)
    {
        List<MagentoClassInfo.UriType> typesAsList = Arrays.asList(types);
        String key = factory;
        if(typesAsList.contains(MagentoClassInfo.UriType.MODEL)){
            key += "&&model";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.RESOURCEMODEL)){
            key += "&&resourcemodel";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.HELPER)){
            key += "&&helper";
        }
        if(typesAsList.contains(MagentoClassInfo.UriType.BLOCK)){
            key += "&&block";
        }
        return key;
    }

    public void invalidateCache()
    {
        _cachedFactories = null;
        _cachedXmlDocument = null;
    }




}
