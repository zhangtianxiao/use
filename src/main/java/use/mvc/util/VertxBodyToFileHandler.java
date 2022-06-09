package use.mvc.util;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.RoutingContextInternal;
import use.mvc.parabind.RequestBody;

import java.io.File;
import java.util.UUID;

public class VertxBodyToFileHandler implements BodyHandler {
  private boolean isPreallocateBodyBuffer = DEFAULT_PREALLOCATE_BODY_BUFFER;
  private String uploadsDir = DEFAULT_UPLOADS_DIRECTORY;

  final long bodyLimit;

  public VertxBodyToFileHandler(long bodyLimit) {
    this.bodyLimit = bodyLimit;
  }

  @Override
  public BodyHandler setHandleFileUploads(boolean handleFileUploads) {
    return null;
  }

  @Override
  public BodyHandler setBodyLimit(long bodyLimit) {
    return null;
  }

  @Override
  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
    uploadsDir = uploadsDirectory;
    return this;
  }

  @Override
  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
    return null;
  }

  @Override
  public BodyHandler setDeleteUploadedFilesOnEnd(boolean deleteUploadedFilesOnEnd) {
    return null;
  }

  @Override
  public BodyHandler setPreallocateBodyBuffer(boolean isPreallocateBodyBuffer) {
    return null;
  }

  static class BHandler implements Handler<Buffer> {
    boolean ended;
    boolean failed;
    int uploadSize;

    final RoutingContext context;
    final long contentLength;
    final long bodyLimit;
    final AsyncFile asyncFile;
    final File file;

    public BHandler(RoutingContext context, long bodyLimit, long contentLength, AsyncFile asyncFile, File file) {
      this.context = context;
      this.bodyLimit = bodyLimit;
      this.contentLength = contentLength;
      this.asyncFile = asyncFile;
      this.file = file;
      // the request clearly states that there should
      // be a body, so we respect the client and ensure
      // that the body will not be null
      if (contentLength != -1) {
        initBodyBuffer();
      }
    }

    private void initBodyBuffer() {
    }

    private void cancelAndCleanupFileUploads() {
    }


    void end() {
      ended = true;
      if (failed) {
        return;
      }
      asyncFile.close();
      context.put(RequestBody.AS_FILE, file);
      //context.put(RequestBody.AS_ASYNC_FILE, asyncFile);
      context.next();
    }

    @Override
    public void handle(Buffer buff) {
      if (failed) {
        return;
      }
      uploadSize += buff.length();
      if (bodyLimit != -1 && uploadSize > bodyLimit) {
        failed = true;
        cancelAndCleanupFileUploads();
        context.fail(413);
      }
      else {
        asyncFile.write(buff);
        // multipart requests will not end up in the request body
        // url encoded should also not, however jQuery by default
        // post in urlencoded even if the payload is something else
        // if (!isMultipart /* && !isUrlEncoded */)
      }
    }
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.headers().contains(HttpHeaders.UPGRADE, HttpHeaders.WEBSOCKET, true)) {
      context.next();
      return;
    }
    // we need to keep state since we can be called again on reroute
    if (!((RoutingContextInternal) context).seenHandler(RoutingContextInternal.BODY_HANDLER)) {
      long contentLength = isPreallocateBodyBuffer ? parseContentLengthHeader(request) : -1;
      File file = new File(uploadsDir, UUID.randomUUID().toString());
      String uploadedFileName = file.getPath();
      FileSystem fileSystem = context.vertx().fileSystem();
      Future<AsyncFile> fu = fileSystem.open(uploadedFileName, new OpenOptions());
      fu.onFailure(e -> {
        context.response().setStatusCode(413).end();
      });
      request.pause();
      fu.onSuccess(asyncFile -> {
        request.resume();
        BHandler handler = new BHandler(context, bodyLimit, contentLength, asyncFile, file);
        request.handler(handler);
        request.endHandler(v -> handler.end());
        ((RoutingContextInternal) context).visitHandler(RoutingContextInternal.BODY_HANDLER);
      });
    }
    else {
      context.next();
    }
  }

  private long parseContentLengthHeader(HttpServerRequest request) {
    String contentLength = request.getHeader(HttpHeaders.CONTENT_LENGTH);
    if (contentLength == null || contentLength.isEmpty()) {
      return -1;
    }
    try {
      long parsedContentLength = Long.parseLong(contentLength);
      return parsedContentLength < 0 ? -1 : parsedContentLength;
    } catch (NumberFormatException ex) {
      return -1;
    }
  }

}
