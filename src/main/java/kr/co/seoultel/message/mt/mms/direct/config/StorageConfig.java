package kr.co.seoultel.message.mt.mms.direct.config;

import com.google.gson.reflect.TypeToken;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.fileIoHandler.CsvFileIOHandler;
import kr.co.seoultel.message.mt.mms.core_module.utils.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class StorageConfig {

    @Bean
    public HashMapStorage<String, String> fileStorage(@Value("${file-server.image-download-path}") String rootFilePath) throws IOException {
        HashMapStorage<String, String> fileStorage = new HashMapStorage<String, String>(rootFilePath);

        fileStorage.createFileAnd().getSubDirectoryPaths().ifPresent((subDirectoryPaths) -> {
            for (Path groupCodeDirectoryPath : subDirectoryPaths) {
                String groupCode = String.valueOf(groupCodeDirectoryPath.getFileName());
                Arrays.stream(Objects.requireNonNullElse(groupCodeDirectoryPath.toFile().listFiles(), new File[0]))
                      .filter((file) -> ImageUtil.isUsableImageFile(file.getName()))
                      .forEach((file) -> {
                          String fileName = file.getName();
                          String absoluteFilePath = file.getAbsolutePath();

                          String imageName = ImageUtil.excludeExtension(fileName);
                          String imageKey = ImageUtil.getImageKey(groupCode, imageName);

                          fileStorage.put(imageKey, absoluteFilePath);
                      });
            }
        });

        fileStorage.setSynchronized(false);
        return fileStorage;
    }

    @Bean
    public HashMapStorage<String, MessageDelivery> deliveryStorage(@Value("${sender.storage.persistence.file-path}") String filePath) throws IOException {
        HashMapStorage<String, MessageDelivery> deliveryStorage = new HashMapStorage<String, MessageDelivery>(filePath);
        Type type = new TypeToken<Collection<MessageDelivery>>(){}.getType();
        deliveryStorage.createFileAnd().readFileAsCollection(type).ifPresent(collection -> {
                collection.forEach(messageDelivery -> {
                    deliveryStorage.put(messageDelivery.getDstMsgId(), messageDelivery);
                });
            }
        );

        return (HashMapStorage<String, MessageDelivery>) deliveryStorage.destroyBy(deliveryStorage::values);
    }

    @Bean
    public HashMapStorage<String, MessageHistory> historyStorage(@Value("${sender.storage.history.file-path}") String filePath) throws IOException {
        HashMapStorage<String, MessageHistory> historyStorage = new HashMapStorage<String, MessageHistory>(filePath);

        Type type = new TypeToken<Collection<MessageHistory>>(){}.getType();
        historyStorage.createFileAnd().readFileAsCollection(type).ifPresent(collection ->
                collection.forEach(messageHistory -> historyStorage.put(messageHistory.getMessageId(), messageHistory))
        );

        return (HashMapStorage<String, MessageHistory>) historyStorage.destroyBy(historyStorage::values);
    }

    @Bean
    public QueueStorage<MessageDelivery> republishQueueStorage(@Value("${sender.storage.republish.file-path}") String filePath) throws IOException {
        QueueStorage<MessageDelivery> queueStorage = new QueueStorage<MessageDelivery>(filePath);
        Type type = new TypeToken<Collection<MessageDelivery>>(){}.getType();
        queueStorage.createFileAnd().readFileAsCollection(type).ifPresent(queueStorage::addAll);

        return queueStorage;
    }

    @Bean
    public QueueStorage<MrReport> reportQueueStorage(@Value("${sender.storage.report.file-path}") String filePath) throws IOException {
        QueueStorage<MrReport> queueStorage = new QueueStorage<MrReport>(filePath);
        Type type = new TypeToken<Collection<MrReport>>(){}.getType();
        queueStorage.createFileAnd().readFileAsCollection(type).ifPresent((collection) -> {
            queueStorage.addAll(collection);
        });

        return queueStorage;
    }
}
