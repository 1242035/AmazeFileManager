package com.amaze.filemanager.filesystem;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import com.amaze.filemanager.Constant;
import com.amaze.filemanager.utils.Futils;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
/**
 * Created by Arpit on 07-07-2015.
 */
//Hybrid file for handeling all types of files
public class HFile {
    String path;
    public static final int ROOT_MODE=3,LOCAL_MODE=0,UNKNOWN=-1;
    int mode=0;
    public HFile(int mode, String path) {
        this.path = path;
        this.mode = mode;
    }

    public HFile(int mode,String path, String name,boolean isDirectory) {
        this.mode = mode;
        this.path = path + "/" + name;
    }
    public void generateMode(Context context){

        if(context==null){
            mode=LOCAL_MODE;
            return;
        }
        boolean rootmode=PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constant.ROOT_MODE,false);
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)
        {
            mode=LOCAL_MODE;
            if( rootmode ) {
                if(!getFile().canRead()) {
                    mode=ROOT_MODE;
                }
            }
            return;
        }
        if(FileUtil.isOnExtSdCard(getFile(),context)){
            mode=LOCAL_MODE;
        }
        else if(rootmode){
            if(!getFile().canRead()){
                mode=ROOT_MODE;
            }
        }
        if(mode==UNKNOWN){
            mode=LOCAL_MODE;
        }
    }
    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isLocal(){
        return mode==LOCAL_MODE;
    }
    public boolean isRoot(){
        return mode==ROOT_MODE;
    }
    File getFile(){return new File(path);}
    BaseFile generateBaseFileFromParent(){
        ArrayList<BaseFile> arrayList= RootHelper.getFilesList(getFile().getParent(),true,true,null);
        for(BaseFile baseFile:arrayList){
            if(baseFile.getPath().equals(path)) {
                return baseFile;
            }
        }
        return null;
    }
    public long lastModified() throws MalformedURLException {
        switch (mode){
            case LOCAL_MODE:
                new File(path).lastModified();
                break;
            case ROOT_MODE:
                BaseFile baseFile=generateBaseFileFromParent();
                if(baseFile!=null) {
                    return baseFile.getDate();
                }
        }
        return new File("/").lastModified();
    }
    public long length() {
        long s = 0l;
        switch (mode){
            case LOCAL_MODE:
                s = new File(path).length();
                return s;
            case ROOT_MODE:
                BaseFile baseFile=generateBaseFileFromParent();
                if(baseFile!=null) {
                    return baseFile.getSize();
                }
        }
        return s;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        String name = null;
        switch (mode){
            case LOCAL_MODE:
                return new File(path).getName();
            case ROOT_MODE:
                return new File(path).getName();
        }
        return name;
    }
    public boolean isCustomPath(){
        if(path.equals("0") ||
                path.equals("1") ||
                path.equals("2") ||
                path.equals("3") ||
                path.equals("5") ||
                path.equals("6") ||
                path.equals("4")) {
            return true;
        }
        return false;
    }
    public String getParent() {
        return new File(path).getParent();
    }
    public boolean isDirectory() {
        boolean isDirectory = false;
        if(isLocal()){
            isDirectory = new File(path).isDirectory();
        }
        else if(isRoot()){
            isDirectory=RootHelper.isDirectory(path,true,5);
        }
        return isDirectory;

    }

    public long folderSize() {
        long size = 0l;
        size = new Futils().folderSize(new File(path));
        return size;
    }

    public long getUsableSpace() {
        long size = 0l;
        size = (new File(path).getUsableSpace());
        return size;
    }

    public ArrayList<BaseFile> listFiles(boolean rootmode) {
        ArrayList<BaseFile> arrayList =null;

        arrayList = RootHelper.getFilesList(path, rootmode, true,null);

        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        return arrayList;
    }
    public String getReadablePath(String path){
        return path;
    }

    public InputStream getInputStream() {
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            inputStream = null;
            e.printStackTrace();
        }
        return inputStream;
    }

    public OutputStream getOutputStream(Context context) {
        OutputStream inputStream = null;
        try {
            inputStream = FileUtil.getOutputStream(new File(path), context, length());
        } catch (Exception e) {
            inputStream=null;
        }
        return inputStream;
    }

    public boolean exists() {
        boolean exists = false;
       if(isLocal())  {
           exists = new File(path).exists();
       }
       else if(isRoot()){
           return RootHelper.fileExists(path);
       }
       return exists;
    }
    public boolean isSimpleFile(){
        if(!isCustomPath() && !android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches()){
            if(!new File(path).isDirectory())return true;
        }
        return false;
    }
    public boolean setLastModified(long date){
        File f=new File(path);
        return f.setLastModified(date);

    }
    public void mkdir(Context context) {

        FileUtil.mkdir(new File(path), context);
    }
    public boolean delete(Context context,boolean rootmode){
        boolean b= FileUtil.deleteFile(new File(path), context);
        if(!b && rootmode){
            setMode(ROOT_MODE);
            RootTools.remount(getParent(),"rw");
            String s=RootHelper.runAndWait("rm -r \""+getPath()+"\"",true);
            RootTools.remount(getParent(),"ro");
        }
        return !exists();
    }
}
