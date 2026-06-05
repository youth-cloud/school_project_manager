package org.example.backend.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.entity.EduClass;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数校验失败";
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Object> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数绑定失败";
        return Result.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        return Result.fail(500, "系统异常，请稍后再试");
    }
}
