package com.dian.sixggroup.util;

import com.dian.sixggroup.netty.DataBusConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Component
public class Upload {

    public static String upload(MultipartFile img) {
        SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        String dirName = f1.format(now);
        String basePath = DataBusConstant.BASE_PATH;
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

    public static String uploadFromBytes(byte[] img) {
        SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        String dirName = f1.format(now);
        String basePath = DataBusConstant.BASE_PATH;
        File dir = new File(basePath + dirName);
        // 检测是否存在目录
        if (!dir.exists()) dir.mkdirs();
        String fileName = UUID.randomUUID() + ".jpg";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(basePath + dirName + "/" + fileName);
            fos.write(img);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return dirName + "/" + fileName;
    }

}
