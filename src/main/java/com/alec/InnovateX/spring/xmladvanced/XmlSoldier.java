package com.alec.InnovateX.spring.xmladvanced;

/** prototype 士兵：每次 lookup 都是新兵 */
public class XmlSoldier {

    public String whoAmI() {
        return "Soldier@" + Integer.toHexString(System.identityHashCode(this));
    }
}
