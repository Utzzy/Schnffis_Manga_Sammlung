package com.utzzy.schnffismangasammlung;

public class ISBNUpdateEvent {
    private String updatedISBN;

    public ISBNUpdateEvent(String updatedISBN) {
        this.updatedISBN = updatedISBN;
    }

    public String getUpdatedISBN() {
        return updatedISBN;
    }
}
