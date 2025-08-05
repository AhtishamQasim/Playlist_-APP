package com.web.playlist;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String playlistUrl = request.getParameter("playlistUrl");

        if (playlistUrl == null || playlistUrl.trim().isEmpty()) {
            sendJsonError(response, "No playlist URL provided.");
            return;
        }

        // yt-dlp path
        String ytDlpPath = getServletContext().getRealPath("/yt-dlp.exe");

        // Extract playlist ID for folder name (fallback)
        String playlistId = extractPlaylistId(playlistUrl);
        if (playlistId == null) playlistId = "playlist_" + System.currentTimeMillis();

        // Create a folder for this playlist
        String baseOutputFolder = "E:/DownloadedVideos/";
        String playlistFolder = baseOutputFolder + playlistId + "/";
        new File(playlistFolder).mkdirs();

        // Output template
        
        String outputTemplate = playlistFolder + "%(playlist_index)s - %(title)s.%(ext)s";

        List<String> command = Arrays.asList(
                ytDlpPath,
                "--yes-playlist",
                "--ignore-errors",
                "--no-call-home",
                "--no-warnings",
                "--no-part",
                "--no-overwrites",
                "-f", "best[height<=360]",
                "-o", outputTemplate,
                playlistUrl
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        Set<String> titles = new LinkedHashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("Destination:") || line.contains("Resuming download at")) {
                    int colonIndex = line.indexOf(":");
                    if (colonIndex > -1) {
                        String filePath = line.substring(colonIndex + 1).trim();
                        File f = new File(filePath);
                        String name = f.getName().replaceFirst("\\.[^.]+$", "");
                        titles.add(name);
                    }
                }
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            sendJsonError(response, "Download interrupted.");
            return;
        }

        // Build JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print("{\"success\":true, \"message\":\"Download completed to folder: " + escapeJson(playlistFolder) + "\", \"titles\":[");
        int count = 0;
        for (String title : titles) {
            out.print("\"" + escapeJson(title) + "\"");
            if (++count < titles.size()) out.print(",");
        }
        out.print("]}");
        out.flush();
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String extractPlaylistId(String url) {
        int index = url.indexOf("list=");
        if (index != -1) {
            String id = url.substring(index + 5);
            int end = id.indexOf("&");
            return end != -1 ? id.substring(0, end) : id;
        }
        return null;
    }
}
