package kr.co.seoultel.message.mt.mms.direct.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/* FileServer 관련 상수 관리 // TOKEN, IMAGE-REQUEST-URI, IMAGE-DOWNLOAD_URI */
@Slf4j
@Setter
@Configuration
public class FileServerConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultFileServerConfig {

    @Value("${file-server.token}")
    public void setToken(String token) {
        TOKEN = token;
    }

    @Value("${file-server.image-request-uri}")
    public void setImageRequestUri(String imageRequestUri) {
        IMAGE_REQUEST_URI = imageRequestUri;
    }

    @Value("${file-server.image-download-path}")
    public void setImageDownloadPath(String imageDownloadPath) {
        IMAGE_DOWNLOAD_PATH = imageDownloadPath;
    }
}
