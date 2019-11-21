package org.jflame.context.dubbo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

import org.jflame.commons.util.CollectionHelper;

/**
 * ip白明单限制服务调用客户端
 * 
 * @author yucan.zhang
 */
public class WhitelistFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(WhitelistFilter.class);
    private List<String> ipWhiteList;
    private String excludeProtocol = "rest";

    public WhitelistFilter() {
        URL url = WhitelistFilter.class.getResource("/whitelist");
        if (url != null) {
            Path whiteFilePath;
            try {
                whiteFilePath = Paths.get(url.toURI());
                logger.debug("whitelist path:{}", whiteFilePath);
                if (Files.exists(whiteFilePath)) {
                    ipWhiteList = Files.readAllLines(whiteFilePath, StandardCharsets.UTF_8);
                } else {
                    logger.warn("白名单文件whitelist不存在{}", url);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.warn("白名单文件whitelist不存在");
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // rest协议不限制ip
        if (CollectionHelper.isNotEmpty(ipWhiteList)
                && !excludeProtocol.equals(RpcContext.getContext().getUrl().getProtocol())) {
            String clientIp = RpcContext.getContext().getRemoteHost();
            logger.debug("访问ip:{}", clientIp);
            if (!ipWhiteList.contains(clientIp)) {
                throw new RpcException("不允许访问服务,IP: " + clientIp);
            }
        }
        return invoker.invoke(invocation);
    }

}
