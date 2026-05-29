package com.sprint.mission.discodeit.log;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

  @Around("execution(* com.sprint.mission.discodeit.controller..*.*(..)) "
      + "|| execution(* com.sprint.mission.discodeit.service..*.*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    String className = joinPoint.getTarget().getClass().getName();
    String methodName = joinPoint.getSignature().getName();

    log.debug("[START] {}.{}() | Args: {}", className, methodName,
        Arrays.toString(joinPoint.getArgs()));

    try {
      Object result = joinPoint.proceed();
      long excutionTime = System.currentTimeMillis() - start;
      log.debug("[END] {}.{}() | Taken: {} ms", className, methodName, excutionTime);
      return result;
    } catch (Throwable e) {
      long excutionTime = System.currentTimeMillis() - start;
      log.error("[FAIL] {}.{}() | Exception: {} | Taken: {} ms", className, methodName,
          e.getMessage(), excutionTime, e);
      throw e;
    }
  }

}
