package com.alec.InnovateX;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // 文件根目录（固定为本地 Downloads 目录）
    private final String ROOT = "/Users/mandala/Downloads";
    private final String baseUrl;

    // 用于安全过滤
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    // 用于文件名过滤
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    public HttpFileServerHandler(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 检查请求是否解码成功
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // 只支持 GET 方法
        if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // 对 URI 进行解码
        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 将请求路径映射到本地目录 ROOT 下
        File file = new File(ROOT, path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 如果请求的是目录
        if (file.isDirectory()) {
            // 如果 URI 以 '/' 结尾，则返回目录列表
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                // 否则重定向到以 '/' 结尾的 URI
                sendRedirect(ctx, uri + "/");
            }
            return;
        }

        // 只允许访问普通文件
        if (!file.isFile()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (Exception e) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = raf.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpUtil.setContentLength(response, fileLength);
        // 设置简单的 Content-Type，这里默认使用 application/octet-stream
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        ctx.write(response);

        // 使用 DefaultFileRegion 进行零拷贝文件传输
        ChannelFuture sendFileFuture;
        sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
        // 写入 LastHttpContent 以标识响应结束
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                // 这里可以添加文件传输进度处理逻辑
            }
            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                // 传输完成后关闭文件
                raf.close();
            }
        });
    }

    /**
     * 对 URI 进行解码、过滤，防止安全问题
     */
    private String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                uri = URLDecoder.decode(uri, StandardCharsets.ISO_8859_1);
            } catch (Exception e1) {
                return null;
            }
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        // 将 '/' 替换为当前系统的文件分隔符
        uri = uri.replace('/', File.separatorChar);
        // 进行安全检查
        if (uri.contains(File.separator + ".") || uri.contains("." + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        return uri;
    }

    /**
     * 生成目录列表页面
     */
    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><meta charset='utf-8'><title>目录列表: ");
        buf.append(dirPath);
        buf.append("</title></head><body>\r\n");

        buf.append("<h3>目录列表: ");
        buf.append(dirPath);
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li><a href=\"../\">..</a></li>\r\n");

        for (File f: Objects.requireNonNull(dir.listFiles())) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li><a href=\"");
            buf.append(name);
            if (f.isDirectory()) {
                buf.append("/");
            }
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");

        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        FullHttpResponse response =
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpUtil.setContentLength(response, buffer.readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送重定向响应
     */
    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response =
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送错误响应
     */
    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf buf = Unpooled.copiedBuffer("错误: " + status.toString() + "\r\n", CharsetUtil.UTF_8);
        FullHttpResponse response =
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, buf.readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
