package com.leyou.common.exception;


import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class lyException extends RuntimeException {
    public ExceptionEnum exceptionEnum;
}
