/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.j2k.ast

public class File(val packageName: String,
                  val body: List<Node>,
                  val mainFunction: String) : Node {

    override fun toKotlin(): String {
        val common = body.filterNot { it is Element && it.isEmpty() }.toKotlin("\n") + mainFunction
        if (packageName.isEmpty()) {
            return common
        }

        return "package " + packageName + "\n" + common
    }
}
