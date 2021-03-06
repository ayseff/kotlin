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

package org.jetbrains.jet.lang;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.ImportPath;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import java.util.List;

public class DefaultModuleConfiguration implements ModuleConfiguration {
    public static final List<ImportPath> DEFAULT_JET_IMPORTS = ImmutableList.of(
            new ImportPath("kotlin.*"),
            new ImportPath("kotlin.io.*"),
            new ImportPath("jet.*"));

    public static final ModuleConfiguration INSTANCE = new DefaultModuleConfiguration();

    private DefaultModuleConfiguration() {
    }

    @Override
    public void extendNamespaceScope(@NotNull NamespaceDescriptor namespaceDescriptor, @NotNull WritableScope namespaceMemberScope) {
        if (DescriptorUtils.getFQName(namespaceDescriptor).equalsTo(KotlinBuiltIns.getInstance().getBuiltInsPackageFqName())) {
            namespaceMemberScope.importScope(KotlinBuiltIns.getInstance().getBuiltInsScope());
        }
    }
}
