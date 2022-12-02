package com.dian.sixggroup.service;

import com.dian.sixggroup.common.util.Client;
import com.dian.sixggroup.common.util.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: tanjun
 * @CreateTime: 2022-11-21 21:42
 */
@Service
public class BaseService {
    @Autowired
    private Upload upload;
    @Autowired
    private Client client;
    public String resImage(MultipartFile file){
        String imgPath = upload.upload(file);
        return client.remoteCall(imgPath);
    }

}
