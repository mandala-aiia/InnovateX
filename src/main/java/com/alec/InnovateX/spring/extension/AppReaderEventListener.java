package com.alec.InnovateX.spring.extension;

import org.springframework.beans.factory.parsing.*;


public class AppReaderEventListener extends EmptyReaderEventListener {
    @Override
    public void componentRegistered(ComponentDefinition componentDefinition) {
        System.out.println(componentDefinition.getName() + "......created");
    }
}
