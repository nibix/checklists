/* 
 * Copyright 2024 Nils Bandener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Based on code which is:
 * 
 * Copyright 2022-2024 floragunn GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.selectivem.check;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CheckListTest {
    @Test
    public void checkIf() {
        CheckList<String> subject = CheckList.create(setOf("a", "b", "xxx", "c", "d"));
        subject.checkIf((e) -> e.length() > 1);
        Assert.assertEquals(setOf("xxx"), subject.getCheckedElements());
        subject.checkIf((e) -> e.length() <= 1);
        Assert.assertEquals(setOf("a", "b", "xxx", "c", "d"), subject.getCheckedElements());
    }

    @Test
    public void uncheckIf() {
        CheckList<String> subject = CheckList.create(setOf("a", "b", "xxx", "c", "d"));
        subject.checkAll();
        subject.uncheckIf((e) -> e.length() > 1);

        Assert.assertEquals(setOf("a", "b", "c", "d"), subject.getCheckedElements());
    }

    @Test
    public void checkIf_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "xxx"));
        subject.checkIf((e) -> e.length() > 1);
        Assert.assertEquals(setOf("xxx"), subject.getCheckedElements());
        subject.checkIf((e) -> e.length() <= 1);
        Assert.assertEquals(setOf("a", "xxx"), subject.getCheckedElements());
    }

    @Test
    public void uncheckIf_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "xxx"));
        subject.checkAll();
        subject.uncheckIf((e) -> e.length() > 1);

        Assert.assertEquals(setOf("a"), subject.getCheckedElements());
    }

    @Test
    public void check_unknown() {
        CheckList<String> subject = CheckList.create(setOf("a", "b", "c", "d"));

        try {
            subject.check("xxx");
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void check_unknown_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));

        try {
            subject.check("xxx");
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void uncheck_unknown_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));

        try {
            subject.uncheck("xxx");
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void uncheckIfPresent() {
        CheckList<String> subject = CheckList.create(setOf("a", "b", "c", "d"));
        subject.check("a");
        subject.check("b");
        subject.uncheckIfPresent("a");
        Assert.assertEquals(setOf("b"), subject.getCheckedElements());
        Assert.assertEquals(setOf("a", "c", "d"), subject.getUncheckedElements());
    }

    @Test
    public void uncheckIfPresent_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        subject.check("a");
        subject.check("b");
        subject.uncheckIfPresent("a");
        Assert.assertEquals(setOf("b"), subject.getCheckedElements());
        Assert.assertEquals(setOf("a"), subject.getUncheckedElements());
    }

    @Test
    public void uncheckIfPresent_unknown() {
        CheckList<String> subject = CheckList.create(setOf("a", "b", "c", "d"));
        subject.check("a");
        subject.uncheckIfPresent("xxx");
        Assert.assertEquals(setOf("a"), subject.getCheckedElements());
        Assert.assertEquals(setOf("b", "c", "d"), subject.getUncheckedElements());
    }

    @Test
    public void uncheckIfPresent_unknown_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        subject.check("a");
        subject.uncheckIfPresent("xxx");
        Assert.assertEquals(setOf("a"), subject.getCheckedElements());
        Assert.assertEquals(setOf("b"), subject.getUncheckedElements());
    }

    @Test
    public void isBlank_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        Assert.assertTrue(subject.isBlank());
        subject.check("a");
        Assert.assertFalse(subject.isBlank());
    }

    @Test
    public void isComplete_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        Assert.assertFalse(subject.isComplete());
        Assert.assertFalse(subject.check("a"));
        Assert.assertTrue(subject.check("b"));
        Assert.assertTrue(subject.isComplete());
    }

    @Test
    public void isChecked_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        Assert.assertFalse(subject.isChecked("a"));
        Assert.assertFalse(subject.isChecked("b"));
        subject.check("a");
        Assert.assertTrue(subject.isChecked("a"));
        Assert.assertFalse(subject.isChecked("b"));
        subject.check("b");
        Assert.assertTrue(subject.isChecked("a"));
        Assert.assertTrue(subject.isChecked("b"));
    }

    @Test
    public void isChecked_unknown_2e() {
        CheckList<String> subject = CheckList.create(setOf("a", "b"));
        try {
            subject.uncheck("xxx");
            Assert.fail();
        } catch (IllegalArgumentException e) {

        }
    }

    @SafeVarargs
    static <E> Set<E> setOf(E... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
