package com.amaze.filemanager.filesystem.scanner;

/**
 * Created by onemax on 8/24/16.
 */
public interface Visitor {
    void visit(DirElement d);
    void visit(FileElement f);
}
