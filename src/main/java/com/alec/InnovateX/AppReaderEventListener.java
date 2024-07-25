package com.alec.InnovateX;

import org.springframework.beans.factory.parsing.*;


public class AppReaderEventListener extends EmptyReaderEventListener {
    @Override
    public void componentRegistered(ComponentDefinition componentDefinition) {
        System.out.println(componentDefinition.getName() + "......created");
    }
}
