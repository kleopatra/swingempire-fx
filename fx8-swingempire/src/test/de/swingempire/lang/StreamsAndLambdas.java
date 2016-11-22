/*
 * Created on 24.08.2016
 *
 */
package de.swingempire.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.lang.StreamsAndLambdas.Bar;
import de.swingempire.lang.StreamsAndLambdas.Foo;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class StreamsAndLambdas {

    @Test
    public void testFlatMapAllInOneWithNew() {
        IntStream.range(1, 4)
            .mapToObj(i -> "Foo" + i)
            .map(Foo::new)
            .peek(f -> IntStream.range(1, 4)
                .mapToObj(i -> "Bar" + i + " <- " + f.name)    
                .map(Bar::new)
                .forEach(f.bars::add))
            .flatMap(f -> f.bars.stream())
            .forEach(b -> System.out.println(b.name));
    }
    
    @Test
    public void testFlatMapAllInOne() {
        IntStream
            .range(1, 4)
            .mapToObj(i -> new Foo("Foo" + i))
            .peek(f -> IntStream.range(1, 4)
                    .mapToObj(i -> new Bar("Bar" + i + " <- " + f.name))
                    .forEach(f.bars::add))
            .flatMap(f -> f.bars.stream())
                .forEach(b -> System.out.println(b.name));
    }

    @Test
    public void testFlatMapWithList() {
        List<Foo> foos = new ArrayList<>();

        // create foos
        IntStream
            .range(1, 4)
            .forEach(i -> foos.add(new Foo("Foo" + i)));

        // create bars
        foos.forEach(f -> IntStream
                .range(1, 4)
                .forEach(i -> f.bars.add(new Bar("Bar" + i + " <- " + f.name))));

        foos.stream()
            .flatMap(f -> f.bars.stream())
            .forEach(b -> System.out.println(b.name));

    }
    
    public static class Foo {
        String name;
        List<Bar> bars = new ArrayList<>();

        Foo(String name) {
            this.name = name;
        }
    }

    public static class Bar {
        String name;

        Bar(String name) {
            this.name = name;
        }
    }


}
