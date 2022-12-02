package com.dian.sixggroup.service;

import com.dian.sixggroup.common.util.SocketClient;
import com.dian.sixggroup.common.util.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: tanjun
 * @CreateTime: 2022-11-21 21:42
 */
@Service
@Slf4j
public class BaseService {
    @Autowired
    private Upload upload;
    public String resImage(MultipartFile file){
        long t1 = System.currentTimeMillis();
        String imgPath = upload.upload(file);
        long t2 = System.currentTimeMillis();
        log.info("upload image cost [{}]ms",t2-t1);
        String s = SocketClient.remoteCallByNettyChannel(imgPath);
        long t3 = System.currentTimeMillis();
        log.info("remote call cost [{}]ms",t3-t2);
        return s;
    }

}
