package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.lyException;
import com.leyou.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

//    public static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");

    public String uploadImg(MultipartFile file) {
        try {
        //        对上传的文件进行格式的验证
        //        如果文件上传的格式不是我们约定的类型
            String contentType = file.getContentType();
            if( !prop.getAllowType().contains(contentType) ){
            throw  new lyException(ExceptionEnum.UPLOAD_FILE_ERROR);
          }
        //        对上传的文件内容的验证
            BufferedImage image = ImageIO.read(file.getInputStream());
        if(image == null){
            throw  new lyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

////        1.保存图片到本地
//            File dest = new File("D:\\BaiduNetdiskDownload\\黑马优乐商城\\Img",file.getOriginalFilename());
//            file.transferTo(dest);
//            //        2.返回路径
//            return "http://images.leyou.img.com/" + file.getOriginalFilename();
//            上传到fdfs
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            //        2.返回路径
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
           log.error("上传文件失败！",e);
           throw  new lyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }

    }
}
