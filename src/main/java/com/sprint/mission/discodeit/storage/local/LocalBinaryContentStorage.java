package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.mapstruct.ap.shaded.freemarker.core.ReturnInstruction.Return;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@Component
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value(".discodeit/storage") Path root
  ) {
    this.root = root;
  }

  public void init() {
    if (!Files.exists(root)) {
      try {
        Files.createDirectory(root);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    Path path = resolvePath(binaryContentId);

    if (Files.exists(path)) {
      throw new RuntimeException("File already exists with path: " + path);
    }

    try (OutputStream outputStream = Files.newOutputStream(path)) {
      outputStream.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    return binaryContentId;
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    Path path = resolvePath(binaryContentId);
    if (!Files.exists(path)) {
      throw new RuntimeException("File does not exist with path: " + path);
    }

    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto metaData) {
    InputStream input = get(metaData.id());
    Resource resource = new InputStreamResource(input);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header("Content-Disposition", "attachment; filename=\"" + metaData.fileName() + "\"")
        .header("Content-Type", metaData.contentType())
        .header("Content-Length", String.valueOf(metaData.size()))
        .body(resource);
  }
}
