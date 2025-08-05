<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>YouTube Playlist Downloader (360p)</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 30px;
            background-color: #f4f4f4;
        }

        h2 {
            color: #333;
        }

        input, button {
            padding: 10px;
            margin: 10px 0;
            width: 400px;
            font-size: 14px;
        }

        button {
            cursor: pointer;
            background-color: #4CAF50;
            border: none;
            color: white;
            font-weight: bold;
        }

        button:hover {
            background-color: #45a049;
        }

        #status {
            margin-top: 20px;
            font-weight: bold;
            color: #333;
        }

        ul#videoList {
            margin-top: 15px;
            padding-left: 20px;
            background: #fff;
            border: 1px solid #ccc;
            padding: 10px;
            list-style-type: disc;
        }

        li {
            margin-bottom: 5px;
        }
    </style>
</head>
<body>

    <h2>YouTube Playlist Downloader (360p)</h2>

    <input type="text" id="playlistUrl" placeholder="Enter YouTube playlist URL">
    <br>
    <button onclick="startDownload()">Start Download</button>

    <div id="status">Status: Idle</div>

    <ul id="videoList"></ul>

    <script>
        function updateStatus(msg, color = "#333") {
            const statusDiv = document.getElementById("status");
            statusDiv.innerText = "Status: " + msg;
            statusDiv.style.color = color;
        }

        function displayTitles(titles) {
            const list = document.getElementById("videoList");
            list.innerHTML = "";
            if (titles.length === 0) {
                const li = document.createElement("li");
                li.textContent = "No videos downloaded.";
                list.appendChild(li);
            } else {
                titles.forEach(title => {
                    const li = document.createElement("li");
                    li.textContent = title;
                    list.appendChild(li);
                });
            }
        }

        function startDownload() {
            const url = document.getElementById("playlistUrl").value.trim();
            if (!url) {
                alert("Please enter a playlist URL.");
                return;
            }

            updateStatus("Downloading...", "#007bff");

            fetch("DownloadServlet", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: "playlistUrl=" + encodeURIComponent(url)
            })
            .then(res => res.json())
            .then(data => {
                if (data.error) {
                    updateStatus("Error: " + data.error, "#cc0000");
                    displayTitles([]);
                } else {
                    updateStatus("Download Complete", "#28a745");
                    displayTitles(data.titles);
                }
            })
            .catch(err => {
                console.error("Error:", err);
                updateStatus("Download failed ", "#cc0000");
            });
        }
    </script>

</body>
</html>
