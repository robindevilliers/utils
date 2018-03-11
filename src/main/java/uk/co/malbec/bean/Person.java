package uk.co.malbec.bean;

import java.util.List;

public class Person {
    private String firstName;
    private String lastName;
    private Address address;
    private int age;
    private List<Person> children;

    public Person(){

    }

    public Person(String firstName, String lastName, Address address, int age, List<Person> children) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.age = age;
        this.children = children;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }

    public int getAge(){
        return age;
    }

    public List<Person> getChildren() {
        return children;
    }
}
