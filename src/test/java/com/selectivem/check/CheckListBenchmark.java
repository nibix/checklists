package com.selectivem.check;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class CheckListBenchmark {
    private static final Set<String> SET_2 = testSet(2);
    private static final Set<String> SET_2_EVEN = testSetEven(2);
    private static final Set<String> SET_20 = testSet(20);
    private static final Set<String> SET_20_EVEN = testSetEven(20);
    private static final Set<String> SET_100 = testSet(100);
    private static final Set<String> SET_100_EVEN = testSetEven(100);
    private static final Set<String> SET_200 = testSet(200);
    private static final Set<String> SET_200_EVEN = testSet(200);
    private static final Set<String> SET_500 = testSet(500);
    private static final Set<String> SET_500_EVEN = testSet(500);

    @Benchmark
    public Object twoElementCheckList_2() {
        CheckList<String> subject = CheckList.create(SET_2);

        for (String e : SET_2_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object arrayCheckList_2() {
        CheckList<String> subject = new CheckListImpl.ArrayCheckList<>(SET_2, "element");

        for (String e : SET_2_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object hashMapCheckList_2() {
        CheckList<String> subject = new CheckListImpl.HashMapCheckList<>(SET_2, "element");

        for (String e : SET_2_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object arrayCheckList_20() {
        CheckList<String> subject = new CheckListImpl.ArrayCheckList<>(SET_20, "element");

        for (String e : SET_20_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object hashMapCheckList_20() {
        CheckList<String> subject = new CheckListImpl.HashMapCheckList<>(SET_20, "element");

        for (String e : SET_20_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object arrayCheckList_100() {
        CheckList<String> subject = new CheckListImpl.ArrayCheckList<>(SET_100, "element");

        for (String e : SET_100_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object hashMapCheckList_100() {
        CheckList<String> subject = new CheckListImpl.HashMapCheckList<>(SET_100, "element");

        for (String e : SET_100_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object arrayCheckList_200() {
        CheckList<String> subject = new CheckListImpl.ArrayCheckList<>(SET_200, "element");

        for (String e : SET_200_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object hashMapCheckList_200() {
        CheckList<String> subject = new CheckListImpl.HashMapCheckList<>(SET_200, "element");

        for (String e : SET_200_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object arrayCheckList_500() {
        CheckList<String> subject = new CheckListImpl.ArrayCheckList<>(SET_500, "element");

        for (String e : SET_500_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    @Benchmark
    public Object hashMapCheckList_500() {
        CheckList<String> subject = new CheckListImpl.HashMapCheckList<>(SET_500, "element");

        for (String e : SET_500_EVEN) {
            subject.check(e);
        }

        for (String e : subject.iterateUncheckedElements()) {
            subject.check(e);
        }

        return subject;
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    static Set<String> testSet(int size) {
        HashSet<String> result = new HashSet<>();

        for (int i = 0; i < size; i++) {
            result.add("s_" + i);
        }

        return result;
    }

    static Set<String> testSetEven(int size) {
        HashSet<String> result = new HashSet<>();

        for (int i = 0; i < size; i+=2) {
            result.add("s_" + i);
        }

        return result;
    }
}
