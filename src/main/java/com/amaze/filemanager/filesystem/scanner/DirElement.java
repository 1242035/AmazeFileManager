package com.amaze.filemanager.filesystem.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by onemax on 8/24/16.
 */
public class DirElement extends Element implements Iterable<Element> {
    private final List<Element> elemList;
    DirElement(final String path) {
        elemList = new ArrayList<Element>();
        rootPath = new File(path);
        for (File f : rootPath.listFiles()) {
            if (f.isDirectory()) {
                elemList.add(new DirElement(f.getAbsolutePath()));
            } else if (f.isFile()) {
                elemList.add(new FileElement(f.getAbsolutePath()));
            }
        }
    }

    @Override
    void accept(final Visitor v) {
        v.visit(this);
    }

    public Iterator<Element> iterator() {
        return elemList.iterator();
    }
}
