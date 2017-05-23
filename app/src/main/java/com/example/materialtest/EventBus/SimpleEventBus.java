package com.example.materialtest.EventBus;

/**
 * Created by surine on 2017/5/23.
 */

public class SimpleEventBus {
    private int id;
    private String string;

    public SimpleEventBus(int id, String string) {
        this.id = id;
        this.string = string;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
