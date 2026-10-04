package com.alec.InnovateX.spring.extension;

/** BFPP 改写 Definition 的观测目标：构造即置位，用来判断"何时被实例化" */
public class ExtensionDemoBean {

    public static boolean instantiated = false;

    public ExtensionDemoBean() {
        instantiated = true;
        System.out.println("ExtensionDemoBean 构造（被 BFPP 改成 lazy 后，refresh 结束才应看到这行之后的 getBean）");
    }
}
