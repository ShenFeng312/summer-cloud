package org.summer.cloud.config.client;

import org.summer.cloud.config.SummerCloudConfigProperties;

/**
 * @author shenfeng
 */
public class SummerCloudConfigClientFactory {
    private static volatile SummerCloudConfigClient CLIENT;

    public static SummerCloudConfigClient build(SummerCloudConfigProperties summerCloudConfigProperties) {
        if (CLIENT == null) {
            synchronized (SummerCloudConfigClientFactory.class) {
                if(CLIENT == null){
                    CLIENT = new SummerCloudConfigClient(summerCloudConfigProperties);
                }

            }
        }
        return CLIENT;
    }

    public static SummerCloudConfigClient get(){
        return CLIENT;
    }

}
