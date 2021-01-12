package com.borasoft.naqcc.sprint.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPDownloader {

    FTPClient ftp = null;

    public FTPDownloader(String host, String user, String pwd) throws Exception {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
    }

    public void downloadFile(String remoteFilePath, String localFilePath) {
        try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
            this.ftp.retrieveFile(remoteFilePath, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException f) {
                // do nothing as file is already downloaded from FTP server
            }
        }
    }

    public static void main(String[] args) {
        try {
            FTPDownloader ftpDownloader = new FTPDownloader("ftp.journaldev.com", "ftpUser", "ftpPassword");
            ftpDownloader.downloadFile("/mailforms/sprintlog.txt", "/Users/kihupboo/naqcc/mailforms/sprintlog.txt");
            ftpDownloader.downloadFile("/mailforms/sprintlog_va3pen.txt", "/Users/kihupboo/naqcc/mailforms/sprintlog_va3pen.txt");
            ftpDownloader.downloadFile("/mailforms/sprintlog_closed.txt", "/Users/kihupboo/naqcc/mailforms/sprintlog_closed.txt");
            System.out.println("FTP File downloaded successfully");
            ftpDownloader.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}