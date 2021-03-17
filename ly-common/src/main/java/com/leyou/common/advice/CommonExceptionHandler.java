package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//这个注解专门用来标识处理controller的异常处理的建议
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(lyException.class)
    public ResponseEntity<ExceptionResult> handleException(lyException e){

        return ResponseEntity.status(e.getExceptionEnum().getCode())
                .body(new ExceptionResult(e.getExceptionEnum()));

    }
}
