package com.amaze.filemanager.filesystem.scanner;

/**
 * Created by onemax on 8/24/16.
 */
public class ElementWalker {
    private final String rootDir;
    ElementWalker(final String dir) {
        rootDir = dir;
    }

    private void traverse() {
        Element d = new DirElement(rootDir);
        d.accept(new Walker());
    }

    public static void main(final String[] args) {
        ElementWalker t = new ElementWalker("C:\\temp");
        t.traverse();
    }

    private class Walker implements Visitor {
        public void visit(final DirElement d) {
            System.out.println(d);
            for(Element e:d) {
                e.accept(this);
            }
        }

        public void visit(final FileElement f) {
            System.out.println(f);
        }
    }
}
