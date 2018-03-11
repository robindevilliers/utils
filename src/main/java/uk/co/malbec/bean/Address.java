package uk.co.malbec.bean;

public class Address {
    private String firstLine;
    private String postCode;

    public Address() {
    }

    public Address(String firstLine, String postCode) {
        this.firstLine = firstLine;
        this.postCode = postCode;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public String getPostCode() {
        return postCode;
    }
}
