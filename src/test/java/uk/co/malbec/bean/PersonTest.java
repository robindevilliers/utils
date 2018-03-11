package uk.co.malbec.bean;

import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.co.malbec.bean.BeanMatcher.isBean;

public class PersonTest {

    @Test
    public void testPerson() {

        Address address = new Address("38 Egremont", "E201BF");

        Person person = new Person("Robin", "de Villiers", address, 41, asList(
                new Person("Connor", "de Villiers", address, 4, null),
                new Person("Leah", "de Villiers", address, 2, null)
        ));

        assertThat(person, isBean(Person.class)
                .with(Person::getFirstName, is("Robin"))
                .with(Person::getAddress,
                        isBean(Address.class)
                                .with(Address::getFirstLine, is("38 Egremont"))
                                .with(Address::getPostCode, is("E201BF"))
                )
                .with(Person::getAge, is(41))
                .with(Person::getChildren, hasItems(
                        isBean(Person.class)
                                .with(Person::getFirstName, is("Connor")),
                        isBean(Person.class)
                                .with(Person::getFirstName, is("Leah 2"))
                ))
        );
    }

}
