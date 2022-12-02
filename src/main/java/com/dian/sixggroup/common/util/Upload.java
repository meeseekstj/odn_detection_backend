package com.dian.sixggroup.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Component
public class Upload {

    @Value("${upload.basePath}")
    private String basePath;

    public String upload(MultipartFile img) {
        SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        String dirName = f1.format(now);
        File dir = new File(basePath + dirName);
        // 检测是否存在目录
        if (!dir.exists()) dir.mkdirs();
        String fileName = UUID.randomUUID() + ".jpg";
        try {
            File dest = new File(basePath + dirName + "/" + fileName);
            img.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirName + "/" + fileName;
    }

}