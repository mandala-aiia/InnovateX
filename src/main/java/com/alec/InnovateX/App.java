package com.alec.InnovateX;

import lombok.Data;
import org.springframework.core.io.Resource;

import java.util.List;


/**
 * {@link AppDev}
 *
 * @see com.alec.InnovateX.AppDev
 */
@Data
public class App {
    private String appName;

    private AppDev appDev;

    private List<String> desc;

    private String appFirSec;

    private Resource appResource;

    public void initApp() {
        System.out.println("App....init");
    }

    public void destroyApp() {
        System.out.println("App....destroy");
    }

    @Override
    public String toString() {
        return "App{" +
                "appName='" + appName + '\'' +
                ", appDev=" + appDev +
                ", desc=" + desc +
                ", appFirSec='" + appFirSec + '\'' +
                ", appResource='" + appResource + '\'' +
                '}';
    }
}
