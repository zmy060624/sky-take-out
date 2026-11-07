package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//标注这个注解可以放在哪个位置 method 方法
@Target(ElementType.METHOD)
//说明这个注解要保留到什么时候
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //数据库的操作类型
    OperationType value();
}
