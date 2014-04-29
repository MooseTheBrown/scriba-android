package org.scribacrm.scriba;

public enum EntryType {
    COMPANY(1),
    EVENT(2),
    POC(3),
    PROJECT(4);

    private final int _loaderId;

    EntryType(int loaderId) {
        _loaderId = loaderId;
    }

    public int loaderId() {
        return _loaderId;
    }

    public int id() {
        return _loaderId;
    }
}
