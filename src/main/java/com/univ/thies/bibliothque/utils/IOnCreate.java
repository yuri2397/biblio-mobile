package com.univ.thies.bibliothque.utils;

import java.util.regex.Pattern;

public interface IOnCreate {
    Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,253}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,253}[a-zA-Z0-9])?)*$", Pattern.CASE_INSENSITIVE);

    default void init(){
        findView();
        initData();
        manageView();
        setListeners();
    }
    void findView();
    void initData();
    void manageView();
    void setListeners();
}
