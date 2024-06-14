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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class CheckTableTest {

    @Test
    public void basic() {
        Set<String> rows = setOf("esb-prod-1", "esb-prod-2", "esb-prod-3", "esb-prod-4", "esb-prod-5", ".monitoring-es-7-2021.12.30", ".async-search",
                "humanresources", "finance", "suggest", "nested", "logs", "auditlog-2021.12.30");

        Set<String> columns = setOf("indices:admin/settings/update");

        CheckTable<String, String> checkTable = CheckTable.create(rows, columns);

        checkTable.checkIf(rows, (action) -> true);

        Assert.assertTrue(checkTable.toString(), checkTable.isComplete());
    }

    @Test
    public void uncheckAll() {
        Set<String> rows = setOf("a1", "a2", "a3", "b1", "b2", "b3");
        Set<Integer> columns = setOf(1, 2, 3, 4, 5, 6);
        CheckTable<String, Integer> root = CheckTable.create(rows, columns);
        root.checkIf(rows, (i) -> true);
        Assert.assertTrue(root.isComplete());
        root.uncheckAll();
        Assert.assertTrue(root.isBlank());
    }

    @Test
    public void checkIf() {
        Set<String> rows = setOf("a1", "a2", "a3", "b1", "b2", "b3");
        Set<Integer> columns = setOf(1, 2, 3, 4, 5, 6);
        CheckTable<String, Integer> root = CheckTable.create(rows, columns);
        root.checkIf("a1", (i) -> i >= 4);
        Assert.assertTrue(root.isChecked("a1", 4));
        Assert.assertFalse(root.isChecked("a1", 3));
        Assert.assertFalse(root.isChecked("a2", 4));

        root.checkIf((r) -> r.equals("a1"), 3);
        Assert.assertTrue(root.isChecked("a1", 3));
        Assert.assertFalse(root.isChecked("a2", 3));
    }

    @Test
    public void uncheckIf() {
        Set<String> rows = setOf("a1", "a2", "a3", "b1", "b2", "b3");
        Set<Integer> columns = setOf(1, 2, 3, 4, 5, 6);
        CheckTable<String, Integer> root = CheckTable.create(rows, columns);
        root.checkIf(rows, (i) -> true);
        root.uncheckIf("a1", (i) -> i >= 4);
        Assert.assertFalse(root.isChecked("a1", 4));
        Assert.assertTrue(root.isChecked("a1", 3));
        Assert.assertTrue(root.isChecked("a2", 4));

        root.uncheckIf((r) -> r.equals("a1"), 3);
        Assert.assertFalse(root.isChecked("a1", 3));
        Assert.assertTrue(root.isChecked("a2", 3));
    }
    
    @Test
    public void uncheckIf_iterable() {
        Set<String> rows = setOf("a1", "a2", "a3", "b1", "b2", "b3");
        Set<Integer> columns = setOf(1, 2, 3, 4, 5, 6);
        CheckTable<String, Integer> root = CheckTable.create(rows, columns);
        root.checkIf(rows, (i) -> true);
        root.uncheckIf(setOf("a1", "a3", "b1"), (i) -> i >= 4);
        Assert.assertFalse(root.isChecked("a1", 4));
        Assert.assertTrue(root.isChecked("a1", 3));
        Assert.assertTrue(root.isChecked("a2", 4));
        Assert.assertFalse(root.isChecked("a3", 4));
        Assert.assertTrue(root.isChecked("a3", 3));
        Assert.assertFalse(root.isChecked("b1", 4));
        
        root.uncheckIf((r) -> r.equals("a1"), setOf(3, 4, 5));
        Assert.assertFalse(root.isChecked("a1", 3));
        Assert.assertFalse(root.isChecked("a1", 4));
        Assert.assertFalse(root.isChecked("a1", 5));
        Assert.assertTrue(root.isChecked("a2", 3));
        Assert.assertTrue(root.isChecked("a1", 1));
    }

    @Test
    public void uncheckRowIf() {
        Set<String> rows = setOf("a1", "a2", "a3", "b1", "b2", "b3");
        Set<Integer> columns = setOf(1, 2, 3, 4, 5, 6);
        CheckTable<String, Integer> root = CheckTable.create(rows, columns);
        root.checkIf(rows, (i) -> true);
        root.uncheckRowIf((r) -> r.equals("a1"));
        Assert.assertFalse(root.isChecked("a1", 1));
        Assert.assertTrue(root.isChecked("a2", 1));
    }
    
    @SafeVarargs
    static <E> Set<E> setOf(E... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

}
