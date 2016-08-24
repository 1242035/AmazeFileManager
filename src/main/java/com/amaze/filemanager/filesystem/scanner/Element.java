package com.amaze.filemanager.filesystem.scanner;

import java.io.File;

/**
 * Created by onemax on 8/24/16.
 */
public abstract class Element {
    protected File rootPath;
    abstract void accept(Visitor v);

    @Override
    public String toString() {
        return rootPath.getAbsolutePath();
    }
}
