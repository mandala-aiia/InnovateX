package com.alec.InnovateX.spring.xmladvanced;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** 集合注入载体：map/set/props + util: 命名空间的独立集合 Bean */
public class XmlCollectionsBean {

    private Map<String, Object> map;

    private Set<String> set;

    private Properties props;

    private List<String> utilList;

    private Map<String, String> utilMap;

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public List<String> getUtilList() {
        return utilList;
    }

    public void setUtilList(List<String> utilList) {
        this.utilList = utilList;
    }

    public Map<String, String> getUtilMap() {
        return utilMap;
    }

    public void setUtilMap(Map<String, String> utilMap) {
        this.utilMap = utilMap;
    }
}
