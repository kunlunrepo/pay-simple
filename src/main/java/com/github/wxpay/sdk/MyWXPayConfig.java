package com.github.wxpay.sdk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

/**
 * description : 微信支付配置实现类
 *
 * @author kunlunrepo
 * date :  2024-03-01 16:21
 */
@Component
public class MyWXPayConfig extends WXPayConfig{

    @Value("${application.app-id}")
    private String appId;

    @Value("${application.mch-id}")
    private String mchId;

    @Value("${application.key}")
    private String key;

    @Value("${application.cert-path}")
    private String certPath;

    private byte[] certData;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() throws Exception {

        File file = new File(certPath);
        FileInputStream in = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(in);

        this.certData = new byte[(int) file.length()];
        bin.read(this.certData);

        bin.close();
        in.close();

    }


    @Override
    String getAppID() {
        return appId;
    }

    @Override
    String getMchID() {
        return mchId;
    }

    @Override
    String getKey() {
        return key;
    }

    @Override
    InputStream getCertStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(this.certData);
        return in;
    }

    @Override
    IWXPayDomain getWXPayDomain() {

        return new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(
                       WXPayConstants.DOMAIN_API, true
                );
            }
        };
    }
}
