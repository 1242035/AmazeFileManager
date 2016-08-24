package com.amaze.filemanager.filesystem.scanner;

import java.io.File;

/**
 * Created by onemax on 8/24/16.
 */
public class FileElement extends Element {
    FileElement(final String path) {
        rootPath = new File(path);
    }

    @Override
    void accept(final Visitor v) {
        v.visit(this);
    }
}
