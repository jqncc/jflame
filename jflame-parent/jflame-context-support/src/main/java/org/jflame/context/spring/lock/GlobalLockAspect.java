package org.jflame.context.spring.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import org.jflame.commons.exception.BusinessException;
import org.jflame.commons.util.ArrayHelper;
import org.jflame.commons.util.StringHelper;
import org.jflame.context.cache.redis.RedisClient;
import org.jflame.context.lock.DistributedLock;
import org.jflame.context.lock.RedisLock;
import org.jflame.context.lock.ZookeeperLock;
import org.jflame.context.spring.SpringContextHolder;
import org.jflame.context.spring.lock.GlobalLock.LockType;
import org.jflame.context.zookeeper.ZookeeperClient;

/**
 * 分布式锁切面.基于spring aop实现,方法执行自动开启分布式锁
 * <p>
 * 注:如果是AOP事务,应该执行于本地数据库事务之前
 * 
 * @author yucan.zhang
 */
@Component
@Aspect
@Order(-1000)
public class GlobalLockAspect {

    @Around("@annotation(lockAnnotatation)")
    public Object lockAround(ProceedingJoinPoint joinPoint, GlobalLock lockAnnotatation) throws Throwable {
        Object returnObj = null;
        if (StringHelper.isEmpty(lockAnnotatation.lockName())) {
            throw new IllegalArgumentException("GlobalLock lockName not be empty");
        }
        Object[] args = joinPoint.getArgs();
        ExpressionParser parser = new SpelExpressionParser();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        EvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] params = discoverer.getParameterNames(signature.getMethod());
        String lockKey = null;
        if (ArrayHelper.isNotEmpty(params)) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
            Expression expression = parser.parseExpression(lockAnnotatation.lockName());
            lockKey = expression.getValue(context, String.class);
        } else {
            lockKey = lockAnnotatation.lockName();
        }
        DistributedLock lock = null;
        boolean isLocked = false;
        try {
            lock = createLock(lockAnnotatation.lockType(), lockKey, lockAnnotatation.lockTime());
            isLocked = lock.lock(lockAnnotatation.waitTime());
            if (isLocked) {
                returnObj = joinPoint.proceed();
            } else {
                throw new BusinessException(String.format("获取分布式锁失败,方法:%s,锁名:%s", signature.getName(), lockKey));
            }
        } finally {
            if (lock != null && isLocked) {
                lock.unlock();
            }
        }

        return returnObj;
    }

    DistributedLock createLock(LockType lockType, String lockKey, Integer lockTime) {
        if (lockType == LockType.redis) {
            RedisClient redisClient = SpringContextHolder.getBean(RedisClient.class);
            return new RedisLock(redisClient, lockKey, lockTime);
        } else if (lockType == LockType.zk) {
            ZookeeperClient zkClient = SpringContextHolder.getBean(ZookeeperClient.class);
            return new ZookeeperLock(zkClient, lockKey, lockTime);
        }
        throw new IllegalStateException(lockType + " not be supported");
    }

}
