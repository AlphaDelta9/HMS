package com.pmc.huawei;

import java.io.Serializable;

public class Item implements Serializable {
    private String string1, string2;

    public Item(String string1, String string2) {
        this.string1 = string1;
        this.string2 = string2;
    }

    public String getString1() {
        return string1;
    }

    public String getString2() {
        return string2;
    }

    @Override
    public String toString() {
        return "{" +
                "string1='" + string1 + '\'' +
                ", string2='" + string2 + '\'' +
                '}';
    }
}
