package com.lame.mp3.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * created by wangguoqun at 2019-07-13
 */
public class PcmFileToMp3 {

    private String pcmFileName;

    private String mp3FileName;

    public PcmFileToMp3(String pcmFileName, String mp3FileName) {
        this.pcmFileName = pcmFileName;
        this.mp3FileName = mp3FileName;
    }

    private void convertToMp3() {
        FileInputStream fis;
        DataOutputStream dos;

        try {
            File pcmFile = new File(pcmFileName);
            fis = new FileInputStream(pcmFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) pcmFile.length());
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while ((len = bis.read(buffer, 0, buf_size)) != -1) {
                bos.write(buffer, 0, len);
            }
            byte[] bytes = bos.toByteArray();


            dos = new DataOutputStream(new FileOutputStream(mp3FileName));

            while (fis.read() != -1) {

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
