package unq.dapp.grupoj.soccergenius.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class AuditWebService {

    private static final Logger logger = LoggerFactory.getLogger(AuditWebService.class);

    @Around("execution(public * unq.dapp.grupoj.soccergenius.controllers..*(..)) && @within(org.springframework.web.bind.annotation.RestController)")
    public Object auditWebServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String declaringTypeName = signature.getDeclaringTypeName();
        Object[] methodArgs = joinPoint.getArgs();
        String user = getCurrentUser();

        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            LocalDateTime timestamp = LocalDateTime.now();

            String parameters = Arrays.toString(methodArgs);

            logger.info("AUDIT - Timestamp: [{}], User: [{}], Operation: [{}.{}], Parameters: [{}], ExecutionTime: [{}ms]",
                    timestamp,
                    user,
                    declaringTypeName,
                    methodName,
                    parameters,
                    executionTime);
        }
    }

    private String getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            if (request != null && request.getUserPrincipal() != null) {
                return request.getUserPrincipal().getName();
            }
        }
        return "anonymousUser";
    }
}
