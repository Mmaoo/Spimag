package com.mmaoo.spimag.model;

/**
 * Universal runnable method to execute
 * @param <T> - class of method's parameter
 * @param <R> - class of method's result
 */
public interface Command<T,R>{
    public R run(T param);
}