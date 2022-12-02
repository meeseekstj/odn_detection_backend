package com.dian.sixggroup.contoller;

import com.dian.sixggroup.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @Author: tanjun
 * @CreateTime: 2022-11-21 21:36
 */
@RestController
@RequestMapping("/api/detection")
public class BaseController {

    @Autowired
    private BaseService baseService;

    @PostMapping(value = "/resImage", produces = {MediaType.IMAGE_JPEG_VALUE})
    public BufferedImage resImage(@RequestPart MultipartFile file) throws IOException {
        String absResPath = baseService.resImage(file);
        return ImageIO.read(Files.newInputStream(new File(absResPath).toPath()));
    }
}
