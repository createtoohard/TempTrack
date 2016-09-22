package com.cl.temptrack.temptrack;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by jiezhao on 16/9/21.
 */
public class Utils {
    private final static boolean DEBUG = true;
    private final static String TAG = "ChenLong";
    private static Process mProcess;

    public static final char COMMA = ',';
    public static final char COLON = ':';
//    public static final char SPACE = "'\'s*";

    /**
     * 拷贝文件*/
    public static void copyFile(Context context, String readFileName, String writeFileName) {
        saveFile(context, writeFileName, readFile(context, readFileName));
    }

    /**
     * 读取文件
     * @param context 上下文，用于获取 Context 的 FileInputStream() 方法
     * @param fileName 需要读取的文件名
     * */
    public static String readFile (Context context, String fileName) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            fis = context.openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            if (DEBUG) Log.e(TAG, "read file but file not foud " + e.getMessage());
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "read file but appear io exception " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (DEBUG) Log.e(TAG, "close input stream appear io exception " + e.getMessage());
                }
            }
        }
        return sb.toString();
    }

    /**
     * 保存文件
     * @param context 用于获取 Context 的 OpenFileOutput() 方法
     * @param fileName 文件名
     * @param content 写入的内容*/
    public static void saveFile (Context context, String fileName, String content) {
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(content);
        } catch (FileNotFoundException e) {
            if (DEBUG) Log.e(TAG, "save file but file not foud " + e.getMessage());
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "save file but appear io exception " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    if (DEBUG) Log.e(TAG, "close output stream appear io exception " + e.getMessage());
                }
            }
        }
    }

    /**
     * 读取文件
     * @param filePath 文件路径
     * @return 文件内容
     * */
    public static String readFile (File filePath) {
        FileReader fr = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();

        try {
            fr = new FileReader(filePath);
            reader = new BufferedReader(fr);

            String line = "";
            while ((line = reader.readLine()) != null) {
                if (DEBUG) Log.e(TAG, "READ CONTENT : " + line);
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    /**
     * 读取文件
     * @param fileName 文件名称（路径）
     * @return 文件内容
     * */
    public static String readFile (String fileName) {
        return readFile(new File(fileName));
    }

    /**
     * 保存文件
     * @param filePath 文件路径
     * @param content 写入的内容
     * */
    public static void saveFile (File filePath, String content) {
        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fw = new FileWriter(filePath);
            writer = new BufferedWriter(fw);
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveFile (String fileName, String content) {
        saveFile(new File(fileName), content);
    }


    /**
     * 判断 sdcard 是否存在且可用
     * */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void saveFileByCsv(String readFileName, String saveFileName, String splite) {
        FileReader fr = null;
        BufferedReader reader = null;

        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fr = new FileReader(readFileName);
            reader = new BufferedReader(fr);

            fw = new FileWriter(saveFileName, true);
            writer = new BufferedWriter(fw);

            String line = "";
            while ((line = reader.readLine()) != null) {
                if (DEBUG) Log.e(TAG, "READ CONTENT : " + line);
                writer.write(line.replace(splite, ",") + "\r\n");
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveCommandInfoByCsv(String command, String saveFileName) {
        Runtime runtime;
        DataOutputStream dos = null;
        BufferedReader reader = null;

        FileWriter fw = null;
        BufferedWriter writer = null;

        String line = "";
        runtime = Runtime.getRuntime();

        try {
            mProcess = runtime.exec("ps",null,new File("/"));

            dos = new DataOutputStream(mProcess.getOutputStream());
            dos.writeBytes(command + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));

            fw = new FileWriter(saveFileName, true);
            writer = new BufferedWriter(fw);

            while ((line = reader.readLine()) != null) {
                if (DEBUG) Log.e(TAG, "READ CONTENT: " + line);
                writer.write(line.replaceAll("\\s{1,}",",") + "\r\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

 }
